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
//Service Description Table
package mpeg.psi;

import mpeg.psi.descriptors.DescriptorList;
import mpeg.psi.descriptors.TSinformation;
import mpeg.sbtvd.SpecialSemantic;
import sys.BitWise;

public class SDT extends Table {

	public SDT() {
		id = 0x42;
		pid = 0x11;
		name = "SDT actual";
	}

	public boolean printDescription(byte[] ba) {
		if (!verifyMultiSection(ba))
			return false;
		printSectionInfo();
		// original_network_id 16 uimsbf
		int onid = bw.pop16();
		addSubItem("original_network_id: " + SpecialSemantic.parseNetworkID(onid));

		// reserved_future_use 8 bslbf
		bw.pop();
		int svcLoopLevel = addSubItem("Services");
		while (bw.getAvailableSize() > 0) {
			// service_id 16 uimsbf
			int service_id = bw.pop16();
			int svcIdLevel = addSubItem("service_id: " + BitWise.toHex(service_id), svcLoopLevel);
			addSubItem("type: " + TSinformation.svcTypes[BitWise.stripBits(service_id, 5, 2)], svcIdLevel);
			addSubItem("number: " + (BitWise.stripBits(service_id, 3, 3) + 1), svcIdLevel);

			// reserved_future_use 6 bslbf
			bw.consumeBits(6);
			// EIT_schedule_flag 1 bslbf
			// EIT_present_following_flag 1 bslbf
			addSubItem("EIT_schedule_flag: " + bw.consumeBits(1), svcIdLevel);
			addSubItem("EIT_present_following_flag: " + bw.consumeBits(1), svcIdLevel);

			// running_status 3 uimsbf
			// free_CA_mode 1 bslbf
			// descriptors_loop_length 12 uimsbf
			onid = bw.pop16();
			String[] rs = { "undefined", "off", "in a few minutes", "paused", "running" };
			addSubItem("running_status: " + rs[BitWise.stripBits(onid, 16, 3) % 5], svcIdLevel);
			int descriptorsLenght = BitWise.stripBits(onid, 12, 12);
			int netDescIndx = addSubItem("network descriptors: (lenght " + descriptorsLenght + ")", svcIdLevel);
			int mark = bw.getByteCount();
			while ((bw.getByteCount() - mark < descriptorsLenght) && (bw.getAvailableSize() > 0)) {
				DescriptorList.getInstance().print(bw, netDescIndx);
			}
		}
		return true;
	}
}
