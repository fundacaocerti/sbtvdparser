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
//Event Information Table;
package mpeg.psi;

import gui.MainPanel;
import mpeg.psi.descriptors.DescriptorList;
import mpeg.psi.descriptors.ParentalRating;
import mpeg.psi.descriptors.ShortEvent;
import parsers.Packet;
import sys.BitWise;
import sys.EPG;

public class EIT extends Table {

	private static final int sectionPidIndx = 0, sectionSvcIdIndx = 1, sectionNumberIndx = 2, sectionCountIndx = 3,
			sectionVsnIndx = 4, maxSessions = 800;

	int[][] sectionInfo = { new int[maxSessions], new int[maxSessions], new int[maxSessions], new int[maxSessions],
			new int[maxSessions] };

	int transport_stream_id, original_network_id, segment_last_section_number, last_table_id, sectionInfoCount = 0,
			EITpf = 0, EITbasic = 0, EIText = 0, EITpfFilled = 0, EITbasicFilled = 0, EITextFilled = 0;

	EPG epgNN = new EPG(), epgDaily = new EPG(), epg7d = new EPG();

	public static final int ONESEGPID = 0x0027, FULLSEGPID = 0x0012;

	public EIT(int pid) {
		this.pid = pid;
		id = 0x4e;
		idLimit = 0x5f;
	}

	private boolean isReplicated() {
		if (sectionInfoCount == maxSessions)
			return true;
		for (int i = 0; i < sectionInfoCount; i++)
			if (sectionInfo[sectionNumberIndx][i] == sectionNumber && sectionInfo[sectionVsnIndx][i] == versionNumber
					&& sectionInfo[sectionPidIndx][i] == readTableID && sectionInfo[sectionSvcIdIndx][i] == idExt) {
				sectionInfo[sectionCountIndx][i]++;
				return true;
			}
		sectionInfo[sectionNumberIndx][sectionInfoCount] = sectionNumber;
		sectionInfo[sectionPidIndx][sectionInfoCount] = readTableID;
		sectionInfo[sectionSvcIdIndx][sectionInfoCount] = idExt;
		sectionInfo[sectionVsnIndx][sectionInfoCount] = versionNumber;
		sectionInfoCount++;
		return false;
	}

