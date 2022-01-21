part of 'record_bloc.dart';

@immutable
abstract class RecordEvent {}

class RecordCheckPermissionsEvent extends RecordEvent {}

class RecordRequestPermissionsEvent extends RecordEvent {}

class RecordRecognizeStateUpdatedEvent extends RecordEvent {
  RecognizeStateUpdate stateUpdate;

  RecordRecognizeStateUpdatedEvent({required this.stateUpdate});

}

class RecordRecognizeResultUpdatedEvent extends RecordEvent {
  RecognizeResult recognizeResult;

  RecordRecognizeResultUpdatedEvent({required this.recognizeResult});

}

class RecordRecognizeSwitchEvent extends RecordEvent {}

class RecordRecognizeInitializeEvent extends RecordEvent {}

class RecordRecognizeStartEvent extends RecordEvent {}

class RecordRecognizePauseEvent extends RecordEvent {}

class RecordRecognizeStopEvent extends RecordEvent {}

class RecordRecognizeClearEvent extends RecordEvent {}

class RecordRecognizeSaveEvent extends RecordEvent {}