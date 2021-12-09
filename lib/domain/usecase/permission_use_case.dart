
import 'package:permission_handler/permission_handler.dart';

class PermissionUseCase {

  Future<bool> isAudioRecordAvailable() async {
    return await Permission.speech.isGranted;
  }

  Future<bool> requestAudioRecord() async {
    bool isPermanentlyDenied = await Permission.speech.isPermanentlyDenied;
    PermissionStatus permissionStatus = PermissionStatus.denied;
    if (!isPermanentlyDenied) {
      permissionStatus = await Permission.speech.request();
    } else {
      openAppSettings();
    }
    return permissionStatus.isGranted || permissionStatus.isLimited;
  }

  Future<bool> isExternalStorageAvailable() async {
    return await Permission.storage.isGranted;
  }

  Future<bool> requestExternalStorage() async {
    bool isPermanentlyDenied = await Permission.storage.isPermanentlyDenied;
    PermissionStatus permissionStatus = PermissionStatus.denied;
    if (!isPermanentlyDenied) {
      permissionStatus = await Permission.storage.request();
    } else {
      openAppSettings();
    }
    return permissionStatus.isGranted || permissionStatus.isLimited;
  }

}