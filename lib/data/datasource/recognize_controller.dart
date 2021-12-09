import 'package:flutter/services.dart';
import 'package:logging/logging.dart';
import 'package:voice_note/core/util/platform.dart';
import 'package:voice_note/domain/entity/recognize_listener.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';

const String _METHOD_ON_RECOGNIZE_STATE_CHANGED = "onRecognizeStateChanged";

class RecognizeController {
  final MethodChannel _channelRecognize = PlatformUtils.channelRecognize;
  RecognizeListener? _recognizeListener;

  RecognizeController() {
    _channelRecognize.setMethodCallHandler((call) =>
        _processMethod(call)
    );
  }

  void setRecognizeListener(RecognizeListener listener) {
    _recognizeListener = listener;
  }

  Future<dynamic> _processMethod(MethodCall call) {
    switch(call.method) {
      case _METHOD_ON_RECOGNIZE_STATE_CHANGED:
        int oldStateId = call.arguments["oldState"];
        int newStateId = call.arguments["newState"];
        RecognizeState oldState = parseRecognizeState(stateId: oldStateId);
        RecognizeState newState = parseRecognizeState(stateId: newStateId);
        Logger.root.info("RecognizeController: _processMethod() oldState=${oldState} newState=${newState}");
        _recognizeListener?.onRecognizeStateChanged(oldState, newState);
        return Future.value(null);
      default:
        return Future.value(null);
    }
  }

  Future<RecognizeState> getRecognizeState() async {
    int? recognizeStateId;
    try {
      recognizeStateId = await _channelRecognize.invokeMethod("configureRecognize");
    } on PlatformException catch (e) {
      Logger.root.shout("configureRecognize: ${e.message}", e);
    }
    if (recognizeStateId != null) {
      return parseRecognizeState(stateId: recognizeStateId);
    } else {
      return RecognizeState.idle;
    }
  }

  Future<void> configureRecognize() async {
    try {
      await _channelRecognize.invokeMethod("configureRecognize");
    } on PlatformException catch (e) {
      Logger.root.shout("configureRecognize: ${e.message}", e);
    }
  }

  Future<void> startRecognize() async {
    try {
      await _channelRecognize.invokeMethod("startRecognize");
    } on PlatformException catch (e) {
      Logger.root.shout("startRecognize: ${e.message}", e);
    }
  }

  Future<void> pauseRecognize() async {
    try {
      await _channelRecognize.invokeMethod("pauseRecognize");
    } on PlatformException catch (e) {
      Logger.root.shout("pauseRecognize: ${e.message}", e);
    }
  }

  Future<void> stopRecognize() async {
    try {
      await _channelRecognize.invokeMethod("stopRecognize");
    } on PlatformException catch (e) {
      Logger.root.shout("stopRecognize: ${e.message}", e);
    }
  }
}