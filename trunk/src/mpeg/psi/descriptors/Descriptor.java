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
package mpeg.psi.descriptors;

import gui.MainPanel;
import sys.BitWise;
import sys.Messages;

public class Descriptor {

	int[] predefinedTags;

	// TODO: add 0x06 data_stream_alignment / 0x0a ISO_639_lang._desc / 0x1c
	// MPEG4_audio
	/*
	 * Refactor the list to use tuples #define DESC_VIDEO_STREAM 2 #define
	 * DESC_AUDIO_STREAM 3 #define DESC_HIERARCHY 4 #define DESC_REGISTRATION 5
	 * #define DESC_DATA_STREAM_ALIGNMENT 6 #define DESC_TARGET_BACKGROUND_GRID
	 * 7 #define DESC_VIDEO_WINDOW 8 #define DESC_CA 9 #define
	 * DESC_ISO_639_LANGUAGE 10 #define DESC_SYSTEM_CLOCK 11 #define
	 * DESC_MULTIPLEX_BUFFER_UTILISATION 12 #define DESC_COPYRIGHT 13 #define
	 * DESC_MAXIMUM_BITRATE 14 #define DESC_PRIVATE_DATA_INDICATOR 15 #define
	 * DESC_SMOOTHING_BUFFER 16 #define DESC_STD 17 #define DESC_IBP 18
	 * 
	 * #define DESC_DIRAC_TC_PRIVATE 0xAC DVB tags #define DESC_DVB_NETWORK_NAME
	 * 0x40 #define DESC_DVB_SERVICE_LIST 0x41 #define DESC_DVB_STUFFING 0x42
	 * #define DESC_DVB_SATELLITE_DELIVERY_SYSTEM 0x43 #define
	 * DESC_DVB_CABLE_DELIVERY_SYSTEM 0x44 #define DESC_DVB_VBI_DATA 0x45
	 * #define DESC_DVB_VBI_TELETEXT 0x46 #define DESC_DVB_BOUQUET_NAME 0x47
	 * #define DESC_DVB_SERVICE 0x48 #define DESC_DVB_COUNTRY_AVAILABILITY 0x49
	 * #define DESC_DVB_LINKAGE 0x4A #define DESC_DVB_NVOD_REFERENCE 0x4B
	 * #define DESC_DVB_TIME_SHIFTED_SERVICE 0x4C #define DESC_DVB_SHORT_EVENT
	 * 0x4D #define DESC_DVB_EXTENDED_EVENT 0x4E #define
	 * DESC_DVB_TIME_SHIFTED_EVENT 0x4F #define DESC_DVB_COMPONENT 0x50 #define
	 * DESC_DVB_MOSAIC 0x51 #define DESC_DVB_STREAM_IDENTIFIER 0x52 #define
	 * DESC_DVB_CA_IDENTIFIER 0x53 #define DESC_DVB_CONTENT 0x54 #define
	 * DESC_DVB_PARENTAL_RATING 0x55 #define DESC_DVB_TELETEXT 0x56 #define
	 * DESC_DVB_TELEPHONE 0x57 #define DESC_DVB_LOCAL_TIME_OFFSET 0x58 #define
	 * DESC_DVB_SUBTITLING 0x59 #define DESC_DVB_TERRESTRIAL_DELIVERY_SYSTEM
	 * 0x5A #define DESC_DVB_MULTILINGUAL_NETWORK_NAME 0x5B #define
	 * DESC_DVB_MULTILINGUAL_BOUQUET_NAME 0x5C #define
	 * DESC_DVB_MULTILINGUAL_SERVICE_NAME 0x5D #define
	 * DESC_DVB_MULTILINGUAL_COMPONENT 0x5E #define DESC_DVB_PRIVATE_DATA 0x5F
	 * #define DESC_DVB_SERVICE_MOVE 0x60 #define
	 * DESC_DVB_SHORT_SMOOTHING_BUFFER 0x61 #define DESC_DVB_FREQUENCY_LIST 0x62
	 * #define DESC_DVB_PARTIAL_TRANSPORT_STREAM 0x63 #define
	 * DESC_DVB_DATA_BROADCAST 0x64 #define DESC_DVB_SCRAMBLING 0x65 #define
	 * DESC_DVB_DATA_BROADCAST_ID 0x66 #define DESC_DVB_TRANSPORT_STREAM 0x67
	 * #define DESC_DVB_DSNG 0x68 #define DESC_DVB_PDC 0x69 #define DESC_DVB_AC3
	 * 0x6A #define DESC_DVB_ANCILLARY_DATA 0x6B #define DESC_DVB_CELL_LIST 0x6C
	 * #define DESC_DVB_CELL_FREQUENCY_LINK 0x6D #define
	 * DESC_DVB_ANNOUNCEMENT_SUPPORT 0x6E #define
	 * DESC_DVB_APPLICATION_SIGNALLING 0x6F #define
	 * DESC_DVB_ADAPTATION_FIELD_DATA 0x70 #define DESC_DVB_SERVICE_IDENTIFIER
	 * 0x71 #define DESC_DVB_SERVICE_AVAILABILITY 0x72 #define
	 * DESC_DVB_DEFAULT_AUTHORITY 0x73 #define DESC_DVB_RELATED_CONTENT 0x74
	 * #define DESC_DVB_TVA_ID 0x75 #define DESC_DVB_CONTENT_IDENTIFIER 0x76
	 * #define DESC_DVB_TIMESLICE_FEC_IDENTIFIER 0x77 #define
	 * DESC_DVB_ECM_REPETITION_RATE 0x78 #define
	 * DESC_DVB_S2_SATELLITE_DELIVERY_SYSTEM 0x79 #define DESC_DVB_ENHANCED_AC3
	 * 0x7A #define DESC_DVB_DTS 0x7B #define DESC_DVB_AAC 0x7C
	 */

