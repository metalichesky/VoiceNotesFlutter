import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:flutter/material.dart';
import 'package:logging/logging.dart';
import 'package:meta/meta.dart';
import 'package:voice_note/domain/entity/recognize_result.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';
import 'package:voice_note/domain/entity/record.dart';
import 'package:voice_note/domain/entity/synthesize_state.dart';
import 'package:voice_note/domain/usecase/permission_use_case.dart';
import 'package:voice_note/domain/usecase/recognize_use_case.dart';
import 'package:voice_note/domain/usecase/records_use_case.dart';
import 'package:voice_note/domain/usecase/synthesize_use_case.dart';
import 'package:voice_note/domain/usecase/system_use_case.dart';

part 'record_event.dart';

part 'record_state.dart';

class RecordBloc extends Bloc<RecordEvent, RecordState> {
  SystemUseCase systemUseCase;
  RecognizeUseCase recognizeUseCase;
  SynthesizeUseCase synthesizeUseCase;
  PermissionUseCase permissionUseCase;
  RecordsUseCase recordsUseCase;

  late StreamController<RecognizeStateUpdate> recognizeStateStream;
  late StreamController<RecognizeResult> recognizeResultStream;
  late StreamController<SynthesizeStateUpdate> synthesizeStateStream;
  late StreamSubscription recognizeStateStreamSubscription;
  late StreamSubscription recognizeResultStreamSubscription;
  late StreamSubscription synthesizeStateStreamSubscription;

  RecordBloc(
      {required this.systemUseCase,
      required this.recognizeUseCase,
      required this.synthesizeUseCase,
      required this.permissionUseCase,
      required this.recordsUseCase})
      : super(RecordInitialState()) {
    _registerEventMapping();
    recognizeStateStream = recognizeUseCase.recognizeStateStream;
    recognizeResultStream = recognizeUseCase.recognizeResultStream;
    synthesizeStateStream = synthesizeUseCase.synthesizeStateStream;
    recognizeStateStreamSubscription =
        recognizeStateStream.stream.listen((event) {
      add(RecordRecognizeStateUpdatedEvent(stateUpdate: event));
    });
    recognizeResultStreamSubscription =
        recognizeResultStream.stream.listen((event) {
      add(RecordRecognizeResultUpdatedEvent(recognizeResult: event));
    });
    synthesizeStateStreamSubscription =
        synthesizeStateStream.stream.listen((event) {
      add(RecordSynthesizeStateUpdatedEvent(stateUpdate: event));
    });
    add(RecordCheckPermissionsEvent());
    add(RecordSynthesizeInitializeEvent());
  }

  void _registerEventMapping() {
    on<RecordCheckPermissionsEvent>((event, emit) async {
      await _checkPermissions(event, emit);
    });
    on<RecordRequestPermissionsEvent>((event, emit) async {
      await _requestPermissions(event, emit);
    });
    on<RecordInputUpdatedEvent>((event, emit) async {
      await _handleInputUpdated(event, emit);
    });
    on<RecordRecognizeStateUpdatedEvent>((event, emit) async {
      await _handleRecognizeStateUpdated(event, emit);
    });
    on<RecordRecognizeResultUpdatedEvent>((event, emit) async {
      await _handleRecognizeResultUpdated(event, emit);
    });
    on<RecordSynthesizeStateUpdatedEvent>((event, emit) async {
      await _handleSynthesizeStateUpdated(event, emit);
    });
    on<RecordRecognizeInitializeEvent>((event, emit) async {
      await _initializeRecognize();
    });
    on<RecordRecognizeStartEvent>((event, emit) async {
      await _startRecognize();
    });
    on<RecordRecognizePauseEvent>((event, emit) async {
      await _pauseRecognize();
    });
    on<RecordRecognizeStopEvent>((event, emit) async {
      await _stopRecognize();
    });
    on<RecordRecognizeSwitchEvent>((event, emit) async {
      await _switchRecognize(event, emit);
    });
    on<RecordSynthesizeInitializeEvent>((event, emit) async {
      await _initializeSynthesize();
    });
    on<RecordSynthesizeStartEvent>((event, emit) async {
      await _startSynthesize();
    });
    on<RecordSynthesizePauseEvent>((event, emit) async {
      await _pauseSynthesize();
    });
    on<RecordSynthesizeStopEvent>((event, emit) async {
      await _stopSynthesize();
    });
    on<RecordSynthesizeSwitchEvent>((event, emit) async {
      await _switchSynthesize();
    });
    on<RecordSaveEvent>((event, emit) async {
      await _saveRecord(event, emit);
    });
    on<RecordClearEvent>((event, emit) async {
      await _clearRecord(event, emit);
    });
    on<RecordCloseEvent>((event, emit) async {
      await _closeRecord(event, emit);
    });
  }

  Future<void> _checkPermissions(
      RecordEvent event, Emitter<RecordState> emit) async {
    RecordState newState = RecordInitialState();
    newState.from(state);
    bool audioGranted = await permissionUseCase.isAudioRecordAvailable();
    bool storageGranted = await permissionUseCase.isExternalStorageAvailable();
    newState.audioPermissionsGranted = audioGranted;
    newState.storagePermissionsGranted = storageGranted;
    Logger.root.info(
        "RecordBloc: mapEventToState: HomeCheckPermissionsEvent storageGranted=${storageGranted} audioGranted=${audioGranted}");
    emit(newState);
    if (!audioGranted || !storageGranted) {
      add(RecordRequestPermissionsEvent());
    } else {
      add(RecordRecognizeInitializeEvent());
    }
  }

