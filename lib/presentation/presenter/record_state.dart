part of 'record_bloc.dart';

@immutable
abstract class RecordState {
  RecognizeState? recognizeState;
  RecognizeState? oldRecognizeState;
  bool audioPermissionsGranted;
  bool storagePermissionsGranted;

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