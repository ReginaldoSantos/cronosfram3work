package de.codecentric.example;

import java.util.Locale;

import de.codecentric.example.enums.UserType;
import de.codecentric.messages.LocaleContextHolderDummy;
import de.codecentric.messages.Messages;

/**
 * Sample-Calls
 */
public class ExampleApplication {

    public static void main(String[] args) {
	LocaleContextHolderDummy.setLocale(Locale.ENGLISH);
	System.out.println(Messages.get().welcomescreenCaption());
	System.out.println(Messages.get().welcomescreenHelloUser("Daniel Reuter", Messages.getEnumText(UserType.USER)));

	LocaleContextHolderDummy.setLocale(Locale.GERMAN);
	System.out.println(Messages.get().welcomescreenCaption());
	System.out.println(Messages.get().welcomescreenHelloUser("Daniel Reuter", Messages.getEnumText(UserType.USER)));
    }

}
