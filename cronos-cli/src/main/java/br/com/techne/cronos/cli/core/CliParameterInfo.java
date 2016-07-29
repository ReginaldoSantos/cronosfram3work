package br.com.techne.cronos.cli.core;

import java.lang.reflect.Field;

import br.com.techne.cronos.cli.core.annotation.CliParameter;
import br.com.techne.cronos.cli.i18n.CliMessage;

/**
 * Classe utilitária que concentra métodos de tratamento do input/output do terminal,
 * bem como possui métodos auxiliares utilizados durante o parsing da linha de comando.
 *
 * @author Techne
 * @version 1.0
 * @since 04/07/2016
 */
class CliParameterInfo {

  Field field;
  CliParameter annotation;
  boolean set;

  CliParameterInfo(Field _field, CliParameter _annotation) {
    this.field = _field;
    this.annotation = _annotation;
    this.set = false;
  }

  String help() {

    String cliParameterText = Util.formatOpts(annotation.options());

    String cliParameterDescText;{
      String description = CliMessage.getMessage(annotation.description());

      if (description == null || "".equals(description)){
        description = annotation.description();
      }

      cliParameterDescText = Util.format(description, true);
    }

    return String.format("%s  %s", cliParameterText, cliParameterDescText);
  }
}
