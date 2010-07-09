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

public class TerrestrialSystemDelivery extends Descriptor {

	public static int tag = 0xFA;

	static String name = "Terrestrial System Delivery";

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		addSubItem("descriptor_length: " + descriptor_length, level);

		// area_code 12 uimsbf
		int tmp = bw.pop16();
		addSubItem("area_code: " + BitWise.stripBits(tmp, 16, 12), level);

		// guard_interval 2 uimsbf
		addSubItem("guard_interval: 1/" + (32 >> (BitWise.stripBits(tmp, 4, 2))),
				level);

		// tx_mode 2 uimsbf
		addSubItem(
				"transmission_mode: mode " + (1 + (BitWise.stripBits(tmp, 2, 2))),
				level);

		// frequency loop
		for (int i = 2; i < descriptor_length; i += 2) {
			float fl = bw.pop16() / 7;
			int freq = Math.round(fl);
			fl = fl - 473 - 1 / 7;
			int chan = Math.round(fl / 6 + 14);
			addSubItem("frequency: " + freq + ".143 MHz - channel " + chan,
					level);
			// (473 + 6 x (X – 14) + 1/7) x 7 = xxx MHz
		}
	}
}
