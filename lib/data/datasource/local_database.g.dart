// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'local_database.dart';

// **************************************************************************
// MoorGenerator
// **************************************************************************

// ignore_for_file: unnecessary_brace_in_string_interps, unnecessary_this
class RecordModelData extends DataClass implements Insertable<RecordModelData> {
  final int id;
  final String title;
  final String content;
  final String createDate;
  final String lastChangeDate;
  RecordModelData(
      {required this.id,
      required this.title,
      required this.content,
      required this.createDate,
      required this.lastChangeDate});
  factory RecordModelData.fromData(Map<String, dynamic> data,
      {String? prefix}) {
    final effectivePrefix = prefix ?? '';
    return RecordModelData(
      id: const IntType()
          .mapFromDatabaseResponse(data['${effectivePrefix}id'])!,
      title: const StringType()
          .mapFromDatabaseResponse(data['${effectivePrefix}title'])!,
      content: const StringType()
          .mapFromDatabaseResponse(data['${effectivePrefix}text'])!,
      createDate: const StringType()
          .mapFromDatabaseResponse(data['${effectivePrefix}createDate'])!,
      lastChangeDate: const StringType()
          .mapFromDatabaseResponse(data['${effectivePrefix}lastEditDate'])!,
    );
  }
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['title'] = Variable<String>(title);
    map['text'] = Variable<String>(content);
    map['createDate'] = Variable<String>(createDate);
    map['lastEditDate'] = Variable<String>(lastChangeDate);
    return map;
  }

  RecordModelCompanion toCompanion(bool nullToAbsent) {
    return RecordModelCompanion(
      id: Value(id),
      title: Value(title),
      content: Value(content),
      createDate: Value(createDate),
      lastChangeDate: Value(lastChangeDate),
    );
  }

  factory RecordModelData.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return RecordModelData(
      id: serializer.fromJson<int>(json['id']),
      title: serializer.fromJson<String>(json['title']),
      content: serializer.fromJson<String>(json['content']),
      createDate: serializer.fromJson<String>(json['createDate']),
      lastChangeDate: serializer.fromJson<String>(json['lastChangeDate']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'title': serializer.toJson<String>(title),
      'content': serializer.toJson<String>(content),
      'createDate': serializer.toJson<String>(createDate),
      'lastChangeDate': serializer.toJson<String>(lastChangeDate),
    };
  }

  RecordModelData copyWith(
          {int? id,
          String? title,
          String? content,
          String? createDate,
          String? lastChangeDate}) =>
      RecordModelData(
        id: id ?? this.id,
        title: title ?? this.title,
        content: content ?? this.content,
        createDate: createDate ?? this.createDate,
        lastChangeDate: lastChangeDate ?? this.lastChangeDate,
      );
  @override
  String toString() {
    return (StringBuffer('RecordModelData(')
          ..write('id: $id, ')
          ..write('title: $title, ')
          ..write('content: $content, ')
          ..write('createDate: $createDate, ')
          ..write('lastChangeDate: $lastChangeDate')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode =>
      Object.hash(id, title, content, createDate, lastChangeDate);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is RecordModelData &&
          other.id == this.id &&
          other.title == this.title &&
          other.content == this.content &&
          other.createDate == this.createDate &&
          other.lastChangeDate == this.lastChangeDate);
}

