
import 'package:voice_notes/data/datasource/system_datasource.dart';
import 'package:voice_notes/domain/abstractions/system_repository.dart';

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