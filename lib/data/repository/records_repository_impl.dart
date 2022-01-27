import 'dart:async';

import 'package:drift/drift.dart';
import 'package:rxdart/rxdart.dart';
import 'package:voice_note/data/datasource/local_database.dart';
import 'package:voice_note/data/model/record_model.dart';
import 'package:voice_note/domain/abstractions/records_repository.dart';
import 'package:voice_note/domain/entity/record.dart';

class RecordsRepositoryImpl extends RecordsRepository {
  LocalDatabase localDatabase;
  @override
  StreamController<List<TextRecord>> allRecords = BehaviorSubject();

  RecordsRepositoryImpl({required this.localDatabase}) {
    localDatabase.select(localDatabase.recordModel).watch().listen((event) {
      var records = event.map((e) => e.toData()).toList();
      allRecords.add(records);
    });
  }

  @override
  Future<int> getAllRecordsCount() async {
    return (await localDatabase.allRecordsCount().get()).single;
  }

  @override
  Future<List<TextRecord>> getAllRecords() async {
    var recordModels = await localDatabase.select(localDatabase.recordModel).get();
    return recordModels.map((e) => e.toData()).toList();
  }

  @override
  Future<bool> hasRecord(TextRecord record) async {
    var selected = localDatabase.select(localDatabase.recordModel);
    selected.where((tbl) => tbl.id.equals(record.id));
    return (await selected.get()).isNotEmpty;
  }

  @override
  Future<TextRecord> saveRecord(TextRecord record) async {
    late RecordModelData savedRecord;
    if (record.id < 0) {
      savedRecord = await localDatabase.into(localDatabase.recordModel).insertReturning(
          RecordModelCompanion.insert(
              title: record.title,
              content: record.text,
              createDate: record.createDate,
              lastChangeDate: record.lastChangeDate),
          mode: InsertMode.insertOrReplace);
    } else {
      savedRecord = await localDatabase.into(localDatabase.recordModel).insertReturning(
          record.toModel(),
          mode: InsertMode.insertOrReplace
      );
    }
    return savedRecord.toData();
  }

  @override
  void deleteRecord(TextRecord record) {
    localDatabase.delete(localDatabase.recordModel).delete(record.toModel());
  }

  @override
  void deleteAllRecords() {
    localDatabase.delete(localDatabase.recordModel).go();
  }

}
