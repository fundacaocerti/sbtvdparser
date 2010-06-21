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

import java.util.Vector;

public class FileList {
	
	static Vector contentList = new Vector();
	
	public static void add(DSMCCFile file) {
		contentList.add(file);
	}
	
	public static DSMCCFile getByObjKey(byte[] objKey) {
		for (int i = 0; i < contentList.size(); i++)
			if (((DSMCCFile)contentList.get(i)).isTheSame(objKey))
				return (DSMCCFile)contentList.get(i);
		return null;
	}
	
	public static DSMCCFile setName(byte[] objKey, String name) {
		DSMCCFile f = getByObjKey(objKey);
		if (f == null) {
			f = new DSMCCFile(objKey);
			add(f);
		}
		f.setName(name);
		return f;
	}
	
	public static void setContent(byte[] objKey, byte[] contents, int startOffset, int lenght) {
		DSMCCFile f = getByObjKey(objKey);
		if (f == null) {
			f = new DSMCCFile(objKey);
			add(f);
		}
		f.setContent(contents, startOffset, lenght);
	}

	public static void add(byte[] objKey, DSMCCFile file) {
		DSMCCFile f = getByObjKey(objKey);
		if (f == null) {
			f = new DSMCCDir(objKey);
			add(f);
		}
		if (f instanceof DSMCCDir)
			((DSMCCDir)f).add(file);
	}
}
