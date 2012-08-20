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
package mpeg;

import sys.BitWise;

public class AdaptationField {

	public static int lenght, // 8 uimsbf
			pcrPid = -1;

	public static void parse(byte[] content, int offset, int pid) {
		lenght = content[offset];
		if (lenght < 0)
			lenght += 256;
		if (lenght > 184)
			lenght = 0;
		if (pcrPid == pid) {
			BitWise bw = new BitWise(content);
			bw.setOffset(offset);
			bw.setBufferSize(lenght + offset + 1);
			boolean pcrFlag = BitWise.stripBits(bw.pop(), 5, 1) == 1;
			if (pcrFlag) {
				PCR.getInstance().update(bw);
			}
		}
	}

}
