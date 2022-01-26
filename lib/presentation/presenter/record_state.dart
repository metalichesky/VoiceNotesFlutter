part of 'record_bloc.dart';

abstract class RecordState {
  RecognizeState? recognizeState;
  RecognizeState? oldRecognizeState;
  SynthesizeState? synthesizeState;
  SynthesizeState? oldSynthesizeState;

  RecognizeResult? lastRecognizeResult;
  RecordRecognized recordRecognized = RecordRecognized();

  bool audioPermissionsGranted;
  bool storagePermissionsGranted;

  bool isAllPermissionsGranted() {
    return audioPermissionsGranted && storagePermissionsGranted;
  }

  RecordState({
    this.recognizeState = RecognizeState.idle,
    this.oldRecognizeState,
    this.synthesizeState = SynthesizeState.idle,
    this.oldSynthesizeState,
    this.audioPermissionsGranted = false,
    this.storagePermissionsGranted = false
  }) : super();

  void from(RecordState state) {
    this.audioPermissionsGranted = state.audioPermissionsGranted;
    this.storagePermissionsGranted = state.storagePermissionsGranted;
    this.recognizeState = state.recognizeState;
    this.oldRecognizeState = state.oldRecognizeState;
    this.lastRecognizeResult = state.lastRecognizeResult;
    this.recordRecognized = state.recordRecognized;
    this.synthesizeState = state.synthesizeState;
    this.oldSynthesizeState = state.oldSynthesizeState;
  }
}

class HomeInitialState extends RecordState {

}

class HomeRecognizeStateUpdatedState extends RecordState {

  void setRecognizeUpdate(RecognizeStateUpdate recognizeStateUpdate) {
    this.recognizeState = recognizeStateUpdate.newState;
    this.oldRecognizeState = recognizeStateUpdate.oldState;
  }

  void setSynthesizeUpdate(SynthesizeStateUpdate synthesizeStateUpdate) {
    this.synthesizeState = synthesizeStateUpdate.newState;
    this.oldSynthesizeState = synthesizeStateUpdate.oldState;
  }
}

class HomeRecognizeResultUpdatedState extends RecordState {

  bool addResult(RecognizeResult recognizeResult) {
    this.lastRecognizeResult = recognizeResult;
    return this.recordRecognized.addResult(recognizeResult);
  }
}