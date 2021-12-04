import 'package:flutter/cupertino.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_params_model.g.dart';

@JsonSerializable()
class RecordParamsModel {
  RecordParamsModel({required this.channelsCount});

  final int channelsCount;

  factory RecordParamsModel.fromJson(Map<String, dynamic> json) =>
      _$RecordParamsModelFromJson(json);

  Map<String, dynamic> toJson() => _$RecordParamsModelToJson(this);
}