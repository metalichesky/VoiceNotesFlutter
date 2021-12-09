part of 'home_bloc.dart';

@immutable
abstract class HomeEvent {}

class HomeCheckPermissionsEvent extends HomeEvent {}

class HomeRequestPermissionsEvent extends HomeEvent {}

class HomeRecognizeStateUpdatedEvent extends HomeEvent {
  RecognizeStateUpdate stateUpdate;

  HomeRecognizeStateUpdatedEvent({required this.stateUpdate});

}

class HomeRecognizeSwitchEvent extends HomeEvent {}