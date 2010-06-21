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
package dsmcc;

import gui.MainPanel;

import java.io.File;
import java.util.Vector;

public class DSMCCDir extends DSMCCFile {
	
	Vector contentList = new Vector();

	public DSMCCDir(byte[] objKey) {
		super(objKey);
	}

	public void add(DSMCCFile file) {
		contentList.add(file);
//		for (int i = 0; i < objKey.length; i++) 
//			System.out.print(Integer.toHexString(objKey[i]));
	}
	
	public void mountTree(int msgLvl) {
		int dirLvl = MainPanel.addTreeItem(name, msgLvl, MainPanel.DSMCC_TREE);
		MainPanel.setTreeData(dirLvl, this);
		for (int i = 0; i < contentList.size(); i++)
			((DSMCCFile)contentList.get(i)).mountTree(dirLvl);
	}
	
	public void saveIn(File parentDir) {
		file = new File(parentDir, name);
		save(file);
	}
	
	public void save(File file) {
		this.file = file;
		if (file.exists())
			file.delete();
//		try {
			file.mkdir();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		for (int i = 0; i < contentList.size(); i++)
			((DSMCCFile)contentList.get(i)).saveIn(file);
	}
}
