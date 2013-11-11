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

public class ComponentName extends Descriptor {

	public static int tag = 0xa3;

	static String name = "Component Name Descriptor (ATSC)";

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		int string_count = bw.pop();
		level = addSubItem("string_count: " + string_count, level);
		while (string_count > 0) {
			int sx = addSubItem("str"+string_count+":", level);
			addSubItem(parseISO639(bw), sx);
			int number_segments = bw.pop();
			while (number_segments > 0) {
				int sgx = addSubItem("seg"+number_segments+":", sx);
				int compression_type = bw.pop();
				if (compression_type == 0)
					addSubItem("compression_type: No compression", sgx);
				else if (compression_type == 1)
					addSubItem("compression_type: Huffman (ATSC a65 table C4 and C5)", sgx);
				else if (compression_type == 2)
					addSubItem("compression_type: Huffman (ATSC a65 table C6 and C7)", sgx);
				else
					addSubItem("compression_type: proprietary", sgx);
				addSubItem("text_mode: "+BitWise.toHex(bw.pop()), sgx);
				addSubItem(parseText(bw), sgx);
				number_segments--;
			}
			string_count--;
		}
	}

	public static String parseISO639(BitWise bw) {
		char[] langCode = new char[3];
		for (int i = 0; i < 3; i++)
			langCode[i] = (char) bw.pop();
		return "ISO_639_language_code: " + String.valueOf(langCode);
	}

	public static String parseText(BitWise bw) {
		int string_len = bw.pop();
		StringBuffer sb = new StringBuffer();
		while (string_len > 0) {
			sb.append((char) bw.pop());
			string_len--;
		}
		return "text_description: " + sb.toString();
	}
}
