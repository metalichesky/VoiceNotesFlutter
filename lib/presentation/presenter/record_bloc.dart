import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:flutter/material.dart';
import 'package:logging/logging.dart';
import 'package:meta/meta.dart';
import 'package:voice_note/domain/entity/recognize_result.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';
import 'package:voice_note/domain/entity/record.dart';
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
  late StreamController<RecognizeResult> recognizeResultStream;
  late StreamSubscription recognizeStateStreamSubscription;
  late StreamSubscription recognizeResultStreamSubscription;

  RecordBloc(
      {required this.systemUseCase,
      required this.recognizeUseCase,
      required this.permissionUseCase})
      : super(HomeInitialState()) {
    recognizeStateStream = recognizeUseCase.recognizeStateStream;
    recognizeResultStream = recognizeUseCase.recognizeResultStream;
    recognizeStateStreamSubscription =
        recognizeStateStream.stream.listen((event) {
      add(RecordRecognizeStateUpdatedEvent(stateUpdate: event));
    });
    recognizeResultStreamSubscription =
        recognizeResultStream.stream.listen((event) {
      add(RecordRecognizeResultUpdatedEvent(recognizeResult: event));
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
        bool storageGranted =
            await permissionUseCase.isExternalStorageAvailable();
        newState.audioPermissionsGranted = audioGranted;
        newState.storagePermissionsGranted = storageGranted;
        Logger.root.info(
            "RecordBloc: mapEventToState: HomeCheckPermissionsEvent storageGranted=${storageGranted} audioGranted=${audioGranted}");
        yield newState;
        if (!audioGranted || !storageGranted) {
          add(RecordRequestPermissionsEvent());
        } else {
          add(RecordRecognizeInitializeEvent());
        }
        break;
      case RecordRequestPermissionsEvent:
        RecordState newState = HomeInitialState();
        newState.from(state);
        if (!state.audioPermissionsGranted) {
          newState.audioPermissionsGranted =
              await permissionUseCase.requestAudioRecord();
        }
        if (!state.storagePermissionsGranted) {
          newState.storagePermissionsGranted =
              await permissionUseCase.requestExternalStorage();
        }
        if (newState.storagePermissionsGranted &&
            newState.audioPermissionsGranted) {
          add(RecordRecognizeInitializeEvent());
        }
        yield newState;
        break;
      case RecordRecognizeStateUpdatedEvent:
        RecordRecognizeStateUpdatedEvent updatedEvent =
            event as RecordRecognizeStateUpdatedEvent;
        HomeRecognizeStateUpdatedState newState =
            HomeRecognizeStateUpdatedState();
        newState.from(state);
        newState.setUpdate(updatedEvent.stateUpdate);
        yield newState;
        break;
      case RecordRecognizeResultUpdatedEvent:
        RecordRecognizeResultUpdatedEvent updatedEvent =
            event as RecordRecognizeResultUpdatedEvent;
        HomeRecognizeResultUpdatedState newState =
            HomeRecognizeResultUpdatedState();
        newState.from(state);
        if (newState.addResult(updatedEvent.recognizeResult)) {
          yield newState;
        }
        break;
      case RecordRecognizeInitializeEvent:
        _initializeRecognize();
        break;
      case RecordRecognizeStartEvent:
        _startRecognize();
        break;
      case RecordRecognizePauseEvent:
        _pauseRecognize();
        break;
      case RecordRecognizeStopEvent:
        _stopRecognize();
        break;
      case RecordRecognizeSaveEvent:
        // TODO: save record
        break;
      case RecordRecognizeClearEvent:
        state.recordRecognized.clear();
        HomeRecognizeResultUpdatedState newState =
            HomeRecognizeResultUpdatedState();
        newState.from(state);
        yield newState;
        break;
      case RecordRecognizeSwitchEvent:
        RecognizeState? currentRecognizeState = state.recognizeState;
        switch (currentRecognizeState) {
          case RecognizeState.idle:
            _initializeRecognize();
            break;
          case RecognizeState.preparing:
            // just wait...
            break;
          case RecognizeState.ready:
            _startRecognize();
            break;
          case RecognizeState.stared:
            _pauseRecognize();
            break;
          case RecognizeState.paused:
            _startRecognize();
            break;
          case RecognizeState.stopped:
            _startRecognize();
            break;
          default:
            // skip
            break;
        }
    }
  }

  void _initializeRecognize() {
    if (state.isAllPermissionsGranted()) {
      recognizeUseCase.configureRecognize();
    } else {
      add(RecordCheckPermissionsEvent());
    }
  }

  void _startRecognize() {
    if (state.recognizeState == RecognizeState.ready ||
        state.recognizeState == RecognizeState.paused ||
        state.recognizeState == RecognizeState.stopped) {
      if (state.isAllPermissionsGranted()) {
        recognizeUseCase.startRecognize();
      } else {
        add(RecordCheckPermissionsEvent());
      }
    }
  }

  void _pauseRecognize() {
    if (state.recognizeState == RecognizeState.stared) {
      recognizeUseCase.pauseRecognize();
    }
  }

  void _stopRecognize() {
    if (state.recognizeState == RecognizeState.stared) {
      recognizeUseCase.stopRecognize();
    }
  }

  @override
  Future<void> close() {
    recognizeStateStreamSubscription.cancel();
    return super.close();
  }
}
