import 'dart:core';

import 'package:voice_note/domain/entity/recognize_result.dart';

abstract class Record {
  int id = 0;
  String title = "";
  String text = "";
  String createDate = "";
  String lastChangeDate = "";

  Record();

  Record.create(
      {required this.id,
      required this.title,
      required this.text,
      required this.createDate,
      required this.lastChangeDate});

  Record.empty();

  DateTime? get createDateTime {
    return DateTime.tryParse(createDate);
  }

  DateTime? get lastChangeDateTime {
    return DateTime.tryParse(lastChangeDate);
  }

  void setText(String text) {
    this.text = text;
  }

  void setTitle(String title) {
    this.title = title;
  }
}

class TextRecord extends Record {
  TextRecord.create(
      {required int id,
      required String title,
      required String text,
      required String createDate,
      required String lastChangeDate})
      : super.create(
            id: id,
            title: title,
            text: text,
            createDate: createDate,
            lastChangeDate: lastChangeDate);
}

class EditableTextRecord extends Record {
  RecognizeResult? lastRecognizeResult = null;

  EditableTextRecord();

  EditableTextRecord.fromTextRecord(TextRecord record) {
    from(record);
  }

  String get lastRecognizedText {
    String? lastRecognizedText = lastRecognizeResult?.anyResult;
    if (lastRecognizedText != null) {
      return lastRecognizedText;
    } else {
      return "";
    }
  }

  String get allText {
    StringBuffer stringBuffer = StringBuffer();
    stringBuffer.write(text);
    if (lastRecognizeResult != null) {
      if (stringBuffer.isNotEmpty) {
        stringBuffer.write(" ");
      }
      stringBuffer.write(lastRecognizedText);
    }
    return stringBuffer.toString();
  }

  bool isEmpty() {
    return allText.isEmpty;
  }

  void removeResult() {
    lastRecognizeResult = null;
  }

  bool addResult(RecognizeResult recognizeResult) {
    String? newText = recognizeResult.anyResult;
    if (newText != null && newText.isNotEmpty) {
      if (recognizeResult.type == RecognizeResultType.TEXT) {
        StringBuffer textBuffer = StringBuffer();
        textBuffer.write(text);
        if (textBuffer.isNotEmpty || !text.endsWith(" ")) {
          textBuffer.write(" ");
        }
        textBuffer.write(recognizeResult.text ?? "");
        setText(textBuffer.toString());
        lastRecognizeResult = null;
      } else {
        lastRecognizeResult = recognizeResult;
      }
      return true;
    } else {
      return false;
    }
  }

  TextRecord complete() {
    return TextRecord.create(
        id: id,
        title: title,
        text: text,
        createDate: createDate,
        lastChangeDate: lastChangeDate
    );
  }

  void from(TextRecord record) {
    id = record.id;
    title = record.title;
    text = record.text;
    createDate = record.createDate;
    lastChangeDate = record.lastChangeDate;
  }

  void clear() {
    lastRecognizeResult = null;
    setText("");
  }
}
