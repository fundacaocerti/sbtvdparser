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
package mpeg.psi;

import gui.MainPanel;
import parsers.Packet;
import sys.BitWise;
import sys.CRC32;
import sys.Log;
import sys.Messages;
import sys.PIDStats;

public abstract class Table {

	public boolean parsed = false;

	public String name = null;

	public int id = 0xFFFF, pid = -1, treeIndx, layer = 0, idLimit = 0xFFFF, crcFails = 0, versionNumber = -1, crc,
			continuityOld = -1;

	int section_syntax_indicator = 0, section_length = 0, bufWriteIndx = 0, idExt, versionInfo, sectionNumber,
			lastSectionNumber, readTableID, sectionInfo;

	BitWise bw;

	byte[] bigBuffer;

	int[] knownTids = { 0x00, 0x01, 0x02, 0x40, 0x41, 0x42, 0x46, 0x4A, 0x4E, 0x4F, 0x70, 0x71, 0x72, 0x73, 0x74, 0x7E,
			0x7F, 0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xD0, 0xD1, 0xD2 };

	String[] knownNames = { "PAT", "CAT", "PMT", "NIT (actual)", "NIT (other)", "SDT (actual)", "SDT (other)", "BAT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			"EIT (now/next actual)", "EIT (now/next other)", "TDT", "RST", "ST", "TOT", "AIT", "DIT", "SIT", "DCT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
			"DLT", "PCAT", "SDTT", "BIT", "NBIT (net group info.)", "NBIT (reference net group info.)", "LDT", "CDT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			"LIT", "ERT", "ITT" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	boolean onStats = false;
	
	//public Table(int pid) {};

	public String getTableName(int tid) {
		String tn = ""; //$NON-NLS-1$
		// 0x3A – 0x3F Seção DSM-CC
		if (tid > 0x30 && tid < 0x40)
			tn = "DSM-CC"; //$NON-NLS-1$
		// 0x50 – 0x57 EIT (stream atual, grade de programação)
		if (tid > 0x4F && tid < 0x58)
			tn = "EIT (actual - schedule basic)"; //$NON-NLS-1$
		// 0x58 – 0x5F EIT (stream atual, grade de programação)
		if (tid > 0x57 && tid < 0x60)
			tn = "EIT (actual - schedule extended)"; //$NON-NLS-1$
		// 0x60 – 0x6F EIT (outro stream, grade de programação)
		if (tid > 0x5F && tid < 0x67)
			tn = "EIT (other - schedule)"; //$NON-NLS-1$
		// 0x82 – 0x83 ECM
		if (tid == 0x82 || tid == 0x83)
			tn = "ECM"; //$NON-NLS-1$
		// 0x84 – 0x85 EMM
		if (tid == 0x84 || tid == 0x85)
			tn = "EMM"; //$NON-NLS-1$
		// 0x90 – 0xBF Série selecionável para alocação de “table_id” pelas
		// empresas
		if (tid > 0x8F && tid < 0xC0)
			tn = "Broadcaster defined"; //$NON-NLS-1$
		for (int i = 0; i < knownTids.length; i++)
			if (knownTids[i] == tid)
				tn = knownNames[i];
		return tn;
	}

	public boolean printDescription(byte[] ba) {
		addSubItem(name + Messages.getString("Table.notImp"), 0); //$NON-NLS-1$
		return true;
	}

	boolean headerIncomplete = false;
	byte[] headerPart;
	public byte[] searchBytes = null;

	public void setLowLevelSearch(byte[] target) {
		searchBytes = target;
	}

	public boolean verifySection(byte[] ba) {
		if (ba.length < 3) {// não tem nem o tamanho da sessão nos dados
			headerPart = ba;
			headerIncomplete = true;
			return false;
		}
		if (!onStats) {
			PIDStats.setIdentification(pid, name);
			onStats = true;
		}
		bw = new BitWise(ba);
		readTableID = bw.pop();
		if (resetMultissection && readTableID != 0xFF) {
			bufWriteIndx = 0;
			resetMultissection = false;
		}
		if (readTableID == 0xFF)
			return false; // only stuffing
		if (idLimit == 0xFFFF)
			idLimit = id;
		name = getTableName(readTableID);
		int tmp = bw.pop16();
		section_length = BitWise.stripBits(tmp, 12, 12);
		section_syntax_indicator = BitWise.stripBits(tmp, 16, 1);
		// TODO section_lenght conta 1 byte a mais, weird
		bw.setBufferSize(section_length - 1);
		if (ba.length < section_length + 3) {// 3 bytes são de table header
			bigBuffer = new byte[section_length + 3]; // includes CRC

			// bw.setBufferSize(183);
			// bw.printBuffer();
			// debug.setBuffer(bigBuffer);

			feedPart(ba, 0, 183);
			return false;
		} else
			bufWriteIndx = section_length + 3;
		if (CRC32.calc(ba, 0, section_length + 3) != 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("Section CRC failure for pid "); //$NON-NLS-1$
			sb.append(BitWise.toHex(pid));
			sb.append(" at "); //$NON-NLS-1$
			sb.append(sectionStart);
			sb.append(":"); //$NON-NLS-1$
			sb.append(Packet.byteCount);
			Log.printWarning(sb.toString());
			// Log.printStackTrace(e);
			crcFails++;
			// bw.printBuffer(0, 20);
			return false;
		}
		int lastCrc = crc;
		crc = 0;
		for (int i = -1; i < 3; i++)
			crc = (crc << 8) | ((ba[section_length + i]) & 0xff);
		if (crc == lastCrc)
			return false; //no need to re-parse
		if (readyToParse())
			return true;
		if (readTableID < id || readTableID > idLimit) { // EITs have ranges
			// of ids
			int errMsg = addSubItem(Messages.getString("Table.unknown") + BitWise.toHex(readTableID)); //$NON-NLS-1$
			addSubItem(Messages.getString("Table.tidName") + name, errMsg); //$NON-NLS-1$
			addSubItem(Messages.getString("Table.expectedTid") + BitWise.toHex(id) + " - " + name, errMsg); //$NON-NLS-1$ //$NON-NLS-2$
			addSubItem(Messages.getString("Table.content") + bw.getHexSequence(section_length) + "]", errMsg); //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}
		// id = table_id;
		return true;
	}

	private boolean readyToParse() {
		return bufWriteIndx == section_length + 3;
	}

	boolean resetMultissection = false;

	long sectionStart = 0;

	public void resetMultissection() {
		sectionStart = Packet.byteCount;
		resetMultissection = true;
	}

	public void feedPart(byte[] source, int srcOffset, int writeSize) {
		if (headerIncomplete) {
			byte[] header = new byte[3];
			for (int i = 0; i < headerPart.length; i++)
				header[i] = headerPart[i];
			for (int i = headerPart.length; i < 3; i++)
				header[i] = source[i + srcOffset - headerPart.length];
			srcOffset += 3 - headerPart.length;
			writeSize -= 3 - headerPart.length;
			headerIncomplete = false;
			verifyMultiSection(header);
		}
		if (bigBuffer == null)
			return;
		if (bufWriteIndx + writeSize > section_length + 3)
			writeSize = section_length - bufWriteIndx + 3;
		if (source.length - srcOffset < writeSize)
			writeSize = source.length - srcOffset;
		try {
			System.arraycopy(source, srcOffset, bigBuffer, bufWriteIndx, writeSize);
		} catch (RuntimeException e) {
			Log.printStackTrace(e);
		}
		bufWriteIndx += writeSize;

		if (readyToParse())
			parsed = printDescription(bigBuffer);
	}

	int eitGroup = 0;

	public void printBasicInfo() {
		if (EIT.class.isInstance(this)) {
			if (eitGroup == 0)
				if (pid == EIT.FULLSEGPID)
					eitGroup = addSubItem("Full-seg EITs", 0); //$NON-NLS-1$
				else
					eitGroup = addSubItem("One-seg EITs", 0); //$NON-NLS-1$
			treeIndx = addSubItem(name, eitGroup);
		} else
			treeIndx = addSubItem(name + " (pid " + BitWise.toHex(pid) + ")", 0); //$NON-NLS-1$ //$NON-NLS-2$
		sectionInfo = addSubItem("Section info"); //$NON-NLS-1$
		MainPanel.setTreeData(treeIndx, bw);
		if (layer != 0)
			addSubItem("layer: " + "ABC".charAt(layer - 1), sectionInfo); //$NON-NLS-1$ //$NON-NLS-2$
		addSubItem("Id: " + BitWise.toHex(readTableID), sectionInfo); //$NON-NLS-1$
		addSubItem("section CRC: " + BitWise.toHex(crc), sectionInfo); //$NON-NLS-1$
		if (crcFails > 0)
			addSubItem("CRC failures: " + crcFails, sectionInfo); //$NON-NLS-1$
		addSubItem("section_length: " + section_length, sectionInfo); //$NON-NLS-1$
		addSubItem("section_syntax_indicator: " + section_syntax_indicator, sectionInfo); //$NON-NLS-1$
	}

	public void printSectionInfo() {
		printBasicInfo();
		addSubItem("Id extension: " + BitWise.toHex(idExt), sectionInfo); //$NON-NLS-1$
		addSubItem("version number: " + BitWise.toHex(versionNumber), sectionInfo); //$NON-NLS-1$
		String[] cn = { "next", "current" }; //$NON-NLS-1$ //$NON-NLS-2$
		addSubItem("current/next: " + cn[BitWise.stripBits(versionInfo, 1, 1)], sectionInfo); //$NON-NLS-1$
		addSubItem("section number: " + BitWise.toHex(sectionNumber), sectionInfo); //$NON-NLS-1$
		addSubItem("last section number: " + BitWise.toHex(lastSectionNumber), sectionInfo); //$NON-NLS-1$
	}

	public boolean verifyMultiSection(byte[] ba) {
		if (!verifySection(ba))
			return false;
		bw.mark();
		idExt = bw.pop16();
		versionInfo = bw.pop();
		versionNumber = BitWise.stripBits(versionInfo, 6, 5);
		// currentNextIndicator = BitWise.stripBits(versionInfo, 1, 1);
		sectionNumber = bw.pop();
		lastSectionNumber = bw.pop();
		return true;
	}

	public int addSubItem(String msg, int parent, int rootIndx) {
		return MainPanel.addTreeItem(msg, parent, rootIndx);
	}

	public int addSubItem(String msg, int parent) {
		return MainPanel.addTreeItem(msg, parent);
	}

	public int addSubItem(String msg) {
		return MainPanel.addTreeItem(msg, treeIndx);
	}
}