	public boolean printDescription(byte[] ba) {
		if (!verifyMultiSection(ba))
			return false;
		transport_stream_id = bw.pop16();
		original_network_id = bw.pop16();
		segment_last_section_number = bw.pop();
		last_table_id = bw.pop();

		if (readTableID == 0x4E)
			EITpf++;
		if (readTableID > 0x4F && readTableID < 0x58)
			EITbasic++;
		if (readTableID > 0x57 && readTableID < 0x60)
			EIText++;

		if (bw.getAvailableSize() == 0)
			return false;

		if (readTableID == 0x4E)
			EITpfFilled++;
		if (readTableID > 0x4F && readTableID < 0x58)
			EITbasicFilled++;
		if (readTableID > 0x57 && readTableID < 0x60)
			EITextFilled++;

		int i;
		if (isReplicated())
			return false;

		printSectionInfo();
		// System.out.println(name);

		addSubItem("transport_stream_id: " + BitWise.toHex(transport_stream_id));
		addSubItem("original_network_id: " + BitWise.toHex(original_network_id));
		addSubItem("segment_last_section_number: " + BitWise.toHex(segment_last_section_number));
		addSubItem("last_table_id: " + BitWise.toHex(last_table_id));
		bw.mark();
		addSubItem("Event info. lenght: " + bw.getAvailableSize());// TODO não é
		// loop
		// lenght?
		int loopLevel = addSubItem("Event loop:");
		while (bw.getAvailableSize() > 0) {
			int id = bw.pop16();
			int[] start = TOT.parseMJD(bw);
			int hexTime;
			StringBuffer duration = new StringBuffer();
			for (i = 0; i < 3; i++) {
				hexTime = bw.pop();
				if (hexTime < 10)
					duration.append("0");
				duration.append(Integer.toHexString(hexTime));
				if (i < 2)
					duration.append(":");
			}

			int evtLevel = addSubItem("event_id: " + BitWise.toHex(id), loopLevel);
			addSubItem("start_time: " + TOT.formatMJD(start), evtLevel);
			addSubItem("duration: " + duration.toString(), evtLevel);

			String[] runningStatus = { "undefined", "not running", "starts soon", "paused", "running", "off-air",
					"reserved", "reserved" };
			addSubItem("running_status: " + runningStatus[bw.consumeBits(3)], evtLevel);
			String[] free_CA_mode = { "not-scrambled", "scrambled" };
			addSubItem("free_CA_mode: " + free_CA_mode[bw.consumeBits(1)], evtLevel);

			int descriptorsLenght = bw.consumeBits(12);
			int descLevel = addSubItem("Descriptors loop:", evtLevel);
			int mark = bw.getByteCount();
			while ((bw.getByteCount() - mark < descriptorsLenght) && (bw.getAvailableSize() > 0)) {
				// System.out.println(bw.pop(0));
				DescriptorList.print(bw, descLevel);
			}
			String evtName = ShortEvent.evtName;
			ShortEvent.evtName = "n/d";
			String evtDesc = ShortEvent.evtDesc;
			ShortEvent.evtDesc = "n/d";
			String rating = ParentalRating.ratingTxt;
			ParentalRating.ratingTxt = "n/d";
			if (readTableID == 0x4E)
				epgNN.addEvent(id, start, duration.toString(), evtName, evtDesc, rating);
			if (readTableID > 0x4F && readTableID < 0x58)
				epgDaily.addEvent(id, start, duration.toString(), evtName, evtDesc, rating);
			if (readTableID > 0x57 && readTableID < 0x60)
				epg7d.addEvent(id, start, duration.toString(), evtName, evtDesc, rating);
		}
		return false;
	}

	public void printEPG() {
		int epgLevel;
		if (pid == EIT.FULLSEGPID)
			epgLevel = addSubItem("Full-seg EPG", 0, MainPanel.EPG_TREE);
		else
			epgLevel = addSubItem("One-seg EPG", 0, MainPanel.EPG_TREE);
		epgNN.printDescription(addSubItem("now/next", epgLevel));
		epgDaily.printDescription(addSubItem("basic", epgLevel));
		epg7d.printDescription(addSubItem("extended", epgLevel));
	}

	public void printStatistics() {
		if ((EITbasic + EIText + EITpf) == 0)
			return;
		int statLevel;
		if (pid == EIT.FULLSEGPID)
			statLevel = addSubItem("Full-seg EIT Stats", 0, MainPanel.STATS_TREE);
		else
			statLevel = addSubItem("One-seg EIT Stats", 0, MainPanel.STATS_TREE);
		long totalPackets = Packet.packetCount;
		float duration = (float) (totalPackets / TOT.lastBitrate * 188 * 8 / 1e6);
		addSubItem("EITs: " + (EITbasic + EIText + EITpf), statLevel);
		if (duration != 0) {
			addSubItem("EIT-p/f sections/s: " + ((float) EITpf / duration), statLevel, MainPanel.STATS_TREE);
			addSubItem("EIT-8day sections/s: " + ((float) EITbasic / duration), statLevel, MainPanel.STATS_TREE);
			addSubItem("EIT-extended sections/s: " + ((float) EIText / duration), statLevel, MainPanel.STATS_TREE);
		}
		addSubItem("Filled EITs: " + (EITbasicFilled + EITextFilled + EITpfFilled), statLevel);
		if (duration != 0) {
			addSubItem("EIT-p/f sections/s: " + ((float) EITpfFilled / duration), statLevel, MainPanel.STATS_TREE);
			addSubItem("EIT-8day sections/s: " + ((float) EITbasicFilled / duration), statLevel, MainPanel.STATS_TREE);
			addSubItem("EIT-extended sections/s: " + ((float) EITextFilled / duration), statLevel, MainPanel.STATS_TREE);
		}
	}
}
