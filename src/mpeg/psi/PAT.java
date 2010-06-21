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
//Program Association Table;
package mpeg.psi;

import mpeg.sbtvd.SpecialSemantic;

public class PAT extends Table {

	public PAT() {
		id = 0x00;
		pid = 0x00;
		name = "PAT";
	}

	public boolean printDescription(byte[] ba) {
		if (!verifyMultiSection(ba))
			return false;
		printSectionInfo();
		addSubItem("tx_network: "+SpecialSemantic.parseNetworkID(idExt));
		int serviceCount = 0;
		// for(i=0;i<N;i++){
		while (bw.getAvailableSize() > 0) {
			// program_number 16 uimsbf
			int programNumber = bw.pop16();
			int progIndx = addSubItem("program_number: "
					+ bw.toHex(programNumber));

			// Reserved 3 bslbf
			if (programNumber == 0) {
				// network_PID 13 uimsbf
				addSubItem("network_PID: "
						+ bw.toHex(bw.stripBits(bw.pop16(), 13, 13)), progIndx);
			} else {
				// program_map_PID 13 uimsbf
				int pid = bw.stripBits(bw.pop16(), 13, 13);
				addSubItem("program_map_PID: " + bw.toHex(pid), progIndx);
				TableList.addTable(new PMT(pid));
				serviceCount++;
			}
		}
		addSubItem("service_count: " + serviceCount);
		return true;
	}
}
