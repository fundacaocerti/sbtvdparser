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
//Program Clock Reference
package mpeg;

import parsers.Packet;
import sys.BitWise;
import sys.Log;

public class PCR {
	// TS may have many, but only one per program
	private static PCR thisInstance = new PCR();

	public static double firstTimestamp = -1;
	static double lastTimeStamp = -1, averageBitrate = 30;

	static long firstPacketCount = 0, lastPacketCount = 0;

	static int id = 0;

	public static PCR getInstance() {
		return thisInstance;
	}

	public static float getAverageBitrate() {
		return (float) averageBitrate / 1000000;
	}

	public static float getCurrentTimestamp() {
		double bits = (Packet.packetCount - lastPacketCount) * Packet.realPktLenght * 8;
		if (firstTimestamp == lastTimeStamp)
			return 0;
		return (float) (bits / averageBitrate + (lastTimeStamp - firstTimestamp));
	}

	public void update(BitWise bw) {
		long pcr_base = bw.pop16();
		pcr_base = (pcr_base << 16) + bw.pop16();
		int pcr_extension = bw.pop16();
		pcr_base = (pcr_base << 1) + (pcr_extension & 0x80);
		pcr_extension = BitWise.stripBits(pcr_extension, 9, 9);
		long pcr = pcr_base * 300 + pcr_extension;
		// System.out.println(pcr+" - "+pcr_base+":"+pcr_extension);
		float timeStamp = ((float) pcr) / 27000000;
		// System.out.println(timeStamp);
		if (firstTimestamp == -1) {
			firstTimestamp = timeStamp;
			lastTimeStamp = timeStamp;
			firstPacketCount = Packet.packetCount;
			lastPacketCount = firstPacketCount;
		} else {
			if (timeStamp - lastTimeStamp > 2 || timeStamp - lastTimeStamp < 0) {
				lastTimeStamp = timeStamp;
				Log.printWarning("PCR err"); //$NON-NLS-1$
				Log.printWarning("lts: " + lastTimeStamp); //$NON-NLS-1$
				Log.printWarning("ts: " + timeStamp); //$NON-NLS-1$
				return;
			}
			lastTimeStamp = timeStamp;
			lastPacketCount = Packet.packetCount;
			averageBitrate = (float) (lastPacketCount - firstPacketCount) / (timeStamp - firstTimestamp)
					* Packet.realPktLenght * 8;
		}
	}
}
