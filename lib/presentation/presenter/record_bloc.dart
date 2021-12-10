import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:logging/logging.dart';
import 'package:meta/meta.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';
import 'package:voice_note/domain/usecase/permission_use_case.dart';
import 'package:voice_note/domain/usecase/recognize_use_case.dart';
import 'package:voice_note/domain/usecase/system_use_case.dart';

part 'record_event.dart';
part 'record_state.dart';

class RecordBloc extends Bloc<RecordEvent, RecordState> {
  SystemUseCase systemUseCase;
  RecognizeUseCase recognizeUseCase;
  PermissionUseCase permissionUseCase;

  @override
  RecordState get initialState => HomeInitialState();

  late StreamController<RecognizeStateUpdate> recognizeStateStream;
  late StreamSubscription recognizeStateStreamSubscription;

  RecordBloc({
    required this.systemUseCase,
    required this.recognizeUseCase,
    required this.permissionUseCase
  }): super(HomeInitialState()) {
    recognizeStateStream = recognizeUseCase.recognizeStateStream;
    recognizeStateStreamSubscription = recognizeStateStream.stream.listen((event) {
      add(RecordRecognizeStateUpdatedEvent(stateUpdate: event));
    });

    add(RecordCheckPermissionsEvent());
  }

  @override
  Stream<RecordState> mapEventToState(
    RecordEvent event,
  ) async* {
    switch (event.runtimeType) {
      case RecordCheckPermissionsEvent:
        RecordState newState = HomeInitialState();
        newState.from(state);
        bool audioGranted = await permissionUseCase.isAudioRecordAvailable();
        bool storageGranted = await permissionUseCase.isExternalStorageAvailable();
        newState.audioPermissionsGranted = audioGranted;
        newState.storagePermissionsGranted = storageGranted;
        Logger.root.info("RecordBloc: mapEventToState: HomeCheckPermissionsEvent storageGranted=${storageGranted} audioGranted=${audioGranted}");
        yield newState;
        if (!audioGranted || !storageGranted) {
          add(RecordRequestPermissionsEvent());
        }
        break;
      case RecordRequestPermissionsEvent:
        RecordState newState = HomeInitialState();
        newState.from(state);
        if (!state.audioPermissionsGranted) {
          newState.audioPermissionsGranted = await permissionUseCase.requestAudioRecord();
        }
        if (!state.storagePermissionsGranted) {
          newState.storagePermissionsGranted = await permissionUseCase.requestExternalStorage();
        }
        yield newState;
        break;
      case RecordRecognizeStateUpdatedEvent:
          RecordRecognizeStateUpdatedEvent updatedEvent = event as RecordRecognizeStateUpdatedEvent;
          HomeRecognizeStateUpdatedState newState = HomeRecognizeStateUpdatedState();
          newState.from(state);
          newState.setUpdate(updatedEvent.stateUpdate);
          yield newState;
          break;
      case RecordRecognizeSwitchEvent:
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
