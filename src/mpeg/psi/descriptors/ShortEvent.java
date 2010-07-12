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

public class ShortEvent extends Descriptor {

	public static int tag = 0x4d;

	public static String name = "Short event Descriptor", evtName, evtDesc;

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		// ISO_639_language_code 24 bslbf
		addSubItem(Component.parseISO639(bw), level);
		// event_name_length 8 uimsbf
		// for (i=0;i<event_name_length;i++){
		// event_name_char 8 uimsbf
		// }
		evtName = readText(bw);
		addSubItem("event_name: " + evtName, level);
		// text_length 8 uimsbf
		// for (i=0;i<text_length;i++){
		// text_char 8 uimsbf
		// }
		evtDesc = readText(bw);
		addSubItem("text: " + evtDesc, level);
	}

	public String readText(BitWise bw) {
		int textLenght = bw.pop();
		StringBuffer ts = new StringBuffer();
		while (textLenght > 0) {
			ts.append((char) bw.pop());
			textLenght--;
		}
		return ts.toString();
	}
}
