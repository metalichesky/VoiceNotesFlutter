
import 'dart:async';
import 'dart:core';

import 'package:voice_note/domain/abstractions/synthesize_controller.dart';
import 'package:voice_note/domain/entity/synthesize_state.dart';

class SynthesizeUseCase {
  SynthesizeController synthesizeController;
  late StreamController<SynthesizeStateUpdate> synthesizeStateStream;

  SynthesizeUseCase({required this.synthesizeController}) {
    synthesizeStateStream = synthesizeController.synthesizeStateStream;
  }

  Future<SynthesizeState> getSynthesizeState() {
    return synthesizeController.getSynthesizeState();
  }

  Future<void> configureSynthesize() {
    return synthesizeController.configureSynthesize();
  }

  Future<void> startSynthesize(String text) {
    return synthesizeController.startSynthesize(text);
  }

  Future<void> resumeSynthesize() {
    return synthesizeController.resumeSynthesize();
  }

  Future<void> pauseSynthesize() {
    return synthesizeController.pauseSynthesize();
  }

  Future<void> stopSynthesize() {
    return synthesizeController.stopSynthesize();
  }

}