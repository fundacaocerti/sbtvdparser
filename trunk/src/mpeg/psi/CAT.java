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
//Conditional Access Table;
package mpeg.psi;

import mpeg.psi.descriptors.DescriptorList;

public class CAT extends Table {

	public CAT(int pid) {
		this.pid = pid;
		id = 0x01;
		name = "CAT";
	}
	
	@Override
	public boolean printDescription(final byte[] ba) {
		if (!verifyMultiSection(ba)) return false;
		printSectionInfo();
		final int catLvl = addSubItem("Descriptors");
		while (bw.getAvailableSize() > 0)
			DescriptorList.getInstance().print(bw, catLvl);
		return true;
	}
}
