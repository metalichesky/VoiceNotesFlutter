import 'package:flutter/material.dart';
import 'package:voice_note/presentation/ui/home_page.dart';

const String ROUTE_HOME = '/';
const String ROUTE_RECORD = '/record';
const String ROUTE_SETTINGS = '/settings';

class RouterUtils {
  static Route<dynamic> generateRoute(RouteSettings settings) {
    switch (settings.name) {
      case ROUTE_HOME:
        return MaterialPageRoute(builder: (_) => HomePage());
      // case ROUTE_RECORD:
      //   return MaterialPageRoute(builder: (_) => RecordPage());
      // case ROUTE_SETTINGS:
      //   return MaterialPageRoute(builder: (_) => SettingsPage());
      default:
        return MaterialPageRoute(builder: (_) => HomePage());
    }
  }
}