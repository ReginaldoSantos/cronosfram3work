package br.com.techne.cronos.cli.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import br.com.techne.cronos.cli.core.annotation.CliCommand;
import br.com.techne.cronos.cli.core.annotation.CliParameter;
import br.com.techne.cronos.cli.i18n.CliMessage;
import br.com.techne.cronos.i18n.MessageUtil;

/**
 *
 * @author Techne
 * @version 1.0
 * @since 01/07/2016
 */
class CliCommandInfo {

  Object command;
  CliCommand annotation;
  Map<String, CliParameterInfo> map;

  CliCommandInfo(Object command, CliCommand _annotation) {
    this.command = command;
    this.annotation = _annotation;

    map = new HashMap<>();
    Class<?> klass = command.getClass();
    for(Field field : klass.getDeclaredFields()) {
      if(!field.isSynthetic()) {
        CliParameter cliParameter = field.getAnnotation(CliParameter.class);
        if(cliParameter != null) {
          String[] options = cliParameter.options();
          if(options.length == 0) {
            throw new RuntimeException(
              MessageUtil.format(CliMessage.get().CLI_PARAMETER_OPTIONS_EMPTY, field.getName())
            );
          }

          if(cliParameter.required() && cliParameter.hidden()) {
            throw new RuntimeException(
              MessageUtil.format(CliMessage.get().CLI_PARAMETER_REQUIRED_CANNOT_BE_HIDDEN, options[0], field)
            );
          }

          CliParameterInfo parameterInfo = new CliParameterInfo(field, cliParameter);
          for(String opt : options) {
            String key = opt.replaceFirst("^(-)+", "");
            if(map.containsKey(key)) {
              throw new RuntimeException(
                MessageUtil.format(CliMessage.get().CLI_PARAMETER_OPTIONS_CONFLICT, opt, map.get(key).field, field)
              );
            }
            map.put(key, parameterInfo);
          }
        }
      }
    }
  }

  String help(boolean showNotes) {
    StringBuilder sb = new StringBuilder();

    String[] descriptions = Util.getTextInfo(annotation.descriptions());

    String cmdDesc = Util.format(descriptions, false);
    sb.append(cmdDesc);

    List<String> list = new ArrayList<>(map.size());
    for(CliParameterInfo cliPI : new HashSet<>(map.values())) {
      if(!cliPI.annotation.hidden()) {
        list.add(cliPI.help());
      }
    }

    Collections.sort(list, Util.OPTIONS_COMPARATOR);
    for(String string : list) {
      sb.append("\n").append(string);
    }

    if(showNotes) {
      String[] notes = Util.getTextInfo(annotation.notes());
      sb.append(Util.format(notes, true));
    }

    return sb.toString();
  }
}