  Future<void> _requestPermissions(
      RecordEvent event, Emitter<RecordState> emit) async {
    RecordState newState = RecordInitialState();
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
    emit(newState);
  }

  Future<void> _handleInputUpdated(
      RecordEvent event, Emitter<RecordState> emit) async {
    if (event is RecordInputUpdatedEvent) {
      var newState = RecordInputUpdatedState();
      newState.from(state);
      recordsUseCase.changeEditedRecordText(event.text);
      newState.editedRecord = recordsUseCase.editedRecord;
      emit(newState);
    }
  }

  Future<void> _handleRecognizeStateUpdated(
      RecordEvent event, Emitter<RecordState> emit) async {
    RecordRecognizeStateUpdatedEvent updatedEvent =
        event as RecordRecognizeStateUpdatedEvent;
    RecordRecognizeStateUpdatedState newState =
        RecordRecognizeStateUpdatedState();
    newState.from(state);
    newState.setRecognizeUpdate(updatedEvent.stateUpdate);
    emit(newState);
  }

  Future<void> _handleRecognizeResultUpdated(
      RecordEvent event, Emitter<RecordState> emit) async {
    if (event is RecordRecognizeResultUpdatedEvent) {
      if (recordsUseCase
          .addEditedRecordRecognizeResult(event.recognizeResult)) {
        RecordRecognizeResultUpdatedState newState =
            RecordRecognizeResultUpdatedState();
        newState.from(state);
        newState.editedRecord = recordsUseCase.editedRecord;
        emit(newState);
      }
    }
  }

  Future<void> _switchRecognize(
      RecordEvent event, Emitter<RecordState> emit) async {
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
      case RecognizeState.started:
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

  Future<void> _initializeRecognize() async {
    if (state.isAllPermissionsGranted()) {
      recognizeUseCase.configureRecognize();
    } else {
      add(RecordCheckPermissionsEvent());
    }
  }

  Future<void> _startRecognize() async {
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

  Future<void> _pauseRecognize() async {
    if (state.recognizeState == RecognizeState.started) {
      recognizeUseCase.pauseRecognize();
    }
  }

  Future<void> _stopRecognize() async {
    if (state.recognizeState == RecognizeState.started) {
      recognizeUseCase.stopRecognize();
    }
  }

  Future<void> _switchSynthesize() async {
    SynthesizeState? currentSynthesizeState = state.synthesizeState;
    switch (currentSynthesizeState) {
      case SynthesizeState.idle:
        _initializeSynthesize();
        break;
      case SynthesizeState.preparing:
        // just wait...
        break;
      case SynthesizeState.ready:
        _startSynthesize();
        break;
      case SynthesizeState.started:
        _stopSynthesize();
        break;
      case SynthesizeState.paused:
        _resumeSynthesize();
        break;
      case SynthesizeState.stopped:
        _startSynthesize();
        break;
      default:
        // skip
        break;
    }
  }

  Future<void> _handleSynthesizeStateUpdated(
      RecordEvent event, Emitter<RecordState> emit) async {
    RecordSynthesizeStateUpdatedEvent updatedEvent =
        event as RecordSynthesizeStateUpdatedEvent;
    RecordSynthesizeStateUpdatedState newState =
        RecordSynthesizeStateUpdatedState();
    newState.from(state);
    newState.setSynthesizeUpdate(updatedEvent.stateUpdate);
    emit(newState);
  }

  Future<void> _initializeSynthesize() async {
    synthesizeUseCase.configureSynthesize();
  }

  Future<void> _startSynthesize() async {
    String text = recordsUseCase.editedRecord?.allText ?? "";
    if (state.synthesizeState == SynthesizeState.ready ||
        state.synthesizeState == SynthesizeState.paused ||
        state.synthesizeState == SynthesizeState.stopped) {
      synthesizeUseCase.startSynthesize(text);
    }
  }

  Future<void> _resumeSynthesize() async {
    if (state.synthesizeState == SynthesizeState.paused) {
      synthesizeUseCase.resumeSynthesize();
    }
  }

  Future<void> _pauseSynthesize() async {
    if (state.synthesizeState == SynthesizeState.started) {
      synthesizeUseCase.pauseSynthesize();
    }
  }

  Future<void> _stopSynthesize() async {
    if (state.synthesizeState == SynthesizeState.started) {
      synthesizeUseCase.stopSynthesize();
    }
  }

  Future<void> _clearRecord(
      RecordEvent event, Emitter<RecordState> emit) async {
    recordsUseCase.clearEditedRecordText();
    RecordRecognizeResultUpdatedState newState =
        RecordRecognizeResultUpdatedState();
    newState.from(state);
    newState.editedRecord = recordsUseCase.editedRecord;
    emit(newState);
  }

  Future<void> _saveRecord(RecordEvent event, Emitter<RecordState> emit) async {
    recordsUseCase.saveEditedRecord();
    recordsUseCase.editedRecord = null;
    var newState = RecordSavedState();
    newState.from(state);
    newState.editedRecord = null;
    emit(newState);
  }

  Future<void> _closeRecord(
      RecordEvent event, Emitter<RecordState> emit) async {
    recordsUseCase.editedRecord = null;
    RecordClosedState newState = RecordClosedState();
    newState.from(state);
    newState.editedRecord = null;
    emit(newState);
  }

  @override
  Future<void> close() {
    recognizeStateStreamSubscription.cancel();
    recognizeResultStreamSubscription.cancel();
    synthesizeStateStreamSubscription.cancel();
    return super.close();
  }
}
