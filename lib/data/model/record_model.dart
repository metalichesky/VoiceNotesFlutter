import 'package:drift/drift.dart';
import 'package:voice_note/data/datasource/local_database.dart';
import 'package:voice_note/domain/entity/record.dart';

class RecordModel extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get title => text().named('title')();
  TextColumn get content => text().named('text')();
  TextColumn get createDate => text().named('createDate')();
  TextColumn get lastChangeDate => text().named('lastEditDate')();
}

extension RecordModelExtension on RecordModelData {

  TextRecord toData() {
    return TextRecord.create(
        id: this.id,
        title: this.title,
        text: this.content,
        createDate: this.createDate,
        lastChangeDate: this.lastChangeDate);
  }
}


extension RecordExtension on Record {

  RecordModelData toModel() {
    return RecordModelData(
        id: this.id,
        title: this.title,
        content: this.text,
        createDate: this.createDate,
        lastChangeDate: this.lastChangeDate
    );
  }
}
