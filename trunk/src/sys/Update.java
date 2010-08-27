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
		if (Persistence.get(Persistence.UI_LANG_IDIOM).equals("pt"))
			info = getLatestVersionInfo("http://sbtvdparser.sourceforge.net/downloads_pt.htm");
		else
			info = getLatestVersionInfo("http://sbtvdparser.sourceforge.net/downloads.htm");
		if (info[0].compareToIgnoreCase(Persistence.get(Persistence.LAST_UPDATE_VERSION)) < 1)
			return;
		if (info[0].compareToIgnoreCase(Persistence.CURRENT_SW_VERSION) < 1) {
			Persistence.set(Persistence.LAST_UPDATE_VERSION, Persistence.CURRENT_SW_VERSION);
			return;
		}
		System.out.println("version: [" + info[0] + "]");
		System.out.println("url: [" + info[1] + "]");
		System.out.println("description: [" + info[2] + "]");
	}

	public String[] getLatestVersionInfo(String from) {
		try {
			String[] info = new String[3];
			URL url = new URL(from);
			URLConnection cnx = url.openConnection();
			cnx.setDoInput(true);
			// cnx.setUseCaches(false);
			BufferedReader br = new BufferedReader(new InputStreamReader(cnx.getInputStream()));
			// BufferedReader br = new BufferedReader(new InputStreamReader(new
			// FileInputStream(from), "utf-8"));
			String line = br.readLine();
			StringBuffer description = null;
			boolean descStart = false;
			while (line != null) {
				if (descStart) {
					line = line.replace("<br>", "\n");
					description.append(line.replaceAll("<.+?>", ""));
					if (line.indexOf("/p") > 0) {
						descStart = false;
						info[2] = description.toString().trim();
						return info;
					}
				}
				int href = line.indexOf("href");
				if (href > 0) {
					int urlStart = line.indexOf("\"", href) + 1;
					int urlEnd = line.indexOf("\"", urlStart);
					String dlUrl = line.substring(urlStart, urlEnd);
					if (dlUrl.endsWith("download")) {
						info[0] = line.substring(line.indexOf("(") + 2, line.indexOf(")"));
						info[1] = dlUrl;
						descStart = true;
						description = new StringBuffer();
					}
				}
				line = br.readLine();
			}
		} catch (MalformedURLException e) {
		} catch (IOException e) {
			System.out.println("Could not open download site.");
		}
		return null;
	}
}
