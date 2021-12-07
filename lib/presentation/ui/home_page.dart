import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:voice_notes/core/util/theme.dart';
import 'package:voice_notes/presentation/presenter/home_bloc.dart';

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

  final String title = "Flutter Demo Home Page";

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int _counter = 0;

  void _incrementCounter() {
    setState(() {
      // This call to setState tells the Flutter framework that something has
      // changed in this State, which causes it to rerun the build method below
      // so that the display can reflect the updated values. If we changed
      // _counter without calling setState(), then the build method would not be
      // called again, and so nothing would appear to happen.
      _counter++;
    });
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
                // Column is also a layout widget. It takes a list of children and
                // arranges them vertically. By default, it sizes itself to fit its
                // children horizontally, and tries to be as tall as its parent.
                //
                // Invoke "debug painting" (press "p" in the console, choose the
                // "Toggle Debug Paint" action from the Flutter Inspector in Android
                // Studio, or the "Toggle Debug Paint" command in Visual Studio Code)
                // to see the wireframe for each widget.
                //
                // Column has various properties to control how it sizes itself and
                // how it positions its children. Here we use mainAxisAlignment to
                // center the children vertically; the main axis here is the vertical
                // axis because Columns are vertical (the cross axis would be
                // horizontal).
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  const Text(
                    'You have pushed the button this many times:',
                  ),
                  Text(
                    '$_counter',
                    style: Theme
                        .of(context)
                        .textTheme
                        .headline4,
                  ),
                  _buildBatteryChargeText(),
                  _buildUpdateBatteryChargeButton(),
                ],
              ),
            ),
            floatingActionButton: FloatingActionButton(
              onPressed: _incrementCounter,
              tooltip: 'Increment',
              child: const Icon(Icons.add),
            )
          //This trailing comma makes auto-formatting nicer for build methods.
        )
    );
  }

  BlocBuilder _buildUpdateBatteryChargeButton() {
    return BlocBuilder<HomeBloc, HomeState>(
      builder: (context, state) {
        if (state is HomeState) {
          return ElevatedButton(
            key: Key("buttonUpdateBatteryCharge"),
            // shape: RoundedRectangleBorder(
            //   borderRadius: new BorderRadius.circular(4.0),
            // ),
            // color: CustomColor.logoBlue,
            onPressed: () {
              BlocProvider.of<HomeBloc>(context).add(
                HomeBatteryUpdateEvent(),
              );
            },
            child: Text(
              "Update battery charge",
              style: ThemeUtils.mainTheme.textTheme.headline4,
            ),
          );
        }
        return Container();
      },
    );
  }

  BlocBuilder _buildBatteryChargeText() {
    return BlocBuilder<HomeBloc, HomeState>(
      builder: (context, state) {
        if (state is HomeInitial || state is HomeBatteryChargeUpdated) {
          double? batteryCharge;
          if (state.props.isNotEmpty) {
            batteryCharge = state.props.first;
          } else {
            null;
          }
          String batteryChargeString;
          if (batteryCharge != null) {
            batteryChargeString = (batteryCharge * 100.0).toString() + " %";
          } else {
            batteryChargeString = "Unknown";
          }
          return Container(
            key: Key("textBatteryCharge"),
            child: Text(
              "Battery charge: " + batteryChargeString,
              style: ThemeUtils.mainTheme.textTheme.headline4,
            ),
          );
        }
        return Container();
      },
    );
  }
}