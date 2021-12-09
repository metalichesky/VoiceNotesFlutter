import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:logging/logging.dart';
import 'package:voice_note/core/util/theme.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';
import 'package:voice_note/presentation/presenter/home_bloc.dart';

import '../../dependencies.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  // Invoke "debug painting" (press "p" in the console, choose the
  // "Toggle Debug Paint" action from the Flutter Inspector in Android
  // Studio, or the "Toggle Debug Paint" command in Visual Studio Code)
  // to see the wireframe for each widget.

  final String title = "Flutter Demo Home Page";

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String recognizeState = "";

  @override
  void initState() {
    super.initState();

  }

  @override
  Widget build(BuildContext context) {
    // This method is rerun every time setState is called, for instance as done
    // by the _incrementCounter method above.
    //
    // The Flutter framework has been optimized to make rerunning build methods
    // fast, so that you can just rebuild anything that needs updating rather
    // than having to individually change instances of widgets.
    return _buildPage(context);
  }


  BlocProvider<HomeBloc> _buildPage(BuildContext context) {
    return BlocProvider<HomeBloc>(
        create: (_) => getIt<HomeBloc>(),
        child: Scaffold(
            appBar: AppBar(
              // Here we take the value from the MyHomePage object that was created by
              // the App.build method, and use it to set our appbar title.
              title: Text(widget.title),
            ),
            body:
            Center(
              // Center is a layout widget. It takes a single child and positions it
              // in the middle of the parent.
              child: Column(
                //
                // Column has various properties to control how it sizes itself and
                // how it positions its children. Here we use mainAxisAlignment to
                // center the children vertically; the main axis here is the vertical
                // axis because Columns are vertical (the cross axis would be
                // horizontal).
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  _buildRecognizeStateText(),
                  _buildSwitchRecognizeButton(),
                ],
              ),
            ),
            floatingActionButton: _buildCreateRecordButton()
          //This trailing comma makes auto-formatting nicer for build methods.
        )
    );
  }

  BlocBuilder _buildCreateRecordButton() {
    return BlocBuilder<HomeBloc, HomeState>(
        builder: (context, state) {
      if (state is HomeState) {

      }
        return FloatingActionButton(
          onPressed: ()=> {
              BlocProvider.of<HomeBloc>(context)
              .add(HomeRequestPermissionsEvent())
          },
          tooltip: 'Record new',
          child: const Icon(Icons.add),
        );
      });
  }

  BlocBuilder _buildSwitchRecognizeButton() {
    return BlocBuilder<HomeBloc, HomeState>(
      builder: (context, state) {
        if (state is HomeState) {
          return ElevatedButton(
            key: Key("buttonSwitchRecognize"),
            // shape: RoundedRectangleBorder(
            //   borderRadius: new BorderRadius.circular(4.0),
            // ),
            // color: CustomColor.logoBlue,
            onPressed: () {
              BlocProvider.of<HomeBloc>(context).add(
                  HomeRecognizeSwitchEvent(),
              );
            },
            child: Text(
              "Switch recognizer",
              style: ThemeUtils.mainTheme.textTheme.headline4,
            ),
          );
        }
        return Container();
      },
    );
  }

  BlocBuilder _buildRecognizeStateText() {
    return BlocBuilder<HomeBloc, HomeState>(
      builder: (context, state) {
        if (state is HomeState) {
          Logger.root.info("HomePage: _buildRecognizeStateText: recognizeState=${state.recognizeState}");
          return Container(
            key: Key("textRecognizeState"),
            child: Text(
              "Recognize state: " + getRecognizeStateName(state.recognizeState),
              style: ThemeUtils.mainTheme.textTheme.headline4,
            ),
          );
        }
        return Container();
      },
    );
  }

  String getRecognizeStateName(RecognizeState? state) {
    switch (state) {
      case RecognizeState.idle:
        return "Preparing...";
      case RecognizeState.ready:
        return "Ready";
      case RecognizeState.stared:
        return "Started";
      case RecognizeState.paused:
        return "Paused";
      case RecognizeState.stopped:
        return "Stopped";
      default:
        return "Preparing...";
    }
  }
}