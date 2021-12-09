part of 'home_bloc.dart';

@immutable
abstract class HomeState {
  RecognizeState? recognizeState;
  RecognizeState? oldRecognizeState;
  bool audioPermissionsGranted;
  bool storagePermissionsGranted;

  HomeState({
    this.recognizeState = RecognizeState.idle,
    this.oldRecognizeState,
    this.audioPermissionsGranted = false,
    this.storagePermissionsGranted = false
  }) : super();

  void from(HomeState state) {
    this.audioPermissionsGranted = state.audioPermissionsGranted;
    this.storagePermissionsGranted = state.storagePermissionsGranted;
    this.recognizeState = state.recognizeState;
    this.oldRecognizeState = state.oldRecognizeState;
  }
}

class HomeInitialState extends HomeState {

}

class HomeRecognizeStateUpdatedState extends HomeState {

  void setUpdate(RecognizeStateUpdate recognizeStateUpdate) {
    this.recognizeState = recognizeStateUpdate.newState;
    this.oldRecognizeState = recognizeStateUpdate.oldState;
  }
}