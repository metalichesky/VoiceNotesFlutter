import 'dart:collection';
import 'dart:core';

import 'package:voice_note/domain/entity/recognize_result.dart';

abstract class Record {}

class RecordRecognized extends Record {
  RecognizeResult? lastRecognizeResult = null;

  String get lastRecognizedText {
    String? lastRecognizedText = lastRecognizeResult?.anyResult;
    if (lastRecognizedText != null) {
      return lastRecognizedText;
    } else {
      return "";
    }
  }

  String text = "";

  String get allText {
    StringBuffer stringBuffer = StringBuffer();
    stringBuffer.write(text);
    if (lastRecognizeResult != null) {
      if (stringBuffer.isNotEmpty) {
        stringBuffer.write(" ");
      }
      stringBuffer.write(lastRecognizeResult?.anyResult ?? "");
    }
    return stringBuffer.toString();
  }

  bool isEmpty() {
    return allText.isEmpty;
  }

  void updateText(String text) {
    this.text = text;
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
        text = textBuffer.toString();
        lastRecognizeResult = null;
      } else {
        lastRecognizeResult = recognizeResult;
      }
      return true;
    } else {
      return false;
    }
  }

  void clear() {
    lastRecognizeResult = null;
    text = "";
  }
}
