package br.com.techne.cronos.cli.core;

import static br.com.techne.cronos.cli.core.CliParameterOptionsType.LONG;
import static br.com.techne.cronos.cli.core.CliParameterOptionsType.REVERSE;
import static br.com.techne.cronos.cli.core.CliParameterOptionsType.SHORT;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import br.com.techne.cronos.cli.core.annotation.CliCommand;
import br.com.techne.cronos.cli.core.annotation.CliParameter;
import br.com.techne.cronos.cli.i18n.CliMessage;
import br.com.techne.cronos.i18n.MessageUtil;

/**
 * Rop - A lightweight command line option parser. It also supports level-two
 * sub-commands, as with {@code git add}.
 *
 * @author ryenus
 */
public class CliParser {

  private final Map<Class<?>, Object> _cliInstances;
  private final Map<String, CliCommandInfo> _cliCommandInfoMap;
  private CliCommandInfo _mainCli;
  private CliCommandInfo _cli;

  /**
   * Construct an OptionParse instance. It also accepts one or a group of,
   * command classes or the corresponding instances to be registered with.
   *
   * @see #register(Object)
   */
  public CliParser(Object ... commands) {
    this._cliInstances = new HashMap<>();
    this._cliCommandInfoMap = new HashMap<>();

    for(Object command : commands) {
      if(command instanceof Collection<?>) {
        for(Object object : (Collection<?>)command) {
          register(object);
        }
      }
      else {
        register(command);
      }
    }
  }

  /**
   * Register a command class or its instance. For a class, an instance will
   * be created internally and available via {@link #get(Class)}.
   *
   * <p>
   * The command registered first is treated as the top command, subsequently
   * registered commands are all taken as level-two sub-commands, however,
   * level-three sub-commands are not supported by design.
   * </p>
   *
   * @param command
   *          a command class (or its instance) to be registered, the class
   *          must be annotated with {@link CliCommand}
   *
   * @return the {@link CliParser} instance to support chained invocations
   */
  public CliParser register(Object command) {
    Class<?> klass;
    Object instance;
    if(command instanceof Class) {
      klass = (Class<?>)command;
      instance = instantiate(klass);
    }
    else {
      instance = command;
      klass = instance.getClass();
    }

    register(klass, instance);
    return this;
  }

  private void register(Class<?> klass, Object instance) {
    CliCommand cmdAnno = klass.getAnnotation(CliCommand.class);
    if(cmdAnno == null) {
      throw new RuntimeException(MessageUtil.format(CliMessage.get().CLI_CLICOMMAND_ANNOTATION_MISSING, klass.getName()));
    }

    String cmdName = cmdAnno.name();
    CliCommandInfo existingCmd = _cliCommandInfoMap.get(cmdName);
    if(existingCmd != null) {
      throw new RuntimeException(
        MessageUtil.format(CliMessage.get().CLI_CLICOMMAND_ALREADY_REGISTERED,
        cmdName, klass, existingCmd.command.getClass())
      );
    }

    _cliInstances.put(klass, instance);
    CliCommandInfo cliCommandInfo = new CliCommandInfo(instance, cmdAnno);
    if(_mainCli == null) {
      _mainCli = cliCommandInfo;
    }

    _cliCommandInfoMap.put(cmdName, cliCommandInfo);
  }

  private static Object instantiate(Class<?> klass) {
    try {
      Constructor<?> constr = klass.getDeclaredConstructor();
      constr.setAccessible(true);
      return constr.newInstance();
    }
    catch(InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException exp) {
      throw new RuntimeException(
        MessageUtil.format(CliMessage.get().CLI_CLICOMMAND_INSTANTIATION_ERROR, klass), exp
      );
    }
  }

  /**
   * Parse the command line args, but accept only the first sub-command, all
   * other sub-command from the command line are treated as normal arguments.
   *
   * @param args
   *          this should be the command line args passed to {@code main}
   * @return a map consists of the recognized command and their params
   *
   * @see #parse(String[], boolean)
   */
  public Map<Object, String[]> parse(String[] args) {
    return parse(args, false);
  }

