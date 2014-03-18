package sys;

import gui.MainPanel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Persistence {

	public static final String UI_LANG_REGION = "UI.lang.region"; //$NON-NLS-1$
	public static final String UI_LANG_IDIOM = "UI.lang.idiom"; //$NON-NLS-1$
	public static final String LAST_UPDATE_CHECK = "lastUpdate.date"; //$NON-NLS-1$
	public static final String LAST_UPDATE_VERSION = "lastUpdate.version"; //$NON-NLS-1$
	public static final String CHECK_UPDATES = "checkUpdates"; //$NON-NLS-1$
	public static final String LAST_READ_DIR = "lastOpenDir"; //$NON-NLS-1$
	public static final String LAST_SAVE_DIR = "lastSaveDir"; //$NON-NLS-1$
	public static final String CURRENT_SW_VERSION = "0.33"; //$NON-NLS-1$
	public static final String CURRENT_SW_DATE = "1424c321c10"; //$NON-NLS-1$ 2013-11-12

	static File f = new File(System.getProperty("user.dir"), "sbtvdp.properties"); //$NON-NLS-1$ //$NON-NLS-2$
	static Properties p = new Properties();

	public static void load() {
		try {
			if (f.exists()) if (f.canRead()) p.load(new FileInputStream(f));
			else reportFileErr(Messages.getString("Persistence.load")); //$NON-NLS-1$
			else
				if (f.createNewFile()) if (f.canWrite()) save();
				else reportFileErr(Messages.getString("Persistence.write")); //$NON-NLS-1$
				else reportFileErr(Messages.getString("Persistence.create")); //$NON-NLS-1$
		} catch (final IOException e) {
			System.out.println("Cannot read neither create [" + f.getAbsolutePath() + "] - check permissions."); //$NON-NLS-1$
			reportFileErr(Messages.getString("Persistence.access")); //$NON-NLS-1$
		}

		setDefaults();
	}

	private static void setDefaults() {
		if (p.getProperty(UI_LANG_IDIOM) == null || p.getProperty(UI_LANG_REGION) == null) {
			p.setProperty(UI_LANG_IDIOM, "en"); //$NON-NLS-1$
			p.setProperty(UI_LANG_REGION, "US"); //$NON-NLS-1$
		}
		setIfEmpty(LAST_READ_DIR, System.getProperty("user.dir")); //$NON-NLS-1$
		setIfEmpty(LAST_SAVE_DIR, System.getProperty("user.dir")); //$NON-NLS-1$
		setIfEmpty(CHECK_UPDATES, "yes"); //$NON-NLS-1$
		setIfEmpty(LAST_UPDATE_VERSION, CURRENT_SW_VERSION);
		setIfEmpty(LAST_UPDATE_CHECK, Long.toString(System.currentTimeMillis(), 16));
	}

	private static void setIfEmpty(final String key, final String val) {
		if (p.getProperty(key) == null) p.setProperty(key, val);
	}

	private static void reportFileErr(final String operation) {
		MainPanel
				.cacheMessage(Messages.getString("Persistence.errPrefix") + operation + Messages.getString("Persistence.errPosfix")); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			MainPanel.cacheMessage(Messages.getString("Persistence.file") + f.getCanonicalPath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			MainPanel.cacheMessage(Messages.getString("Persistence.errHelp")); //$NON-NLS-1$
		} catch (final IOException e) {
			MainPanel.cacheMessage(Messages.getString("Persistence.ioErr") + f.getPath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static void save() {
		setDefaults();
		try {
			p.store(new FileOutputStream(f), "SBTVD Parser program settings"); //$NON-NLS-1$
		} catch (final FileNotFoundException e) {
		} catch (final IOException e) {
			reportFileErr(Messages.getString("Persistence.store")); //$NON-NLS-1$
		}
	}

	public static String get(final String name) {
		return p.getProperty(name);
	}

	public static void set(final String name, final String value) {
		p.setProperty(name, value);
	}
}
