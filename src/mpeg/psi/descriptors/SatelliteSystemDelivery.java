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

public class SatelliteSystemDelivery extends Descriptor {

	public static int tag = 0x43;
	/*
	 * satellite_delivery_system_descriptor(){descriptor_tag8 uimsbf
	 * descriptor_length8 uimsbf frequency32 bslbf orbital_position16 bslbf
	 * west_east_flag1 bslbf polarisation2 bslbf modulation5 bslbf symbol_rate28
	 * bslbf FEC_inner4 bslbf}
	 */

	static String name = "Satellite System Delivery";

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		addSubItem("descriptor_length: " + descriptor_length, level);
		String bcd = BitWise.toHex(bw.pop32());
		addSubItem("frequency: " + bcd.substring(2, 5) + "," + bcd.substring(5) + "GHz", level);
		bcd = BitWise.toHex(bw.pop16());
		addSubItem("orbital position: " + bcd.substring(2, 5) + "," + bcd.substring(5) + "⁰ "
				+ (bw.consumeBits(1) == 0 ? "west" : "east"), level);
		addSubItem(
				"polarization: "
						+ (new String[] { "linear - horizontal", "linear - vertical", "circular - left", "circular - right" })[bw
								.consumeBits(2)],
				level);
		addSubItem("roll off: " + (new String[] { "α = 0,35", "α = 0,25", "α = 0,20", "reserved" })[bw.consumeBits(2)],
				level);
		addSubItem("modulation system: " + (new String[] { "DVB-S", "DVB-S2" })[bw.consumeBits(1)], level);
		addSubItem("modulation type: " + (new String[] { "auto", "QPSK", "8PSK", "16-QAM" })[bw.consumeBits(2)], level);
		bcd = BitWise.toHex(bw.pop16() << 12 | bw.pop() << 4 | bw.consumeBits(4));
		addSubItem("symbol rate: " + bcd.substring(2, 4) + "," + bcd.substring(4) + "Msymbol/s", level);
		addSubItem("inner FEC: "
				+ (new String[] { "n/d", "1/2", "2/3", "3/4", "5/6", "7/8", "8/9", "3/5", "4/5", "9/10", "reserved",
						"reserved", "reserved", "reserved", "reserved", "disabled" })[bw.consumeBits(4)], level);
	}
}
