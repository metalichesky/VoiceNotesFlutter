
import 'package:voice_note/domain/entity/record.dart';

abstract class MainState {
  List<TextRecord> records = [];

  MainState() {

  }

  void from(MainState state) {
    records = state.records;
  }

}

class MainInitialState extends MainState {

}

class MainRecordsUpdatedState extends MainState {

  void update(List<TextRecord> records) {
    this.records = records;
  }
}

class MainEditRecordState extends MainState {

}