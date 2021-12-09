
import 'dart:async';

import 'package:voice_note/data/datasource/recognize_controller.dart';
import 'package:voice_note/domain/abstractions/recognize_repository.dart';
import 'package:voice_note/domain/entity/recognize_listener.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';

class RecognizeRepositoryImpl extends RecognizeRepository implements RecognizeListener {
  RecognizeController controller;
  @override
  StreamController<RecognizeStateUpdate> recognizeStateStream = StreamController();

  RecognizeRepositoryImpl({required this.controller}) {
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