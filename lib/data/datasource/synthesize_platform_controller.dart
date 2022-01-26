import 'package:flutter/services.dart';
import 'package:logging/logging.dart';
import 'package:voice_note/core/util/platform.dart';
import 'package:voice_note/domain/abstractions/synthesize_listener.dart';
import 'package:voice_note/domain/entity/synthesize_state.dart';

const String _METHOD_ON_SYNTHESIZE_STATE_CHANGED = "onSynthesizeStateChanged";

class SynthesizePlatformController {
  final MethodChannel _channelSynthesize = PlatformUtils.channelSynthesize;
  SynthesizeListener? _synthesizeListener;

  SynthesizePlatformController() {
    _channelSynthesize.setMethodCallHandler((call) => _processMethod(call));
  }

  void setSynthesizeListener(SynthesizeListener listener) {
    _synthesizeListener = listener;
  }

  Future<dynamic> _processMethod(MethodCall call) {
    switch (call.method) {
      case _METHOD_ON_SYNTHESIZE_STATE_CHANGED:
        int oldStateId = call.arguments["oldState"];
        int newStateId = call.arguments["newState"];
        SynthesizeState oldState = parseSynthesizeState(stateId: oldStateId);
        SynthesizeState newState = parseSynthesizeState(stateId: newStateId);
        Logger.root.info(
            "SynthesizeController: _processMethod() oldState=${oldState} newState=${newState}");
        _synthesizeListener?.onSynthesizeStateChanged(oldState, newState);
        return Future.value(null);
      default:
        return Future.value(null);
    }
  }

  Future<SynthesizeState> getSynthesizeState() async {
    int? synthesizeStateId;
    try {
      synthesizeStateId =
          await _channelSynthesize.invokeMethod("getSynthesizeState");
    } on PlatformException catch (e) {
      Logger.root.shout("getSynthesizeState: ${e.message}", e);
    }
    if (synthesizeStateId != null) {
      return parseSynthesizeState(stateId: synthesizeStateId);
    } else {
      return SynthesizeState.idle;
    }
  }

  Future<void> configureSynthesize() async {
    try {
      await _channelSynthesize.invokeMethod("configureSynthesize");
    } on PlatformException catch (e) {
      Logger.root.shout("configureSynthesize: ${e.message}", e);
    }
  }

  Future<void> startSynthesize(String text) async {
    try {
      var argsMap = {"text": text};
      await _channelSynthesize.invokeMethod("startSynthesize", argsMap);
    } on PlatformException catch (e) {
      Logger.root.shout("startSynthesize: ${e.message}", e);
    }
  }


  Future<void> resumeSynthesize() async {
    try {
      await _channelSynthesize.invokeMethod("resumeSynthesize");
    } on PlatformException catch (e) {
      Logger.root.shout("resumeSynthesize: ${e.message}", e);
    }
  }

  Future<void> pauseSynthesize() async {
    try {
      await _channelSynthesize.invokeMethod("pauseSynthesize");
    } on PlatformException catch (e) {
      Logger.root.shout("pauseSynthesize: ${e.message}", e);
    }
  }

  Future<void> stopSynthesize() async {
    try {
      await _channelSynthesize.invokeMethod("stopSynthesize");
    } on PlatformException catch (e) {
      Logger.root.shout("stopSynthesize: ${e.message}", e);
    }
  }
}
