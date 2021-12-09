import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:logging/logging.dart';
import 'package:meta/meta.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';
import 'package:voice_note/domain/usecase/permission_use_case.dart';
import 'package:voice_note/domain/usecase/recognize_use_case.dart';
import 'package:voice_note/domain/usecase/system_use_case.dart';

part 'home_event.dart';
part 'home_state.dart';

class HomeBloc extends Bloc<HomeEvent, HomeState> {
  SystemUseCase systemUseCase;
  RecognizeUseCase recognizeUseCase;
  PermissionUseCase permissionUseCase;

  @override
  HomeState get initialState => HomeInitialState();

  late StreamController<RecognizeStateUpdate> recognizeStateStream;
  late StreamSubscription recognizeStateStreamSubscription;

  HomeBloc({
    required this.systemUseCase,
    required this.recognizeUseCase,
    required this.permissionUseCase
  }): super(HomeInitialState()) {
    recognizeStateStream = recognizeUseCase.recognizeStateStream;
    recognizeStateStreamSubscription = recognizeStateStream.stream.listen((event) {
      add(HomeRecognizeStateUpdatedEvent(stateUpdate: event));
    });

    add(HomeCheckPermissionsEvent());
  }

  @override
  Stream<HomeState> mapEventToState(
    HomeEvent event,
  ) async* {
    switch (event.runtimeType) {
      case HomeCheckPermissionsEvent:
        HomeState newState = HomeInitialState();
        newState.from(state);
        bool audioGranted = await permissionUseCase.isAudioRecordAvailable();
        bool storageGranted = await permissionUseCase.isExternalStorageAvailable();
        newState.audioPermissionsGranted = audioGranted;
        newState.storagePermissionsGranted = storageGranted;
        Logger.root.info("HomeBloc: mapEventToState: HomeCheckPermissionsEvent storageGranted=${storageGranted} audioGranted=${audioGranted}");
        yield newState;
        if (!audioGranted || !storageGranted) {
          add(HomeRequestPermissionsEvent());
        }
        break;
      case HomeRequestPermissionsEvent:
        HomeState newState = HomeInitialState();
        newState.from(state);
        if (!state.audioPermissionsGranted) {
          newState.audioPermissionsGranted = await permissionUseCase.requestAudioRecord();
        }
        if (!state.storagePermissionsGranted) {
          newState.storagePermissionsGranted = await permissionUseCase.requestExternalStorage();
        }
        yield newState;
        break;
      case HomeRecognizeStateUpdatedEvent:
          HomeRecognizeStateUpdatedEvent updatedEvent = event as HomeRecognizeStateUpdatedEvent;
          HomeRecognizeStateUpdatedState newState = HomeRecognizeStateUpdatedState();
          newState.from(state);
          newState.setUpdate(updatedEvent.stateUpdate);
          yield newState;
          break;
      case HomeRecognizeSwitchEvent:
          RecognizeState? currentRecognizeState = state.recognizeState;
          switch(currentRecognizeState) {
            case RecognizeState.idle:
              recognizeUseCase.configureRecognize();
              break;
            case RecognizeState.ready:
              recognizeUseCase.startRecognize();
              break;
            case RecognizeState.stared:
              recognizeUseCase.pauseRecognize();
              break;
            case RecognizeState.paused:
              recognizeUseCase.startRecognize();
              break;
            case RecognizeState.stopped:
              recognizeUseCase.startRecognize();
              break;
          }
    }
  }

  @override
  Future<void> close() {
    recognizeStateStreamSubscription.cancel();
    return super.close();
  }
}
