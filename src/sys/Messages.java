package sys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "sys.messages"; //$NON-NLS-1$
	// TODO move to /res

	private static ResourceBundle RESOURCE_BUNDLE;

	public static void load() {
		String lang = "en", region = "US";
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(new File(System.getProperty("user.dir"), "sbtvdp.properties")));
			lang = p.getProperty("UI.lang.idiom");
			region = p.getProperty("UI.lang.region");
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		Locale loc = new Locale(lang, region);
		RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, loc);
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
