import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:voice_note/dependencies.dart' as di;
import 'package:logging/logging.dart';

import 'core/util/router.dart';
import 'core/util/theme.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  //Inject all the dependencies and wait for it is done (i.e. UI won't built until all the dependencies are injected)
  await di.init();
  setupLogging();
  await SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);
  runApp(const VoiceNotesApp());
}

void setupLogging() {
  Logger.root.level = Level.ALL;
  Logger.root.onRecord.listen((rec) {
    print('${rec.level.name}: ${rec.time}: ${rec.message}');
  });
}

class VoiceNotesApp extends StatelessWidget {
  const VoiceNotesApp({Key? key}) : super(key: key);
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Voice Notes',
      theme: ThemeUtils.mainTheme,
      onGenerateRoute: RouterUtils.generateRoute,
      initialRoute: ROUTE_RECORD,
    );
  }
}

/// Custom [BlocObserver] that observes all bloc and cubit state changes.
class AppBlocObserver extends BlocObserver {
  @override
  void onChange(BlocBase bloc, Change change) {
    super.onChange(bloc, change);
    if (bloc is Cubit) {
      print(change);
    }
  }

  @override
  void onTransition(Bloc bloc, Transition transition) {
    super.onTransition(bloc, transition);
    print(transition);
  }
}