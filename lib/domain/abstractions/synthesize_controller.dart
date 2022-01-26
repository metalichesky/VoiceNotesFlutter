
import 'dart:async';

import 'package:voice_note/domain/entity/synthesize_state.dart';

abstract class SynthesizeController {

  abstract StreamController<SynthesizeStateUpdate> synthesizeStateStream;

  Future<void> configureSynthesize();
  Future<void> startSynthesize(String text);
  Future<void> stopSynthesize();
  Future<void> resumeSynthesize();
  Future<void> pauseSynthesize();
  Future<SynthesizeState> getSynthesizeState();

}