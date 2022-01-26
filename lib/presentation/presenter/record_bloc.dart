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
import 'package:voice_note/domain/usecase/synthesize_use_case.dart';
import 'package:voice_note/domain/usecase/system_use_case.dart';

part 'record_event.dart';

part 'record_state.dart';

class RecordBloc extends Bloc<RecordEvent, RecordState> {
  SystemUseCase systemUseCase;
  RecognizeUseCase recognizeUseCase;
  SynthesizeUseCase synthesizeUseCase;
  PermissionUseCase permissionUseCase;

  @override
  RecordState get initialState => HomeInitialState();

  late StreamController<RecognizeStateUpdate> recognizeStateStream;
  late StreamController<RecognizeResult> recognizeResultStream;
  late StreamController<SynthesizeStateUpdate> synthesizeStateStream;
  late StreamSubscription recognizeStateStreamSubscription;
  late StreamSubscription recognizeResultStreamSubscription;
  late StreamSubscription synthesizeStateStreamSubscription;

  RecordBloc({
    required this.systemUseCase,
    required this.recognizeUseCase,
    required this.synthesizeUseCase,
    required this.permissionUseCase,
  }) : super(HomeInitialState()) {
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

  @override
  Stream<RecordState> mapEventToState(
    RecordEvent event,
  ) async* {
    switch (event.runtimeType) {
      case RecordCheckPermissionsEvent:
        yield* _checkPermissions(event);
        break;
      case RecordRequestPermissionsEvent:
        yield* _requestPermissions(event);
        break;
      case RecordRecognizeStateUpdatedEvent:
        yield* _handleRecognizeStateUpdated(event);
        break;
      case RecordRecognizeResultUpdatedEvent:
        yield* _handleRecognizeResultUpdated(event);
        break;
      case RecordSynthesizeStateUpdatedEvent:
        yield* _handleSynthesizeStateUpdated(event);
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
      case RecordRecognizeSwitchEvent:
        _switchRecognize();
        break;
      case RecordSynthesizeInitializeEvent:
        _initializeSynthesize();
        break;
      case RecordSynthesizeStartEvent:
        String text = state.recordRecognized.allText;
        _startSynthesize(text);
        break;
      case RecordSynthesizePauseEvent:
        _pauseSynthesize();
        break;
      case RecordSynthesizeStopEvent:
        _stopSynthesize();
        break;
      case RecordSynthesizeSwitchEvent:
        _switchSynthesize();
        break;
      case RecordSaveEvent:
        yield* _saveRecord();
        break;
      case RecordClearEvent:
        yield* _clearRecord();
        break;
    }
  }

  Stream<RecordState> _checkPermissions(RecordEvent event) async* {
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
  }

  Stream<RecordState> _requestPermissions(RecordEvent event) async* {
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
  }

  Stream<RecordState> _handleRecognizeStateUpdated(RecordEvent event) async* {
    RecordRecognizeStateUpdatedEvent updatedEvent = event as RecordRecognizeStateUpdatedEvent;
    HomeRecognizeStateUpdatedState newState = HomeRecognizeStateUpdatedState();
    newState.from(state);
    newState.setRecognizeUpdate(updatedEvent.stateUpdate);
    yield newState;
  }

  Stream<RecordState> _handleRecognizeResultUpdated(RecordEvent event) async* {
    RecordRecognizeResultUpdatedEvent updatedEvent = event as RecordRecognizeResultUpdatedEvent;
    HomeRecognizeResultUpdatedState newState = HomeRecognizeResultUpdatedState();
    newState.from(state);
    if (newState.addResult(updatedEvent.recognizeResult)) {
      yield newState;
    }
  }

  void _switchRecognize() {
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
    if (state.recognizeState == RecognizeState.started) {
      recognizeUseCase.pauseRecognize();
    }
  }

  void _stopRecognize() {
    if (state.recognizeState == RecognizeState.started) {
      recognizeUseCase.stopRecognize();
    }
  }

  void _switchSynthesize() {
    SynthesizeState? currentSynthesizeState = state.synthesizeState;
    switch (currentSynthesizeState) {
      case SynthesizeState.idle:
        _initializeSynthesize();
        break;
      case SynthesizeState.preparing:
        // just wait...
        break;
      case SynthesizeState.ready:
        String text = state.recordRecognized.allText;
        _startSynthesize(text);
        break;
      case SynthesizeState.started:
        _stopSynthesize();
        break;
      case SynthesizeState.paused:
        _resumeSynthesize();
        break;
      case SynthesizeState.stopped:
        String text = state.recordRecognized.allText;
        _startSynthesize(text);
        break;
      default:
        // skip
        break;
    }
  }

  Stream<RecordState> _handleSynthesizeStateUpdated(RecordEvent event) async* {
    RecordSynthesizeStateUpdatedEvent updatedEvent = event as RecordSynthesizeStateUpdatedEvent;
    HomeRecognizeStateUpdatedState newState = HomeRecognizeStateUpdatedState();
    newState.from(state);
    newState.setSynthesizeUpdate(updatedEvent.stateUpdate);
    yield newState;
  }

  void _initializeSynthesize() {
    synthesizeUseCase.configureSynthesize();
  }

  void _startSynthesize(String text) {
    if (state.synthesizeState == SynthesizeState.ready ||
        state.synthesizeState == SynthesizeState.paused ||
        state.synthesizeState == SynthesizeState.stopped) {
      synthesizeUseCase.startSynthesize(text);
    }
  }

  void _resumeSynthesize() {
    if (state.synthesizeState == SynthesizeState.paused) {
      synthesizeUseCase.resumeSynthesize();
    }
  }

  void _pauseSynthesize() {
    if (state.synthesizeState == SynthesizeState.started) {
      synthesizeUseCase.pauseSynthesize();
    }
  }

  void _stopSynthesize() {
    if (state.synthesizeState == SynthesizeState.started) {
      synthesizeUseCase.stopSynthesize();
    }
  }

  Stream<RecordState> _clearRecord() async* {
    state.recordRecognized.clear();
    HomeRecognizeResultUpdatedState newState = HomeRecognizeResultUpdatedState();
    newState.from(state);
    yield newState;
  }

  Stream<RecordState> _saveRecord() async* {
    // TODO: save record
  }

  @override
  Future<void> close() {
    recognizeStateStreamSubscription.cancel();
    return super.close();
  }
}
