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

import mpeg.psi.TOT;
import parsers.Packet;
import sys.BitWise;
import sys.Log;
import sys.Messages;

public class PCR {
	// TS may have many, but only one per program
	private static PCR thisInstance = new PCR();

	public static double firstTimestamp = -1;
	static double lastTimeStamp = -1;
	static double[] smoothAvgBitrate;

	static long firstPacketCount = 0, lastPacketCount = 0, lastpcr = 0, pcrWrap = 0;

	private PCR() {
		smoothAvgBitrate = new double[3];
		smoothAvgBitrate[0] = 0;// Mbps
		smoothAvgBitrate[1] = 0;
		smoothAvgBitrate[2] = 30000000;
	}

	public static PCR getInstance() {
		return thisInstance;
	}

	public static float getAverageBitrate() {
		return (float) smoothAvgBitrate[2] / 1000000;
	}

	public static double getTimestamp(long packetNumber) {
		return packetNumber * Packet.realPktLenght * 8 / smoothAvgBitrate[2];
	}

	public static String getFormatedTimestamp(long packetNumber) {
		if (lastTimeStamp == -1)
			return Messages.getString("PCR.pktTimestamp") + Long.toString(packetNumber); //$NON-NLS-1$
		double sec = getTimestamp(packetNumber);
		String seconds = Double.toString(sec) + "000"; //$NON-NLS-1$
		seconds = seconds.substring(0, seconds.indexOf('.') + 4);
		String utcTime = TOT.getTimeStamp(sec);
		if (utcTime != null)
			return Messages.getString("PCR.realTimestamp") + " " + seconds + "s (" + utcTime + " UTC-3)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return Messages.getString("PCR.offsetTimestamp") + " " + seconds + "s"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static float getCurrentTimestamp() {
		double bits = (Packet.packetCount - lastPacketCount) * Packet.realPktLenght * 8;
		if (firstTimestamp == lastTimeStamp)
			return 0;
		float currentTs = (float) (bits / smoothAvgBitrate[2] + (lastTimeStamp - firstTimestamp));
		if (currentTs < 0 || currentTs > 60 * 60 * 24)
			currentTs = 0;
		return currentTs;
	}

	public void update(BitWise bw) {
		long pcr_base = bw.pop16();
		pcr_base = (pcr_base << 16) + bw.pop16();
		int pcr_extension = bw.pop16();
		pcr_base = ((pcr_base << 1) | (pcr_extension & 0x80));
		if (pcr_base < 1e4 && lastpcr > 0x1ffff0000l) {// PCR wrap
			Log.printWarning("PCR wrapped: " + getFormatedTimestamp(Packet.packetCount)); //$NON-NLS-1$
			pcrWrap += 0x1ffffffffl;
		}
		lastpcr = pcr_base;
		pcr_base += pcrWrap;
		// The first 33 bits are based on a 90 kHz clock. The last 9 are based
		// on a 27 MHz clock.
		pcr_extension = BitWise.stripBits(pcr_extension, 9, 9);
		long pcr = pcr_base * 300 + pcr_extension;// ticks of a 27MHz clock
		double timeStamp = ((double) pcr) / 27000000;
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
			// double currentBitrate = (Packet.packetCount - lastPacketCount) /
			// (timeStamp - lastTimeStamp)
			// * Packet.realPktLenght * 8;
			lastTimeStamp = timeStamp;
			lastPacketCount = Packet.packetCount;
			double avgBitrate = (lastPacketCount - firstPacketCount) / (timeStamp - firstTimestamp)
					* Packet.realPktLenght * 8;
			if (smoothAvgBitrate[0] == 0) {
				smoothAvgBitrate[0] = avgBitrate;
				smoothAvgBitrate[1] = avgBitrate;
			}
			smoothAvgBitrate[0] = smoothAvgBitrate[1];
			smoothAvgBitrate[1] = smoothAvgBitrate[2];
			smoothAvgBitrate[2] = smoothAvgBitrate[0] * 0.475 + smoothAvgBitrate[1] * 0.475 + avgBitrate * 0.05;
			/*
			 * TODO: make some PCR jitter tests with gnuplot plot 'data.txt'
			 * using 0:2 "%lf %lf %lf\n" with lines, 'data.txt' using 0:3
			 * "%lf %lf %lf\n" with lines System.out.print(currentBitrate);
			 * System.out.print(';'); System.out.print(avgBitrate);
			 * System.out.print(';'); System.out.println(smoothAvgBitrate[2]);
			 */
		}
	}
}
