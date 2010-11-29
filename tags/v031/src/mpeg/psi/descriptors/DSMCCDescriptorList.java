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
import dsmcc.ModuleList;

public class DSMCCDescriptorList extends DescriptorList {

	private static DSMCCDescriptorList thisClass;

	public static DSMCCDescriptorList getInstance() {
		if (thisClass == null) {
			thisClass = new DSMCCDescriptorList();
			thisClass.descList = new Class<?>[] { CompressedModule.class };
		}
		return thisClass;
	}

	public void print(BitWise bw, int treeIndex, ModuleList ml) {
		int tag = DSMCCDescriptor.preparse(bw);
		thisClass.exception = false;
		for (int i = 0; i < thisClass.descList.length; i++) {
			Class<?> descClass = thisClass.descList[i];
			if (thisClass.getTag(descClass) == tag) {
				DSMCCDescriptor d = (DSMCCDescriptor) thisClass.getDescriptor(descClass, treeIndex, bw);
				d.setUp(treeIndex, bw, ml);
				thisClass.invokeMethod(descClass, d, "printDescription");
				if (!thisClass.exception)
					return;
			}
		}
		DSMCCDescriptor d = new DSMCCDescriptor();
		d.setUp(treeIndex, bw, ml);
		d.print();
	}
}
