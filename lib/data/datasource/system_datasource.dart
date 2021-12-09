
import 'package:flutter/services.dart';
import 'package:logging/logging.dart';
import 'package:voice_note/core/util/platform.dart';

class SystemDataSource {
  final MethodChannel _systemChannel = PlatformUtils.channelSystem;

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