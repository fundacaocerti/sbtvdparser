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

import java.util.HashMap;

public class FileList {

	HashMap<Long, DSMCCObject> contentList = new HashMap<Long, DSMCCObject>();

	private Long svcGatewayObjKey = null;

	public DSMCCObject getRoot() {
		return getByObjKey(svcGatewayObjKey);
	}

	public void reset() {
		contentList.clear();
	}

	public void add(DSMCCObject file) {
		contentList.put(file.objKey, file);
	}

	public DSMCCObject getByObjKey(Long objKey) {
		return contentList.get(objKey);
	}

	public DSMCCObject setName(Long objKey, String name) {
		DSMCCObject f = getByObjKey(objKey);
		if (f == null) {
			f = new DSMCCObject(objKey);
			add(f);
		}
		f.setName(name);
		return f;
	}

	public void setContent(Long objKey, byte[] contents, int startOffset, int lenght) {
		DSMCCObject f = getByObjKey(objKey);
		if (f == null) {
			f = new DSMCCObject(objKey);
			add(f);
		}
		f.setContent(contents, startOffset, lenght);
	}

	public void addChildren(Long objKey, DSMCCObject file) {
		DSMCCObject f = getByObjKey(objKey);
		if (f == null) {
			f = new DSMCCObject(objKey);
			add(f);
		}
		f.addChildren(file);
	}

	public void setSvcGatewayObjKey(Long svcGatewayObjKey) {
		this.svcGatewayObjKey = svcGatewayObjKey;
	}
}
