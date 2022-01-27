import 'dart:async';

import 'package:voice_note/domain/abstractions/records_repository.dart';
import 'package:voice_note/domain/entity/recognize_result.dart';
import 'package:voice_note/domain/entity/record.dart';

class RecordsUseCase {
  RecordsRepository recordsRepository;
  EditableTextRecord? editedRecord;

  late StreamController<List<TextRecord>> allRecords;

  RecordsUseCase({required this.recordsRepository}) {
    allRecords = recordsRepository.allRecords;
  }

  Future<List<TextRecord>> getAllRecords() async {
    return await recordsRepository.getAllRecords();
  }

  Future<int> getAllRecordsCount() async {
    return await recordsRepository.getAllRecordsCount();
  }

  Future<TextRecord> createNewRecord() async {
    var record = TextRecord.create(
        id: -1,
        title: "New Record",
        text: "",
        createDate: DateTime.now().toIso8601String(),
        lastChangeDate: DateTime.now().toIso8601String()
    );
    record = await saveRecord(record);
    return record;
  }

  void editRecord(TextRecord record) {
    editedRecord = EditableTextRecord.fromTextRecord(record);
  }

  bool addEditedRecordRecognizeResult(RecognizeResult result) {
    return editedRecord?.addResult(result) ?? false;
  }

  void removeEditedRecordRecognizeResult() {
    editedRecord?.removeResult();
  }

  void changeEditedRecordTitle(String title) {
    editedRecord?.title = title;
  }

  void changeEditedRecordText(String text) {
    editedRecord?.text = text;
  }

  void clearEditedRecordText() {
    editedRecord?.clear();
  }

  Future<TextRecord?> saveEditedRecord() async {
    TextRecord? resultRecord = null;
    if (editedRecord != null) {
      resultRecord = await saveRecord(editedRecord!.complete());
    }
    return resultRecord;
  }

  Future<TextRecord> saveRecord(TextRecord record) async {
    record.lastChangeDate = DateTime.now().toIso8601String();
    return await recordsRepository.saveRecord(record);
  }

  void deleteRecord(TextRecord record) {
    recordsRepository.deleteRecord(record);
  }
}
