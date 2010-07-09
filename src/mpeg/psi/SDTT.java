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
import mpeg.sbtvd.SpecialSemantic;
import sys.BitWise;

//Software_download_trigger_section
public class SDTT extends Table {
	
	public static final int LOW_PROTECTION_LAYER_PID = 0x0023;
	public static final int HIGH_PROTECTION_LAYER_PID = 0x0028;

	public SDTT(int pid) {
		this.pid = pid;
		id = 0xc3;
		name = "SDTT";
	}

	public boolean printDescription(byte[] ba) {
		if (!verifyMultiSection(ba))
			return false;
		printSectionInfo();
		if (pid == LOW_PROTECTION_LAYER_PID)
			addSubItem("transmission: low protection layer");
		else
			addSubItem("transmission: high protection layer");
//		Transport_stream_id	16
		addSubItem("transport_stream_id: "
				+ SpecialSemantic.parseNetworkID(bw.pop16()));
//		Original_network_id	16
		addSubItem("original_network_id: "
				+ SpecialSemantic.parseNetworkID(bw.pop16()));
//		Service_id	16
		addSubItem("service_id: "
				+ SpecialSemantic.parseNetworkID(bw.pop16()));
//		Num_of_contents	8
		int contentCount = bw.pop();
//		For(i=0;i<num_of_contents;I++){	
		while (contentCount-- > 0 && bw.getAvailableSize() > 0) {
			int gtv =  bw.pop16();
//			Group	4
			addSubItem("group: " + BitWise.stripBits(gtv, 16, 4));
//			target_version	12
			addSubItem("target_version: " + BitWise.stripBits(gtv, 12, 12));
//			new_version	12
			addSubItem("new_version: " + bw.consumeBits(2));
//			download_level	2
			addSubItem("download_level: " + bw.consumeBits(2));
//			version_indicator	2
			addSubItem("version_indicator: " + bw.consumeBits(2));
//			content_description_length	12
			int contentlenght = bw.consumeBits(12);
			addSubItem("content_description_length: " + contentlenght);
//			Reserved	4
			bw.consumeBits(4);
			bw.mark();
//			schedule_description_length	12
			int sheduleLenght = bw.consumeBits(12);
			addSubItem("schedule_description_length: " + sheduleLenght);
//			schedule_time-shift_information	4
			int sheduleLoop = addSubItem("schedule_time-shift_information: " + bw.consumeBits(4));
//			for(i=0;i<N;i++){	
			while (sheduleLenght > 0) {
//				start_time	40
				addSubItem("start_time: " + TOT.formatMJD(TOT.parseMJD(bw)), sheduleLoop);
//				duration	24
				int duration = bw.pop16() << 8 + bw.pop();
				addSubItem("duration: " + duration, sheduleLoop);
				sheduleLenght -= 8;
			}
//			for(j=0;j<N2;j++){
			int descLevel = addSubItem("descriptors:");
			while (bw.getByteCount() < contentlenght && bw.getAvailableSize() > 0)
				DescriptorList.print(bw, descLevel);
//				descriptors()	
//			}	
		}	
		return true;
	}
}
