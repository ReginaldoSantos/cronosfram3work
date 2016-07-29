package br.com.techne.cronos.cli.command;

import br.com.techne.cronos.cli.core.CliParser;
import br.com.techne.cronos.cli.core.annotation.CliCommand;
import br.com.techne.cronos.cli.core.annotation.CliParameter;
import br.com.techne.cronos.cli.i18n.CliMessage;

/**
 *
 *
 */
@CliCommand(
  name         = "cronos",
  descriptions = { CliMessage.CRONOS_COMMAND_DESCRIPTIONS_KEY, CliMessage.CRONOS_COMMAND_USAGE_KEY },
  notes        = {CliMessage.CRONOS_COMMAND_NOTE_1_KEY, CliMessage.CRONOS_COMMAND_NOTE_2_KEY}
)
public class CronosCommand {

  /**
   * Define se exibe ou não a versão.
   */
  @CliParameter( options = { "--version" }, description = CliMessage.CRONOS_PARAMETER_VERSION_DESCRIPTIONS_KEY, required = true )
  boolean version;

  private static CliParser parser;

  public static void main(String[] args) {

    /*
     * FIXME: Aqui o ponto para fazer a leitura de commands > usar o component scan do spring?
     */

    parser = new CliParser(CronosCommand.class)
              .register(CronosImportCommand.class)
              .register(CronosExportCommand.class);

    parser.parse(args);
  }

  void run() {

    if(version) {
      System.out.println(getBanner());
      System.exit(0);
    }

  }

  private static String getBanner() {
    return "\nCronos CLI Version: \"" + getVersion() + " \n";
  }

  /**
   * Retorna a versão corrente do "cronos" CliCommand.
   *
   * FIXME: obter versão do artefato.
   *
   * @return
   */
  private static String getVersion() {
    return "0.0.1-SNAPSHOT";
  }

}
