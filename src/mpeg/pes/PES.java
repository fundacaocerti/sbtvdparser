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
import sys.BitWise;

public class PES {

	public String name = null;

	public int id = 0xFFFF, pid = -1, treeIndx, layer = 0, idLimit = 0xFFFF, crcFails = 0, pidAlt = -1,
			bufWriteIndx = 0, esId = 0, packetLenght = 0;

	BitWise bw;

	byte[] bigBuffer;

	protected int thisPacket;

	public void startPacket(byte[] source, int srcOffset, int writeSize) {
		// System.out.println("PES start: "+writeSize);
		bw = new BitWise(source);
		bw.setOffset(srcOffset);
		if (bw.pop() != 0 & bw.pop() != 0 & bw.pop() != 1) {
			System.out.println("   start prefix not found");
			return;
		}
		esId = bw.pop();
		if (esId != id) {
			int errMsg = addSubItem("ES content not recognized: TID = " + BitWise.toHex(esId));
			addSubItem("expected TID: " + BitWise.toHex(id) + " - " + name, errMsg);
			// addSubItem("content: ["+bw.getHexSequence(section_length)+"]",
			// errMsg);
			return;
		}
		packetLenght = bw.pop16();
		if (packetLenght > 10000 || packetLenght == 0) // my safety limit
			return;
		bigBuffer = new byte[packetLenght];
		bw = new BitWise(bigBuffer);
		bufWriteIndx = 0;
		feedPart(source, srcOffset + 6, writeSize - 6);
	}

	public void parse() {
		// System.out.println("PES complete");
		bw.printBuffer();
	}

	public void feedPart(byte[] source, int srcOffset, int writeSize) {
		// System.out.println("PES feed: "+writeSize);
		if (bigBuffer == null)
			return;
		if (bufWriteIndx == bigBuffer.length) {
			// System.out.println(" full");
			return;
		}
		// if (bufWriteIndx+writeSize > section_length+3)
		// writeSize = section_length - bufWriteIndx+3;
		// if (source.length - srcOffset < writeSize)
		// writeSize = source.length - srcOffset;
		// System.out.println("feed bs_"+bigBuffer.length+", ws_"+writeSize+",
		// bwi_"+bufWriteIndx
		// +", sle_"+source.length+", sof_"+srcOffset);
		if (bufWriteIndx + writeSize > bigBuffer.length)
			writeSize = bigBuffer.length - bufWriteIndx;
		if (srcOffset + writeSize > source.length)
			writeSize = source.length - srcOffset;
		System.arraycopy(source, srcOffset, bigBuffer, bufWriteIndx, writeSize);
		bufWriteIndx += writeSize;

		if (bufWriteIndx == bigBuffer.length)
			printHeader();
	}

	public void printHeader() {
		// bw.printBuffer();
		thisPacket = addSubItem("ES packet");
		addSubItem("packet_lenght: " + bigBuffer.length, thisPacket);
		parse();
	}

