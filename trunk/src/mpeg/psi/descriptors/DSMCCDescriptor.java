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
import sys.Messages;
import dsmcc.ModuleList;

public class DSMCCDescriptor extends Descriptor {

	ModuleList ml;

	public DSMCCDescriptor() {
		predefinedTags = new int[] { 0x09, 0x71 };
		predefinedNames = new String[] { "compressed_module", "caching_priority" };
	}

	public void setUp(int treeIndex, BitWise tableBw, ModuleList ml) {
		this.ml = ml;
		super.setUp(treeIndex, tableBw);
	}

	public void print() {
		String descriptorName = Messages.getString("Descriptor.unknown"); //$NON-NLS-1$
		for (int i = 0; i < predefinedTags.length; i++)
			if (predefinedTags[i] == parsedTag)
				descriptorName = predefinedNames[i];
		descriptorName = checkRange(descriptorName);

		int descIndx = addSubItem(descriptorName + " descriptor", tableIndx);
		addSubItem("descriptor semantic unknown", descIndx);
		addSubItem("tag = " + BitWise.toHex(parsedTag), descIndx);
		if (descriptor_length > 0 && descriptor_length < 100)
			addSubItem("content: " + bw.getHexSequence(descriptor_length), descIndx);
		else
			addSubItem("lenght seems invalid: " + descriptor_length, descIndx);
	}

	protected String checkRange(String descriptorName) {
		// if (parsedTag > 0x79 && parsedTag < 0xC0)
		// descriptorName = "Broadcaster defined";
		// if (parsedTag > 0xE0 && parsedTag < 0xF7)
		// descriptorName = "Reserved";
		return descriptorName;
	}

}
