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
package sys;

public class CRC16 {

	// fucking endianess
	public static int reverseInt(int b) {
		int r = 0;
		int m = 1;
		int bit;
		for (int i = 0; i < 16; i++) {
			bit = (b & m) >>> i;
			r = r | (bit << (15 - i));
			m = m << 1;
		}
		return r;
	}

	public static byte reverseByte(int b) {
		int r = 0;
		int m = 1;
		int bit;
		for (int i = 0; i < 8; i++) {
			bit = (b & m) >>> i;
			r = r | (bit << (7 - i));
			m = m << 1;
		}
		return (byte) (r & 0xff);
	}

	// someone puh-leaze optimize this
	public static int calc(byte[] ba, int offset, int count) {
		int crc = 0; // initial contents of LFBSR
		int poly = reverseInt(0x1021); // CRC16-CCITT polynomial
		int temp;

		for (int j = offset; j < count + offset; j++) {
			temp = (crc ^ reverseByte(ba[j])) & 0xff;

			// read 8 bits one at a time
			for (int i = 0; i < 8; i++) {
				if ((temp & 1) == 1)
					temp = (temp >>> 1) ^ poly;
				else
					temp = (temp >>> 1);
			}
			crc = (crc >>> 8) ^ temp;
		}
		return reverseInt(crc);
	}

	// someone puh-leaze optimize this
	public static int calcR(byte[] ba, int offset, int count) {
		int crc = 0; // initial contents of LFBSR
		int poly = 0x1021; // CRC16-CCITT polynomial
		int temp;

		for (int j = offset; j < count + offset; j++) {
			temp = (crc ^ ba[j]) & 0xff;

			// read 8 bits one at a time
			for (int i = 0; i < 8; i++) {
				if ((temp & 1) == 1)
					temp = (temp << 1) ^ poly;
				else
					temp = (temp << 1);
			}
			crc = (crc << 8) ^ temp;
		}
		return crc;
	}
}
