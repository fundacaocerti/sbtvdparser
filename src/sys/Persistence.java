package sys;

import gui.MainPanel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Persistence {

	public static final String UI_LANG_REGION = "UI.lang.region";
	public static final String UI_LANG_IDIOM = "UI.lang.idiom";
	public static final String LAST_UPDATE_CHECK = "lastUpdate.date";
	public static final String LAST_UPDATE_VERSION = "lastUpdate.version";
	public static final String CHECK_UPDATES = "checkUpdates";
	public static final String LAST_READ_DIR = "lastOpenDir";
	public static final String LAST_SAVE_DIR = "lastSaveDir";
	public static final String CURRENT_SW_VERSION = "0.2";
	public static final String CURRENT_SW_DATE = "12ac3f278fb";

	static File f = new File(System.getProperty("user.dir"), "sbtvdp.properties");
	static Properties p = new Properties();

	public static void load() {
		try {
			if (f.exists())
				if (f.canRead())
					p.load(new FileInputStream(f));
				else
					reportFileErr("load");
			else if (f.createNewFile())
				if (f.canWrite())
					save();
				else
					reportFileErr("write");
			else
				reportFileErr("create");
		} catch (IOException e) {
			reportFileErr("access");
		}

		setDefaults();
	}

	private static void setDefaults() {
		if (p.getProperty(UI_LANG_IDIOM) == null || p.getProperty(UI_LANG_REGION) == null) {
			p.setProperty(UI_LANG_IDIOM, "en");
			p.setProperty(UI_LANG_REGION, "US");
		}
		setIfEmpty(LAST_READ_DIR, System.getProperty("user.dir"));
		setIfEmpty(LAST_SAVE_DIR, System.getProperty("user.dir"));
		setIfEmpty(CHECK_UPDATES, "yes");
		setIfEmpty(LAST_UPDATE_VERSION, CURRENT_SW_VERSION);
		setIfEmpty(LAST_UPDATE_CHECK, Long.toString(System.currentTimeMillis(), 16));
	}

	private static void setIfEmpty(String key, String val) {
		if (p.getProperty(key) == null) {
			p.setProperty(key, val);
		}
	}

	private static void reportFileErr(String operation) {
		MainPanel.cacheMessage("Could not " + operation + " the settings file!");
		try {
			MainPanel.cacheMessage("File: [" + f.getCanonicalPath() + "]");
			MainPanel.cacheMessage("Adjust the permissions to read and write the above path.");
		} catch (IOException e) {
			MainPanel.cacheMessage("I/O error operating the file [" + f.getPath() + "]");
		}
	}

	public static void save() {
		setDefaults();
		try {
			p.store(new FileOutputStream(f), "SBTVD Parser program settings");
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			reportFileErr("store");
		}
	}

	public static String get(String name) {
		return p.getProperty(name);
	}

	public static void set(String name, String value) {
		p.setProperty(name, value);
	}
}
