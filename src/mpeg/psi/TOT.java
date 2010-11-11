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
//Time Offset Table;
package mpeg.psi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import mpeg.PCR;
import mpeg.psi.descriptors.DescriptorList;
import parsers.Packet;
import sys.BitWise;

public class TOT extends Table {

	static int currentTimeStamp, lastTimeStamp, totsParsed = 0;
	// reset de todas as tabelas

	static long lastpacketCounter = 0, totPacketCounter = 0;

	public static float lastBitrate = 0;

	public TOT() {
		id = 0x73;
		pid = 0x14;
		name = "TOT";
	}

	public static void reset() {
		totsParsed = 0;
		lastpacketCounter = 0;
		totPacketCounter = 0;
		lastBitrate = 0;
	}

	public boolean printDescription(byte[] ba) {
		if (!verifySection(ba))
			return false;
		if (totPacketCounter == 0)
			totPacketCounter = Packet.packetCount;
		// time_offset_section(){
		// UTC-3_time 40 bslbf
		ts = parseMJD(bw);
		String timestamp = formatMJD(ts);
		calculateTsBitrate();
		totsParsed++;
		if (totsParsed > 1) {
			bw.pop(bw.getAvailableSize());
			return false;
		}

		printBasicInfo();
		addSubItem("UTC-3_time: " + timestamp);
		// reserved 4 bslbf
		bw.consumeBits(4);
		// descriptors_loop_length 12 uimsbf
		int descriptorsLenght = bw.consumeBits(12);
		int descLevel = addSubItem("Descriptors loop:");
		int mark = bw.getByteCount();
		// for(j=0;j<N;j++){
		// Descriptor()
		while ((bw.getByteCount() - mark < descriptorsLenght) && (bw.getAvailableSize() > 0)) {
			DescriptorList.getInstance().print(bw, descLevel);
		}
		return false;
	}

	private void calculateTsBitrate() {
		if (lastpacketCounter != 0) {
			long packetsFromLastTOT = Packet.packetCount - lastpacketCounter;
			int secondsFromLastTOT;
			if (currentTimeStamp < lastTimeStamp)
				secondsFromLastTOT = currentTimeStamp + 3600 - lastTimeStamp;
			else
				secondsFromLastTOT = currentTimeStamp - lastTimeStamp;
			float bitrate;
			if (Packet.is204b)
				bitrate = (float) packetsFromLastTOT / secondsFromLastTOT * 204 * 8 / 1000000;
			else
				bitrate = (float) packetsFromLastTOT / secondsFromLastTOT * 188 * 8 / 1000000;
			if (lastBitrate == 0)
				lastBitrate = bitrate;
			else
				lastBitrate = (lastBitrate * (totsParsed - 1) + bitrate) / totsParsed;
		}
		lastTimeStamp = currentTimeStamp;
		lastpacketCounter = Packet.packetCount;
	}

	static Date ts = null;

	public static String getTimeStamp(long secondOffset) {
		if (ts == null)
			return null;
		secondOffset -= PCR.getTimestamp(totPacketCounter);
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(ts);
		calendar.add(Calendar.SECOND, (int) secondOffset);
		return formatMJD(calendar.getTime());
	}

	public static String formatMJD(Date d) {
		if (d != null) {
			SimpleDateFormat sdf = new SimpleDateFormat();
			return sdf.format(d);
		}
		return "invalid MJD";
	}

	public static Date parseMJD(BitWise bw) {
		int[] ts = new int[6];
		int mjd = bw.pop16();

		// tks to the web
		int jdi = mjd + 2400001;
		int tmp = jdi + 68569;
		int n = 4 * tmp / 146097;
		tmp = tmp - (146097 * n + 3) / 4;
		int year = 4000 * (tmp + 1) / 1461001;
		tmp = tmp - 1461 * year / 4 + 31;
		int month = 80 * tmp / 2447;
		int day = tmp - 2447 * month / 80;
		tmp = month / 11;
		month = month + 2 - 12 * tmp;
		year = 100 * (n - 49) + year + tmp;
		if (year > 1990) {
			ts[2] = day;
			ts[1] = month;
			ts[0] = year;
		} else
			return null;

		currentTimeStamp = 0;
		int secondPerUnit = 3600;
		for (int i = 0; i < 3; i++) {
			tmp = bw.pop();
			if ((tmp > 9 && tmp < 0x10) || (tmp > 0x19 && tmp < 0x20) || (tmp > 0x23))
				return null;
			tmp = Integer.valueOf(Integer.toHexString(tmp)).intValue();
			currentTimeStamp += tmp * secondPerUnit;
			secondPerUnit = secondPerUnit / 60;
			ts[i] = tmp;
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 3; i++) {
			if (ts[2 - i] < 10)
				sb.append('0');
			sb.append(Integer.toString(ts[2 - i]));
			if (i < 2)
				sb.append('-');
		}
		sb.append(' ');
		for (int i = 0; i < 3; i++) {
			if (ts[i + 3] < 10)
				sb.append('0');
			sb.append(Integer.toString(ts[i + 3]));
			if (i < 2)
				sb.append(':');
		}
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("DD-MM-yyyy HH:mm:ss");
			Date d = sdf.parse(sb.toString());
			System.out.println(sb.toString() + " > " + formatMJD(d));
			return d;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
