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
package mpeg.pes;

import gui.MainPanel;
import mpeg.es.CCPresentation;
import mpeg.psi.descriptors.Component;
import sys.BitWise;
import sys.CRC16;

public class CC extends PES {

	int dataGroupLvl;

	private static int ccTrackNumb = 0;

	public static void reset() {
		ccTrackNumb = 0;
	}

	public CC(int pid) {
		id = 0xbd;
		this.pid = pid;
		ccTrackNumb++;
		name = "Closed Caption " + ccTrackNumb;
		treeIndx = MainPanel.addTreeItem(name, 0, MainPanel.CC_TREE);
	}

	int mgmtPackets = 0;

	int[] mgmtCrcs = new int[100];

	int mgmtCount = 0;

	public void printHeader() {
		if (bigBuffer.length > 46)
			super.printHeader();
		else if (mgmtPackets == 0)
			mgmtPackets = addSubItem("Management packets");
		else {
			int i = bw.getAbsolutePosition();
			bw.setOffset(bigBuffer.length - 2);
			int crc = bw.pop16();
			bw.setOffset(i);
			for (i = 0; i < mgmtCount; i++)
				if (mgmtCrcs[i] == crc)
					return;
			mgmtCrcs[mgmtCount++] = crc;
			thisPacket = addSubItem("ES packet " + BitWise.toHex(crc), mgmtPackets);
			addSubItem("packet_lenght: " + bigBuffer.length, thisPacket);
			parse();
		}
	}

	public void parse() {
		parseExtHeader(thisPacket);
		/*
		 * data_identifier 0x80 private_stream_id 0xFF
		 * PES_data_packet_header_length Shows the length of
		 * PES_data_private_data_byte. Normally 0x00 is input. *2
		 */
		int[] prefix = { 0x80, 0xff, 0xf0 };
		for (int i = 0; i < prefix.length; i++)
			if (prefix[i] != bw.pop())
				return;
		addSubItem("start prefix OK", thisPacket);
		while (bw.getAvailableSize() > 7) {// 5b de header + 2 de crc = mínimo
			// dataGroup
			int crcStart = bw.getAbsolutePosition();
			String id = "A-";
			int data_group_id = bw.consumeBits(6);
			if (data_group_id > 0x1F)
				id = "B-";
			if (data_group_id % 0x20 == 0)
				id += "management";
			else
				id += "statement lang. " + (data_group_id % 0x20);
			dataGroupLvl = addSubItem("data_group_id: " + id, thisPacket);
			addSubItem("data_group_version: " + bw.consumeBits(2), dataGroupLvl);
			bw.pop16(); // data_group_link_number; last_data_group_link_number
			int data_group_size = bw.pop16();
			addSubItem("data_group_size: " + data_group_size, dataGroupLvl);

			// bw.printBuffer(bw.getAbsolutePosition(), bw.getBufferSize());
			if (data_group_id % 0x20 == 0)
				parseCCManagement(bw, data_group_size);
			else
				parseCCStatement(bw, data_group_size);

			int readCrc = bw.pop16();
			int crc = CRC16.calc(bigBuffer, crcStart, data_group_size + 5); // 5
			// bytes no header
			addSubItem("CRC16: " + BitWise.toHex(crc) + " " + (readCrc == crc), dataGroupLvl);
		}
	}

	void parseTMD(BitWise bw, boolean parseFree) {
		String[] tmdTxt = { "Free", "Real-time", "Offset time", "reserved" };
		// TMD 2
		bw.remainingBits = 0;
		int tmd = bw.consumeBits(2);
		addSubItem("TMD: " + tmdTxt[tmd], dataGroupLvl);
		// Reserved 6
		bw.consumeBits(6);
		// if(TMD==’10’){
		if (tmd == 2 || (parseFree && tmd == 1)) {
			StringBuffer sb = new StringBuffer();
			// OTM 36
			sb.append(BitWise.toHex(bw.pop()).substring(2));
			sb.append(':');
			sb.append(BitWise.toHex(bw.pop()).substring(2));
			sb.append(':');
			sb.append(BitWise.toHex(bw.pop()).substring(2));
			sb.append('.');
			sb.append(BitWise.toHex(bw.pop()).substring(2));
			sb.append(BitWise.toHex(bw.consumeBits(4)).substring(3));
			// Reserved 4
			bw.consumeBits(4);
			addSubItem("OTM: " + sb.toString(), dataGroupLvl);
		}
	}

