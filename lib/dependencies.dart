import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:voice_note/data/datasource/local_database.dart';
import 'package:voice_note/data/datasource/recognize_platform_controller.dart';
import 'package:voice_note/data/datasource/synthesize_platform_controller.dart';
import 'package:voice_note/data/datasource/system_datasource.dart';
import 'package:voice_note/data/repository/recognize_controller_impl.dart';
import 'package:voice_note/data/repository/synthesize_controller_impl.dart';
import 'package:voice_note/data/repository/system_repository_impl.dart';
import 'package:voice_note/domain/abstractions/recognize_controller.dart';
import 'package:voice_note/domain/abstractions/records_repository.dart';
import 'package:voice_note/domain/abstractions/synthesize_controller.dart';
import 'package:voice_note/domain/abstractions/system_repository.dart';
import 'package:voice_note/domain/usecase/permission_use_case.dart';
import 'package:voice_note/domain/usecase/recognize_use_case.dart';
import 'package:voice_note/domain/usecase/synthesize_use_case.dart';
import 'package:voice_note/domain/usecase/system_use_case.dart';
import 'package:voice_note/presentation/presenter/main_bloc.dart';
import 'package:voice_note/presentation/presenter/record_bloc.dart';

import 'data/repository/records_repository_impl.dart';
import 'domain/usecase/records_use_case.dart';

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
  getIt.registerLazySingleton<RecognizeController>(() => RecognizeControllerImpl(
    controller: getIt()
  ));
  getIt.registerLazySingleton<SynthesizeController>(() => SynthesizeControllerImpl(
      controller: getIt()
  ));
  getIt.registerLazySingleton<RecordsRepository>(() => RecordsRepositoryImpl(
    localDatabase: getIt()
  ));

  // DataSources
  getIt.registerLazySingleton<SystemDataSource>(() => SystemDataSource());
  getIt.registerLazySingleton<RecognizePlatformController>(() => RecognizePlatformController());
  getIt.registerLazySingleton<SynthesizePlatformController>(() => SynthesizePlatformController());
  getIt.registerLazySingleton<LocalDatabase>(() => LocalDatabase());

  // Use Cases
  getIt.registerLazySingleton(() => SystemUseCase(
      systemRepository: getIt()
  ));
  getIt.registerLazySingleton(() => RecognizeUseCase(
      recognizeController: getIt()
  ));
  getIt.registerLazySingleton(() => SynthesizeUseCase(
      synthesizeController: getIt()
  ));
  getIt.registerLazySingleton(() => PermissionUseCase());
  getIt.registerLazySingleton(() => RecordsUseCase(
    recordsRepository: getIt()
  ));

  // Blocs
  getIt.registerFactory(() => RecordBloc(
      systemUseCase: getIt(),
      recognizeUseCase: getIt(),
      synthesizeUseCase: getIt(),
      permissionUseCase: getIt(),
      recordsUseCase: getIt(),
  ));
  getIt.registerFactory(() => MainBloc(
      recordsUseCase: getIt()
  ));
}