  /**
   * After registering all {@link CliCommand} classes/objects, invoke this method
   * to parse the command line args and populate the {@link CliParameter} fields of
   * the registered command objects.
   *
   * <p>
   * If the built-in option {@literal "--help"} is found, the parser will
   * generate and display the help information, then call
   * {@code System.exit(0)}.
   * </p>
   *
   * <p>
   * All the parsed commands would be collected in a {@link Map}, with each
   * one's class as key, and it's parameters array as value.
   * </p>
   *
   * <p>
   * After parsing, for each parsed command if it has the method
   * {@code run(OptionParser, String[])} defined, the method will be invoked
   * automatically, with the OptionParser object and its parameters passed in.
   * </p>
   *
   * @param args
   *          this should be the command line args passed to {@code main}
   * @param multi
   *          whether to support multiple sub-commands, like with
   *          {@literal `mvn clean test`}
   * @return a map consists of the recognized command and their params
   */
  public Map<Object, String[]> parse(String[] args, boolean multi) {
    if(_mainCli == null) {
      throw new RuntimeException(CliMessage.get().CLI_CLICOMMAND_NO_COMMAND);
    }

    Map<Object, String[]> cpm = new LinkedHashMap<>();
    List<String> params = new ArrayList<>();
    _cli = _mainCli;

    ListIterator<String> lit = Arrays.asList(args).listIterator();
    while(lit.hasNext()) {
      String arg = lit.next();

      if(arg.equals("--help")) {
        showHelp();
        System.exit(0);
      }

      CliCommandInfo ci = _cliCommandInfoMap.get(arg);
      if(ci != null) {
        if(ci == _cli || cpm.containsKey(ci.command) || (!multi && cpm.size() > 0)) {
          params.add(arg);
        }
        else {
          stage(cpm, _cli, params);
          params.clear();
          _cli = ci;
        }

        continue;
      }

      if(arg.equals("--")) { // treat everything else as parameters
        while(lit.hasNext()) {
          params.add(lit.next());
        }
      }
      else if(arg.startsWith(LONG.prefix)) {
        String opt = arg.substring(2);
        parseParametersOption(opt, lit, LONG);
      }
      else if(arg.startsWith(SHORT.prefix) || arg.startsWith(REVERSE.prefix)) {
        CliParameterOptionsType type = CliParameterOptionsType.get(arg.substring(0, 1));
        String opt = arg.substring(1);
        if(_cli.map.containsKey(opt)) {
          parseParametersOption(opt, lit, type);
        }
        else {
          String[] opts = Util.csplit(opt);
          parseParametersOptions(opts, lit, type);
        }
      }
      else {
        params.add(arg.startsWith("\\") ? arg.substring(1) : arg);
      }
    }

    stage(cpm, _cli, params);

    /*
     * Invoca método run do CliCommand
     */

    invokeRun(cpm);
    return cpm;
  }

  private static void stage(Map<Object, String[]> cpm, CliCommandInfo cliCommandInfo, List<String> params) {
    cpm.put(cliCommandInfo.command, params.toArray(new String[params.size()]));
    for(CliParameterInfo cliPI : new HashSet<>(cliCommandInfo.map.values())) {
      if(cliPI.annotation.required() && !cliPI.set) {
        System.err.println(MessageUtil.format(CliMessage.get().CLI_PARAMETER_REQUIRED_MISSING, cliPI.field.getName()));
        System.exit(-1);
      }
    }
  }

  private void parseParametersOptions(String[] opts, ListIterator<String> liter, CliParameterOptionsType parameterType) {
    for(String option : opts) {
      parseParametersOption(option, liter, parameterType);
    }
  }

