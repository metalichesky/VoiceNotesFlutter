
enum SynthesizeState {
  idle,
  preparing,
  ready,
  started,
  paused,
  stopped
}

class SynthesizeStateUpdate {
  SynthesizeState oldState;
  SynthesizeState newState;

  SynthesizeStateUpdate({
    required this.oldState,
    required this.newState
  });
}

SynthesizeState parseSynthesizeState({required int stateId}) {
  return SynthesizeState.values.firstWhere((element) =>
    element.stateId == stateId
  );
}

extension SynthesizeStateExtension on SynthesizeState {
  int get stateId {
    switch (this) {
      case SynthesizeState.idle:
        return 0;
      case SynthesizeState.preparing:
        return 1;
      case SynthesizeState.ready:
        return 2;
      case SynthesizeState.started:
        return 3;
      case SynthesizeState.paused:
        return 4;
      case SynthesizeState.stopped:
        return 5;
      default:
        return 0;
    }
  }
}