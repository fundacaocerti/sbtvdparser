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
package mpeg.psi;

import java.util.Arrays;
import java.util.Vector;

public class TableList {

	static Vector<Table> tableList = new Vector<Table>();

	static Table forcedTable;

	public static int[] continuityErrorCounters = new int[200];

	public static int tableIndex = 0;

	// TODO: remover todos os publics e p�r os m�todos que usam as vars.
	public static void setDefaultPids() {
		tableList.add(new PAT(0x00));
		tableList.add(new SDT(0x11));
		tableList.add(new CAT(0x01));
		tableList.add(new NIT(0x10));
		tableList.add(new EIT(EIT.FULLSEGPID));
		tableList.add(new EIT(EIT.ONESEGPID));
		tableList.add(new TOT(0x14));
		tableList.add(new SDTT(SDTT.HIGH_PROTECTION_LAYER_PID));
		tableList.add(new SDTT(SDTT.LOW_PROTECTION_LAYER_PID));
	}

	public static Table getByPid(final int pid) {
		if (pid == 0xFFFF) return null;
		for (int i = 0; i < tableList.size(); i++)
			if (tableList.get(i).pid == pid) {
				tableIndex = i;
				// if (!tablesParsed[i]) //TODO: colocar na UI como opção (pegar
				// todas vs. pegar 1a)
				return tableList.get(i);
			}
		return null;
	}

	public static Table getByIndex(final int index) {
		if (index >= tableList.size()) return null;
		return tableList.get(index);
	}

	public static int getLenght() {
		return tableList.size();
	}

	public static void forceTable(final Table table) {
		forcedTable = table;
	}

	public static void addTable(final Table table) {
		tableList.add(table);
	}

	public static boolean tablesCaught() {
		boolean res = true;
		for (int i = 0; i < tableList.size(); i++)
			if (!tableList.get(i).parsed) res = false;
		return res;
	}

	public static void resetList() {
		Arrays.fill(continuityErrorCounters, 0);
		tableList.removeAllElements();
		setDefaultPids();
		if (forcedTable != null) addTable(forcedTable);
		tableIndex = 0;
	}
}
