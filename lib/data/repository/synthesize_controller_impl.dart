import 'dart:async';

import 'package:rxdart/rxdart.dart';
import 'package:voice_note/data/datasource/synthesize_platform_controller.dart';
import 'package:voice_note/domain/abstractions/synthesize_controller.dart';
import 'package:voice_note/domain/abstractions/synthesize_listener.dart';
import 'package:voice_note/domain/entity/synthesize_state.dart';

class SynthesizeControllerImpl extends SynthesizeController
    implements SynthesizeListener {
  SynthesizePlatformController controller;
  @override
  StreamController<SynthesizeStateUpdate> synthesizeStateStream =
      BehaviorSubject();

  SynthesizeControllerImpl({required this.controller}) {
    controller.setSynthesizeListener(this);
    synthesizeStateStream.add(SynthesizeStateUpdate(
        oldState: SynthesizeState.idle, newState: SynthesizeState.idle));
  }

  @override
  void onSynthesizeStateChanged(
      SynthesizeState oldState, SynthesizeState newState) {
    synthesizeStateStream
        .add(SynthesizeStateUpdate(oldState: oldState, newState: newState));
  }

  @override
  Future<SynthesizeState> getSynthesizeState() {
    return controller.getSynthesizeState();
  }

  @override
  Future<void> configureSynthesize() {
    return controller.configureSynthesize();
  }

  @override
  Future<void> startSynthesize(String text) {
    return controller.startSynthesize(text);
  }

  @override
  Future<void> resumeSynthesize() {
    return controller.resumeSynthesize();
  }

  @override
  Future<void> pauseSynthesize() {
    return controller.pauseSynthesize();
  }

  @override
  Future<void> stopSynthesize() {
    return controller.stopSynthesize();
  }
}
