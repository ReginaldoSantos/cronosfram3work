package br.com.techne.cronos.i18n;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Classe responsável pela internacionalização das mensagens do sistema.
 *
 * Uso:
 *
 * Implementações de NLS Object devem utilizar o método <code> get(String, Class) </code> como abaixo:
 *
 * <pre>
 * {@code public static Messages get() {
 *  return (br.com.techne.myapp.i18n.Messages) MessageUtils.get(BUNDLE_NAME, br.com.techne.myapp.i18n.Messages.class);
 * }}
 * </pre>
 *
 * @author Techne
 * @version 1.0
 * @since 01/07/2016
 */
public class MessageUtil {

  /**
   * Classe responsável pela geração de logs.
   */
  private static Logger logger = Logger.getLogger(MessageUtil.class.getName());

  /**
   * Variável local à thread para garantir instância única do NLS Object.
   */
  public static ThreadLocal<Object> threadLocal = new ThreadLocal<>();

  /**
   * Nome utilizado para localizar o arquivo de "bundle"
   * de mensagens em <code>src/main/resources/i18n</code>.
   */
  public static final String BUNDLE_NAME = "i18n.messages";

  /**
   * Mapeia bundles à Objetos do tipo
   */
  private final static Map<ResourceBundle, Object> map = new HashMap<ResourceBundle, Object>();

  /**
   * Este método formata a mensagem passada como parâmetro substituindo
   * o padrão de mensagens pelo argumento recebido no segundo parâmetro.
   *
   * @param pattern
   *          Message a ser formatada.
   * @param arguments
   *          Objeto que será utilizado no replace da mensagem.
   *
   * @return Mensagem formatada e com os valores substituídos.
   */
  public static String format(String pattern, Object ... arguments) {

    /*
     * MessageFormat não aceita apóstofro simples diretamente.
     */

    String fixedPattern = pattern.replace("'", "''");
    return MessageFormat.format(fixedPattern, arguments);
  }

  /**
   * Método responsável por obter a classe atual em tempo de execução
   * a partir do bundle name default.
   *
   * @param clazz
   *          NLS Object class que define as constantes relativas aos ResourceBundles
   * @return NLS Object para internacionalização.
   */
  public static Object get(Class<?> clazz) {
    Object messages = threadLocal.get();

    if(messages == null) {
      messages = getUTF8Encoded(BUNDLE_NAME, clazz);
    }

    return messages;
  }

  /**
   * Método responsável por obter a classe atual em tempo de execução
   * a partir do bundle name
   *
   * @param bundleName
   *          nome utilizado para localizar o arquivo de "bundle" de mensagens
   * @param clazz
   *          NLS Object class que define as constantes relativas aos ResourceBundles
   * @return NLS Object para internacionalização.
   */
  public static Object get(String bundleName, Class<?> clazz) {
    Object messages = threadLocal.get();

    if(messages == null) {
      messages = getUTF8Encoded(bundleName, clazz);
    }

    return messages;
  }

  /**
   * Método responsável por obter a classe atual em tempo de execução
   * a partir do bundle name
   *
   * @param bundleName
   * @param clazz
   * @param locale
   * @return
   */
  public static Object get(String bundleName, Class<?> clazz, Locale locale) {
    Object messages = threadLocal.get();

    if(messages == null) {
      messages = getUTF8Encoded(bundleName, clazz, locale);
    }

    return messages;
  }

  /**
   * Retorna um objeto NLS para um determinado "bundle" identificado por <code>bundleName</code>.
   * Veja a descrição de classe para informações de uso.
   *
   * Os pacotes de recursos lidos por este método tem que ser codificados como UTF-8.
   *
   * Note que este "approach" não está de acordo com a especificação {link Propriedades java.util.Properties}
   * e destina-se a um uso mais conveniente.
   *
   * FIXME: Falta um mecanismo para obter o locale do usuário quando uso for web e não command-line.
   *
   * @return NLS Object para internacionalização.
   * @param bundleName
   *          O resource bundle a ser carregado.
   * @param clazz
   *          the class of the NLS object to load.
   */
  public static Object getUTF8Encoded(String bundleName, Class<?> clazz) {
    ClassLoader loader = clazz.getClassLoader();
    ResourceBundle bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault(), loader);
    return internalGet(bundle, clazz);
  }

  /**
   * Retorna um objeto NLS para um determinado "bundle" identificado por <code>bundleName</code>.
   * Veja a descrição de classe para informações de uso.
   *
   * Os pacotes de recursos lidos por este método tem que ser codificados como UTF-8.
   *
   * Note que este "approach" não está de acordo com a especificação {link Propriedades java.util.Properties}
   * e destina-se a um uso mais conveniente.
   *
   * @param bundleName
   * @param clazz
   * @param locale
   * @return
   */
  public static Object getUTF8Encoded(String bundleName, Class<?> clazz, Locale locale) {
    ClassLoader loader = clazz.getClassLoader();
    ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale, loader);
    return internalGet(bundle, clazz);
  }

  /**
   * Inicializa um NLS Object propriamente dito (<code>clazz</code>)
   * com o resource identificado por <code>bundle</code>.
   *
   * @param bundle
   *          fonte de mensagens a ser carregado
   * @param clazz
   *          classe com as constantes de mensagens
   * @return NLS Object carregado com mensagens locais
   */
  private static Object internalGet(ResourceBundle bundle, Class<?> clazz) {

    Object result;

    synchronized(map) {
      result = map.get(bundle);
      if(result == null) {

        try {
          result = clazz.newInstance();
        }
        catch(InstantiationException e) {
          e.printStackTrace();
        }
        catch(IllegalAccessException e) {
          e.printStackTrace();
        }

        Field[] fields = clazz.getDeclaredFields();
        for(int i = 0; i < fields.length; i++) {
          String fieldName = fields[i].getName();
          try {

            /*
             * Altera valor "public String" não estática e não final.
             */

            if(String.class.isAssignableFrom(fields[i].getType())
               && Modifier.isPublic(fields[i].getModifiers())
               && !Modifier.isStatic(fields[i].getModifiers())
               && !Modifier.isFinal(fields[i].getModifiers())) {

              try {
                String value = bundle.getString(fieldName);
                if(value != null) {
                  fields[i].setAccessible(true);
                  fields[i].set(result, value);
                }
              }
              catch(Throwable mre) {
                fields[i].setAccessible(true);
                fields[i].set(result, "");
                throw mre;
              }
            }
            else if(ResourceBundle.class.isAssignableFrom(fields[i].getType()) && Modifier.isPublic(fields[i].getModifiers())) {
              try {
                if(bundle != null) {
                  fields[i].setAccessible(true);
                  fields[i].set(result, bundle);
                }
              }
              catch(Throwable mre) {
                fields[i].setAccessible(true);
                fields[i].set(result, "");
                throw mre;
              }
            }
          }
          catch(Exception ex) {
            String qualifiedName = clazz.getName() + "#" + fieldName;
            logger.throwing(qualifiedName, "internalGet(ResourceBundle bundle, Class<?> clazz)", ex);
          }
        }
        map.put(bundle, result);
      }
    }

    return result;
  }
}
