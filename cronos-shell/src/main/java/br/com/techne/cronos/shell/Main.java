package br.com.techne.cronos.shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.shell.Bootstrap;

public class Main {

  /**
   * Delega chamada para Spring Shell's Bootstrap simplificando o debug
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    /*
     * Removendo comandos padrão do spring-shell
     */

    String[] argsArray; {
      ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));
      argsList.add("--disableInternalCommands");

      argsArray = new String[argsList.size()];
      argsArray = argsList.toArray(argsArray);
    }

    Bootstrap.main(args);
  }

}
