package br.com.techne.cronos.cli.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.techne.cronos.cli.core.CliParser;

/**
 * Transforma qualquer classe em um CliCommand que poderá ser analisado
 * pelo {@link CliParser}.
 *
 * <p>
 * Os parâmetros descriptions e notes serão utilizados para montar o help
 * automaticamente.
 * </p
 *
 * @see CliCommand#descriptions()
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CliCommand {

  /**
   * Nome do comando
   */
  String name();

  /**
   * Os descriptions do CliCommand são utilizados para montar o help
   * automaticamente.
   *
   * <p>
   * Aceita um array de descrições onde os itens podem estar prefixados,
   * por exemplo, com {@code '\n'} permitindo quebra de linha.
   *
   * Veja os exemplos:
   * </p>
   *
   * <pre>
   * descriptions                         result
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   * {"descrição 1",                      descrição 1
   *  "\ndescrição 2"}
   *                                      descrição 2
   * -------------------------------------------------------------------
   * {"descrição 1",                      descrição 1
   *  " item A com identação 1 espaço"     item A com identação 1 espaço
   *  "\titem B com tabulação"                item B com tabulação
   *  "\nitem C com quebra de linha"}
   *                                      item C com quebra de linha
   * </pre>
   *
   */
  String[] descriptions() default {};

  /**
   * Os notes do CliCommand são exibidos no help, logo ao final da descrição
   * do comando automaticamente.
   *
   * <p>
   * As mesmas regras de formatação dos {@link #descriptions()} podem ser utilizadas.
   * </p>
   */
  String[] notes() default {};
}