  private void parseParametersOption(String pOption, ListIterator<String> liter, CliParameterOptionsType pOptionType) {
    CliParameterInfo parameterInfo = _cli.map.get(pOption);

    if(parameterInfo == null) {
      System.err.println(MessageUtil.format(CliMessage.get().CLI_PARAMETER_UNKNOWN, pOption));
      System.exit(-1);
    }

    Field field = parameterInfo.field;
    field.setAccessible(true);
    Class<?> fieldType = field.getType();

    Object value = null;
    if(parameterInfo.annotation.secret()) {
      value = Util.readSecret(parameterInfo.annotation.prompt());
    }
    else if(fieldType == boolean.class || fieldType == Boolean.class) {
      value = (pOptionType != REVERSE);
    }
    else {

      if(!liter.hasNext()) {
        System.err.println(MessageUtil.format(CliMessage.get().CLI_PARAMETER_ARGUMENT_MISSING, pOptionType.prefix, pOption));
        System.exit(-1);
      }

      value = parseValue(fieldType, liter.next());
    }

    try {
      field.set(_cli.command, value);
      parameterInfo.set = true;
    }
    catch(IllegalArgumentException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object parseValue(Class<?> type, String value) {
    if(type == String.class) {
      return value;
    }
    else if(type == int.class || type == Integer.class) {
      return Integer.decode(value);
    }
    else if(type == long.class || type == Long.class) {
      return Long.decode(value);
    }
    else if(type == byte.class || type == Byte.class) {
      return Byte.decode(value);
    }
    else if(type == short.class || type == Short.class) {
      return Short.decode(value);
    }
    else if(type == double.class || type == Double.class) {
      return Double.parseDouble(value);
    }
    else if(type == float.class || type == Float.class) {
      return Float.parseFloat(value);
    }
    else if(type == char.class || type == Character.class) {
      return value.charAt(0);
    }
    else if(type == File.class) {
      return new File(value);
    }
    else if(type == Path.class) {
      return Paths.get(value);
    }
    return value;
  }

  private void invokeRun(Map<Object, String[]> cpm) {
    for(Object cmd : cpm.keySet()) {
      invokeRun(cmd, cpm.get(cmd));
    }
  }

  private Object invokeRun(Object cmd, String[] params) {
    try {
      try {
        Method run = cmd.getClass().getDeclaredMethod("run", CliParser.class, String[].class);
        run.setAccessible(true);
        return run.invoke(cmd, this, params);
      }
      catch(NoSuchMethodException e) {
        try {
          Method run = cmd.getClass().getDeclaredMethod("run", String[].class, CliParser.class);
          run.setAccessible(true);
          return run.invoke(cmd, params, this);
        }
        catch(NoSuchMethodException e1) {
          try {
            Method run = cmd.getClass().getDeclaredMethod("run", String[].class);
            run.setAccessible(true);
            return run.invoke(cmd, (Object)params); // bypass the var-args magic
          }
          catch(NoSuchMethodException e2) {
            try {
              Method run = cmd.getClass().getDeclaredMethod("run", CliParser.class);
              run.setAccessible(true);
              return run.invoke(cmd, this);
            }
            catch(NoSuchMethodException e3) {
              try {
                Method run = cmd.getClass().getDeclaredMethod("run");
                run.setAccessible(true);
                return run.invoke(cmd);
              }
              catch(NoSuchMethodException e4) {
                return null;
              }
            }
          }
        }
      }
    }
    catch(SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Display the help information, which is constructed from all the registered
   * Commands and their Options.
   */
  public void showHelp() {
    StringBuilder sb = new StringBuilder();
    sb.append(_mainCli.help(false));
    sb.append(String.format("\n      --help %20s %s", "", CliMessage.get().CLI_HELP));

    List<CliCommandInfo> cmds = new ArrayList<>(_cliCommandInfoMap.values());
    cmds.remove(_mainCli);
    Collections.sort(cmds, Util.CMD_COMPARATOR);
    for(CliCommandInfo cliCI : cmds) {
      sb.append(String.format("\n\n[%s '%s']\n\n", CliMessage.get().CLI_COMMAND, cliCI.annotation.name()));
      sb.append(cliCI.help(true));
    }

    String[] notes = Util.getTextInfo(_mainCli.annotation.notes());
    sb.append(Util.format(notes, true)).append('\n');
    System.out.print(sb.toString());
  }

  /**
   * Get the instance of the provided CliCommand class if it's registered.
   *
   * @param klass
   *          a registered CliCommand class
   * @return the instance of the given CliCommand class, null if CliCommand not
   *         registered
   */
  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> klass) {
    return (T)_cliInstances.get(klass);
  }

}
