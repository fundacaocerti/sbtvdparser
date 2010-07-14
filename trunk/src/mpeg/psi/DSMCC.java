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
//Test DSMCC
/*if the section_syntax_indicator is set to '1', then the CRC_32 shall be present and
 correct. In the case where the section_syntax_indicator is '0', the syntax of the section is the same as when the
 section_syntax_indicator is '1', except that the CRC_32 field is replaced with the checksum field.*/

package mpeg.psi;

import gui.GuiMethods;
import gui.MainPanel;
import mpeg.psi.descriptors.Compatibility;
import sys.BitWise;
import sys.Log;
import sys.Messages;
import dsmcc.Module;
import dsmcc.ModuleList;

public class DSMCC extends Table {

	static final int DII_MESSAGE = 0x1002, DDB_MESSAGE = 0x1003, DSI_MESSAGE = 0x1006;
	int totalLenght = 0, downloaded = 0;
	public int progressLvl = 0, downloadId = -1;
	ModuleList moduleList;

	public DSMCC(int pid) {
		id = 0x3c;
		this.pid = pid;
		name = "DSMCC"; //$NON-NLS-1$
		progressLvl = MainPanel.addTreeItem(Messages.getString("DSMCC.mount") + pid + " 0%", 0, MainPanel.DSMCC_TREE); //$NON-NLS-1$ //$NON-NLS-2$
		// setLowLevelSearch(new byte[] {0x10, 0x02, (byte)0x80});
		moduleList = new ModuleList(this);
	}

	public void updateDlSize(int bytes) {
		totalLenght += bytes;
	}

	public void updateProgress(int bytes) {
		downloaded += bytes;
		if (totalLenght == 0)
			GuiMethods.runMethod(GuiMethods.CHANGEITEM, new Object[] { Messages.getString("DSMCC.mount") + pid + " 0%", //$NON-NLS-1$ //$NON-NLS-2$
					new Integer(progressLvl) }, true);
		else
			GuiMethods.runMethod(GuiMethods.CHANGEITEM, new Object[] {
					Messages.getString("DSMCC.mount") + pid + " " + (downloaded * 100 / totalLenght) + "%", new Integer(progressLvl) }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					true);
	}

	public boolean printDescription(byte[] ba) {
		if (moduleList.isReadyToMount())
			return true;
		if (!verifyMultiSection(ba))
			return false;
		// printSectionInfo();
		// bw.printBuffer(0, 20);
		// addSubItem("pid: "+BitWise.toHex(pid));

		if (section_syntax_indicator == 0)
			System.out.println("checksum used"); //$NON-NLS-1$
		bw.mark();
		switch (readTableID) {
		case 0x3A:
			System.out.println("LLCSNAP();");// TCP/IP encapsulation //$NON-NLS-1$
			break;
		case 0x3B:
			// System.out.println("\nDSI or DII");
			// bw.printBuffer(0, bw.getAvailableSize());
			userNetworkMessage();
			break;
		case 0x3C:
			// System.out.println("DDB");
			downloadDataMessage();
			break;
		case 0x3D:
			System.out.println("DSMCC_descriptor_list();"); //$NON-NLS-1$
			break;
		case 0x3E:
			System.out.println("DSMCC_private_data_byte();"); //$NON-NLS-1$
			bw.printBuffer(bw.getAbsolutePosition(), section_length - 9);
			break;
		}
		// System.out.println("available: "+bw.getAvailableSize());
		return false;
	}

	void userNetworkMessage() {
		// System.out.println("net");
		int unmLvl = 0;
		// int unmLvl = addSubItem("userNetworkMessage");
		// System.out.print("userNetMsg: ");
		// System.out.println(" "+section_length+"b");
		int msgId = dsmccMsgHeader(unmLvl);
		if (msgId == 0x1002)
			downloadInfoIndication();
		if (msgId == 0x1006)
			downloadServerInitiate();
	}

	private void downloadServerInitiate() {
		// Server-ID
		// System.out.println("Server-ID: "+bw.getHexSequence(20));
		bw.pop(20);
		// compatibilityDescriptor()
		new Compatibility().parse(bw);
		// privateDataLength
		bw.pop16();
		// System.out.println("privateDataLength: "+BitWise.toHex(privateDataLength));
	}

	int lastSectionVersion = -1, lastCrc = -1;

