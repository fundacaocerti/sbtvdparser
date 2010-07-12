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

import gui.MainPanel;

import java.io.File;

import parsers.Parameters;

public class BatchAnalisys extends Thread implements Runnable {

	boolean accept(String name) {
		if (name.toLowerCase().endsWith(".ts"))
			return true;
		if (name.toLowerCase().endsWith(".trp"))
			return true;
		if (name.toLowerCase().endsWith(".mpg"))
			return true;
		if (name.toLowerCase().endsWith(".mpeg"))
			return true;
		if (name.toLowerCase().endsWith(".lnk"))
			return true;
		return false;
	}

	private String dirPath;
	private String[] cmdlParams;
	private boolean recursive;
	public static boolean stopThread = true;

	public BatchAnalisys(String dir, boolean recursive, String[] cmdlParams) {
		this.dirPath = dir;
		this.cmdlParams = cmdlParams;
		this.recursive = recursive;
		setName("batch");
	}

	public void run() {
		stopThread = false;
		if (dirPath == null)
			return;
		File dir = new File(dirPath);
		parseDir(dir);
	}

	private void parseDir(File dir) {
		if (dir.exists() && dir.canRead()) {
			MainPanel.setProgress(1);
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (stopThread)
					return;
				if (files[i].isDirectory() && recursive)
					parseDir(files[i]);
				else if (files[i].exists() && files[i].canRead() && accept(files[i].getName())) {
					Parameters.batchResults = true;
					cmdlParams[0] = files[i].getAbsolutePath();
					Parameters.startParser(cmdlParams);
					while (Parameters.isAlive())
						try {
							sleep(200);
						} catch (InterruptedException e) {
						}
				}
			}
		} else
			MainPanel.addTreeItem("Diretório " + dir.getAbsolutePath() + "não pode ser lido.", 0);
	}

}
