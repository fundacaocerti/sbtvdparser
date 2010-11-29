package sys;

import gui.MainPanel;
import parsers.Parameters;

public class SBTVDParser {

	public static void main(String[] cmdArgs) {
		Persistence.load();
		Messages.load();
		if (Persistence.get(Persistence.CHECK_UPDATES).equals("yes")) //$NON-NLS-1$
			new Update().start();

		try {
			MainPanel thisClass = null;
			if (!Parameters.noGui) {// TODO: how can the parameters be accessed
				// before the cmdArgs?
				thisClass = new MainPanel();
				thisClass.initialize();
			}
			// s.setPriority(3);
			CRC32.makeTable();
			Parameters.startParser(cmdArgs);

			if (!Parameters.noGui) {
				thisClass.handleEvents();
				thisClass.dispose();
			}
		} catch (RuntimeException e) {
			Log.printStackTrace(e);
		}
		Persistence.save();
		System.exit(0);
	}

}
