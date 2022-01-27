import 'package:flutter/material.dart';
import 'package:voice_note/core/util/theme.dart';
import 'package:voice_note/domain/entity/record.dart';

abstract class RecordListItem {
  Widget buildWidget(BuildContext context);
}

class TextRecordListItem implements RecordListItem {
  final TextRecord record;
  final List<EditRecordMenuChoice> choices;
  final FunctionOnTap? onTap;
  final FunctionOnLongTap? onLongTap;
  final FunctionOnEditTap? onEditTap;
  final FunctionOnEditMenuTap? onEditMenuTap;

  TextRecordListItem(this.record, this.choices,
      {this.onTap, this.onLongTap, this.onEditTap, this.onEditMenuTap});

  @override
  Widget buildWidget(BuildContext context) {
    return ListTile(
      title: Text(
        "${record.title} ${record.id}",
        style: Theme.of(context).textTheme.headline5,
      ),
      subtitle: Text(
        record.lastChangeDate,
        style: Theme.of(context).textTheme.bodyText1,
      ),
      leading: const Icon(Icons.text_fields),
      trailing: EditRecordMenu(this, choices, onEditMenuTap),
      // IconButton(
      //   icon: const Icon(Icons.edit),
      //   onPressed: () {
      //     onEditTap?.call(this);
      //   },
      //   splashColor: Colors.cyan,
      // ),
      onTap: () {
        onTap?.call(this);
      },
      onLongPress: () {
        onLongTap?.call(this);
      },
    );
  }
}

class EditRecordMenu extends StatelessWidget {
  List<EditRecordMenuChoice> choices;
  TextRecordListItem item;
  FunctionOnEditMenuTap? onEditMenuTap;

  EditRecordMenu(this.item, this.choices, this.onEditMenuTap, {Key? key})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    var popupMenu = PopupMenuButton(
      onSelected: (selection) {
        // var choice = choices.firstWhere((element) => element.text == selection,
        //     orElse: () => choices.first);
        // _onSelected(context, choice);
      },
      padding: EdgeInsets.zero,
      // initialValue: choices[_selection],
      itemBuilder: (BuildContext context) {
        return choices.map((EditRecordMenuChoice choice) {
          var popupMenuItem = PopupMenuItem<String>(
            value: choice.text,
            child: Text(
              choice.text,
              style: ThemeUtils.mainTheme.textTheme.bodyText1,
            ),
            onTap: () {
              _onSelected(context, choice);
            },
          );
          return popupMenuItem;
        }).toList();
      },
    );
    return popupMenu;
  }

  void _onSelected(BuildContext context, EditRecordMenuChoice choice) {
    Future.delayed(
        const Duration(milliseconds: 10),
        () {
          onEditMenuTap?.call(item, choice);
        }
    );
  }
}

abstract class EditRecordMenuChoice {
  abstract String text;
}

typedef FunctionOnTap = Function(TextRecordListItem item);
typedef FunctionOnLongTap = Function(TextRecordListItem item);
typedef FunctionOnEditTap = Function(TextRecordListItem item);
typedef FunctionOnEditMenuTap = Function(
    TextRecordListItem item, EditRecordMenuChoice choice);
