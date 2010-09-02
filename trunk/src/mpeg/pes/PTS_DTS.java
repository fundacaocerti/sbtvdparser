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
//Decoding Time Stamp
package mpeg.pes;

import sys.BitWise;

public class PTS_DTS {

	static float lastTS = 0;

	// generally units of 90KHz
	public static float parse(BitWise bw, int startCode) {
		// bw.printBuffer(bw.getAbsolutePosition(), bw.getAbsolutePosition()+5);
		// 40 bits
		// startcode
		if (startCode != bw.consumeBits(4))
			return -1;

		// PTS [32..30] 3 bslbf
		long pts = ((long) bw.consumeBits(3)) << 30;
		// marker_bit 1 bslbf
		if (bw.consumeBits(1) != 1)
			return -1;
		// PTS [29..15] 15 bslbf
		pts = pts | (long) bw.pop() << 22;
		pts = pts | (long) bw.consumeBits(7) << 15;
		// marker_bit 1 bslbf
		if (bw.consumeBits(1) != 1)
			return -1;

		// PTS [14..0] 15 bslbf
		pts = pts | (long) bw.pop() << 7;
		pts = pts | bw.consumeBits(7);
		// marker_bit 1 bslbf
		if (bw.consumeBits(1) != 1)
			return -1;
		float timestamp = (float) pts / 90000;
		if (lastTS == 0)
			lastTS = timestamp;
		return timestamp - lastTS;
	}
}
