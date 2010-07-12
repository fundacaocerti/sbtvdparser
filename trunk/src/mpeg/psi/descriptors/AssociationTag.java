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

public class AssociationTag extends Descriptor {

	public static int tag = 0x14;

	static String name = "Association Tag Descriptor";

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		addSubItem("association_tag: " + BitWise.toHex(bw.pop16()), level);
		int use = bw.pop16();
		addSubItem("use: " + BitWise.toHex(use), level);
		if (use == 0) {
			addSubItem("selector_length: " + BitWise.toHex(bw.pop()), level);
			addSubItem("transaction_id: " + BitWise.toHex(bw.pop32()), level);
			addSubItem("timeout: " + BitWise.toHex(bw.pop32()), level);
		} else if (use == 1) {
			addSubItem("selector_length: " + BitWise.toHex(bw.pop()), level);
		} else {
			int selLen = bw.pop();
			addSubItem("selector_length: " + BitWise.toHex(selLen), level);
			addSubItem("selector: " + bw.getHexSequence(selLen), level);
			addSubItem("private_data: " + bw.getHexSequence(bw.getAvailableSize()), level);
		}
	}
}
