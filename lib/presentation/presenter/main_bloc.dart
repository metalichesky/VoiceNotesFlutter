import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:logging/logging.dart';
import 'package:voice_note/domain/entity/record.dart';
import 'package:voice_note/domain/usecase/records_use_case.dart';

import 'main_event.dart';
import 'main_state.dart';

class MainBloc extends Bloc<MainEvent, MainState> {
  RecordsUseCase recordsUseCase;
  late StreamController<List<TextRecord>> allRecordsStream;
  late StreamSubscription allRecordsStreamSubscription;

  MainBloc({required this.recordsUseCase}) : super(MainInitialState()) {
    _registerEventMapping();
    allRecordsStream = recordsUseCase.allRecords;
    allRecordsStreamSubscription = allRecordsStream.stream.listen((event) {
      Logger.root.info("MainBloc: records ${event.length}");
      add(MainRecordsUpdatedEvent(records: event));
    });
  }

  void _registerEventMapping() {
    on<MainCreateRecordEvent>((event, emit) async {
      await _createRecord(event, emit);
    });
    on<MainDeleteRecordEvent>((event, emit) async {
      _deleteRecord(event, emit);
    });
    on<MainRecordsUpdatedEvent>((event, emit) async {
      _updateRecords(event, emit);
    });
    on<MainEditRecordEvent>((event, emit) async {
      _editRecord(event, emit);
    });
  }

  Future<void> _updateRecords(MainEvent event, Emitter<MainState> emit) async {
    if (event is MainRecordsUpdatedEvent) {
      MainRecordsUpdatedState newState = MainRecordsUpdatedState();
      newState.from(state);
      newState.update(event.records);
      emit(newState);
    }
  }

  Future<void> _editRecord(MainEvent event, Emitter<MainState> emit) async {
    if (event is MainEditRecordEvent) {
      var newState = MainEditRecordState();
      recordsUseCase.editRecord(event.record);
      newState.from(state);
      emit(newState);
    }
  }

  Future<void> _createRecord(MainEvent event, Emitter<MainState> emit) async {
    Logger.root.info("MainBloc: createRecord");
    TextRecord record = await recordsUseCase.createNewRecord();
    recordsUseCase.editRecord(record);
    Logger.root.info(
        "MainBloc: createRecord: records ${await recordsUseCase.getAllRecordsCount()}");
  }

  Future<void> _deleteRecord(MainEvent event, Emitter<MainState> emit) async {
    if (event is MainDeleteRecordEvent) {
      recordsUseCase.deleteRecord(event.record);
    }
  }

  @override
  Future<void> close() {
    allRecordsStreamSubscription.cancel();
    return super.close();
  }
}
