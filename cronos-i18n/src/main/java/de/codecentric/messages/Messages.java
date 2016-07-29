package de.codecentric.messages;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import de.codecentric.example.messages.OurProjectMessages;

/**
 * Entry-Point for Messages. Provides access to ResourceBundles.<br>
 * <br>
 * Messages can be accessed by calling {@link Messages#get()}.nameOfMessage(). This class is using a Java Proxy class to resolve the messages.<br>
 * <br>
 * Enum-Translations can be accessed by calling {@link Messages#getEnumText(Displayable)}. The enum has to implement {@link Displayable} and there has to be an
 * entry in the message.properties with the following pattern: "Enum_" + Enum-Short-Classname + "_" + Enum-Value
 */
public final class Messages {

    protected static final String PREFIX_ENUM = "Enum_";
    private static final String BUNDLE_NAME = "messages";

    private static OurProjectMessages messages = (OurProjectMessages) Proxy.newProxyInstance(//
	    OurProjectMessages.class.getClassLoader(),//
	    new Class[] { OurProjectMessages.class }, //
	    new MessageResolver());

    private Messages() {
	// No instances
    }

    private static class MessageResolver implements InvocationHandler {
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
	    return Messages.getString(method.getName(), args);
	}
    }

    /**
     * @param enumValue
     *            Value of {@link Displayable}-Enumeration
     * @return Display-Text
     */
    public static String getEnumText(Displayable enumValue) {
	String key = PREFIX_ENUM + enumValue.getClass().getSimpleName() + "_" + enumValue.toString();
	return getString(key, null);
    }

    /**
     * @return Proxy of OurProjectMessages - Can be used to access all messages
     */
    public static OurProjectMessages get() {
	return messages;
    }

    private static String getString(String key, Object[] args) {
	Locale locale = LocaleContextHolderDummy.getLocale();
	try {
	    String message = ResourceBundle.getBundle(BUNDLE_NAME, locale).getString(key);
	    if (args != null) {
		MessageFormat formatter = new MessageFormat(message, locale);
		message = formatter.format(args);
	    }
	    return message;
	} catch (MissingResourceException e) {
	    return '!' + key + '!';
	}
    }
}
