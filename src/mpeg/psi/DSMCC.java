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
import dsmcc.Module;
import dsmcc.ModuleList;

public class DSMCC extends Table {
	
	static final int DII_MESSAGE = 0x1002, DDB_MESSAGE = 0x1003, DSI_MESSAGE = 0x1006;
	int totalLenght = 0, downloaded = 0;
	public int progressLvl = 0;
	ModuleList moduleList;

	public DSMCC(int pid) {
		id = 0x3c;
		this.pid = pid;
		name = "DSMCC";
		progressLvl = MainPanel.addTreeItem("parsing pid "+pid+" 0%", 0, MainPanel.DSMCC_TREE);
//		setLowLevelSearch(new byte[] {0x10, 0x02, (byte)0x80});
		moduleList = new ModuleList(this);
	}
	
	public void updateDlSize(int bytes) {
		totalLenght += bytes;
	}
	
	public void updateProgress(int bytes) {
		downloaded += bytes;
		if (totalLenght == 0)
			return;
		GuiMethods.runMethod(GuiMethods.CHANGEITEM,  
				new Object[] {"parsing pid "+pid+" "+
				(downloaded*100/totalLenght)+"%", new Integer(progressLvl)}, true);
	}

	public boolean printDescription(byte[] ba) {
		if (moduleList.isReadyToMount())
			return true;
		if (!verifyMultiSection(ba))
			return false;
//		printSectionInfo();
//		bw.printBuffer(0, 20);
//		addSubItem("pid: "+bw.toHex(pid));

		if (section_syntax_indicator == 0)
			System.out.println("checksum used");
		bw.mark();
		switch (readTableID) {
		case 0x3A:
			System.out.println("LLCSNAP();");//TCP/IP encapsulation
			break;
		case 0x3B:
//			System.out.println("\nDSI or DII");
//			bw.printBuffer(0, bw.getAvailableSize());
			userNetworkMessage();
			break;
		case 0x3C:
//			System.out.println("DDB");
			downloadDataMessage();
			break;
		case 0x3D:
			System.out.println("DSMCC_descriptor_list();");
			break;
		case 0x3E:
			System.out.println("DSMCC_private_data_byte();");
			bw.printBuffer(bw.getAbsolutePosition(), section_length-9);
			break;
		}
//		System.out.println("available: "+bw.getAvailableSize());
		return false;
	}
	
	void userNetworkMessage() {
//		System.out.println("net");
		int unmLvl = 0;
//		int unmLvl = addSubItem("userNetworkMessage");
//		System.out.print("userNetMsg: ");
//		System.out.println(" "+section_length+"b");
		int msgId = dsmccMsgHeader(unmLvl);
		if (msgId == 0x1002)
			downloadInfoIndication();
		if (msgId == 0x1006)
			downloadServerInitiate();
	}
	
	private void downloadServerInitiate() {
//		Server-ID
//		System.out.println("Server-ID: "+bw.getHexSequence(20));
		bw.pop(20);
//		compatibilityDescriptor()
		new Compatibility().parse(bw);
//		privateDataLength
		int privateDataLength = bw.pop16();
//		System.out.println("privateDataLength: "+bw.toHex(privateDataLength));
	}
	
	int lastSectionVersion = -1;
	private void downloadInfoIndication() {
		if (lastSectionVersion == versionNumber)
			return; //may cause data lost
		lastSectionVersion = versionNumber;
		int diiLvl = addSubItem("downloadInfoIndication", progressLvl);
		int downloadId = bw.pop16()<<16+bw.pop16();
		addSubItem("downloadId: "+bw.toHex(downloadId), diiLvl);
		int blockSize = bw.pop16();
		addSubItem("blockSize: "+bw.toHex(blockSize), diiLvl);
//		int windowSize = bw.pop();
//		addSubItem("windowSize: "+bw.toHex(windowSize), unmLvl);
//		int ackPeriod = bw.pop();
//		addSubItem("ackPeriod: "+bw.toHex(ackPeriod), unmLvl);
//		int tCDownloadWindow = bw.pop16()<<16+bw.pop16();
//		addSubItem("tCDownloadWindow: "+bw.toHex(tCDownloadWindow), unmLvl);
//		int tCDownloadScenario = bw.pop16()<<16+bw.pop16();
//		addSubItem("tCDownloadScenario: "+bw.toHex(tCDownloadScenario), unmLvl);
		System.out.println("00's: "+bw.getHexSequence(10));
//		compatibilityDescriptor()
		new Compatibility().parse(bw);
//		bw.pop16();
		int numberOfModules = bw.pop16();
		int moduleLvl = addSubItem("modules ("+numberOfModules+")", diiLvl);
		for(int i=0;i< numberOfModules;i++)
			moduleList.createModule(bw, moduleLvl);
		int privateDataLength = bw.pop16();
		if (privateDataLength > 0) {
			int pdLvl = addSubItem("privateData: "+bw.toHex(privateDataLength), diiLvl);
			addSubItem(bw.getHexSequence(privateDataLength), pdLvl);
		}
	}
	
	void downloadDataMessage() {
		int ddmLvl = 0;
//		int ddmLvl = addSubItem("downloadDataMessage");
		//DownloadDataBlock 
		if (dsmccMsgHeader(ddmLvl) != 0x1003)
			return; //its not a DDB message
		int moduleId = bw.pop16();
		Module m = moduleList.getById(moduleId);
		if (m == null || m.isComplete())
			return;
		ddmLvl = addSubItem("downloadDataMessage", 
				m.partLvl);
//		addSubItem("moduleId: "+bw.toHex(moduleId), ddmLvl);
		int moduleVersion = bw.pop();
//		addSubItem("moduleVersion: "+bw.toHex(moduleVersion), ddmLvl);
		bw.pop(); //reserved
		int blockNumber = bw.pop16();
		
		addSubItem("blockNumber: "+bw.toHex(blockNumber), ddmLvl);
		moduleList.feedData(moduleId, bw.buf, bw.getAbsolutePosition(),
				bw.getAvailableSize()+1, blockNumber, ddmLvl);
	}
	
	int messageLength;

	private int dsmccMsgHeader(int ddmLvl) {
		//ARIB B24 vol 3 item 6.2.2
//		bw.printBuffer(bw.getAbsolutePosition(), bw.getAbsolutePosition()+20);
		if (bw.pop() != 0x11) {
			System.out.println("DSMCC: wrong protocol");
			return -1;
		}
		if (bw.pop() != 0x03) {//0x03 == download U-N
			System.out.println("DSMCC: wrong type");
			return -1;
		}
		int messageId = bw.pop16();
//		addSubItem("messageId: "+bw.toHex(messageId), ddmLvl);
//		System.out.println("messageId: "+bw.toHex(messageId));
//		transaction_id 32 uimsbf - 2x 16
		int transactionId = bw.pop16()<<16 | bw.pop16();
//		System.out.println(bw.toHex(transactionId));
//		addSubItem("transaction_id: "+bw.toHex(downloadId), ddmLvl);
		bw.pop(); //reserved
		int adaptationLength = bw.pop();
//		System.out.println("adaptationLength: "+adaptationLength);
		messageLength = bw.pop16();
//		System.out.println("messageLength: "+messageLength);
//		addSubItem("messageLength: "+bw.toHex(messageLength), ddmLvl);
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
