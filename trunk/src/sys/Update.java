package sys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Update extends Thread {

	public void run() {
		long lastCheck = Long.parseLong(Persistence.get(Persistence.LAST_UPDATE_CHECK), 16);
		if (System.currentTimeMillis() - lastCheck < 2e8) // near two days
			return;
		Persistence.set(Persistence.LAST_UPDATE_CHECK, Long.toString(System.currentTimeMillis(), 16));
		String[] info = null;
		if (Persistence.get(Persistence.UI_LANG_IDIOM).equals("pt")) //$NON-NLS-1$
			info = getLatestVersionInfo("http://sbtvdparser.sourceforge.net/downloads_pt.htm"); //$NON-NLS-1$
		else
			info = getLatestVersionInfo("http://sbtvdparser.sourceforge.net/downloads.htm"); //$NON-NLS-1$
		if (info[0].compareToIgnoreCase(Persistence.get(Persistence.LAST_UPDATE_VERSION)) < 1)
			return;
		if (info[0].compareToIgnoreCase(Persistence.CURRENT_SW_VERSION) < 1) {
			Persistence.set(Persistence.LAST_UPDATE_VERSION, Persistence.CURRENT_SW_VERSION);
			return;
		}
		gui.dialogs.Update.open(info[0], info[1], info[2], Long.parseLong(info[3], 16));
	}

	public String[] getLatestVersionInfo(String from) {
		try {
			String[] info = new String[4];
			URL url = new URL(from);
			URLConnection cnx = url.openConnection();
			cnx.setDoInput(true);
			cnx.setUseCaches(false);
			BufferedReader br = new BufferedReader(new InputStreamReader(cnx.getInputStream(), "utf-8"));
			// BufferedReader br = new BufferedReader(new InputStreamReader(new
			// FileInputStream(from), "utf-8"));
			String line = br.readLine();
			StringBuffer description = null;
			boolean descStart = false;
			while (line != null) {
				if (descStart) {
					line = line.replace("<br>", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					description.append(line.replaceAll("<.+?>", "")); //$NON-NLS-1$ //$NON-NLS-2$
					if (line.indexOf("/p") > 0) { //$NON-NLS-1$
						descStart = false;
						info[2] = description.toString().trim();
						return info;
					}
				}
				if (line.indexOf("releasedate") > 0) { //$NON-NLS-1$
					int hexbegin = line.indexOf('"', line.indexOf("value")) + 1; //$NON-NLS-1$
					info[3] = line.substring(hexbegin, hexbegin + 11);
				}

				int href = line.indexOf("href"); //$NON-NLS-1$
				if (href > 0) {
					int urlStart = line.indexOf("\"", href) + 1; //$NON-NLS-1$
					int urlEnd = line.indexOf("\"", urlStart); //$NON-NLS-1$
					String dlUrl = line.substring(urlStart, urlEnd);
					if (dlUrl.endsWith("download")) { //$NON-NLS-1$
						info[0] = line.substring(line.indexOf("(") + 2, line.indexOf(")")); //$NON-NLS-1$ //$NON-NLS-2$
						info[1] = dlUrl;
						descStart = true;
						description = new StringBuffer();
					}
				}
				line = br.readLine();
			}
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		return null;
	}
}
