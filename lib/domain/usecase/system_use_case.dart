
import 'package:voice_notes/domain/abstractions/system_repository.dart';

class SystemUseCase {
  SystemRepository systemRepository;

  SystemUseCase({required this.systemRepository});

  Future<double?> getBatteryCharge() {
    return systemRepository.getBatteryCharge();
  }
}