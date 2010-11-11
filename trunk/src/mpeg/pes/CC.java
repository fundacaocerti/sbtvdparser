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
import sys.Messages;

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
		name = Messages.getString("CC.caption") + ccTrackNumb; //$NON-NLS-1$
		treeIndx = MainPanel.addTreeItem(name, 0, MainPanel.CC_TREE);
	}

	int mgmtPackets = 0;

	int[] mgmtCrcs = new int[100];

	int mgmtCount = 0;

	public void printHeader() {
		if (bigBuffer.length > 46)
			super.printHeader();
		else if (mgmtPackets == 0)
			mgmtPackets = addSubItem("Management packets"); //$NON-NLS-1$
		else {
			int i = bw.getAbsolutePosition();
			bw.setOffset(bigBuffer.length - 2);
			int crc = bw.pop16();
			bw.setOffset(i);
			for (i = 0; i < mgmtCount; i++)
				if (mgmtCrcs[i] == crc)
					return;
			mgmtCrcs[mgmtCount++] = crc;
			thisPacket = addSubItem("ES packet " + BitWise.toHex(crc), mgmtPackets); //$NON-NLS-1$
			addSubItem(Messages.getString("CC.pktSize") + bigBuffer.length, thisPacket); //$NON-NLS-1$
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
		addSubItem(Messages.getString("CC.startOk"), thisPacket); //$NON-NLS-1$
		while (bw.getAvailableSize() > 7) {// 5b de header + 2 de crc = mínimo
			// dataGroup
			int crcStart = bw.getAbsolutePosition();
			String id = "A-"; //$NON-NLS-1$
			int data_group_id = bw.consumeBits(6);
			if (data_group_id > 0x1F)
				id = "B-"; //$NON-NLS-1$
			if (data_group_id % 0x20 == 0)
				id += "management"; //$NON-NLS-1$
			else
				id += "statement lang. " + (data_group_id % 0x20); //$NON-NLS-1$
			dataGroupLvl = addSubItem("data_group_id: " + id, thisPacket); //$NON-NLS-1$
			addSubItem("data_group_version: " + bw.consumeBits(2), dataGroupLvl); //$NON-NLS-1$
			bw.pop16(); // data_group_link_number; last_data_group_link_number
			int data_group_size = bw.pop16();
			addSubItem("data_group_size: " + data_group_size, dataGroupLvl); //$NON-NLS-1$

			// bw.printBuffer(bw.getAbsolutePosition(), bw.getBufferSize());
			if (data_group_id % 0x20 == 0)
				parseCCManagement(bw, data_group_size);
			else
				parseCCStatement(bw, data_group_size);

			int readCrc = bw.pop16();
			int crc = CRC16.calc(bigBuffer, crcStart, data_group_size + 5); // 5
			// bytes no header
			addSubItem("CRC16: " + BitWise.toHex(crc) + " " + (readCrc == crc), dataGroupLvl); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	void parseTMD(BitWise bw, boolean parseFree) {
		String[] tmdTxt = { "Free", "Real-time", "Offset time", "reserved" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		// TMD 2
		bw.remainingBits = 0;
		int tmd = bw.consumeBits(2);
		addSubItem("TMD: " + tmdTxt[tmd], dataGroupLvl); //$NON-NLS-1$
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
			addSubItem("OTM: " + sb.toString(), dataGroupLvl); //$NON-NLS-1$
		}
	}

	void parseDataUnit(BitWise bw) {
		// bw.printBuffer(bw.getAbsolutePosition(), bw.getAbsolutePosition()+4);
		int data_unit_loop_length = (bw.pop16() << 8) + bw.pop();
		int duLvl = addSubItem("data_unit", dataGroupLvl); //$NON-NLS-1$
		addSubItem("data_unit_loop_length: " + data_unit_loop_length, duLvl); //$NON-NLS-1$
		if (data_unit_loop_length < 5)
			return;
		addSubItem("unit separator: " + (0x1f == bw.pop()), duLvl); //$NON-NLS-1$
		int[] typeId = { 0x20, 0x28, 0x2c, 0x30, 0x31, 0x34, 0x35 };
		String[] typeNames = { "text", "geometric", "sound", "1b DRCS", "2b DRCS", "color map", "bitmap" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		String typeName = Messages.getString("CC.unknown"); //$NON-NLS-1$
		int type = bw.pop();
		for (int i = 0; i < typeNames.length; i++)
			if (type == typeId[i])
				typeName = typeNames[i];
		addSubItem(Messages.getString("CC.duType") + typeName, duLvl); //$NON-NLS-1$
		int data_unit_size = (bw.pop16() << 8) + bw.pop();
		addSubItem(Messages.getString("CC.duSize") + data_unit_size, duLvl); //$NON-NLS-1$
		int contentLvl = addSubItem(Messages.getString("CC.duContent"), duLvl); //$NON-NLS-1$
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
		addSubItem(Messages.getString("CC.duLangs") + num_languages, dataGroupLvl); //$NON-NLS-1$
		// for(i=0;i<N;i++){
		for (int i = 0; i < num_languages; i++) {
			// language_tag 3 bslbf
			addSubItem("tag: " + bw.consumeBits(3), dataGroupLvl); //$NON-NLS-1$
			// reserved 1 bslbf
			bw.consumeBits(1);
			// DMF 4 bslbf
			int dmf = bw.consumeBits(4);
			String[] dispType = { "Auto", "Non", "Select", "Specific" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			addSubItem("dmf: " + dispType[BitWise.stripBits(dmf, 4, 2)] + " display", dataGroupLvl); //$NON-NLS-1$ //$NON-NLS-2$
			if (dmf == 12 || dmf == 13 || dmf == 14)
				addSubItem("dc: " + bw.pop()); //$NON-NLS-1$
			// if (DMF==’1100’ || DMF==’1101’ || DMF==’1110’){
			// DC 8 bslbf
			// }
			addSubItem(Component.parseISO639(bw), dataGroupLvl);

			// ISO_639_language_code 24 uimsbf
			int df = bw.consumeBits(4);
			String[] hv = { "Horizontal", "Vertical" }; //$NON-NLS-1$ //$NON-NLS-2$
			String[] dens = { "standard dens.", "high density", "western lang.", "1920x1080", "960x540", "720x480", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
					"1280x720", "reserved" }; //$NON-NLS-1$ //$NON-NLS-2$
			addSubItem(
					Messages.getString("CC.format") + hv[df & 1] + Messages.getString("CC.wMode") + dens[df >> 1], dataGroupLvl); //$NON-NLS-1$ //$NON-NLS-2$
			addSubItem("TCS: " + bw.consumeBits(2), dataGroupLvl); //$NON-NLS-1$
			addSubItem("rollup: " + Messages.getString("CC.yesNo").charAt(bw.consumeBits(2) & 1), dataGroupLvl); //$NON-NLS-1$ //$NON-NLS-2$
			// Format 4 bslbf
			// TCS 2 bslbf
			// rollup_mode 2 bslbf

		}
		while (bw.getByteCount() < size)
			parseDataUnit(bw);
	}
}
