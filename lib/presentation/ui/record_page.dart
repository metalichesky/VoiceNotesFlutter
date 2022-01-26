import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_quill/flutter_quill.dart' hide Text;
import 'package:logging/logging.dart';
import 'package:voice_note/core/util/color.dart';
import 'package:voice_note/core/util/document.dart';
import 'package:voice_note/domain/entity/recognize_state.dart';
import 'package:voice_note/domain/entity/synthesize_state.dart';
import 'package:voice_note/presentation/presenter/record_bloc.dart';

import '../../dependencies.dart';

class RecordPage extends StatefulWidget {
  const RecordPage({Key? key}) : super(key: key);

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

  final String title = "Record Page";

  @override
  State<RecordPage> createState() => _RecordPageState();
}

class _RecordPageState extends State<RecordPage> {
  String recognizeState = "";

  @override
  void initState() {
    Logger.root.info("RecordPage: initState");
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

  BlocProvider<RecordBloc> _buildPage(BuildContext context) {
    return BlocProvider<RecordBloc>(
        create: (_) => getIt<RecordBloc>(),
        child: Scaffold(
            body: SafeArea(
                child: Stack(
                  children: const [
                    RecognizedEditText(),
                    HeaderTools(),
                    FooterTools(),
                  ],
                )),
            resizeToAvoidBottomInset: false));
  }

  BlocBuilder _buildCreateRecordButton() {
    return BlocBuilder<RecordBloc, RecordState>(builder: (context, state) {
      return FloatingActionButton(
        onPressed: () =>
        {
          BlocProvider.of<RecordBloc>(context)
              .add(RecordRequestPermissionsEvent())
        },
        tooltip: 'Record new',
        child: const Icon(Icons.add),
      );
    });
  }
}

class RecognizedEditText extends StatelessWidget {
  const RecognizedEditText({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<RecordBloc, RecordState>(builder: (context, state) {
      RecordBloc recordBloc = BlocProvider.of<RecordBloc>(context);
      Document newDocument = Document();
      String allText = state.recordRecognized.allText;
      Logger.root.info("RecognizedEditText: text=$allText");
      if (allText.isNotEmpty) {
        newDocument.insert(0, allText);
        String lastRecognizeText = state.recordRecognized.lastRecognizedText;
        if (lastRecognizeText.isNotEmpty) {
          newDocument.format(allText.length - lastRecognizeText.length,
              lastRecognizeText.length, Attribute.bold);
        }
      } else {
        newDocument.insert(0, " ");
      }
      QuillController controller = QuillController(
        document: newDocument,
        selection: TextSelection.collapsed(offset: newDocument.length),
      );
      controller.changes.listen((event) {
        if (event.item3 == ChangeSource.LOCAL) {
          var currentDocument = controller.document;
          var newText = currentDocument.toPlainTextCorrect();
          Logger.root.info("RecordPage textChanged text=$newText");
          recordBloc.state.recordRecognized.updateText(newText);
        }
      });
      var scrollController = ScrollController();
      var editor = QuillEditor(
        controller: controller,
        focusNode: FocusNode(),
        padding: const EdgeInsets.only(
            left: 8,
            right: 8,
            top: HeaderTools.roundRadius + 10,
            bottom: FooterTools.roundRadius + 10),
        autoFocus: false,
        expands: true,
        readOnly: false,
        scrollController: scrollController,
        scrollable: true,
      );
      // _focusNode.requestFocus();
      recordBloc.stream.listen((event) {
        if (scrollController.hasClients && scrollController.position.hasContentDimensions) {
          scrollController.animateTo(
            scrollController.position.maxScrollExtent,
            duration: const Duration(milliseconds: 300),
            curve: Curves.linear,
          );
        }
      });
      return Container(
          child: Padding(
            padding: const EdgeInsets.only(
                top: HeaderTools.height - HeaderTools.roundRadius,
                bottom: FooterTools.height - FooterTools.roundRadius),
            child: editor,
            // TextField(
            //   decoration: const InputDecoration(
            //     border: OutlineInputBorder(),
            //     hintText: 'Recognized text...',
            //   ),
            //   controller: textController,
            // ),
          ));
    });
  }
}

class HeaderTools extends StatelessWidget {
  const HeaderTools({Key? key}) : super(key: key);
  static const double height = 60;
  static const double roundRadius = 18;

