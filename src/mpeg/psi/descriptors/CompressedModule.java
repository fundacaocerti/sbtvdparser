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
package mpeg.psi.descriptors;

import sys.BitWise;

public class CompressedModule extends DSMCCDescriptor {

	public static int tag = 0x09;

	static String name = "CompressedModule";

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		addSubItem("descriptor_length: " + descriptor_length, level);
		int cmp = bw.pop();
		addSubItem("compression_method: " + BitWise.toHex(cmp) + ((cmp == 8) ? " ZLIB" : " unknown"), level);
		int origZize = bw.pop32();
		addSubItem("original_size: " + BitWise.toHex(origZize), level);
		if (cmp == 8) {
			ml.setCompression(origZize);
		}
	}
}
