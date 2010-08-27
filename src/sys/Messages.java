package sys;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "sys.messages"; //$NON-NLS-1$
	// TODO move to /res

	private static ResourceBundle RESOURCE_BUNDLE;

	public static void load() {
		RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, new Locale(Persistence.get(Persistence.UI_LANG_IDIOM),
				Persistence.get(Persistence.UI_LANG_REGION)));
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
