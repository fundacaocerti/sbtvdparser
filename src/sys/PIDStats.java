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

import mpeg.PCR;
import gui.GuiMethods;
import parsers.Packet;

public class PIDStats {

	static final int MAXPIDS = 300;

	static Set[] sets = new Set[MAXPIDS];

	static int foundPids = 0;

	private static class Set implements QuickSort.Comparable {
		public int pid, counter, lastCount;
		public float lastTimestamp;
		public String name = "unknown";
		public float[] bitrates = new float[512];

		public int getValue() {
			return counter;
		}
	}

	public static void reset() {
		int i = 0;
		while (i < foundPids) {
			sets[i] = null;
			i++;
		}
		foundPids = 0;
	}

	public static void setIdentification(int pid, String idText) {
		int i = 0;
		while (i < foundPids && sets[i].pid != pid)
			i++;
		if (i == MAXPIDS)
			return;
		if (i == foundPids)
			addPid(pid);
		sets[i].name = idText;
	}

	public static void increasePid(int pid) {
		int i = 0;
		while (i < foundPids && sets[i].pid != pid)
			i++;
		if (i == MAXPIDS)
			return;
		if (i == foundPids)
			addPid(pid);
		else {
			sets[i].counter++;
			if ((PCR.getCurrentTimestamp() - sets[i].lastTimestamp) > measureRate) {
				float snapshot = (float) ((sets[i].counter - sets[i].lastCount) * Packet.realPktLenght * 8
						/ measureRate / 1e6);
				sets[i].lastCount = sets[i].counter;
				sets[i].lastTimestamp = PCR.getCurrentTimestamp();
				int position = (int) (PCR.getCurrentTimestamp() / measureRate);
				if (position < 512)
					sets[i].bitrates[position] = snapshot;
			}
		}
	}

	static float measureRate = (float) 1; // em segundos

	static void addPid(int pid) {
		if (foundPids == MAXPIDS)
			return;
		Set set = new Set();
		set.pid = pid;
		set.counter = 1;
		set.lastCount = 1;
		sets[foundPids] = set;
		foundPids++;
	}

	public static void printStats(float bitrate) {
		if (foundPids == 0)
			return;

		PIDStats.setIdentification(0x1fff, "byte stuffing");
		PIDStats.setIdentification(0x24, "BIT");
		PIDStats.setIdentification(0x12, "EIT");
		PIDStats.setIdentification(0x27, "EIT one-seg");

		QuickSort.sort(sets, foundPids);
		// int pidStatsLevel = MainPanel.addTreeItem("PID stats", 0,
		// MainPanel.STATS_TREE);
		int i = foundPids - 1;
		int maxCount = sets[i].counter;
		while (i >= 0 && sets[i].counter > 1) {

			// MainPanel.addTreeItem("pid " + Integer.toHexString(sets[i].pid)
			// + ": " + pidBitrateStr + " " + multiplier + " ("
			// + sets[i].name + ")", pidStatsLevel);
			Object[] bar = {
					new Integer(sets[i].pid),
					new Integer(sets[i].counter),
					new Integer(maxCount),
					formatScaleFactor((float) sets[i].counter / Packet.packetCount * bitrate) + "bps (" + sets[i].name
							+ ")" };
			GuiMethods.runMethod(GuiMethods.ADDPIDBAR, bar, true);
			i--;
		}
	}

	public static float[] getBitrates(int indx) {
		return sets[foundPids - indx - 1].bitrates;
	}

	public static int getPid(int indx) {
		return sets[foundPids - indx - 1].pid;
	}

	public static String formatScaleFactor(float mega) {
		String pidBitrateStr, multiplier = "M";
		if (mega < 1) {
			mega = mega * 1000;
			multiplier = "K";
		}
		if (mega < 1) {
			mega = mega * 1000;
			multiplier = "";
		}
		pidBitrateStr = Float.toString(mega) + "00";
		pidBitrateStr = pidBitrateStr.substring(0, pidBitrateStr.indexOf('.') + 3);
		return pidBitrateStr + multiplier;
	}
}