	// if (stream_id != program_stream_map
	// && stream_id != padding_stream
	// && stream_id != private_stream_2
	// && stream_id != ECM
	// && stream_id != EMM
	// && stream_id != program_stream_directory
	// && stream_id != DSMCC_stream
	// && stream_id != ITU-T Rec. H.222.1 type E stream) {
	public void parseExtHeader(int parent) {
		int hdr = addSubItem("header", parent);
		// '10' 2 bslbf
		if (bw.consumeBits(2) != 2)
			return;
		// System.out.println("PES_scrambling_control: "+bw.consumeBits(2));
		// System.out.println("PES_priority: "+bw.consumeBits(1));
		// System.out.println("data_alignment_indicator: "+bw.consumeBits(1));
		// System.out.println("copyright: "+bw.consumeBits(1));
		// System.out.println("original_or_copy: "+bw.consumeBits(1));
		bw.consumeBits(bw.remainingBits);

		int PTS_DTS_flags = bw.consumeBits(2);
		addSubItem("PTS_DTS_flags: " + bw.printBin(PTS_DTS_flags, 2), hdr);
		// System.out.println("PTS_DTS_flags: "+PTS_DTS_flags);
		// PTS_DTS_flags 2 bslbf

		addSubItem("ESCR_flag: " + bw.consumeBits(1), hdr);
		addSubItem("ES_rate_flag: " + bw.consumeBits(1), hdr);
		addSubItem("DSM_trick_mode_flag: " + bw.consumeBits(1), hdr);
		addSubItem("additional_copy_info_flag: " + bw.consumeBits(1), hdr);
		addSubItem("PES_CRC_flag: " + bw.consumeBits(1), hdr);
		boolean PES_extension_flag = bw.consumeBits(1) == 1;
		addSubItem("PES_extension_flag: " + PES_extension_flag, hdr);
		// bw.consumeBits(bw.remainingBits);

		int header_data_length = bw.pop();
		addSubItem("PES_header_data_length: " + header_data_length, hdr);
		bw.mark();
		// PES_header_data_length 8 uimsbf
		// if (PTS_DTS_flags = = '10') {
		if (PTS_DTS_flags == 2)
			addSubItem("PTS: " + PTS_DTS.parse(bw, 2) + "s", hdr);
		if (PTS_DTS_flags == 3) {
			addSubItem("PTS: " + PTS_DTS.parse(bw, 3) + "s", hdr);
			addSubItem("DTS: " + PTS_DTS.parse(bw, 1) + "s", hdr);
		}

		// if (ESCR_flag = = '1') {
		// reserved 2 bslbf
		// ESCR_base[32..30] 3 bslbf
		// marker_bit 1 bslbf
		// ESCR_base[29..15] 15 bslbf
		// marker_bit 1 bslbf
		// ESCR_base[14..0] 15 bslbf
		// marker_bit 1 bslbf
		// ESCR_extension 9 uimsbf
		// marker_bit 1 bslbf
		// }
		// if (ES_rate_flag = = '1') {
		// marker_bit 1 bslbf
		// ES_rate 22 uimsbf
		// marker_bit 1 bslbf
		// }
		// if (DSM_trick_mode_flag = = '1') {
		// trick_mode_control 3 uimsbf
		// if ( trick_mode_control = = fast_forward ) {
		// field_id 2 bslbf
		// intra_slice_refresh 1 bslbf
		// frequency_truncation 2 bslbf
		// }
		// else if ( trick_mode_control = = slow_motion ) {
		// rep_cntrl 5 uimsbf
		// }
		// else if ( trick_mode_control = = freeze_frame ) {
		// field_id 2 uimsbf
		// reserved 3 bslbf
		// }
		// else if ( trick_mode_control = = fast_reverse ) {
		// field_id 2 bslbf
		// intra_slice_refresh 1 bslbf
		// frequency_truncation 2 bslbf
		// else if ( trick_mode_control = = slow_reverse ) {
		// rep_cntrl 5 uimsbf
		// }
		// else
		// reserved 5 bslbf
		// }
		// if ( additional_copy_info_flag = = '1') {
		// marker_bit 1 bslbf
		// additional_copy_info 7 bslbf
		// }
		// if ( PES_CRC_flag = = '1') {
		// previous_PES_packet_CRC 16 bslbf
		// }
		// if ( PES_extension_flag = = '1') {
		// if (PES_extension_flag) {
		// boolean PES_private_data_flag = bw.consumeBits(1) == 1;
		// addSubItem("PES_private_data_flag: "+PES_private_data_flag);
		// addSubItem("pack_header_field_flag: "+bw.consumeBits(1));
		// addSubItem("program_packet_sequence_counter_flag:
		// "+bw.consumeBits(1));
		// addSubItem("P-STD_buffer_flag: "+bw.consumeBits(1));
		// bw.consumeBits(3);
		// addSubItem("PES_extension_flag_2: "+bw.consumeBits(1));
		// if (PES_private_data_flag)
		// bw.pop(16);
		// }
		// PES_private_data_flag 1 bslbf
		// pack_header_field_flag 1 bslbf
		// program_packet_sequence_counter_flag 1 bslbf
		// P-STD_buffer_flag 1 bslbf
		// reserved 3 bslbf
		// PES_extension_flag_2 1 bslbf
		// if ( PES_private_data_flag = = '1') {
		// PES_private_data 128 bslbf
		// }
		// if (pack_header_field_flag = = '1') {
		// pack_field_length 8 uimsbf
		// pack_header()
		// }
		// if (program_packet_sequence_counter_flag = = '1') {
		// marker_bit 1 bslbf
		// program_packet_sequence_counter 7 uimsbf
		// marker_bit 1 bslbf
		// MPEG1_MPEG2_identifier 1 bslbf
		// original_stuff_length 6 uimsbf
		// }
		// if ( P-STD_buffer_flag = = '1') {
		// '01' 2 bslbf
		// P-STD_buffer_scale 1 bslbf
		// P-STD_buffer_size 13 uimsbf
		// }
		// if ( PES_extension_flag_2 = = '1') {
		// marker_bit 1 bslbf
		// PES_extension_field_length 7 uimsbf
		// for (i = 0; i < PES_extension_field_length; i++) {
		// reserved 8 bslbf
		// }
		// }
		// }
		// for (i = 0; i < N1; i++) {
		// stuffing_byte 8 bslbf
		// }
		// for (i = 0; i < N2; i++) {
		// PES_packet_data_byte 8 bslbf
		// }
		// }
		bw.pop(header_data_length - bw.getByteCount());
		bw.remainingBits = 0;
		// while (bw.getByteCount() < header_data_length)
		// System.out.print(Integer.toHexString(bw.pop())+" ");
		// System.out.println();
	}

	public int addSubItem(String msg, int parent) {
		return MainPanel.addTreeItem(msg, parent);
	}

	public int addSubItem(String msg) {
		return MainPanel.addTreeItem(msg, treeIndx);
	}
}
