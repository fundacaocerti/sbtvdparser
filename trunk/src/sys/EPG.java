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

import java.util.Date;
import java.util.Vector;

import mpeg.psi.TOT;

public class EPG {

	Vector<Evt> evtList = new Vector<Evt>();

	int evtCount = 0;

	BitWise bw = new BitWise(null);

	private class Evt implements Comparable<Evt> {
		public String[] description;
		String startTime;
		public Date startDate;

		public String toString() {
			return description[0] + " - " + startTime + " " + description[1] + " : " + description[2] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ "(" + description[4] + ") " + description[3]; //$NON-NLS-1$ //$NON-NLS-2$
		}

		public int compareTo(Evt e) {
			return startDate.compareTo(e.startDate);
		}
	}

	public void addEvent(int id, String start, String duration, String name, String description, String rating) {
		String[] desc = new String[6];
		desc[0] = BitWise.toHex(id);
		desc[1] = duration;
		desc[2] = name;
		desc[3] = description;
		desc[4] = rating;
		Evt e = new Evt();
		e.description = desc;
		e.startTime = start;
		e.startDate = TOT.parseMJD(start);
		evtList.add(e);
	}

	public void printDescription(int epgLevel) {
		int i = 0;
		java.util.Collections.sort(evtList);
		while (i < evtList.size()) {
			String s = (evtList.elementAt(i++)).toString();
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
