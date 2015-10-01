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
import mpeg.PCR;
import parsers.Packet;
import sys.BitWise;

public class PES {

	public String name = null;

	public int id = -1, pid = -1, treeIndx, layer = 0, idLimit = 0xFFFF, crcFails = 0, pidAlt = -1, bufWriteIndx = 0,
			esId = 0, packetLenght = 0;

	BitWise bw;

	protected byte[] bigBuffer;

	protected int thisPacket;

	public void startPacket(final byte[] source, final int srcOffset, final int writeSize) {
		bw = new BitWise(source);
		bw.setOffset(srcOffset);
		if (bw.pop() != 0 & bw.pop() != 0 & bw.pop() != 1) return;
		esId = bw.pop();
		if (id != -1 && esId != id) {
			final int errMsg = addSubItem("Conteúdo da ES não reconhecido: " + BitWise.toHex(esId)); //$NON-NLS-1$
			addSubItem("esID esperado: " + BitWise.toHex(id) + " - " + name, errMsg); //$NON-NLS-1$ //$NON-NLS-2$
			// addSubItem("content: ["+bw.getHexSequence(section_length)+"]",
			// errMsg);
			return;
		} // else addSubItem("ES_id: " + BitWise.toHex(esId));
		packetLenght = bw.pop16();
		if (packetLenght > 100000 || packetLenght == 0) // my safety limit
		return;
		bigBuffer = new byte[packetLenght];
		bw = new BitWise(bigBuffer);
		bufWriteIndx = 0;
		feedPart(source, srcOffset + 6, writeSize - 6);
	}

	public void parse() {
		parseExtHeader(thisPacket);
		bw.printBuffer();
	}

	public void feedPart(final byte[] source, final int srcOffset, int writeSize) {
		if (bigBuffer == null) return;
		if (bufWriteIndx == bigBuffer.length) return;
		if (bufWriteIndx + writeSize > bigBuffer.length) writeSize = bigBuffer.length - bufWriteIndx;
		if (srcOffset + writeSize > source.length) writeSize = source.length - srcOffset;
		try {
			System.arraycopy(source, srcOffset, bigBuffer, bufWriteIndx, writeSize);

			bufWriteIndx += writeSize;

			if (bufWriteIndx == bigBuffer.length) printHeader();
		} catch (final ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

	public void printHeader() {
		// bw.printBuffer();
		thisPacket = addSubItem("ES packet"); //$NON-NLS-1$
		addSubItem("packet_lenght: " + bigBuffer.length, thisPacket); //$NON-NLS-1$
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
	public void parseExtHeader(final int parent) {
		final int hdr = addSubItem("header", parent); //$NON-NLS-1$
		// '10' 2 bslbf
		if (bw.consumeBits(2) != 2) addSubItem("bad magic!", hdr);
		// System.out.println("PES_scrambling_control: "+bw.consumeBits(2));
		// System.out.println("PES_priority: "+bw.consumeBits(1));
		// System.out.println("data_alignment_indicator: "+bw.consumeBits(1));
		// System.out.println("copyright: "+bw.consumeBits(1));
		// System.out.println("original_or_copy: "+bw.consumeBits(1));
		bw.consumeBits(bw.remainingBits);

		final int PTS_DTS_flags = bw.consumeBits(2);
		addSubItem("PTS_DTS_flags: " + bw.printBin(PTS_DTS_flags, 2), hdr); //$NON-NLS-1$
		addSubItem("ESCR_flag: " + bw.consumeBits(1), hdr); //$NON-NLS-1$
		addSubItem("ES_rate_flag: " + bw.consumeBits(1), hdr); //$NON-NLS-1$
		addSubItem("DSM_trick_mode_flag: " + bw.consumeBits(1), hdr); //$NON-NLS-1$
		addSubItem("additional_copy_info_flag: " + bw.consumeBits(1), hdr); //$NON-NLS-1$
		addSubItem("PES_CRC_flag: " + bw.consumeBits(1), hdr); //$NON-NLS-1$
		final boolean PES_extension_flag = bw.consumeBits(1) == 1;
		addSubItem("PES_extension_flag: " + PES_extension_flag, hdr); //$NON-NLS-1$
		// bw.consumeBits(bw.remainingBits);

		final int header_data_length = bw.pop();
		addSubItem("PES_header_data_length: " + header_data_length, hdr); //$NON-NLS-1$
		bw.mark();
		// PES_header_data_length 8 uimsbf
		// if (PTS_DTS_flags = = '10') {
		if (PTS_DTS_flags == 2) addSubItem("PTS: " + PTS_DTS.parse(bw, 2) + "s", hdr); //$NON-NLS-1$ //$NON-NLS-2$
		if (PTS_DTS_flags == 3) {
			addSubItem("PTS: " + PTS_DTS.parse(bw, 3) + "s", hdr); //$NON-NLS-1$ //$NON-NLS-2$
			addSubItem("DTS: " + PTS_DTS.parse(bw, 1) + "s", hdr); //$NON-NLS-1$ //$NON-NLS-2$
		}
		addSubItem(PCR.getFormatedTimestamp(Packet.packetCount), hdr);
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

	public int addSubItem(final String msg, final int parent) {
		return MainPanel.addTreeItem(msg, parent);
	}

	public int addSubItem(final String msg) {
		return MainPanel.addTreeItem(msg, treeIndx);
	}
}
