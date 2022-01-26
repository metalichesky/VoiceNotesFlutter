
import 'dart:async';

import 'package:rxdart/rxdart.dart';
import 'package:voice_note/data/datasource/recognize_platform_controller.dart';
import 'package:voice_note/domain/abstractions/recognize_controller.dart';
import 'package:voice_note/domain/abstractions/recognize_listener.dart';
import 'package:voice_note/domain/entity/recognize_result.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';

class RecognizeControllerImpl extends RecognizeController implements RecognizeListener {
  RecognizePlatformController controller;
  @override
  StreamController<RecognizeStateUpdate> recognizeStateStream = BehaviorSubject();
  @override
  StreamController<RecognizeResult> recognizeResultStream = BehaviorSubject();

  RecognizeControllerImpl({required this.controller}) {
    controller.setRecognizeListener(this);
    recognizeStateStream.add(RecognizeStateUpdate(
        oldState: RecognizeState.idle,
        newState: RecognizeState.idle)
    );
  }

  @override
  void onRecognizeStateChanged(RecognizeState oldState, RecognizeState newState) {
    recognizeStateStream.add(RecognizeStateUpdate(
        oldState: oldState,
        newState: newState
    ));
  }

  @override
  void onRecognizeResult(RecognizeResult result) {
    recognizeResultStream.add(result);
  }

  @override
  Future<RecognizeState> getRecognizeState() {
    return controller.getRecognizeState();
  }

  @override
  Future<void> configureRecognize() {
    return controller.configureRecognize();
  }

  @override
  Future<void> startRecognize() {
    return controller.startRecognize();
  }

  @override
  Future<void> pauseRecognize() {
    return controller.pauseRecognize();
  }

  @override
  Future<void> stopRecognize() {
    return controller.stopRecognize();
  }

}