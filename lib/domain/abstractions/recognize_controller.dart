
import 'dart:async';

import 'package:voice_note/domain/entity/recognize_result.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';

abstract class RecognizeController {

  abstract StreamController<RecognizeStateUpdate> recognizeStateStream;
  abstract StreamController<RecognizeResult> recognizeResultStream;

  Future<void> configureRecognize();
  Future<void> startRecognize();
  Future<void> stopRecognize();
  Future<void> pauseRecognize();
  Future<RecognizeState> getRecognizeState();

}