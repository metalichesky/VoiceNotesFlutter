// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'recognize_result.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecognizeResult _$RecognizeResultFromJson(Map<String, dynamic> json) =>
    RecognizeResult(
      text: json['text'] as String?,
      partial: json['partial'] as String?,
    );

Map<String, dynamic> _$RecognizeResultToJson(RecognizeResult instance) =>
    <String, dynamic>{
      'text': instance.text,
      'partial': instance.partial,
    };
