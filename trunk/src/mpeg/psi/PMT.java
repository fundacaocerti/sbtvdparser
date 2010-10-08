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

import gui.GuiMethods;
import mpeg.AdaptationField;
import mpeg.pes.CC;
import mpeg.pes.PESList;
import mpeg.psi.descriptors.DescriptorList;
import mpeg.psi.descriptors.StreamIdentifier;
import mpeg.psi.descriptors.TSinformation;
import sys.BitWise;
import sys.PIDStats;

public class PMT extends Table {

	public PMT(int pid) {
		this.pid = pid;
		id = 0x02;
		name = "PMT (pid " + Integer.toHexString(pid) + ")";
	}

	public boolean printDescription(byte[] ba) {
		if (!verifyMultiSection(ba))
			return false;
		printSectionInfo();
		addSubItem("PID: " + BitWise.toHex(pid));
		int svcIdLevel = addSubItem("service_id: " + BitWise.toHex(idExt));
		name = "PMT (service_id: " + BitWise.toHex(idExt) + ")";
		GuiMethods.runMethod(GuiMethods.CHANGEITEM, new Object[] { name, new Integer(treeIndx) }, true);
		PIDStats.setIdentification(pid, name);
		addSubItem("type: " + TSinformation.svcTypes[BitWise.stripBits(idExt, 5, 2)], svcIdLevel);
		addSubItem("number: " + (BitWise.stripBits(idExt, 3, 3) + 1), svcIdLevel);
		// reserved 3 - não tá na norma
		// PCR_PID 13 uimsbf
		int pcrPid = BitWise.stripBits(bw.pop16(), 13, 13);
		addSubItem("PCR_PID: " + BitWise.toHex(pcrPid), svcIdLevel);
		PIDStats.setIdentification(pcrPid, "prog. " + BitWise.toHex(idExt) + " PCR");
		if (AdaptationField.pcrPid == -1)
			AdaptationField.pcrPid = pcrPid;
		// Reserved 4 bslbf
		// program_info_length 12 uimsbf
		int programInfoLength = BitWise.stripBits(bw.pop16(), 12, 12);
		int programInfoLevel = addSubItem("program info descriptors: (lenght " + programInfoLength + ")", svcIdLevel);
		bw.mark();
		// for(i=0,i<N,i++){ uimsbf
		// descriptor()
		while ((bw.getByteCount() < programInfoLength) && (bw.getAvailableSize() > 0)) {
			DescriptorList.print(bw, programInfoLevel);
		}

		int esLoopLevel = addSubItem("elementary streams:", svcIdLevel);
		// for(i=0,i<N1,i++){
		while (bw.getAvailableSize() > 0) {
			// stream_type 8 uimsbf
			// 0x1C- 0x7D Não definido
			// 0x80-0xFF Uso privado
			int streamType = bw.pop();
			String streamDesc = getStreamType(streamType);

			// Reserved 3 bslbf
			// elementary_PID 13 uimsbf
			int esPid = BitWise.stripBits(bw.pop16(), 13, 13);
			if (streamType == 0x0b || streamType == 0x0d)
				TableList.addTable(new DSMCC(esPid));
			if (streamType == 0x05)
				TableList.addTable(new AIT(esPid));
			int esInfoLevel = addSubItem("ES_PID: " + BitWise.toHex(esPid) + "   type: " + BitWise.toHex(streamType)
					+ "- " + streamDesc, esLoopLevel);
			// Reserved 4 bslbf
			// ES_info_length 12 uimsbf
			int esInfoLenght = BitWise.stripBits(bw.pop16(), 12, 12) + bw.getByteCount();
			// for(i=0,i<N2,i++){
			// Descriptor()
			while ((bw.getByteCount() < esInfoLenght) && (bw.getAvailableSize() > 0)) {
				DescriptorList.print(bw, esInfoLevel);
			}
			if (StreamIdentifier.cTag == 0x30)
				PESList.addElementaryStream(new CC(esPid));
			if (StreamIdentifier.cTag != -1)
				PIDStats.setIdentification(esPid, "prog. " + BitWise.toHex(idExt) + " "
						+ StreamIdentifier.getType(StreamIdentifier.cTag));
			else
				PIDStats.setIdentification(esPid, "prog. " + BitWise.toHex(idExt) + " " + streamDesc);
			StreamIdentifier.cTag = -1;
		}
		return true;
	}

	public static String getStreamType(int streamType) {
		String streamDesc;
		if (streamType > 0x1b && streamType < 0x7e)
			streamDesc = streamTypes[0];
		else if (streamType > 0x7F)
			streamDesc = "private use";
		else
			streamDesc = streamTypes[streamType];
		return streamDesc;
	}

	static String[] streamTypes = { "Undefined", "ISO/IEC 11172-2 Video", "H.262 Video", "ISO/IEC 11172-3 Audio",
			"ISO/IEC 13818-3 Audio", "ITU-T Rec. H.222.0 | ISO/IEC 13818-1 private_sections",
			"ITU-T Rec. H.222.0 | ISO/IEC 13818-1 PES with private data", "ISO/IEC 13522-5 MHEG",
			"H222.0:2002, Annex 1", "H.222.1", "ISO/IEC 13818-6 DSM-CC (type A)", "ISO/IEC 13818-6 DSM-CC (type B)",
			"ISO/IEC 13818-6 DSM-CC (type C)", "ISO/IEC 13818-6 DSM-CC (type D)", "H222.0 auxiliary data",
			"ISO/IEC 13818-7 Audio (ADTS transport syntax)", "ISO/IEC 14496-2", "ISO/IEC 14496-3 Audio",
			"ISO/IEC 14496-1 SL (FlexMux over PES)", "ISO/IEC 14496-1 SL (PES or FlexMux over ISO/IEC 14496)",
			"ISO/IEC 13818-6 download", "Meta data PES", "Meta data over metadata_sections",
			"Meta data over ISO/IEC 13818-6 carroussel", "Meta data over ISO/IEC 13818-6 object carroussel",
			"Meta data over ISO/IEC 13818-6 download", "ISO/IEC 13818-11 IPMP stream",
			"H.264 - ISO/IEC 14496-10 Video", "Data pipe", "IPMP stream" };
}
