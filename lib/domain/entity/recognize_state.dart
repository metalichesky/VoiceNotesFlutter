
enum RecognizeState {
  idle,
  preparing,
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
      case RecognizeState.preparing:
        return 1;
      case RecognizeState.ready:
        return 2;
      case RecognizeState.stared:
        return 3;
      case RecognizeState.paused:
        return 4;
      case RecognizeState.stopped:
        return 5;
      default:
        return 0;
    }
  }
}