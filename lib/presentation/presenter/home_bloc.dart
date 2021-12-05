import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:meta/meta.dart';
import 'package:voice_notes/domain/usecase/system_use_case.dart';

part 'home_event.dart';
part 'home_state.dart';

class HomeBloc extends Bloc<HomeEvent, HomeState> {
  SystemUseCase useCase;
  @override
  HomeState get initialState => HomeInitial();

  HomeBloc({required this.useCase}): super(HomeInitial()) {
    add(HomeBatteryUpdateEvent());
  }

  @override
  Stream<HomeState> mapEventToState(
    HomeEvent event,
  ) async* {
    switch (event.runtimeType) {
      case HomeBatteryUpdateEvent:
          double? batteryCharge = await useCase.getBatteryCharge();
          yield HomeBatteryChargeUpdated(batteryCharge: batteryCharge);
    }
  }
}
