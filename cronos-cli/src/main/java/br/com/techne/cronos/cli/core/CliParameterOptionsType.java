package br.com.techne.cronos.cli.core;

import br.com.techne.cronos.cli.core.annotation.CliCommand;

/**
 * Enumeration que facilita o 'parsing' na passagem de parâmetros para os {@link CliCommand}.
 *
 * @author Techne
 * @version 1.0
 * @since 04/07/2016
 */
enum CliParameterOptionsType {
  LONG("--"), SHORT("-"), REVERSE("+");

  public final String prefix;

  private CliParameterOptionsType(String prefix) {
    this.prefix = prefix;
  }

  public static CliParameterOptionsType get(String prefix) {
    String p = prefix.intern();
    return p == "-" ? SHORT : p == "+" ? REVERSE : LONG;
  }
}
