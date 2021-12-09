
import 'dart:async';
import 'dart:core';

import 'package:voice_note/domain/abstractions/recognize_repository.dart';
import 'package:voice_note/domain/entity/recognize_listener.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';

class RecognizeUseCase {
  RecognizeRepository recognizeRepository;
  late StreamController<RecognizeStateUpdate> recognizeStateStream;

  RecognizeUseCase({required this.recognizeRepository}) {
    recognizeStateStream = recognizeRepository.recognizeStateStream;
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