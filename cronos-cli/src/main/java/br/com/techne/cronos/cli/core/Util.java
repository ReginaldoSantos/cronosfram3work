package br.com.techne.cronos.cli.core;

import java.io.Console;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.regex.Pattern;

import br.com.techne.cronos.cli.i18n.CliMessage;

/**
 * Classe utilitária que concentra métodos de tratamento do input/output do terminal,
 * bem como possui métodos auxiliares utilizados durante o parsing da linha de comando.
 *
 * @author Techne
 * @version 1.0
 * @since 04/07/2016
 */
class Util {

  /**
   * Padding padrão.
   */
  private static final String  PADDING        = String.format("%36s", "");


  /**
   * Pattern para options: '-' ou '--'.
   */
  private static final Pattern OPTIONS_PREFIX = Pattern.compile("^-{1,2}");

  /**
   * Pattern (look-ahead) para split por caractéres ignorando '^'.
   */
  private static final Pattern CHAR_SPLITTER  = Pattern.compile("(?!^)");

  /**
   * Pattern (look-behind) para split de palavras.
   */
  private static final Pattern WORD_SPLITTER  = Pattern.compile("(?<!^)\\s+");


  static final Comparator<CliCommandInfo> CMD_COMPARATOR = new Comparator<CliCommandInfo>() {
    public int compare(CliCommandInfo cliInfo1, CliCommandInfo cliInfo2) {
      return cliInfo1.annotation.name().compareTo(cliInfo2.annotation.name());
    }
  };

  static final Comparator<String> OPTIONS_COMPARATOR = new Comparator<String>() {
    @Override
    public int compare(String s1, String s2) {
      return stripOptPrefix(s1).compareTo(stripOptPrefix(s2));
    }
  };

  private static String stripOptPrefix(String optStr) {
    return OPTIONS_PREFIX.matcher(optStr).replaceFirst("");
  }

  static String formatOpts(String[] opts) {
    String shortOpt = null, longOpt = null;
    for(String opt : opts) {
      if(opt.length() == 2 && shortOpt == null) {
        shortOpt = opt;
      }
      else if(opt.length() > 2 && longOpt == null) {
        longOpt = opt;
      }
    }

    String optStr;
    if(longOpt == null) {
      optStr = String.format("  %s", shortOpt);
    }
    else if(shortOpt == null) {
      optStr = String.format("      %s", longOpt);
    }
    else {
      optStr = String.format("  %s, %s", shortOpt, longOpt);
    }

    if(optStr.length() > 32) {
      return String.format("%s\n%32s", optStr, "");
    }
    else {
      return String.format("%-32s", optStr);
    }
  }

  static String format(String[] sentences, boolean enclosed) {

    if(sentences.length == 0) {
      return "";
    }

    String prefix = enclosed ? "\n" : "";
    String suffix = enclosed ? "" : "\n";

    StringBuilder sb = new StringBuilder(prefix);

    for(String sentence : sentences) {
      sb.append(prefix).append(format(sentence, false)).append(suffix);
    }

    return sb.toString();
  }

  static String format(String sentence, boolean indent) {

    int width = indent ? 44 : 80;
    String padding = indent ? PADDING : "";
    StringBuilder para = new StringBuilder();
    StringBuilder line = new StringBuilder();

    for(String word : wsplit(sentence)) {
      if(line.length() + word.length() <= width) {
        line.append(word).append(' ');
      }
      else {
        para.append(line.deleteCharAt(line.length() - 1)).append("\n").append(padding);
        line = new StringBuilder().append(word).append(' ');
      }
    }

    return para.append(line.deleteCharAt(line.length() - 1)).toString();
  }

  static String[] csplit(String word) {
    return CHAR_SPLITTER.split(word);
  }

  static String[] wsplit(String sentence) {
    return WORD_SPLITTER.split(sentence);
  }

  static char[] readSecret(String prompt) {
    Console console = System.console();

    if(console != null) {

      char[] password = null;

      while(password == null || password.length == 0) {
        password = console.readPassword("%s", prompt);
      }

      return password;
    }

    try (Scanner s = new Scanner(System.in)) {

      String line = null;

      while(line == null || line.length() == 0) {
        System.out.print(prompt);
        line = s.nextLine();
      }

      return line.toCharArray();
    }
  }

  /**
   * Para cada String em <code>infoStr</code> que possuir valor
   * igual a uma chave do resource bundle, a respectiva mensagem
   * localizada será carregada.
   *
   * Caso a String em questão não seja uma chave do resource bundle
   * seu próprio valor é utilizado.
   *
   * @param infoStr vetor com mensagens ou identificar de mensagens
   * do resource bundle.
   *
   * @return String[] com mensagens localizadas.
   */
  static String[] getTextInfo(String[] infoStr) {
    ArrayList<String> localizedDesc = new ArrayList<String>();

    String description;
    for(int i = 0; i < infoStr.length; i++) {
      description = CliMessage.getMessage(infoStr[i]);

      if (description != null && !"".equals(description)){
        localizedDesc.add(description);
      }
      else {
        localizedDesc.add(infoStr[i]);
      }
    }

    return localizedDesc.toArray(new String[0]);
  }
}
