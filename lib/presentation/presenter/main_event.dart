
import 'package:voice_note/domain/entity/record.dart';

abstract class MainEvent {



}

class MainRecordsUpdatedEvent extends MainEvent {
    List<TextRecord> records;

    MainRecordsUpdatedEvent({required this.records});
}

class MainCreateRecordEvent extends MainEvent {

}

class MainEditRecordEvent extends MainEvent {
  TextRecord record;

  MainEditRecordEvent({required this.record});
}

class MainDeleteRecordEvent extends MainEvent {
  TextRecord record;

  MainDeleteRecordEvent({required this.record});
}