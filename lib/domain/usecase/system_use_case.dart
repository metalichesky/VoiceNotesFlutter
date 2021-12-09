
import 'package:voice_note/domain/abstractions/system_repository.dart';

class SystemUseCase {
  SystemRepository systemRepository;

  SystemUseCase({required this.systemRepository});

  Future<double?> getBatteryCharge() {
    return systemRepository.getBatteryCharge();
  }
}