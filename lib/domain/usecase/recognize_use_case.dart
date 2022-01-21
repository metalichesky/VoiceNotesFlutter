
import 'dart:async';
import 'dart:core';

import 'package:voice_note/domain/abstractions/recognize_repository.dart';
import 'package:voice_note/domain/entity/recognize_listener.dart';
import 'package:voice_note/domain/entity/recognize_result.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';

class RecognizeUseCase {
  RecognizeRepository recognizeRepository;
  late StreamController<RecognizeStateUpdate> recognizeStateStream;
  late StreamController<RecognizeResult> recognizeResultStream;

  RecognizeUseCase({required this.recognizeRepository}) {
    recognizeStateStream = recognizeRepository.recognizeStateStream;
    recognizeResultStream = recognizeRepository.recognizeResultStream;
  }

  Future<RecognizeState> getRecognizeState() {
    return recognizeRepository.getRecognizeState();
  }

  Future<void> configureRecognize() {
    return recognizeRepository.configureRecognize();
  }

  Future<void> startRecognize() {
    return recognizeRepository.startRecognize();
  }

  Future<void> pauseRecognize() {
    return recognizeRepository.pauseRecognize();
  }

  Future<void> stopRecognize() {
    return recognizeRepository.stopRecognize();
  }

}