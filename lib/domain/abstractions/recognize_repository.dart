
import 'dart:async';

import 'package:voice_note/domain/entity/recognize_state.dart';

abstract class RecognizeRepository {

  abstract StreamController<RecognizeStateUpdate> recognizeStateStream;

  Future<void> configureRecognize();
  Future<void> startRecognize();
  Future<void> stopRecognize();
  Future<void> pauseRecognize();
  Future<RecognizeState> getRecognizeState();

}