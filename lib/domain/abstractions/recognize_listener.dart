
import 'package:voice_note/domain/entity/recognize_result.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';

abstract class RecognizeListener {
  void onRecognizeStateChanged(RecognizeState oldState, RecognizeState newState);
  void onRecognizeResult(RecognizeResult recognizeResult);
}