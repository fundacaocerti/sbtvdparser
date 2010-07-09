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

public class ParentalRating extends Descriptor {

	public static int tag = 0x55;

	static String name = "Parental Rating Descriptor";

	public static String ratingTxt;

	public void printDescription() {
		// parental_rating_descriptor(){
		// descriptor_tag 8 Uimsbf
		// descriptor_length 8 Uimsbf
		int level = addSubItem(name, tableIndx);
		// for(i=0;i<N;i++){
		for (int i = 0; i < descriptor_length; i += 4) {
			// Contry_code 24 Bslbf
			addSubItem("country_code: " + (char) bw.pop() + (char) bw.pop()
					+ (char) bw.pop(), level);
			// rating 8
			int rating = bw.pop();
			String content = "";
			if (BitWise.stripBits(rating, 5, 1) == 1)
				content += " Drugs";
			if (BitWise.stripBits(rating, 6, 1) == 1)
				content += " Violence";
			if (BitWise.stripBits(rating, 7, 1) == 1)
				content += " Sex";
			String[] age = { "invalid", "Free", "10 years", "12 years",
					"14 years", "16 years", "18 years", "invalid" };
			ratingTxt = age[BitWise.stripBits(rating, 3, 3)] + " - " + content;
			addSubItem("rating: " + ratingTxt, level);
		}
	}
}
