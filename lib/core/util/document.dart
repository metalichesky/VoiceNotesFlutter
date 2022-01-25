
import 'dart:math';

import 'package:flutter_quill/flutter_quill.dart';

extension DocumentExtension on Document {

  String toPlainTextCorrect() {
    String text = toPlainText();
    return text.substring(0, max(0, text.length-1));
  }
}