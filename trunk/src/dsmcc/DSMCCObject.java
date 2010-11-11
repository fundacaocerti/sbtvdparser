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

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import sys.Messages;

public class DSMCCObject {

	boolean isDirectory;

	Long objKey = null;

	public String name = null;

	byte[] contents = null;

	int startOffset, lenght;

	File file = null;

	Vector<DSMCCObject> childrens = new Vector<DSMCCObject>();

	public void addChildren(DSMCCObject file) {
		isDirectory = true;
		childrens.add(file);
	}

	public void mountTree(int msgLvl) {
		if (isDirectory) {
			int dirLvl = MainPanel.addTreeItem(name, msgLvl, MainPanel.DSMCC_TREE);
			MainPanel.setTreeData(dirLvl, this);
			for (int i = 0; i < childrens.size(); i++)
				(childrens.get(i)).mountTree(dirLvl);
		} else {
			int fileLvl = MainPanel.addTreeItem(
					"[" + name + Messages.getString("DSMCCObject.size") + lenght, msgLvl, MainPanel.DSMCC_TREE); //$NON-NLS-1$ //$NON-NLS-2$
			MainPanel.setTreeData(fileLvl, this);
		}
	}

	public void saveIn(File parentDir) {
		file = new File(parentDir, name);
		save(file);
	}

	public static String printObjKey(byte[] ok) {
		return printHex(ok, 0, ok.length);
	}

	public String toString() {
		return name;
	}

	public static String printHex(byte[] ok, int start, int end) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = start; i < end; i++) {
			if ((ok[i] & 0xff) < 0x10)
				sb.append('0');
			sb.append(Integer.toHexString(ok[i] & 0xff));
			sb.append(' ');
		}
		sb.append(']');
		return sb.toString();
	}

	public DSMCCObject(Long objKey) {
		this.objKey = objKey;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setContent(byte[] contents, int startOffset, int lenght) {
		this.contents = contents;
		this.startOffset = startOffset;
		this.lenght = lenght;
		isDirectory = false;
	}

	public boolean equals(Object o) {
		if (!(o instanceof DSMCCObject))
			return false;
		return isTheSame(((DSMCCObject) o).objKey);
	}

	public boolean isTheSame(Long objKey) {
		return objKey.longValue() == this.objKey.longValue();
	}

	public void save(File file) {
		this.file = file;
		if (file.exists())
			file.delete();
		if (isDirectory) {
			// try {
			file.mkdir();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			for (int i = 0; i < childrens.size(); i++)
				(childrens.get(i)).saveIn(file);
		} else
			try {
				file.createNewFile();
				if (contents != null) {
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(contents, startOffset, lenght);
					fos.flush();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public void open() {
		Desktop desk = Desktop.getDesktop();
		if (!java.awt.Desktop.isDesktopSupported())
			return;
		File tempDir = new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
		saveIn(tempDir);
		try {
			desk.open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// java.awt.Desktop
	}

	public boolean isDirectory() {
		return isDirectory;
	}
}
