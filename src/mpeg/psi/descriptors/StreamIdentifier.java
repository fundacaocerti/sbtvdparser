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

public class StreamIdentifier extends Descriptor {

	public static int tag = 0x52, cTag;

	static String name = "Stream Identifier Descriptor";

	public static int[] tagValues = { // guia operacional item 12
	0x0, 0x10, 0x40, 0x30, 0x38, 0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86,
			0x87, 0x88 };

	public static String[] tagAssociations = { // guia operacional item 12
	"Primary video ES", "Primary audio ES", "Primary data ES",
			"Primary caption ES", "Main superimposed ES",
			"Primary data carroussel ES", "Primary video ES",
			"Secondary video ES", "Primary audio ES 24KHz",
			"Primary audio ES 48KHz", "Secondary audio ES 24KHz",
			"Secondary audio ES 48KHz", "Caption ES", "Superimposed", };

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		cTag = bw.pop();
		addSubItem("component_tag: " + BitWise.toHex(cTag) + " - " + getType(cTag),
				level);
	}

	public static String getType(int cTag) {
		String type = "reserved";
		if (cTag < 0xf)
			type = "Secondary video ES";
		if (cTag > 0x10 & cTag < 0x30)
			type = "Secondary audio ES";
		if (cTag > 0x30 & cTag < 0x38)
			type = "Secondary caption ES";
		for (int i = 0; i < tagValues.length; i++)
			if (cTag == tagValues[i])
				type = tagAssociations[i];
		if (cTag > 0x7f && cTag < 0x90)
			type = "1-seg" + type;
		if (cTag >= 0x70 && cTag < 0x7f)
			type = "OAD carroussel" + type;
		return type;
	}
}