  @override
  Widget build(BuildContext context) {
    return Container(
        alignment: Alignment.topCenter,
        child: SizedBox(
          width: double.infinity,
          height: height,
          // height: double.infinity,
          child: Container(
              decoration: BoxDecoration(
                  color: Theme
                      .of(context)
                      .primaryColor,
                  borderRadius: const BorderRadius.only(
                      bottomLeft: Radius.circular(roundRadius),
                      bottomRight: Radius.circular(roundRadius))),
              child: Row(
                children: [
                  Padding(
                      padding: const EdgeInsets.only(left: 10),
                      child: IconButton(
                          alignment: Alignment.centerLeft,
                          onPressed: () =>
                          {
                            if (Navigator.canPop(context))
                              {Navigator.pop(context)}
                          },
                          icon: Icon(
                            Icons.arrow_back,
                            size: 34,
                            color: Theme
                                .of(context)
                                .iconTheme
                                .color,
                          )))
                ],
              )),
        ));
  }
}

class FooterTools extends StatelessWidget {
  const FooterTools({Key? key}) : super(key: key);
  static const double height = 160;
  static const double roundRadius = 18;

  @override
  Widget build(BuildContext context) {
    return Container(
      alignment: Alignment.bottomCenter,
      child: SizedBox(
          width: double.infinity,
          height: height,
          // height: double.infinity,
          child: Container(
              decoration: BoxDecoration(
                  color: Theme
                      .of(context)
                      .primaryColor,
                  borderRadius: const BorderRadius.only(
                      topLeft: Radius.circular(roundRadius),
                      topRight: Radius.circular(roundRadius))),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: const [
                        ClearButton(),
                        SynthesizeButton(),
                        RecognizeButton(),
                        SaveButton()
                      ])
                ],
              ))),
    );
  }

  String getRecognizeStateName(RecognizeState? state) {
    switch (state) {
      case RecognizeState.idle:
        return "Preparing...";
      case RecognizeState.ready:
        return "Ready";
      case RecognizeState.started:
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

class RecognizeButton extends StatelessWidget {
  static const double size = 70;

  const RecognizeButton({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return SizedBox(
        width: size,
        height: size,
        // height: double.infinity,
        child: BlocBuilder<RecordBloc, RecordState>(builder: (context, state) {
          bool isButtonEnabled =
              state.recognizeState != RecognizeState.preparing;
          var iconData = _getRecognizeStateIcon(state.recognizeState);
          var iconColor = Theme
              .of(context)
              .iconTheme
              .color
              ?.withOpacity(isButtonEnabled ? 1 : 0.5);
          return Container(
              decoration: const BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: LinearGradient(
                      colors: [ColorUtils.vividViolet, ColorUtils.chardonnay],
                      transform: GradientRotation(3 * pi / 2))),
              child: MaterialButton(
                  key: const Key("buttonSwitchRecognize"),
                  shape: const CircleBorder(),
                  onPressed: (!isButtonEnabled)
                      ? null
                      : () =>
                  {
                    BlocProvider.of<RecordBloc>(context).add(
                      RecordRecognizeSwitchEvent(),
                    ),
                  },
                  child: Align(
                    alignment: Alignment.center,
                    child: Icon(
                      iconData,
                      color: iconColor,
                      size: size / 2,
                    ),
                  )));
        }));
  }

  IconData _getRecognizeStateIcon(RecognizeState? state) {
    switch (state) {
      case RecognizeState.idle:
        return Icons.settings_voice;
      case RecognizeState.ready:
        return Icons.mic;
      case RecognizeState.started:
        return Icons.mic_off;
      case RecognizeState.paused:
        return Icons.mic;
      case RecognizeState.stopped:
        return Icons.mic;
      default:
        return Icons.mic;
    }
  }
}

class SynthesizeButton extends StatelessWidget {
  static const double size = 70;

  const SynthesizeButton({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return SizedBox(
        width: size,
        height: size,
        // height: double.infinity,
        child: BlocBuilder<RecordBloc, RecordState>(builder: (context, state) {
          bool isButtonEnabled = state.synthesizeState != SynthesizeState.preparing;
          var iconData = _getSynthesizeStateIcon(state.synthesizeState);
          var iconColor = Theme
              .of(context)
              .iconTheme
              .color
              ?.withOpacity(isButtonEnabled ? 1 : 0.5);
          return Container(
              decoration: const BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: LinearGradient(
                      colors: [ColorUtils.vividViolet, ColorUtils.chardonnay],
                      transform: GradientRotation(3 * pi / 2))),
              child: MaterialButton(
                  key: const Key("buttonSwitchSynthesize"),
                  shape: const CircleBorder(),
                  onPressed: (!isButtonEnabled)
                      ? null
                      : () =>
                  {
                    BlocProvider.of<RecordBloc>(context).add(
                      RecordSynthesizeSwitchEvent(),
                    ),
                  },
                  child: Align(
                    alignment: Alignment.center,
                    child: Icon(
                      iconData,
                      color: iconColor,
                      size: size / 2,
                    ),
                  )));
        }));
  }

  IconData _getSynthesizeStateIcon(SynthesizeState? state) {
    switch (state) {
      case SynthesizeState.idle:
        return Icons.record_voice_over;
      case SynthesizeState.ready:
        return Icons.record_voice_over;
      case SynthesizeState.started:
        return Icons.voice_over_off;
      case SynthesizeState.paused:
        return Icons.record_voice_over;
      case SynthesizeState.stopped:
        return Icons.record_voice_over;
      default:
        return Icons.record_voice_over;
    }
  }
}

class ClearButton extends StatelessWidget {
  static const double size = 60;
  static const List<Color> disabledColors = [
    ColorUtils.vividViolet,
    ColorUtils.vividViolet
  ];
  static const List<Color> enabledColors = [
    ColorUtils.vividViolet,
    ColorUtils.mediumRedViolet
  ];

  const ClearButton({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return SizedBox(
        key: const Key("buttonClear"),
        width: size,
        height: size,
        // height: double.infinity,
        child: BlocBuilder<RecordBloc, RecordState>(builder: (context, state) {
          bool isButtonEnabled = !state.recordRecognized.isEmpty() &&
              (state.recognizeState == RecognizeState.paused ||
                  state.recognizeState == RecognizeState.ready);
          var iconData = Icons.clear;
          var iconColor = Theme
              .of(context)
              .iconTheme
              .color
              ?.withOpacity(isButtonEnabled ? 1.0 : 0.5);
          return Container(
              decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: LinearGradient(
                      colors: isButtonEnabled ? enabledColors : disabledColors,
                      transform: const GradientRotation(3 * pi / 2))),
              child: MaterialButton(
                  shape: const CircleBorder(),
                  onPressed: (!isButtonEnabled)
                      ? null
                      : () =>
                  {
                    BlocProvider.of<RecordBloc>(context).add(
                      RecordClearEvent(),
                    ),
                  },
                  child: Align(
                    alignment: Alignment.center,
                    child: Icon(
                      iconData,
                      color: iconColor,
                      size: size / 2,
                    ),
                  )));
        }));
  }
}

class SaveButton extends StatelessWidget {
  static const double size = 60;
  static const List<Color> disabledColors = [
    ColorUtils.vividViolet,
    ColorUtils.vividViolet
  ];
  static const List<Color> enabledColors = [
    ColorUtils.vividViolet,
    ColorUtils.shamrock
  ];

  const SaveButton({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return SizedBox(
        width: size,
        height: size,
        // height: double.infinity,
        child: BlocBuilder<RecordBloc, RecordState>(builder: (context, state) {
          bool isButtonEnabled = !state.recordRecognized.isEmpty() &&
              (state.recognizeState == RecognizeState.paused ||
                  state.recognizeState == RecognizeState.ready);
          var iconData = Icons.check;
          var iconColor = Theme
              .of(context)
              .iconTheme
              .color
              ?.withOpacity(isButtonEnabled ? 1.0 : 0.5);
          return Container(
              decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: LinearGradient(
                      colors: isButtonEnabled ? enabledColors : disabledColors,
                      transform: const GradientRotation(3 * pi / 2))),
              child: MaterialButton(
                  key: const Key("buttonSave"),
                  shape: const CircleBorder(),
                  onPressed: (!isButtonEnabled)
                      ? null
                      : () =>
                  {
                    BlocProvider.of<RecordBloc>(context).add(
                      RecordSaveEvent(),
                    ),
                  },
                  child: Align(
                    alignment: Alignment.center,
                    child: Icon(
                      iconData,
                      color: iconColor,
                      size: size / 2,
                    ),
                  )));
        }));
  }
}
