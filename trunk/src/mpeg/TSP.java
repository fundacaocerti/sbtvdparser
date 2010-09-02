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
//Transport Stream Packet
package mpeg;

import sys.BitWise;

public class TSP {

	public final static int TS_PACKET_LEN = 188;

	public final static int SYNC_BYTE = 0x47;

	// fields are listed in order of appearence
	// 4-byte prefix
	public static int transportErrorIndicator, // 1 bit
			payloadUnitStartIndicator, // 1 bit
			transportPriority, // 1 bit
			pid, // 13 bits Packet ID - one for each ES
			transportScramblingControl, // 2 bits lbf
			adaptationFieldControl, // 2 bits
			continuityCounter, // 4 bits uimsbf
			dataOffset = 0;

	static BitWise bw = new BitWise(null);

	public static void parse(byte[] content) {
		transportErrorIndicator = BitWise.stripBits(content[0], 8, 1);
		payloadUnitStartIndicator = BitWise.stripBits(content[0], 7, 1);
		transportPriority = BitWise.stripBits(content[0], 6, 1);
		pid = BitWise.stripBits(content[0], 5, 5) << 8;
		pid = pid | BitWise.toInt(content[1]);
		transportScramblingControl = BitWise.stripBits(content[2], 8, 2);
		adaptationFieldControl = BitWise.stripBits(content[2], 6, 2);
		continuityCounter = BitWise.stripBits(content[2], 4, 4);
		AdaptationField.lenght = 0;
		/*
		 * 00 Reserved for future use by ISO/IEC 01 No adaptation_field, payload
		 * only 10 Adaptation_field only, no payload 11 Adaptation_field
		 * followed by payload
		 */
		if (adaptationFieldControl == 2 || adaptationFieldControl == 3) {
			AdaptationField.parse(content, 4, pid);
		}
		if (adaptationFieldControl == 1 || adaptationFieldControl == 3) {
			dataOffset = 3 + AdaptationField.lenght;
		}
	}
}
