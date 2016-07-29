package br.com.techne.cronos.cli.i18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import br.com.techne.cronos.i18n.MessageUtil;

/**
 * NLS Object para criação de constantes relativas aos ResourceBundle files.
 *
 * Neste caso, o <code>i18n.cli_messages</code> é usado como nome base, supondo
 * o "locale" definido para pt_BR, os recursos serão carregados dê:
 *
 * <p>
 * /i18n/cli_messages_pt_BR.properties
 * </p>
 *
 * Uso:
 *
 * Os campos "public String" são subtituídos durante a leitura do arquivo de bundle.
 *
 * Já os campos "public static final String" são utilizados para fazer o "get" direto no bundle.
 *
 * @author Techne
 * @version 1.0
 * @since 01/07/2016
 */
public final class CliMessage {

  /**
   * Classe responsável pela geração de logs.
   */
  private static Logger logger = Logger.getLogger(CliMessage.class.getName());

  /**
   * Nome utilizado para localizar o arquivo de "bundle"
   * de mensagens em <code>src/main/resources/i18n</code>.
   */
  private static final String BUNDLE_NAME = "i18n.cli_messages";

  /*
   * Lista de constantes para os message bundles do core do cronos-cli.
   */

  public String CLI_HELP;
  public String CLI_COMMAND;

  public String CLI_CLICOMMAND_ANNOTATION_MISSING;
  public String CLI_CLICOMMAND_ALREADY_REGISTERED;
  public String CLI_CLICOMMAND_INSTANTIATION_ERROR;
  public String CLI_CLICOMMAND_NO_COMMAND;

  public String CLI_PARAMETER_REQUIRED_MISSING;
  public String CLI_PARAMETER_REQUIRED_CANNOT_BE_HIDDEN;
  public String CLI_PARAMETER_OPTIONS_EMPTY;
  public String CLI_PARAMETER_OPTIONS_CONFLICT;
  public String CLI_PARAMETER_UNKNOWN;
  public String CLI_PARAMETER_ARGUMENT_MISSING;

  /*
   * Lista de constantes para os message bundles do cronos-cli annotations.
   */

  public static final String CRONOS_COMMAND_DESCRIPTIONS_KEY           = "CRONOS_COMMAND_DESCRIPTIONS_KEY";
  public static final String CRONOS_COMMAND_USAGE_KEY                  = "CRONOS_COMMAND_USAGE_KEY";
  public static final String CRONOS_COMMAND_NOTE_1_KEY                 = "CRONOS_COMMAND_NOTE_1_KEY";
  public static final String CRONOS_COMMAND_NOTE_2_KEY                 = "CRONOS_COMMAND_NOTE_2_KEY";
  public static final String CRONOS_PARAMETER_VERSION_DESCRIPTIONS_KEY = "CRONOS_PARAMETER_VERSION_DESCRIPTIONS_KEY";

  /**
   * Resource bundle carregado por reflections na classe {@link MessageUtil}.
   */
  public ResourceBundle bundle;

  /**
   * Método responsável por obter a classe atual em tempo de execução
   * a partir do bundle name.
   *
   * @return Classe Messages para internacionalização.
   */
  public static CliMessage get() {
    return (CliMessage)MessageUtil.get(BUNDLE_NAME, CliMessage.class);
  }

  /**
   * Obtém mensagem de texto 'localizada' representada pela chave <code>key</code>.
   *
   * @param key
   *          chave da mensagem do arquivo de bundle.
   * @return
   */
  public static String getMessage(String key) {

    String message = "";

    try{
       message = get().bundle != null && key != null && !"".equals(key) ? get().bundle.getString(key) : "";
    } catch (MissingResourceException e){

      /*
       * Quanto resource bundle tem problemas o inglês deve ser utilizado.
       */
      logger.finest(String.format("Resource bundle key <%s> not found.", key));
    }

    return message;
  }
}
