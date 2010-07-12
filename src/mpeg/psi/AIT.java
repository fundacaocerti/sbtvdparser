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

import mpeg.psi.descriptors.AITDescriptorList;
import mpeg.psi.descriptors.DescriptorList;
import sys.BitWise;

public class AIT extends Table {

	public AIT(int pid) {
		this.pid = pid;
		id = 0x74;
		name = "AIT";
	}

	public boolean printDescription(byte[] ba) {
		if (!verifyMultiSection(ba))
			return false;
		printSectionInfo();
		String[] appTypes = { "Reservado", "DVB-J / Ginga-J", "DVB-HTML", "Reservado", "Reservado", "Reservado",
				"ACAP-J", "ARIB - BML", "Ginga - Bridge", "Ginga-NCL" };
		if (idExt < appTypes.length)
			addSubItem("application_type: " + BitWise.toHex(idExt) + " > " + appTypes[idExt]);
		else
			addSubItem("application_type: " + BitWise.toHex(idExt) + " > unknown");
		int commonDescriptorsLength = BitWise.stripBits(bw.pop16(), 12, 12);
		int commonDescLvl = addSubItem("common descriptors: (lenght " + commonDescriptorsLength + ")");
		bw.mark();
		while ((bw.getByteCount() < commonDescriptorsLength) && (bw.getAvailableSize() > 0)) {
			DescriptorList.print(bw, commonDescLvl);
		}
		int applicationLoopLength = BitWise.stripBits(bw.pop16(), 12, 12);
		int appLvl = addSubItem("application loop: (lenght " + applicationLoopLength + ")");
		bw.mark();
		while ((bw.getByteCount() < applicationLoopLength) && (bw.getAvailableSize() > 0)) {
			// application_identifier ()
			// organization_id 32 bslbf
			addSubItem("organization_id: " + BitWise.toHex(bw.pop32()), appLvl);
			int appId = bw.pop16();
			String appIdSemantic = "";
			if (appId <= 0x3ff)
				appIdSemantic = "not signed app";
			else if (appId <= 0x7ff)
				appIdSemantic = "signed app";
			else if (appId <= 0xfffd)
				appIdSemantic = "DVB reserved";
			else if (appId == 0xfffe)
				appIdSemantic = "any signed app. for this org.";
			else
				appIdSemantic = "any app. for this org.";

			addSubItem("application_id: " + BitWise.toHex(appId) + " > " + appIdSemantic, appLvl);
			// application_control_code 8 uimsbf
			String[] appCC = { "reserved", "autostart", "present", "destroy", "kill", "prefetch", "remote", "unbound" };
			int appCtrlCode = bw.pop();
			if (appCtrlCode < appCC.length)
				addSubItem("application_control_code: " + BitWise.toHex(appCtrlCode) + " > " + appCC[appCtrlCode],
						appLvl);
			else
				addSubItem("application_control_code: " + BitWise.toHex(appCtrlCode), appLvl);
			// reserved_future_use 4 bslbf
			// application_descriptors_loop_length 12 uimsbf
			int appDescLoopLenght = BitWise.stripBits(bw.pop16(), 12, 12);
			int mark = bw.getByteCount();
			int appDescLvl = addSubItem("application descriptors: (lenght " + appDescLoopLenght + ")", appLvl);
			while ((bw.getByteCount() - mark < appDescLoopLenght) && (bw.getAvailableSize() > 0)) {
				AITDescriptorList.print(bw, appDescLvl);
			}
		}
		return true;
	}
}
