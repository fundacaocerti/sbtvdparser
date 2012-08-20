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
package mpeg.pes;

import java.util.Arrays;
import java.util.HashMap;

public class PESList {

	static HashMap<Integer, PES> pesList = new HashMap<Integer, PES>();

	public static int[] continuityErrorCounters = new int[200];

	// TODO: remover todos os publics e por os métodos que usam as vars.
	public static void setDefaultPids() {
		// pesList.add(new CC());
	}

	public static PES getByPid(int pid) {
		if (pid == 0xFFFF)
			return null;
		return pesList.get(new Integer(pid));
	}

	public static int getLenght() {
		return pesList.size();
	}

	public static void addElementaryStream(PES pes) {
		pesList.put(new Integer(pes.pid), pes);
	}

	public static void resetList() {
		pesList.clear();
		Arrays.fill(continuityErrorCounters, 0);
	}
}
