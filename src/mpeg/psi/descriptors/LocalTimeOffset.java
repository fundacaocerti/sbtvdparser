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

import mpeg.psi.TOT;

public class LocalTimeOffset extends Descriptor {

	public static int tag = 0x58;

	static String name = "Local Time Offset Descriptor";

	public static String ratingTxt;

	public void printDescription() {
		// parental_rating_descriptor(){
		// descriptor_tag 8 Uimsbf
		// descriptor_length 8 Uimsbf
		int level = addSubItem(name, tableIndx);

		// for(i=0;i<N;i++){
		for (int i = 0; i < descriptor_length; i += 14) {
			// Contry_code 24 Bslbf
			addSubItem("country_code: " + (char) bw.pop() + (char) bw.pop() + (char) bw.pop(), level);
			// country_region_id 6
			addSubItem("country_region: " + bw.consumeBits(6), level);
			bw.consumeBits(1);
			// reserved 1
			// local_time_offset_polarity 1
			addSubItem("polarity: " + (bw.consumeBits(1) == 0 ? "positive" : "negative"), level);
			// local_time_offset 16
			addSubItem("offset: " + bw.consumeBits(4) + "" + bw.consumeBits(4) + "h " + bw.consumeBits(4) + ""
					+ bw.consumeBits(4) + "m", level);
			// time_of_change 40
			addSubItem("time of change: " + TOT.parseMJD(bw), level);
			// next_time_offset 16
			addSubItem("next time offset: " + bw.consumeBits(4) + "" + bw.consumeBits(4) + "h " + bw.consumeBits(4)
					+ "" + bw.consumeBits(4) + "m", level);
		}
	}
}
