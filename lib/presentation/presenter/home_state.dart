part of 'home_bloc.dart';

@immutable
abstract class HomeState {
  List props;
  HomeState([this.props = const <dynamic>[]]) : super();
}

class HomeInitial extends HomeState {
  HomeInitial({double? batteryCharge}): super([batteryCharge]);
}

class HomeBatteryChargeUpdated extends HomeState {
  HomeBatteryChargeUpdated({double? batteryCharge}): super([batteryCharge]);
}