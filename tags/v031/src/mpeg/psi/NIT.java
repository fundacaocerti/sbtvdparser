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
//Network Information Table
package mpeg.psi;

import mpeg.psi.descriptors.DescriptorList;
import mpeg.sbtvd.SpecialSemantic;
import sys.BitWise;
import sys.Messages;

public class NIT extends Table {

	public NIT() {
		id = 0x40;
		pid = 0x10;
		name = "NIT actual"; //$NON-NLS-1$
	}

	public boolean printDescription(byte[] ba) {
		if (!verifyMultiSection(ba))
			return false;
		printSectionInfo();
		int netDescriptorsLenght = BitWise.stripBits(bw.pop16(), 12, 12);
		int netDescIndx = addSubItem(Messages.getString("NIT.netDescriptors") + netDescriptorsLenght + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		bw.mark();
		while ((bw.getByteCount() < netDescriptorsLenght) && (bw.getAvailableSize() > 0))
			DescriptorList.getInstance().print(bw, netDescIndx);

		int byteCount = 0;
		int tsLoopLenght = BitWise.stripBits(bw.pop16(), 12, 12);
		int tsLoopIndx = addSubItem(Messages.getString("NIT.tsLoop") + tsLoopLenght + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		while ((byteCount < tsLoopLenght) && (bw.getAvailableSize() > 0)) {
			// TS_id 16 uimsbf
			int tsIdIndx = addSubItem("TS_id: " + BitWise.toHex(bw.pop16()), tsLoopIndx); //$NON-NLS-1$
			int onid = bw.pop16();
			addSubItem("Original Net ID: " + SpecialSemantic.parseNetworkID(onid), tsIdIndx); //$NON-NLS-1$

			int tsDescriptorsLenght = BitWise.stripBits(bw.pop16(), 12, 12);
			byteCount += tsDescriptorsLenght + 6;
			bw.mark();
			int tsDescIndx = addSubItem(
					Messages.getString("NIT.tsDescriptors") + BitWise.toHex(tsDescriptorsLenght) + ")", tsIdIndx); //$NON-NLS-1$ //$NON-NLS-2$
			while ((bw.getByteCount() < tsDescriptorsLenght) && (bw.getAvailableSize() > 0))
				DescriptorList.getInstance().print(bw, tsDescIndx);
		}
		return true;
	}
}
