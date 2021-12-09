
import 'package:voice_note/data/datasource/system_datasource.dart';
import 'package:voice_note/domain/abstractions/system_repository.dart';

class SystemRepositoryImpl extends SystemRepository {
  SystemDataSource systemDataSource;

  SystemRepositoryImpl({
    required this.systemDataSource
  });

  @override
  Future<double?> getBatteryCharge() {
    return systemDataSource.getBatteryCharge();
  }


}