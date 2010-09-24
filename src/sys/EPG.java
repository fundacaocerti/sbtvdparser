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

import gui.MainPanel;

import java.util.Vector;

import mpeg.psi.TOT;

public class EPG {

	Vector evtList = new Vector();

	int evtCount = 0;

	BitWise bw = new BitWise(null);

	private class Evt {
		public String[] description;

		public int[] startTime;

		// evt[1] = TOT.formatMJD(start);
		public boolean startsBefore(Evt e) {
			for (int i = 0; i < startTime.length; i++)
				if (startTime[i] > e.startTime[i])
					return true;
			return false;
		}

		public String toString() {
			return description[0] + " - " + TOT.formatMJD(startTime) + " " + description[1] + " : " + description[2]
					+ "(" + description[4] + ") " + description[3];
		}
	}

	public void addEvent(int id, int[] start, String duration, String name, String description, String rating) {
		String[] desc = new String[6];
		desc[0] = BitWise.toHex(id);
		desc[1] = duration;
		desc[2] = name;
		desc[3] = description;
		desc[4] = rating;
		Evt e = new Evt();
		e.description = desc;
		e.startTime = start;
		int i = 0;
		while (i < evtList.size() && e.startsBefore((Evt) evtList.elementAt(i)))
			i++;
		evtList.add(i, e);
	}

	public void printDescription(int epgLevel) {
		int i = 0;
		while (i < evtList.size()) {
			String s = ((Evt) evtList.elementAt(i++)).toString();
			// MainPanel.addTreeItem(s, epgLevel, MainPanel.EPG_TREE);
			int leftChars = s.length();
			int start = 0, end = 100;
			int indx = 0;
			if (leftChars < 100) {
				end = leftChars;
				leftChars = 0;
			} else
				leftChars -= 100;
			indx = MainPanel.addTreeItem(s.substring(start, end), epgLevel, MainPanel.EPG_TREE);
			while (leftChars > 0) {
				start = end;
				if (leftChars > 100)
					end += 100;
				else
					end += leftChars;
				MainPanel.addTreeItem(s.substring(start, end), indx, MainPanel.EPG_TREE);
				leftChars -= (end - start);
			}
		}
	}
}
