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

import sys.BitWise;

public class Component extends Descriptor {

	public static int tag = 0x50;

	static String name = "Component Descriptor";

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		int streamContent = BitWise.stripBits(bw.pop(), 4, 4);
		addSubItem("stream_content: " + BitWise.toHex(streamContent), level);
		int componentType = bw.pop();
		addSubItem("component_type: " + BitWise.toHex(componentType), level);

		int componentTag = bw.pop();
		addSubItem("component_tag: " + BitWise.toHex(componentTag), level);
		// component_tag: This 8-bit field has the same value as the
		// component_tag field in the stream identifier descriptor (if
		// present in the PSI program map section) for the component stream.
		addSubItem(parseISO639(bw), level);
		addSubItem(parseText(bw), level);
	}

	public static String parseISO639(BitWise bw) {
		char[] langCode = new char[3];
		for (int i = 0; i < 3; i++)
			langCode[i] = (char) bw.pop();
		return "ISO_639_language_code: " + String.valueOf(langCode);
	}

	public static String parseText(BitWise bw) {
		StringBuffer sb = new StringBuffer();
		while (bw.getAvailableSize() > 0)
			sb.append((char) bw.pop());
		return "text_description: " + sb.toString();
	}
}

/*
 * 0x00 0x00 – 0xFF Reservado para uso futuro 0x01 0x00 Reservado para uso
 * futuro 0x01 0x01 MPEG2 vídeo 480i(525i), relação de aspecto 4:3 0x01 0x02
 * MPEG2 vídeo 480i(525i), relação de aspecto 16:9 com vetor de pan 0x01 0x03
 * MPEG2 vídeo 480i(525i), relação de aspecto 16:9 sem vetor de pan 0x01 0x04
 * MPEG2 vídeo 480i(525i), > relação de aspecto 16:9 0x01 0x05 – 0xA0 Reservado
 * para uso futuro 0x01 0xA1 MPEG2 vídeo 480p(525p), relação de aspecto 4:3 0x01
 * 0xA2 MPEG2 vídeo 480p(525p), relação de aspecto 16:9 com vetor de pan 0x01
 * 0xA3 MPEG2 vídeo 480p(525p), relação de aspecto 16:9 sem vetor de pan 0x01
 * 0xA4 MPEG2 vídeo 480p(525p), > relação de aspecto 16:9 0x01 0xA5 -0xB0
 * Reservado para uso futuro 0x01 0xB1 MPEG2 vídeo 1080i(1125i), relação de
 * aspecto 4:3 0x01 0xB2 MPEG2 vídeo 1080i(1125i), relação de aspecto 16:9 com
 * vetor de pan 0x01 0xB3 MPEG2 vídeo 1080i(1125i), relação de aspecto 16:9 sem
 * vetor de pan 0x01 0xB4 MPEG2 vídeo 1080i(1125i), > relação de aspecto 16:9
 * 0x01 0xB5 – 0xC0 Reservado para uso futuro 0x01 0xC1 MPEG2 vídeo 720p(750p),
 * relação de aspecto 4:3 0x01 0xC2 MPEG2 vídeo 720p(750p), relação de aspecto
 * 16:9 com vetor de pan 0x01 0xC3 MPEG2 vídeo 720p(750p), relação de aspecto
 * 16:9 semvetor de pan 0x01 0xC4 MPEG2 vídeo 720p(750p), > relação de aspecto
 * 16:9 0x01 0xC5- 0xD0 Reservado para uso futuro 0x01 0xD1 MPEG2 vídeo 240p,
 * relação de aspecto 4:3 0x01 0xD2 MPEG2 vídeo 240p, relação de aspecto 4:3 com
 * vetor de pan 0x01 0xD3 MPEG2 vídeo 240p, relação de aspecto 4:3, sem vetor de
 * pan 0x01 0xD4 MPEG2 vídeo 240p, > relação de aspecto 16:9 0x01 0xD5- 0xE0
 * Reservado para uso futuro 0x01 0xE1 MPEG2 vídeo 1080p(1125p), relação de
 * aspecto 4:3 0x01 0xE2 MPEG2 vídeo 1080p(1125p), relação de aspecto 16:9 com
 * vetor de pan 0x01 0xE3 MPEG2 vídeo 1080p(1125p), relação de aspecto 16:9 sem
 * vetor de pan 0x01 0xE4 MPEG2 vídeo 1080p(1125p), > relação de aspecto 16:9
 * 0x01 0xE5 – 0xFF Reservado para uso futuro 0x02 0x00 Reservado para uso
 * futuro 0x02 0x01 AAC MPEG2 áudio, modo 1/0 (single mono) 0x02 0x02 AAC MPEG2
 * áudio, modo 1/0 + 1/0 (dual mono) 0x02 0x03 AAC MPEG2 áudio, modo 2/0
 * (estéreo) 0x02 0x04 AAC MPEG2 áudio, modo 2/1 0x02 0x05 AAC MPEG2 áudio, modo
 * 3/0 0x02 0x06 AAC MPEG2 áudio, modo 2/2 0x02 0x07 AAC MPEG2 áudio, modo 3/1
 * 0x02 0x08 AAC MPEG2 áudio, modo 3/2 0x02 0x09 AAC MPEG2 áudio, modo 3/2 + LFE
 * 0x02 0x0A – 0x3F Reservado para uso futuro 0x02 0x40 AAC MPEG2 descrição de
 * áudio para deficientes visuais 0x02 0x41 AAC MPEG2 áudio com áudio elevado
 * para deficientes auditivos 0x02 0x42 - OxAF Reservado para uso futuro 0x02
 * OxBO-OxFE Definido pelo usuário 0x02 0xFF Reservado para uso futuro 0x03 –
 * Ox04 0x00 – 0xFF Reservado para uso futuro 0x05 0x00 Reservado para uso
 * futuro 0x05 0x01 H264/AVC vídeo 480i(525i), relação de aspecto 4:3 0x05 0x02
 * H264/AVC vídeo 480i(525i), relação de aspecto 16:9 com vetor de pan 0x05 0x03
 * H264/AVC vídeo 480i(525i), relação de aspecto 16:9 sem vetor de pan 0x05 0x04
 * H264/AVC vídeo 480i(525i), > relação de aspecto 16:9 0x05 0x05 – 0xA0
 * Reservado para uso futuro 0x05 0xA1 H264/AVC vídeo 480p(525p), relação de
 * aspecto 4:3 0x05 0xA2 H264/AVC vídeo 480p(525p), relação de aspecto 16:9, com
 * vetor de pan 0x05 0xA3 H264/AVC vídeo 480p(525p), relação de aspecto 16:9,
 * sem vetor de pan 0x05 0xA4 H264/AVC vídeo 480p(525p), > relação de aspecto
 * 16:9 0x05 0xA5 -0xB0 Reservado para uso futuro 0x05 0xB1 H264/AVC vídeo
 * 1080i(1125i), relação de aspecto 4:3 0x05 0xB2 H264/AVC vídeo 1080i(1125i),
 * relação de aspecto 16:9, com vetor de pan 0x05 0xB3 H264/AVC vídeo
 * 1080i(1125i), relação de aspecto 16:9, sem vetor de pan 0x05 0xB4 H264/AVC
 * vídeo 1080i(1125i), > relação de aspecto 16:9 0x05 0xB5 – 0xC0 Reservado para
 * uso futuro 0x05 0xC1 H264/AVC vídeo 720p(750p), relação de aspecto 4:3 0x05
 * 0xC2 H264/AVC vídeo 720p(750p), relação de aspecto 16:9, com vetor de pan
 * 0x05 0xC3 H264/AVC vídeo 720p(750p), relação de aspecto 16:9, sem vetor de
 * pan 0x05 0xC4 H264/AVC vídeo 720p(750p), > relação de aspecto 16:9 0x05 0xC5-
 * 0xD0 Reservado para uso futuro 0x05 0xD1 H264/AVC vídeo 240p, relação de
 * aspecto 4:3 0x05 0xD2 H264/AVC vídeo 240p, relação de aspecto 16:9 com vetor
 * de pan 0x05 0xD3 H264/AVC vídeo 240p, relação de aspecto 16:9 sem vetor de
 * pan 0x05 0xD4 H264/AVC vídeo 240p, > relação de aspecto 16:9 0x05 0xD5- 0xE0
 * Reservado para uso futuro 0x05 0xE1 H264/AVC vídeo 1080p(1125p), relação de
 * aspecto 4:3 0x05 0xE2 H264/AVC vídeo 1080p(1125p), relação de aspecto 16:9
 * com vetor de pan 0x05 0xE3 H264/AVC vídeo 1080p(1125p), relação de aspecto
 * 16:9 sem vetor de pan 0x05 0xE4 H264/AVC vídeo 1080p(1125p), > relação de
 * aspecto 16:9 0x05 0xE5 – 0xFF Reservado para uso futuro 0x06 0x00 Reservado
 * para uso futuro 0x06 0x01 HE-AAC MPEG4 áudio, modo 1/0 (single mono) 0x06
 * 0x02 HE-AAC MPEG4 áudio, modo 1/0 + 1/0 (dual mono) 0x06 0x03 HE-AAC MPEG4
 * áudio, modo 2/0 (estéreo) 0x06 0x04 HE-AAC MPEG4 áudio, modo 2/1 0x06 0x05
 * HE-AAC MPEG4 áudio, modo 3/0 0x06 0x06 HE-AAC MPEG4 áudio, modo 2/2 0x06 0x07
 * HE-AAC MPEG4 áudio, modo 3/1 0x06 0x08 HE-AAC MPEG4 áudio, modo 3/2 0x06 0x09
 * HE-AAC MPEG4 áudio, modo 3/2 + LFE 0x06 0x0A – 0x3F Reservado para uso futuro
 * 0x06 0x40 HE-AAC MPEG4 descrição de pure audio para deficientes visuais 0x06
 * 0x41 HE-AAC MPEG4 áudio com áudio elevado para deficientes auditivos 0x06
 * 0x42 HE-AAC MPEG4 descrição de mixed áudio para deficientes visuais 0x06 0x43
 * HE-AAC v2 MPEG4 áudio, modo 1/0 (mono) 0x06 0x44 HE-AAC v2 MPEG4 áudio, modo
 * 2/0 (estéreo) 0x06 0x45 HE-AAC v2 MPEG4 descrição de pure audio para
 * deficientes visuais 0x06 0x46 HE-AAC MPEG4 v2 áudio com áudio elevado para
 * deficientes auditivos 0x06 0x47 HE-AAC MPEG4 v2 descrição de mixed audio para
 * deficientes visuais 0x06 0x48– 0x50 Reservado para uso futuro 0x06 0x51 AAC
 * MPEG4 áudio, modo 1/0 (single mono) 0x06 0x52 AAC MPEG4 áudio, modo 1/0 + 1/0
 * (dual mono) 0x06 0x53 AAC MPEG4 áudio, modo 2/0 (estéreo) 0x06 0x54 AAC MPEG4
 * áudio, modo 2/1 0x06 0x55 AAC MPEG4 áudio, modo 3/0 0x06 0x56 AAC MPEG4
 * áudio, modo 2/2 0x06 0x57 AAC MPEG4 áudio, modo 3/1 0x06 0x58 AAC MPEG4
 * áudio, modo 3/2 0x06 0x59 AAC MPEG4 áudio, modo 3/2 + LFE 0x06 0x60 – 0x8E
 * Reservado para uso futuro 0x06 0x9F AAC MPEG4 descrição de pure audio para
 * deficientes visuais 0x06 0xA0 AAC MPEG4 áudio com áudio elevado para
 * deficientes auditivos 0x06 0xA1 AAC MPEG4 descrição de mixed audio para
 * deficientes visuais 0x06 0xA2 – 0xA9 Reservado para uso futuro 0x06 0xB0-0xFE
 * Definido pelo usuário 0x06 0xFF Reservado para uso futuro 0x07 – 0x0F 0x00 –
 * 0xFF Reservado para uso futuro
 */
