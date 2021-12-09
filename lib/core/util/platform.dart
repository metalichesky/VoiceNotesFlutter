
import 'package:flutter/services.dart';

import 'constants.dart';

class PlatformUtils {
  static const channelSystem = MethodChannel("$APP_ID/system");
  static const channelRecognize = MethodChannel("$APP_ID/recognize");

}