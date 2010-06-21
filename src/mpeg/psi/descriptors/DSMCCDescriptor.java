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

import gui.MainPanel;
import sys.BitWise;

public class DSMCCDescriptor {

	int[] predefinedTags = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0xc2};

	String[] predefinedNames = { "type_descriptor", "name_descriptor", "info_descriptor", 
			"module_link_descriptor", "CRC32_descriptor", "location_descriptor", "est_download_time_descriptor", 
			"compression_type_descriptor"};

	String name = null;

	public int tag = 0xFF, parsedTag, tableIndx;

	int descriptor_length = 0;

	BitWise bw;

	// public Descriptor() {}

	public void setUp(int treeIndex, BitWise tableBw) {
		tableIndx = treeIndex;
		int startIndx = tableBw.getAbsolutePosition();
		// descriptor_tag 8 uimsbf
		parsedTag = tableBw.pop();
		// descriptor_length 8 uimsbf
		descriptor_length = tableBw.pop();
		tableBw.pop(descriptor_length);
		bw = tableBw.getCopy(startIndx, descriptor_length + 2);
		bw.pop(2);
	}

	public void print() {
		String descriptorName = "Unknown";
		for (int i = 0; i < predefinedTags.length; i++)
			if (predefinedTags[i] == parsedTag)
				descriptorName = predefinedNames[i];
		if (parsedTag > 0x79 && parsedTag < 0xC0)
			descriptorName = "Broadcaster defined";
		if (parsedTag > 0x07 && parsedTag < 0x80 ||
				parsedTag > 0xbf && parsedTag < 0xc2 || 
				parsedTag > 0xc2)
			descriptorName = "Reserved";

		int descIndx = addSubItem(descriptorName + " descriptor", tableIndx);
		addSubItem("descriptor semantic unknown", descIndx);
		addSubItem("tag = " + bw.toHex(parsedTag), descIndx);
		addSubItem("content: " + bw.getHexSequence(descriptor_length), descIndx);
	}

	public static int preparse(BitWise bw) {
		return bw.pop(0);
	}

	public int addSubItem(String msg, int parent) {
		int level = MainPanel.addTreeItem(msg, parent);
		if (parent == tableIndx)
			MainPanel.setTreeData(level, bw);
		return level;
	}

	public String toString() {
		return null;
	}
}
