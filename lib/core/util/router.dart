import 'package:flutter/material.dart';
import 'package:voice_note/presentation/ui/main_page.dart';
import 'package:voice_note/presentation/ui/record_page.dart';

const String ROUTE_MAIN = '/';
const String ROUTE_RECORD = '/record';
const String ROUTE_SETTINGS = '/settings';

class RouterUtils {
  static Route<dynamic> generateRoute(RouteSettings settings) {
    switch (settings.name) {
      case ROUTE_MAIN:
        return MaterialPageRoute(builder: (_) => MainPage());
      case ROUTE_RECORD:
        return MaterialPageRoute(builder: (_) => RecordPage());
      // case ROUTE_RECORD:
      //   return MaterialPageRoute(builder: (_) => RecordPage());
      // case ROUTE_SETTINGS:
      //   return MaterialPageRoute(builder: (_) => SettingsPage());
      default:
        return MaterialPageRoute(builder: (_) => RecordPage());
    }
  }
}