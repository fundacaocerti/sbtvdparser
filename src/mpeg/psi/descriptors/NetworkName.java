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

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import sys.Log;

public class NetworkName extends Descriptor {

	public static int tag = 0x40;

	static String name = "Network Name Descriptor";

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		byte[] ba = new byte[descriptor_length];
		for (int i = 0; i < ba.length; i++) {
			ba[i] = (byte) bw.pop();
			// System.out.print(" "+bw.toHex(ba[i]));
		}
		try {
			InputStreamReader isr = new InputStreamReader(
					new ByteArrayInputStream(ba), "ISO8859_15_FDIS");
			char[] ca = new char[descriptor_length];
			isr.read(ca);
			addSubItem(new String(ca), level);
		} catch (Exception e) {
			Log.printStackTrace(e);
		}
	}
}