class RecordModelCompanion extends UpdateCompanion<RecordModelData> {
  final Value<int> id;
  final Value<String> title;
  final Value<String> content;
  final Value<String> createDate;
  final Value<String> lastChangeDate;
  const RecordModelCompanion({
    this.id = const Value.absent(),
    this.title = const Value.absent(),
    this.content = const Value.absent(),
    this.createDate = const Value.absent(),
    this.lastChangeDate = const Value.absent(),
  });
  RecordModelCompanion.insert({
    this.id = const Value.absent(),
    required String title,
    required String content,
    required String createDate,
    required String lastChangeDate,
  })  : title = Value(title),
        content = Value(content),
        createDate = Value(createDate),
        lastChangeDate = Value(lastChangeDate);
  static Insertable<RecordModelData> custom({
    Expression<int>? id,
    Expression<String>? title,
    Expression<String>? content,
    Expression<String>? createDate,
    Expression<String>? lastChangeDate,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (title != null) 'title': title,
      if (content != null) 'text': content,
      if (createDate != null) 'createDate': createDate,
      if (lastChangeDate != null) 'lastEditDate': lastChangeDate,
    });
  }

  RecordModelCompanion copyWith(
      {Value<int>? id,
      Value<String>? title,
      Value<String>? content,
      Value<String>? createDate,
      Value<String>? lastChangeDate}) {
    return RecordModelCompanion(
      id: id ?? this.id,
      title: title ?? this.title,
      content: content ?? this.content,
      createDate: createDate ?? this.createDate,
      lastChangeDate: lastChangeDate ?? this.lastChangeDate,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (title.present) {
      map['title'] = Variable<String>(title.value);
    }
    if (content.present) {
      map['text'] = Variable<String>(content.value);
    }
    if (createDate.present) {
      map['createDate'] = Variable<String>(createDate.value);
    }
    if (lastChangeDate.present) {
      map['lastEditDate'] = Variable<String>(lastChangeDate.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('RecordModelCompanion(')
          ..write('id: $id, ')
          ..write('title: $title, ')
          ..write('content: $content, ')
          ..write('createDate: $createDate, ')
          ..write('lastChangeDate: $lastChangeDate')
          ..write(')'))
        .toString();
  }
}

class $RecordModelTable extends RecordModel
    with TableInfo<$RecordModelTable, RecordModelData> {
  final GeneratedDatabase _db;
  final String? _alias;
  $RecordModelTable(this._db, [this._alias]);
  final VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int?> id = GeneratedColumn<int?>(
      'id', aliasedName, false,
      type: const IntType(),
      requiredDuringInsert: false,
      defaultConstraints: 'PRIMARY KEY AUTOINCREMENT');
  final VerificationMeta _titleMeta = const VerificationMeta('title');
  @override
  late final GeneratedColumn<String?> title = GeneratedColumn<String?>(
      'title', aliasedName, false,
      type: const StringType(), requiredDuringInsert: true);
  final VerificationMeta _contentMeta = const VerificationMeta('content');
  @override
  late final GeneratedColumn<String?> content = GeneratedColumn<String?>(
      'text', aliasedName, false,
      type: const StringType(), requiredDuringInsert: true);
  final VerificationMeta _createDateMeta = const VerificationMeta('createDate');
  @override
  late final GeneratedColumn<String?> createDate = GeneratedColumn<String?>(
      'createDate', aliasedName, false,
      type: const StringType(), requiredDuringInsert: true);
  final VerificationMeta _lastChangeDateMeta =
      const VerificationMeta('lastChangeDate');
  @override
  late final GeneratedColumn<String?> lastChangeDate = GeneratedColumn<String?>(
      'lastEditDate', aliasedName, false,
      type: const StringType(), requiredDuringInsert: true);
  @override
  List<GeneratedColumn> get $columns =>
      [id, title, content, createDate, lastChangeDate];
  @override
  String get aliasedName => _alias ?? 'record_model';
  @override
  String get actualTableName => 'record_model';
  @override
  VerificationContext validateIntegrity(Insertable<RecordModelData> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('title')) {
      context.handle(
          _titleMeta, title.isAcceptableOrUnknown(data['title']!, _titleMeta));
    } else if (isInserting) {
      context.missing(_titleMeta);
    }
    if (data.containsKey('text')) {
      context.handle(_contentMeta,
          content.isAcceptableOrUnknown(data['text']!, _contentMeta));
    } else if (isInserting) {
      context.missing(_contentMeta);
    }
    if (data.containsKey('createDate')) {
      context.handle(
          _createDateMeta,
          createDate.isAcceptableOrUnknown(
              data['createDate']!, _createDateMeta));
    } else if (isInserting) {
      context.missing(_createDateMeta);
    }
    if (data.containsKey('lastEditDate')) {
      context.handle(
          _lastChangeDateMeta,
          lastChangeDate.isAcceptableOrUnknown(
              data['lastEditDate']!, _lastChangeDateMeta));
    } else if (isInserting) {
      context.missing(_lastChangeDateMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  RecordModelData map(Map<String, dynamic> data, {String? tablePrefix}) {
    return RecordModelData.fromData(data,
        prefix: tablePrefix != null ? '$tablePrefix.' : null);
  }

  @override
  $RecordModelTable createAlias(String alias) {
    return $RecordModelTable(_db, alias);
  }
}

abstract class _$LocalDatabase extends GeneratedDatabase {
  _$LocalDatabase(QueryExecutor e) : super(SqlTypeSystem.defaultInstance, e);
  late final $RecordModelTable recordModel = $RecordModelTable(this);
  Selectable<int> allRecordsCount() {
    return customSelect('SELECT COUNT(*) FROM record_model;',
        variables: [],
        readsFrom: {
          recordModel,
        }).map((QueryRow row) => row.read<int>('COUNT(*)'));
  }

  @override
  Iterable<TableInfo> get allTables => allSchemaEntities.whereType<TableInfo>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [recordModel];
}
