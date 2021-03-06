/*
    SBTVD TS Parser - MPEG-2 Transport Stream analyser and debugging tool.
    Copyright (C) 2010 Gabriel A. G. Marques
    gabriel.marques@gmail.com
	
    This file is part of the "SBTVD Transport Stream Parser" program.

    The SBTVD Transport Stream Parser is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    The SBTVD Transport Stream Parser is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the SBTVD Stream Parser.  If not, see <http://www.gnu.org/licenses/>.
 
 */
package sys;

import gui.GuiMethods;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;

public class Log {

	static PrintStream log = null;

	static File logfile = null;

	static File tsfile = null;

	public static void setLogFile(File file) {
		logfile = file;
	}

	public static void setTsFile(File file) {
		tsfile = file;
	}

	public static void createLogFile() {
		if (logfile == null) {
			log = System.err;
			return;
		}
		if (!logfile.exists())
			try {
				logfile.createNewFile();
			} catch (IOException e1) {
				System.err.println(Messages.getString("Log.fCreateErr") + logfile.getAbsolutePath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				log = System.err;
				return;
			}

		try {
			log = new PrintStream(new FileOutputStream(logfile, true));
			log.println();
			log.println(Messages.getString("Log.start") + DateFormat.getDateTimeInstance().format(new Date())); //$NON-NLS-1$
			if (tsfile != null)
				log.println("Parsing [" + tsfile.getAbsolutePath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException e) {
			System.err.println(Messages.getString("Log.fWriteErr") + logfile.getAbsolutePath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			log = System.err;
			return;
		}
	}

	public static void printWarning(String msg) {
		System.out.println(msg);
		// if (log == null)
		// createLogFile();
		// log.println(msg);
		GuiMethods.runMethod(GuiMethods.ADDTOLOG, new Object[] { msg + "\n\r" }, true); //$NON-NLS-1$
	}

	// public static void printStackTrace(Exception e) {
	// if (log == null)
	// createLogFile();
	// log.println();
	// if (e.getLocalizedMessage() != null)
	// log.println(e.getLocalizedMessage());
	// log.println(e.getClass().getName());
	// StackTraceElement[] ste = e.getStackTrace();
	// for (int i = 0; i < ste.length; i++)
	// log.println(ste[i].toString());
	// }
	public static void printStackTrace(Exception e) {
		StringBuffer log = new StringBuffer();
		log.append("\n"); //$NON-NLS-1$
		if (e.getLocalizedMessage() != null)
			log.append(e.getLocalizedMessage() + "\n"); //$NON-NLS-1$
		log.append(e.getClass().getName() + "\n"); //$NON-NLS-1$
		StackTraceElement[] ste = e.getStackTrace();
		for (int i = 0; i < ste.length; i++)
			log.append(ste[i].toString() + "\n"); //$NON-NLS-1$
		printWarning(log.toString());
	}

}
