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

public class DataContent extends Descriptor {

	public static int tag = 0xc7;

	static String name = "Data Contents Descriptor";

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		// data_component_descriptor(){
		// descriptor_tag 8 uimsbf
		// descriptor_length 8 uimsbf
		// data_component_id 16 uimsbf
		addSubItem("data_component_id: " + BitWise.toHex(bw.pop16()), level);
		int entryComp = bw.pop();
		addSubItem("entry_component: " + BitWise.toHex(entryComp) + " (" + StreamIdentifier.getType(entryComp) + ")",
				level);
		int loopLen = bw.pop();
		if (loopLen == 5 && entryComp >= 0x30 && entryComp <= 0x38)
			parseAribCCInfo(level);
		else
			addSubItem("selector: " + bw.getHexSequence(loopLen), level);
		// for(i=0;i<N;i++){
		// additional_data_component_info 8 uimsbf
		loopLen = bw.pop();
		int compRef = addSubItem("component_ref: " + BitWise.toHex(loopLen), level);
		loopLen += bw.getByteCount();
		while (bw.getByteCount() < loopLen)
			parseCompRef(compRef);
		addSubItem(Component.parseISO639(bw), level);
		addSubItem(Component.parseText(bw), level);
	}

	void parseCompRef(int level) {
		addSubItem("ref: " + BitWise.toHex(bw.pop()), level);
	}

	void parseAribCCInfo(int level) {
		int ccinflvl = addSubItem("AribCCInfo:", level);
		int nLang = bw.pop();
		int langLvl = addSubItem("languages:", ccinflvl);
		for (int i = 0; i < nLang; i++) {
			addSubItem("language_tag: " + bw.printBin(bw.consumeBits(3), 3), langLvl);
			bw.consumeBits(1);
			String[] dispType = { "Auto", "Non", "Select", "Specific" };
			addSubItem("dmf: " + dispType[bw.consumeBits(4) >> 2] + " display", langLvl);
			addSubItem(Component.parseISO639(bw), langLvl);
		}
	}
}
