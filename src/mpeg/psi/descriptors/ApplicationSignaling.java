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

public class ApplicationSignaling extends Descriptor {

	public static int tag = 0x6f;

	static String name = "Application Signaling Descriptor";

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		// application_signalling_descriptor() {
		// for( i=0; i<N; i++ ){
		for (int i = 0; i < descriptor_length; i += 3) {
			// application_type 16 uimsbf
			addSubItem("application_type: " + BitWise.toHex(bw.pop16()), level);
			// reserved_future_use 3 bslbf
			// AIT_version_number 5 uimsbf
			addSubItem("AIT_version_number: " + BitWise.toHex(BitWise.stripBits(bw.pop(), 5, 5)), level);
		}
	}
}
