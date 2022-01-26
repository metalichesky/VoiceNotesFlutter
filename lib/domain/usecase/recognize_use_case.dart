
import 'dart:async';
import 'dart:core';

import 'package:voice_note/domain/abstractions/recognize_controller.dart';
import 'package:voice_note/domain/abstractions/recognize_listener.dart';
import 'package:voice_note/domain/entity/recognize_result.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';

class RecognizeUseCase {
  RecognizeController recognizeController;
  late StreamController<RecognizeStateUpdate> recognizeStateStream;
  late StreamController<RecognizeResult> recognizeResultStream;

  RecognizeUseCase({required this.recognizeController}) {
    recognizeStateStream = recognizeController.recognizeStateStream;
    recognizeResultStream = recognizeController.recognizeResultStream;
  }

  Future<RecognizeState> getRecognizeState() {
    return recognizeController.getRecognizeState();
  }

  Future<void> configureRecognize() {
    return recognizeController.configureRecognize();
  }

  Future<void> startRecognize() {
    return recognizeController.startRecognize();
  }

  Future<void> pauseRecognize() {
    return recognizeController.pauseRecognize();
  }

  Future<void> stopRecognize() {
    return recognizeController.stopRecognize();
  }

}