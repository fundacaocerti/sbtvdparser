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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

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
		calculateTsBitrate();
		totsParsed++;
		if (totsParsed > 1) {
			bw.pop(bw.getAvailableSize());
			return false;
		}
		ts = parseMJD(bw);
		String timestamp = formatMJD(ts);

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

	public static String getTimeStamp(double secondOffset) {
		if (ts == null)
			return null;
		secondOffset -= PCR.getTimestamp(totPacketCounter);
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(ts);
		calendar.add(Calendar.SECOND, (int) secondOffset);
		calendar.add(Calendar.MILLISECOND, (int) (secondOffset * 1000 % 1000));
		return formatMJD(calendar.getTime());
	}

	public static String formatMJD(Date d) {
		if (d != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss.SSS");
			return sdf.format(d);
		}
		return "invalid MJD";
	}

	public static Date parseMJD(BitWise bw) {
		// if no ids were returned, something is wrong. get out.
		String[] ids = TimeZone.getAvailableIDs(-3 * 60 * 60 * 1000);
		if (ids.length == 0)
			return null;

		// times in TOT and EPG are always GMT-3
		SimpleTimeZone brt = new SimpleTimeZone(-3 * 60 * 60 * 1000, ids[0]);
		GregorianCalendar gc = new GregorianCalendar(brt);

		int[] ts = new int[3];
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
		if (year < 1990)
			return null;

		currentTimeStamp = 0;
		int secondPerUnit = 3600;
		for (int i = 0; i < 3; i++) {
			tmp = bw.pop();

			try {
				tmp = Integer.valueOf(Integer.toHexString(tmp)).intValue();
			} catch (NumberFormatException e) {
				return null;
			}
			currentTimeStamp += tmp * secondPerUnit;
			secondPerUnit = secondPerUnit / 60;
			ts[i] = tmp;
		}

		gc.set(GregorianCalendar.YEAR, year);
		gc.set(GregorianCalendar.MONTH, month - 1);
		gc.set(GregorianCalendar.DAY_OF_MONTH, day);
		gc.set(GregorianCalendar.HOUR_OF_DAY, ts[0]);
		gc.set(GregorianCalendar.MINUTE, ts[1]);
		gc.set(GregorianCalendar.SECOND, ts[2]);
		// SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		// System.out.println(sdf.format(gc.getTime()) + " / " + gc.getTime());
		return gc.getTime();
	}
}
