
import 'package:flutter/services.dart';
import 'package:logging/logging.dart';
import 'package:voice_notes/core/util/platform.dart';

abstract class SystemDataSource {
  Future<double?> getBatteryCharge();
}

class SystemDataSourceImpl extends SystemDataSource {
  final MethodChannel _systemChannel = PlatformUtils.systemChannel;

  @override
  Future<double?> getBatteryCharge() async {
    double? batteryCharge;
    try {
      batteryCharge = await _systemChannel.invokeMethod<double>("getBatteryCharge");
    } on PlatformException catch (e) {
      Logger.root.shout("getBatteryCharge: ${e.message}", e);
    } on MissingPluginException catch(e) {
      Logger.root.shout("getBatteryCharge: ${e.message}", e);
    }
    return Future.value(batteryCharge);
  }
}