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

class RecordSynthesizeStateUpdatedEvent extends RecordEvent {
  SynthesizeStateUpdate stateUpdate;

  RecordSynthesizeStateUpdatedEvent({required this.stateUpdate});
}

class RecordRecognizeSwitchEvent extends RecordEvent {}

class RecordRecognizeInitializeEvent extends RecordEvent {}

class RecordRecognizeStartEvent extends RecordEvent {}

class RecordRecognizePauseEvent extends RecordEvent {}

class RecordRecognizeStopEvent extends RecordEvent {}

class RecordClearEvent extends RecordEvent {}

class RecordSaveEvent extends RecordEvent {}


class RecordSynthesizeSwitchEvent extends RecordEvent {}

class RecordSynthesizeInitializeEvent extends RecordEvent {}

class RecordSynthesizeStartEvent extends RecordEvent {}

class RecordSynthesizeResumeEvent extends RecordEvent {}

class RecordSynthesizePauseEvent extends RecordEvent {}

class RecordSynthesizeStopEvent extends RecordEvent {}