	String[] predefinedNames;

	String name = null;

	public int tag = 0xFF, parsedTag, tableIndx;

	int descriptor_length = 0;

	BitWise bw;

	public Descriptor() {
		predefinedTags = new int[] { 0x09, 0x0D, 0x13, 0x14, 0x15, 0x28, 0x2A, 0x40, 0x41, 0x42, 0x47, 0x48, 0x49,
				0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F, 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x58, 0x63, 0xC0, 0xC1, 0xC2,
				0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF, 0xD0, 0xD1, 0xD2, 0xD3,
				0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xE0, 0xF7, 0xF8, 0xFA, 0xFB, 0xFC,
				0xFD, 0xFE };
		predefinedNames = new String[] { "Conditional access", "Copyright", "Carousel ID", "Association tag",
				"Deferred association tags", "AVC vídeo", "AVC timing and HRD", "Network name", "Service list",
				"Stuffing", "Bouquet name", "Service", "Country availability", "Linkage", "NVOD reference",
				"Time shifted service", "Short event", "Extended event", "Time shifted event", "Component", "Mosaic",
				"Stream identifier", "CA identifier", "Content", "Parental rating", "Local time offset",
				"Partial transport stream", "Hierarchical transmission", "Digital copy control", "Network identifier",
				"Partial transport stream time", "Audio component", "Hyperlink", "Target area", "Data contents",
				"Video decode control", "Download content", "CA EMM TS", "CA contract information", "CA service",
				"TS information descriptior", "Extended broadcaster", "Logo transmission", "Basic local event",
				"Reference", "Node relation", "Short node information", "System time clock reference", "Series",
				"Event group", "SI parameter", "Broadcaster name", "Component group", "SI prime TS",
				"Board information", "LDT linkage", "Connected transmission", "Content availability", "Service group",
				"Carousel compatible composite", "Conditional playback", "Terrestrial delivery system",
				"Partial reception", "Emergency information", "Data component", "System management" };
	}

	public void setUp(int treeIndex, BitWise tableBw) {
		tableIndx = treeIndex;
		int startIndx = tableBw.getAbsolutePosition();
		// descriptor_tag 8 uimsbf
		parsedTag = tableBw.pop();
		addSubItem("descriptor_tag: " + BitWise.toHex(parsedTag), treeIndex);
		// descriptor_length 8 uimsbf
		descriptor_length = tableBw.pop();
		addSubItem("descriptor_length: " + BitWise.toHex(descriptor_length), treeIndex);
		
		tableBw.pop(descriptor_length);
		bw = tableBw.getCopy(startIndx, descriptor_length + 2);
		bw.pop(2);
	}

	public void print() {
		String descriptorName = Messages.getString("Descriptor.unknown"); //$NON-NLS-1$
		for (int i = 0; i < predefinedTags.length; i++)
			if (predefinedTags[i] == parsedTag)
				descriptorName = predefinedNames[i];
		descriptorName = checkRange(descriptorName);

		int descIndx = addSubItem(descriptorName + " descriptor", tableIndx);
		addSubItem("descriptor semantic unknown", descIndx);
		addSubItem("tag = " + BitWise.toHex(parsedTag), descIndx);
		addSubItem("content: " + bw.getHexSequence(descriptor_length), descIndx);
	}

	protected String checkRange(String descriptorName) {
		if (parsedTag > 0x79 && parsedTag < 0xC0)
			descriptorName = "Broadcaster defined";
		if (parsedTag > 0xE0 && parsedTag < 0xF7)
			descriptorName = "Reserved";
		return descriptorName;
	}

	public static int preparse(BitWise bw) {
		return bw.pop(0);
	}

	public int addSubItem(String msg, int parent) {
		int level = MainPanel.addTreeItem(msg, parent);
		if (parent == tableIndx)
			MainPanel.setTreeData(level, bw);
		return level;
	}

	public String toString() {
		return null;
	}
}
