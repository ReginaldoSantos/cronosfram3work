package br.com.techne.cronos.shell.command;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import br.com.techne.cronos.entities.CroClasses;
import br.com.techne.cronos.main.merge.MergeClassesMain;

@Component
public class MergeCommands implements CommandMarker {

  @CliCommand(value = "merge", help = "Carrega versão mais atual do classes.xml no banco de dados.")
  public String merge(
    @CliOption(key = {"", "text"}) String text,
    @CliOption(key = "from", unspecifiedDefaultValue = "all") double doubleN,
    @CliOption(key = "to") long longN)
  {

    System.out.println(text);

    System.out.println(doubleN);

    System.out.println(longN);

    CroClasses croClasses = MergeClassesMain.merge();

    return croClasses.toString();
  }
}