	private void downloadInfoIndication() {
		if (lastSectionVersion == versionNumber && crc == lastCrc)
			return;
		lastCrc = crc;
		if (lastSectionVersion != -1) {
			Log.printWarning(Messages.getString("DSMCC.rebuild")); //$NON-NLS-1$
			totalLenght = 0;
			downloaded = 0;
			updateProgress(0);
			moduleList.reset();
		}
		lastSectionVersion = versionNumber;
		int diiLvl = addSubItem("downloadInfoIndication", progressLvl); //$NON-NLS-1$
		downloadId = bw.pop16() << 16 + bw.pop16();
		addSubItem("downloadId: " + BitWise.toHex(downloadId), diiLvl); //$NON-NLS-1$
		int blockSize = bw.pop16();
		addSubItem("blockSize: " + BitWise.toHex(blockSize), diiLvl); //$NON-NLS-1$
		// int windowSize = bw.pop();
		// addSubItem("windowSize: "+BitWise.toHex(windowSize), unmLvl);
		// int ackPeriod = bw.pop();
		// addSubItem("ackPeriod: "+BitWise.toHex(ackPeriod), unmLvl);
		// int tCDownloadWindow = bw.pop16()<<16+bw.pop16();
		// addSubItem("tCDownloadWindow: "+BitWise.toHex(tCDownloadWindow),
		// unmLvl);
		// int tCDownloadScenario = bw.pop16()<<16+bw.pop16();
		// addSubItem("tCDownloadScenario: "+BitWise.toHex(tCDownloadScenario),
		// unmLvl);
		System.out.println("00's: " + bw.getHexSequence(10)); //$NON-NLS-1$
		// compatibilityDescriptor()
		new Compatibility().parse(bw);
		// bw.pop16();
		int numberOfModules = bw.pop16();
		int moduleLvl = addSubItem(Messages.getString("DSMCC.modules") + numberOfModules + ")", diiLvl); //$NON-NLS-1$ //$NON-NLS-2$
		MainPanel.setTreeData(moduleLvl, moduleList);
		for (int i = 0; i < numberOfModules; i++)
			moduleList.createModule(bw, moduleLvl);
		moduleList.loadCache();
		int privateDataLength = bw.pop16();
		if (privateDataLength > 0) {
			int pdLvl = addSubItem("privateData: " + BitWise.toHex(privateDataLength), diiLvl); //$NON-NLS-1$
			addSubItem(bw.getHexSequence(privateDataLength), pdLvl);
		}
	}

	void downloadDataMessage() {
		// int ddmLvl = addSubItem("downloadDataMessage");
		// DownloadDataBlock
		if (dsmccMsgHeader(0) != 0x1003)
			return; // its not a DDB message
		int moduleId = bw.pop16();
		Module m = moduleList.getById(moduleId);
		// addSubItem("moduleId: "+BitWise.toHex(moduleId), ddmLvl);
		// addSubItem("moduleVersion: "+BitWise.toHex(moduleVersion), ddmLvl);
		bw.pop();
		bw.pop(); // reserved
		int blockNumber = bw.pop16();

		if (m != null) {
			if (m.isComplete())
				return;
			int ddmLvl = addSubItem("downloadDataMessage", m.partLvl); //$NON-NLS-1$
			addSubItem("blockNumber: " + BitWise.toHex(blockNumber), ddmLvl); //$NON-NLS-1$
			moduleList.feedData(m, bw.buf, bw.getAbsolutePosition(), bw.getAvailableSize() + 1, blockNumber, ddmLvl);
		} else {
			moduleList.cacheData(moduleId, bw.buf, bw.getAbsolutePosition(), bw.getAvailableSize() + 1, blockNumber);
		}
	}

	int messageLength;

	private int dsmccMsgHeader(int ddmLvl) {
		// ARIB B24 vol 3 item 6.2.2
		// bw.printBuffer(bw.getAbsolutePosition(),
		// bw.getAbsolutePosition()+20);
		if (bw.pop() != 0x11) {
			System.out.println(Messages.getString("DSMCC.wrongproto")); //$NON-NLS-1$
			return -1;
		}
		if (bw.pop() != 0x03) {// 0x03 == download U-N
			System.out.println(Messages.getString("DSMCC.wrongtype")); //$NON-NLS-1$
			return -1;
		}
		int messageId = bw.pop16();
		// addSubItem("messageId: "+BitWise.toHex(messageId), ddmLvl);
		// System.out.println("messageId: "+BitWise.toHex(messageId));
		// transaction_id 32 uimsbf - 2x 16
		/* int transactionId = */bw.pop32();
		// System.out.println(BitWise.toHex(transactionId));
		// addSubItem("transaction_id: "+BitWise.toHex(downloadId), ddmLvl);
		bw.pop(); // reserved
		int adaptationLength = bw.pop();
		// System.out.println("adaptationLength: "+adaptationLength);
		messageLength = bw.pop16();
		// System.out.println("messageLength: "+messageLength);
		// addSubItem("messageLength: "+BitWise.toHex(messageLength), ddmLvl);
		if (adaptationLength > 0)
			bw.pop(adaptationLength);
		return messageId;
	}

	public int addSubItem(String msg, int parent) {
		return MainPanel.addTreeItem(msg, parent, MainPanel.DSMCC_TREE);
	}

	public int addSubItem(String msg) {
		return addSubItem(msg, 0);
	}
}
