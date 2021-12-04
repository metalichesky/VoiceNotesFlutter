
import 'dart:convert';

import 'package:flutter/cupertino.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:voice_notes/core/error/exceptions.dart';
import 'package:voice_notes/core/util/constants.dart';
import 'package:voice_notes/data/model/record_params_model.dart';
import 'package:voice_notes/domain/entity/record_params.dart';


abstract class RecordParamsDataSource {
  Future<RecordParamsModel> getRecordParams();
  Future<void> setRecordParams(RecordParamsModel recordParams);
}

class RecordParamsDataSourceImpl extends RecordParamsDataSource {
  final SharedPreferences sharedPreferences;

  RecordParamsDataSourceImpl({required this.sharedPreferences});

  @override
  Future<void> setRecordParams(RecordParamsModel recordParamsModel) {
    return sharedPreferences.setString(PREFS_KEY_RECORD_PARAMS, jsonEncode(recordParamsModel));
  }

  @override
  Future<RecordParamsModel> getRecordParams() {
    String? jsonStr = sharedPreferences.getString(PREFS_KEY_RECORD_PARAMS);
    if (jsonStr == null) {
      throw CacheException();
    }
    return Future.value(RecordParamsModel.fromJson(jsonDecode(jsonStr)));
  }
}
