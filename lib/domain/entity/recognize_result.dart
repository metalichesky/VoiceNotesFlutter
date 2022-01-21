

import 'package:json_annotation/json_annotation.dart';

part 'recognize_result.g.dart';

enum RecognizeResultType {
  PARTIAL,
  TEXT,
  UNKNOWN
}

extension RecognizeResultTypeExtension on RecognizeResultType {
  String get typeName {
    switch (this) {
      case RecognizeResultType.PARTIAL:
        return "partial";
      case RecognizeResultType.TEXT:
        return "text";
      case RecognizeResultType.UNKNOWN:
        return "unknown";
      default:
        return "unknown";
    }
  }
}

@JsonSerializable()
class RecognizeResult {
  String? text = null;
  String? partial = null;
  RecognizeResultType get type {
    if (partial != null) {
      return RecognizeResultType.PARTIAL;
    } else if (text != null) {
      return RecognizeResultType.TEXT;
    } else {
      return RecognizeResultType.UNKNOWN;
    }
  }

  RecognizeResult({required this.text, this.partial});

  String? get anyResult {
    if (partial != null) {
      return partial;
    } else {
      return text;
    }
  }

  factory RecognizeResult.fromJson(Map<String, dynamic> json) =>
      _$RecognizeResultFromJson(json);

  Map<String, dynamic> toJson() => _$RecognizeResultToJson(this);
}