

import 'dart:async';

import 'package:voice_note/domain/entity/record.dart';

abstract class RecordsRepository {
  abstract StreamController<List<TextRecord>> allRecords;

  Future<bool> hasRecord(TextRecord record);

  Future<List<TextRecord>> getAllRecords();

  Future<int> getAllRecordsCount();

  Future<TextRecord> saveRecord(TextRecord record);

  void deleteRecord(TextRecord record);

  void deleteAllRecords();
}