	void parseDataUnit(BitWise bw) {
		// bw.printBuffer(bw.getAbsolutePosition(), bw.getAbsolutePosition()+4);
		int data_unit_loop_length = (bw.pop16() << 8) + bw.pop();
		int duLvl = addSubItem("data_unit", dataGroupLvl);
		addSubItem("data_unit_loop_length: " + data_unit_loop_length, duLvl);
		if (data_unit_loop_length < 5)
			return;
		addSubItem("unit separator: " + (0x1f == bw.pop()), duLvl);
		int[] typeId = { 0x20, 0x28, 0x2c, 0x30, 0x31, 0x34, 0x35 };
		String[] typeNames = { "text", "geometric", "sound", "1b DRCS", "2b DRCS", "color map", "bitmap" };
		String typeName = "Unknown";
		int type = bw.pop();
		for (int i = 0; i < typeNames.length; i++)
			if (type == typeId[i])
				typeName = typeNames[i];
		addSubItem("type: " + typeName, duLvl);
		int data_unit_size = (bw.pop16() << 8) + bw.pop();
		addSubItem("data_unit_size: " + data_unit_size, duLvl);
		int contentLvl = addSubItem("content", duLvl);
		if (type == typeId[0])
			new CCPresentation(contentLvl, thisPacket).parse(bw, data_unit_size);
	}

	void parseCCStatement(BitWise bw, int size) {
		int mark = bw.getAbsolutePosition();
		parseTMD(bw, true);
		while (bw.getAbsolutePosition() - mark < size)
			parseDataUnit(bw);
	}

	void parseCCManagement(BitWise bw, int size) {
		bw.mark();
		// bw.printBuffer(bw.getAbsolutePosition(),
		// bw.getAbsolutePosition()+10);
		parseTMD(bw, false);
		// num_languages 8
		int num_languages = bw.pop();
		addSubItem("num_languages: " + num_languages, dataGroupLvl);
		// for(i=0;i<N;i++){
		for (int i = 0; i < num_languages; i++) {
			// language_tag 3 bslbf
			addSubItem("language_tag: " + bw.consumeBits(3), dataGroupLvl);
			// reserved 1 bslbf
			bw.consumeBits(1);
			// DMF 4 bslbf
			int dmf = bw.consumeBits(4);
			String[] dispType = { "Auto", "Non", "Select", "Specific" };
			addSubItem("dmf: " + dispType[BitWise.stripBits(dmf, 4, 2)] + " display", dataGroupLvl);
			if (dmf == 12 || dmf == 13 || dmf == 14)
				addSubItem("dc: " + bw.pop());
			// if (DMF==’1100’ || DMF==’1101’ || DMF==’1110’){
			// DC 8 bslbf
			// }
			addSubItem(Component.parseISO639(bw), dataGroupLvl);

			// ISO_639_language_code 24 uimsbf
			int df = bw.consumeBits(4);
			String[] hv = { "Horizontal", "Vertical" };
			String[] dens = { "standard dens.", "high density", "western lang.", "1920x1080", "960x540", "720x480",
					"1280x720", "reserved" };
			addSubItem("Format: " + hv[df & 1] + " writing in " + dens[df >> 1], dataGroupLvl);
			addSubItem("TCS: " + bw.consumeBits(2), dataGroupLvl);
			addSubItem("rollup_mode: " + "yn".charAt(bw.consumeBits(2) & 1), dataGroupLvl);
			// Format 4 bslbf
			// TCS 2 bslbf
			// rollup_mode 2 bslbf

		}
		while (bw.getByteCount() < size)
			parseDataUnit(bw);
	}
}
