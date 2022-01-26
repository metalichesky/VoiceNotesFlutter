
import 'package:voice_note/domain/entity/synthesize_state.dart';

abstract class SynthesizeListener {
  void onSynthesizeStateChanged(SynthesizeState oldState, SynthesizeState newState);
}