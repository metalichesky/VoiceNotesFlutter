import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:logging/logging.dart';
import 'package:voice_note/core/util/color.dart';
import 'package:voice_note/core/util/router.dart';
import 'package:voice_note/presentation/presenter/main_bloc.dart';
import 'package:voice_note/presentation/presenter/main_event.dart';
import 'package:voice_note/presentation/presenter/main_state.dart';
import 'package:voice_note/presentation/ui/record_list_item.dart';

import '../../dependencies.dart';

class MainPage extends StatefulWidget {
  const MainPage({Key? key}) : super(key: key);

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

  final String title = "Main Page";

  @override
  State<MainPage> createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  @override
  void initState() {
    Logger.root.info("MainPage: initState");
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider<MainBloc>(
        create: (_) => getIt<MainBloc>(),
        child: Scaffold(
          body: SafeArea(
              child: Stack(
            children: const [RecordsList()],
          )),
          resizeToAvoidBottomInset: false,
          floatingActionButton: const CreateRecordButton(),
        ));
  }
}

class CreateRecordButton extends StatelessWidget {
  static const double size = 60;

  const CreateRecordButton({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return SizedBox(
        width: size,
        height: size,
        // height: double.infinity,
        child: BlocBuilder<MainBloc, MainState>(builder: (context, state) {
          return Container(
              decoration: const BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: LinearGradient(
                      colors: [ColorUtils.vividViolet, ColorUtils.chardonnay],
                      transform: GradientRotation(3 * pi / 2))),
              child: MaterialButton(
                  key: const Key("buttonSwitchRecognize"),
                  shape: const CircleBorder(),
                  onPressed: () => {
                        BlocProvider.of<MainBloc>(context).add(
                          MainCreateRecordEvent(),
                        ),
                      },
                  child: Align(
                    alignment: Alignment.center,
                    child: Icon(
                      Icons.add,
                      color: Theme.of(context).iconTheme.color,
                      size: size / 2,
                    ),
                  )));
        }));
  }
}

class RecordsList extends StatelessWidget {
  const RecordsList({Key? key}) : super(key: key);

  static List<EditRecordMenuChoice> choices = [
    EditRecordMenuChoiceImpl("Edit", 0),
    EditRecordMenuChoiceImpl("Delete", 1)
  ];

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<MainBloc, MainState>(builder: (context, state) {
      var records = state.records;
      if (state is MainEditRecordState) {
        Future.delayed(const Duration(milliseconds: 300), () {
          Navigator.of(context).pushNamed(ROUTE_RECORD);
        });
      }
      return ListView.builder(
          // Let the ListView know how many items it needs to build.
          itemCount: records.length,
          // Provide a builder function. This is where the magic happens.
          // Convert each item into a widget based on the type of item it is.
          itemBuilder: (context, index) {
            final item =
                TextRecordListItem(records[index], choices, onTap: (item) {
              _onTap(context, item);
            }, onLongTap: (item) {
              _onLongTap(context, item);
            }, onEditMenuTap: (item, choice) {
              _onEditMenuTap(context, item, choice);
            });

            return item.buildWidget(context);
          });
    });
  }

  void _onTap(BuildContext context, TextRecordListItem item) {
    // BlocProvider.of<MainBloc>(context)
    //     .add(MainEditRecordEvent(record: item.record));
  }

  void _onLongTap(BuildContext context, TextRecordListItem item) {
    _onEdit(context, item);
  }

  void _onEditMenuTap(BuildContext context, TextRecordListItem item,
      EditRecordMenuChoice choice) {
    Logger.root.info("_onEditMenuTap: choice=${choice}");
    if (choice is EditRecordMenuChoiceImpl) {
      Logger.root.info("_onEditMenuTap: choice=${choice} idx=${choice.idx}");
      switch (choice.idx) {
        case 0:
          _onEdit(context, item);
          break;
        case 1:
          _onDelete(context, item);
          break;
      }
    }
  }

  void _onEdit(BuildContext context, TextRecordListItem item) {
    BlocProvider.of<MainBloc>(context)
        .add(MainEditRecordEvent(record: item.record));
  }

  void _onDelete(BuildContext context, TextRecordListItem item) {
    showDialog(
      context: context,
      builder: (BuildContext buildContext) {
        return AlertDialog(
          title: Text("Delete ${item.record.title}"),
          content: SingleChildScrollView(
            child: ListBody(
              children: const <Widget>[
                Text('Are you sure you want to delete record?')
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: const Text('Yes'),
              onPressed: () {
                BlocProvider.of<MainBloc>(context)
                    .add(MainDeleteRecordEvent(record: item.record));
                Navigator.of(context).pop();
              },
            ),
            TextButton(
              child: const Text('No'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
      // bool barrierDismissible = true,
      // Color? barrierColor = Colors.black54,
      // String? barrierLabel,
      // bool useSafeArea = true,
      // bool useRootNavigator = true,
      // RouteSettings? routeSettings, }
    );
  }
}

class EditRecordMenuChoiceImpl extends EditRecordMenuChoice {
  @override
  String text;
  int idx;

  EditRecordMenuChoiceImpl(this.text, this.idx);
}
