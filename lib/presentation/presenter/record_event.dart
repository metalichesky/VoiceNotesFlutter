part of 'record_bloc.dart';

@immutable
abstract class RecordEvent {}

class RecordCheckPermissionsEvent extends RecordEvent {}

class RecordRequestPermissionsEvent extends RecordEvent {}

class RecordRecognizeStateUpdatedEvent extends RecordEvent {
  RecognizeStateUpdate stateUpdate;

  RecordRecognizeStateUpdatedEvent({required this.stateUpdate});

}

class RecordRecognizeSwitchEvent extends RecordEvent {}