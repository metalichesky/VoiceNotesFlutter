part of 'record_bloc.dart';

abstract class RecordState {
  RecognizeState? recognizeState;
  RecognizeState? oldRecognizeState;
  SynthesizeState? synthesizeState;
  SynthesizeState? oldSynthesizeState;

  RecognizeResult? lastRecognizeResult;
  EditableTextRecord? editedRecord;

  bool audioPermissionsGranted;
  bool storagePermissionsGranted;

  bool isAllPermissionsGranted() {
    return audioPermissionsGranted && storagePermissionsGranted;
  }

  RecordState(
      {this.recognizeState = RecognizeState.idle,
      this.oldRecognizeState,
      this.synthesizeState = SynthesizeState.idle,
      this.oldSynthesizeState,
      this.audioPermissionsGranted = false,
      this.storagePermissionsGranted = false})
      : super();

  void updateRecord(EditableTextRecord record) {
    this.editedRecord = record;
  }

  void from(RecordState state) {
    this.audioPermissionsGranted = state.audioPermissionsGranted;
    this.storagePermissionsGranted = state.storagePermissionsGranted;
    this.recognizeState = state.recognizeState;
    this.oldRecognizeState = state.oldRecognizeState;
    this.lastRecognizeResult = state.lastRecognizeResult;
    this.editedRecord = state.editedRecord;
    this.synthesizeState = state.synthesizeState;
    this.oldSynthesizeState = state.oldSynthesizeState;
  }
}

class RecordInitialState extends RecordState {}

class RecordEditState extends RecordState {

}

class RecordRecognizeStateUpdatedState extends RecordState {
  void setRecognizeUpdate(RecognizeStateUpdate recognizeStateUpdate) {
    this.recognizeState = recognizeStateUpdate.newState;
    this.oldRecognizeState = recognizeStateUpdate.oldState;
  }
}

class RecordSynthesizeStateUpdatedState extends RecordState {
  void setSynthesizeUpdate(SynthesizeStateUpdate synthesizeStateUpdate) {
    this.synthesizeState = synthesizeStateUpdate.newState;
    this.oldSynthesizeState = synthesizeStateUpdate.oldState;
  }
}

class RecordRecognizeResultUpdatedState extends RecordState {
  void setRecognizeResult(RecognizeResult recognizeResult) {
    this.lastRecognizeResult = recognizeResult;
  }
}

class RecordClosedState extends RecordState {

}

class RecordSavedState extends RecordState {

}

class RecordInternalUpdatedState extends RecordState {

}

class RecordInputUpdatedState extends RecordState {

}
