package br.com.techne.cronos.cli.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca os campos de {@link CliCommand} como "input parameters"
 *
 * @author Techne
 * @version 1.0
 * @since 01/07/2016
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CliParameter {

  /**
   * Descreve os parâmetros dos {@CliCommand} como {@literal '-f'}, {@literal '--file'}.
   * Multiplos parâmetros são suportados, mas somente os dois primeiros são exibidos no help.
   */
  String[] options();

  /**
   * Descrição do parâmetro. Se maior que 80 caractéres uma quebra de linha com dois espaços
   * de identação serão inseridos.
   */
  String description();

  boolean required() default false;

  /**
   * Esconde a opção do help.
   */
  boolean hidden() default false;

  /**
   * Opção deve ser lida do terminal e não deve ser exibida no mesmo.
   */
  boolean secret() default false;

  /**
   * Prompt exibido para leitura de opção secreta do terminal.
   */
  String prompt() default "password: ";

}
