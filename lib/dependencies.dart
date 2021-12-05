import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:voice_notes/data/datasource/system_datasource.dart';
import 'package:voice_notes/data/repository/system_repository_impl.dart';
import 'package:voice_notes/domain/abstractions/system_repository.dart';
import 'package:voice_notes/domain/usecase/system_use_case.dart';
import 'package:voice_notes/presentation/presenter/home_bloc.dart';

final getIt = GetIt.instance; //sl is referred to as Service Locator

//Dependency injection
Future<void> init() async {
  // Blocs
  getIt.registerFactory(() => HomeBloc(
      useCase: getIt()
  ));

  // Use Cases
  getIt.registerLazySingleton(() => SystemUseCase(
      systemRepository: getIt()
  ));

  // Shared Prefs
  SharedPreferences.setMockInitialValues({});
  final sharedPreferences = await SharedPreferences.getInstance();
  getIt.registerLazySingleton<SharedPreferences>(() => sharedPreferences);

  // Repositories
  getIt.registerLazySingleton<SystemRepository>(() => SystemRepositoryImpl(
    systemDataSource: getIt()
  ));

  // DataSources
  getIt.registerLazySingleton<SystemDataSource>(() => SystemDataSourceImpl());
}
