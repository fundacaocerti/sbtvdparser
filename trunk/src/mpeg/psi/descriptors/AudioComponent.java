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

import mpeg.psi.PMT;

public class AudioComponent extends Descriptor {

	public static int tag = 0xc4;

	static String name = "Audio Component Descriptor";

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		// data_component_descriptor(){
		// descriptor_tag 8 uimsbf
		// descriptor_length 8 uimsbf
		// reserved 4 uimsbf
		bw.consumeBits(4);
		// stream_content 4 uimsbf
		int tmp = bw.consumeBits(4);
		int cntlvl = addSubItem("stream_content: " + bw.toHex(tmp), level);
		if (tmp == 0x06)
			addSubItem("audio", cntlvl);
		// component_type 8
		//TODO: use table below
		addSubItem("component_type: " + bw.toHex(bw.pop()), level);
		// component_tag 8 
		addSubItem("component_tag: " + bw.toHex(bw.pop()), level);
		// stream_type 8
		addSubItem("stream_type: " + PMT.getStreamType(bw.pop()), level);
		// simulcast_group_tag 8
		addSubItem("simulcast_group_tag: " + bw.toHex(bw.pop()), level);
		// ES_multi_lingual_flag 1
		addSubItem("ES_multi_lingual_flag: " + bw.consumeBits(1), level);
		// main_component_flag 1
		addSubItem("main_component_flag: " + bw.consumeBits(1), level);
		// quality_indicator 2
		addSubItem("quality_indicator: mode " + bw.consumeBits(2), level);
		// sampling_rate 3
		String[] samp = {"not defined", "16 KHz", "22,05 KHz", "24 KHz", "?", 
				"32 KHz", "44,1 KHz", "48 KHz"}; 
		addSubItem("sampling_rate: " + samp[bw.consumeBits(3)], level);
		// reserved 1
		bw.consumeBits(1);
						
		// ISO_639_language_code
		addSubItem(Component.parseISO639(bw), level);
		// text
		addSubItem(Component.parseText(bw), level);
	}
 /*
0x00 Reservado para uso futuro
0x01 HE-AAC MPEG4 áudio, modo 1/0 (single mono)
0x02 HE-AAC MPEG4 áudio, modo 1/0 + 1/0 (dual mono)
0x03 HE-AAC MPEG4 áudio, modo 2/0 (estéreo)
0x04 HE-AAC MPEG4 áudio, modo 2/1
0x05 HE-AAC MPEG4 áudio, modo 3/0
0x06 HE-AAC MPEG4 áudio, modo 2/2
0x07 HE-AAC MPEG4 áudio, modo 3/1
0x08 HE-AAC MPEG4 áudio, modo 3/2
0x09 HE-AAC MPEG4 áudio, modo 3/2 + LFE
0x0a - 0x3f
0x40 HE-AAC MPEG4 descrição de pure audio para deficientes visuais
0x41 HE-AAC MPEG4 áudio com áudio elevado para deficientes auditivos
0x42 HE-AAC MPEG4 descrição de mixed audio para deficientes visuais
0x43 HE-AAC v2 MPEG4 áudio, modo 1/0 (mono)
0x44 HE-AAC v2 MPEG4 áudio, modo 2/0 (estéreo)
0x45 HE-AAC v2 MPEG4 descrição de pure audio para deficientes visuais
0x46 HE-AAC MPEG4 v2 áudio com áudio elevado para deficientes auditivos
0x47 HE-AAC MPEG4 v2 descrição de mixed audio para deficientes visuais
0x48 
0x49
0x50
0x51 AAC MPEG4 áudio, modo 1/0 (single mono)
0x52 AAC MPEG4 áudio, modo 1/0 + 1/0 (dual mono)
0x53 AAC MPEG4 áudio, modo 2/0 (estéreo)
0x54 AAC MPEG4 áudio, modo 2/1
0x55 AAC MPEG4 áudio, modo 3/0
0x56 AAC MPEG4 áudio, modo 2/2
0x57 AAC MPEG4 áudio, modo 3/1
0x58 AAC MPEG4 áudio, modo 3/2
0x59 AAC MPEG4 Audio, modo 3/2 + LFE
0x60 – 0x8E
0x9F AAC MPEG4 descrição de pure audio para deficientes visuais
0xA0 AAC MPEG4 áudio com áudio elevado para deficientes auditivos
0xA1 AAC MPEG4 descrição de mixed audio para deficientes visuais
0xA2...
  */

}
