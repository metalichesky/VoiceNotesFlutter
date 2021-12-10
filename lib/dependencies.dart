import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:voice_note/data/datasource/recognize_controller.dart';
import 'package:voice_note/data/datasource/system_datasource.dart';
import 'package:voice_note/data/repository/recognize_repository_impl.dart';
import 'package:voice_note/data/repository/system_repository_impl.dart';
import 'package:voice_note/domain/abstractions/recognize_repository.dart';
import 'package:voice_note/domain/abstractions/system_repository.dart';
import 'package:voice_note/domain/usecase/permission_use_case.dart';
import 'package:voice_note/domain/usecase/recognize_use_case.dart';
import 'package:voice_note/domain/usecase/system_use_case.dart';
import 'package:voice_note/presentation/presenter/record_bloc.dart';

final getIt = GetIt.instance; //sl is referred to as Service Locator

//Dependency injection
Future<void> init() async {
  // Shared Prefs
  SharedPreferences.setMockInitialValues({});
  final sharedPreferences = await SharedPreferences.getInstance();
  getIt.registerLazySingleton<SharedPreferences>(() => sharedPreferences);

  // Repositories
  getIt.registerLazySingleton<SystemRepository>(() => SystemRepositoryImpl(
    systemDataSource: getIt()
  ));
  getIt.registerLazySingleton<RecognizeRepository>(() => RecognizeRepositoryImpl(
    controller: getIt()
  ));

  // DataSources
  getIt.registerLazySingleton<SystemDataSource>(() => SystemDataSource());
  getIt.registerLazySingleton<RecognizeController>(() => RecognizeController());

  // Use Cases
  getIt.registerLazySingleton(() => SystemUseCase(
      systemRepository: getIt()
  ));
  getIt.registerLazySingleton(() => RecognizeUseCase(
      recognizeRepository: getIt()
  ));
  getIt.registerLazySingleton(() => PermissionUseCase());

  // Blocs
  getIt.registerFactory(() => RecordBloc(
      systemUseCase: getIt(),
      recognizeUseCase: getIt(),
      permissionUseCase: getIt()
  ));
}
