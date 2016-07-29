package de.codecentric.messages;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import de.codecentric.example.enums.UserType;
import de.codecentric.example.messages.OurProjectMessages;

/**
 * This class tests all common problems regarding Messages. Missing translations, not used translations, ENUM-translations. Everything is checked. <br>
 * <br>
 * This is just a demo-implementation to transport the idea. The real implementation in our project is using a lot of libraries (Spring, Google-Reflections, of
 * course Logging,...) - Idea was to keep the demo project as small as possible
 */
public class MessagesTest {

    // Just as example - Better scan your classpath for message_*.properties
    private static String[] propertyFiles = new String[] { "messages_de.properties", "messages_en.properties" };
    // Just as example - Better scan your classpath for displayable Enums (with http://code.google.com/p/reflections/)
    private static Class<?>[] displayableEnums = new Class<?>[] { UserType.class };

    private static List<String> methodNames = new ArrayList<String>();
    private static List<String> enumValueKeys = new ArrayList<String>();
    private static Map<String, Properties> bundles = new HashMap<String, Properties>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @BeforeClass
    public static void prepare() throws Exception {
	// Get Method-Names
	Method[] methods = OurProjectMessages.class.getDeclaredMethods();
	for (Method method : methods) {
	    methodNames.add(method.getName());
	}

	// Get all Enum-Values
	for (Class displayableEnum : displayableEnums) {
	    // Hint: Use Spring's ReflectionUtils here
	    Method valuesMethod = displayableEnum.getMethod("values");
	    Object[] enumValues = (Object[]) valuesMethod.invoke(null);
	    for (Object enumValue : enumValues) {
		String messageKey = Messages.PREFIX_ENUM + displayableEnum.getSimpleName() + "_" + enumValue;
		enumValueKeys.add(messageKey);
	    }
	}

	// Get all messages
	for (String propertyFile : propertyFiles) {
	    Properties properties = new Properties();
	    URL url = ClassLoader.getSystemResource(propertyFile);
	    properties.load(url.openStream());
	    bundles.put(propertyFile, properties);
	}
    }

    /**
     * Is there an interface-method for every entry in our properties?
     * 
     * @throws IOException
     *             ignore
     */
    @Test
    public void shouldHaveMessagesForAllInterafaceMethods() throws IOException {
	Set<String> error = new HashSet<String>();

	for (String methodName : methodNames) {
	    for (String propertyFile : propertyFiles) {
		if (!bundles.get(propertyFile).containsKey(methodName)) {
		    error.add(propertyFile + "#" + methodName);
		}
	    }
	}

	if (!error.isEmpty()) {
	    fail("No translations for " + error);
	}

    }

    /**
     * Is there an entry in each message.properties for every method in the interface?
     * 
     * @throws IOException
     *             ignore
     */
    @Test
    public void shouldHaveInterfaceMethodForAllMessages() throws IOException {
	Set<String> error = new HashSet<String>();

	for (String propertyFile : propertyFiles) {
	    Properties bundle = bundles.get(propertyFile);

	    for (Object messageObj : bundle.keySet()) {
		String message = messageObj.toString();
		// Ignore ENUMs
		if (message.startsWith(Messages.PREFIX_ENUM)) {
		    continue;
		}

		if (!methodNames.contains(message)) {
		    error.add(propertyFile + "#" + message);
		}
	    }
	}

	if (!error.isEmpty()) {
	    fail("No interface method for : " + error);
	}
    }

    /**
     * Is there an entry in each message.properties for every Enum-Value?
     * 
     * @throws Exception
     *             ignore
     */
    @Test
    public void shouldHaveEnumMessageForEveryEnumValue() throws Exception {
	List<String> missingKeys = new ArrayList<String>();
	for (String enumValueKey : enumValueKeys) {
	    for (String propertyFile : propertyFiles) {
		if (!bundles.get(propertyFile).containsKey(enumValueKey)) {
		    missingKeys.add(propertyFile + "#" + enumValueKey);
		}
	    }
	}

	if (!missingKeys.isEmpty()) {
	    fail("No translation for " + missingKeys);
	}
    }

    /**
     * Is there an Enum-Value for every Enum-Translation in our message.properties?
     * 
     * @throws IOException
     *             ignore
     */
    @Test
    public void shouldHaveEnumValueForEveryEnumMessage() throws IOException {
	Set<String> error = new HashSet<String>();

	for (String propertyFile : propertyFiles) {
	    Properties bundle = bundles.get(propertyFile);

	    for (Object messageObj : bundle.keySet()) {
		String messageKey = messageObj.toString();
		if (messageKey.startsWith(Messages.PREFIX_ENUM) && !enumValueKeys.contains(messageKey)) {
		    error.add(propertyFile + "#" + messageKey);
		}
	    }
	}

	if (!error.isEmpty()) {
	    fail("No Enum value for " + error);
	}
    }

}
