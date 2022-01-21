part of 'record_bloc.dart';

abstract class RecordState {
  RecognizeState? recognizeState;
  RecognizeState? oldRecognizeState;
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
  }
}

class HomeInitialState extends RecordState {

}

class HomeRecognizeStateUpdatedState extends RecordState {

  void setUpdate(RecognizeStateUpdate recognizeStateUpdate) {
    this.recognizeState = recognizeStateUpdate.newState;
    this.oldRecognizeState = recognizeStateUpdate.oldState;
  }
}

class HomeRecognizeResultUpdatedState extends RecordState {

  bool addResult(RecognizeResult recognizeResult) {
    this.lastRecognizeResult = recognizeResult;
    return this.recordRecognized.addResult(recognizeResult);
  }
}