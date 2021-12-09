
enum RecognizeState {
  idle,
  ready,
  stared,
  paused,
  stopped
}

class RecognizeStateUpdate {
  RecognizeState oldState;
  RecognizeState newState;

  RecognizeStateUpdate({
    required this.oldState,
    required this.newState
  });
}

RecognizeState parseRecognizeState({required int stateId}) {
  return RecognizeState.values.firstWhere((element) =>
    element.stateId == stateId
  );
}

extension RecognizeStateExtension on RecognizeState {
  int get stateId {
    switch (this) {
      case RecognizeState.idle:
        return 0;
      case RecognizeState.ready:
        return 1;
      case RecognizeState.stared:
        return 2;
      case RecognizeState.paused:
        return 3;
      case RecognizeState.stopped:
        return 4;
      default:
        return 0;
    }
  }
}