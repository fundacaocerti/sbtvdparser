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

public class DSMCCFile {
	
	byte[] objKey = null;
	
	public String name = null;
	
	byte[] contents = null;
	
	int startOffset, lenght;
	
	File file = null;
	
	public static String printObjKey(byte[] ok) {
		return printHex(ok, 0, ok.length);
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
	
	public DSMCCFile(byte[] objKey) {
		this.objKey = objKey;
//		printObjKey(objKey);
//		System.out.println();
	}
	
	public void setName(String name) {
		this.name = name;
//		printObjKey(objKey);
	}

	public void setContent(byte[] contents, int startOffset, int lenght) {
		this.contents = contents;
		this.startOffset = startOffset;
		this.lenght = lenght;
//		System.out.print("FS: set contents of ");
//		printObjKey(objKey);
//		System.out.print(" to [");
//		printHex(contents, startOffset, startOffset+4);
//		System.out.print("...");
//		printHex(contents, startOffset+lenght-4, startOffset+lenght);
//		System.out.println("]");
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof DSMCCFile))
			return false;
		return isTheSame(((DSMCCFile)o).objKey);
	}
	
	public boolean isTheSame(byte[] objKey) {
		if (objKey == null || this.objKey == null)
			return false;
		if (objKey.length != this.objKey.length)
			return false;
		for (int i = 0; i < objKey.length; i++)
			if (objKey[i] != this.objKey[i])
				return false;
		return true;
	}
	
	public void mountTree(int dirLvl) {
		int fileLvl = MainPanel.addTreeItem("["+name+"] size: "+lenght, dirLvl, MainPanel.DSMCC_TREE);	
		MainPanel.setTreeData(fileLvl, this);
	}
	
	public void saveIn(File parentDir) {
		file = new File(parentDir, name);
		save(file);
	}

	public void save(File file) {
		this.file = file;
		if (file.exists())
			file.delete();
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
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		saveIn(tempDir);
		try {
			desk.open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
//		java.awt.Desktop
	}
}
