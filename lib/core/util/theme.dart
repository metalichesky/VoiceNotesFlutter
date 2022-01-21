import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:voice_note/core/util/color.dart';

class ThemeUtils {
  static ButtonStyle textButtonStyle =
      TextButton.styleFrom(primary: ColorUtils.valhalla);
  static ButtonStyle elevatedButtonStyle = ElevatedButton.styleFrom(
    primary: ColorUtils.valhalla,
    onPrimary: ColorUtils.white,
    minimumSize: Size(88, 36),
    padding: EdgeInsets.symmetric(horizontal: 12),
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.all(Radius.circular(4)),
    ),
  );

  static ThemeData mainTheme = ThemeData(
    // Default brightness and colors.
    brightness: Brightness.light,
    primaryColor: ColorUtils.violet,
    accentColor: ColorUtils.vividViolet,
    cardColor: ColorUtils.valhalla,
    // Default font family.
    fontFamily: 'Roboto',
    buttonColor: ColorUtils.valhalla,

    // colorScheme: const ColorScheme.dark(
    //   primary: ColorUtils.violet,
    // ),

    textButtonTheme: TextButtonThemeData(style: textButtonStyle),

    elevatedButtonTheme: ElevatedButtonThemeData(style: elevatedButtonStyle),

    buttonTheme: const ButtonThemeData(
      colorScheme: ColorScheme.dark(
        primary: ColorUtils.valhalla
      ),
      buttonColor: ColorUtils.valhalla,
      focusColor: ColorUtils.portGore,
      hoverColor: ColorUtils.portGore,
      disabledColor: ColorUtils.valhalla50,
      splashColor: ColorUtils.portGore,
    ),

    iconTheme: const IconThemeData(color: ColorUtils.white),
    // Default TextTheme. Use this to specify the default
    // text styling for headlines, titles, bodies of text, and etc.

    textTheme: const TextTheme(
      headline1: TextStyle(
        fontSize: 20.0,
        fontWeight: FontWeight.bold,
        color: ColorUtils.white,
      ),
      headline2: TextStyle(
        fontSize: 18.0,
        fontWeight: FontWeight.bold,
        color: ColorUtils.white,
      ),
      headline3: TextStyle(
        fontSize: 16.0,
        fontWeight: FontWeight.bold,
        color: ColorUtils.kimberly,
      ),
      headline4: TextStyle(
        fontSize: 14.0,
        fontWeight: FontWeight.bold,
        color: ColorUtils.kimberly,
        fontFamily: 'Roboto',
      ),
      bodyText1: TextStyle(fontSize: 16.0, color: ColorUtils.kimberly),
      bodyText2: TextStyle(fontSize: 16.0, color: ColorUtils.smoky),
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
