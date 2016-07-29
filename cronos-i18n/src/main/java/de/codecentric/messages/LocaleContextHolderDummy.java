package de.codecentric.messages;

import java.util.Locale;

/**
 * Just an dummy implementation. We are setting the Locale with a servlet filter based on session-attributes into Spring's LocaleContextHolder.
 */
public class LocaleContextHolderDummy {

    private static ThreadLocal<Locale> localeHolder = new ThreadLocal<Locale>();

    public static void setLocale(Locale locale) {
	localeHolder.set(locale);
    }

    public static Locale getLocale() {
	return localeHolder.get();
    }

}
