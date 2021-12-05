import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:voice_notes/core/util/color.dart';


class ThemeUtils {
  static ThemeData mainTheme = ThemeData(
    // Default brightness and colors.
    brightness: Brightness.light,
    primaryColor: ColorUtils.logoBlue,
    accentColor: Colors.cyan[600],

    // Default font family.
    fontFamily: 'Roboto',

    // Default TextTheme. Use this to specify the default
    // text styling for headlines, titles, bodies of text, and etc.
    textTheme: const TextTheme(
      headline1: TextStyle(
        fontSize: 20.0,
        fontWeight: FontWeight.bold,
        color: ColorUtils.fontBlack,
      ),
      headline2: TextStyle(
        fontSize: 18.0,
        fontWeight: FontWeight.bold,
        color: ColorUtils.fontBlack,
      ),
      bodyText1: TextStyle(fontSize: 16.0, color: ColorUtils.fontBlack),
      bodyText2: TextStyle(fontSize: 16.0, color: ColorUtils.hintColor),
      button: TextStyle(
        color: ColorUtils.white,
        fontFamily: 'Roboto',
        fontWeight: FontWeight.w500,
        fontSize: 14,
        letterSpacing: 2,
      ),
    ),
  );
}
