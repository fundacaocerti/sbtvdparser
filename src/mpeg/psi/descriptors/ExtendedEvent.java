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
package mpeg.psi.descriptors;

public class ExtendedEvent extends ShortEvent {

	public static int tag = 0x4e;

	public static String name = "Extended event descriptor", evtName, evtDesc;

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		addSubItem("descriptor_number: " + bw.consumeBits(4), level);
		addSubItem("last_descriptor_number: " + bw.consumeBits(4), level);
		addSubItem(Component.parseISO639(bw), level);
		int itemLvl = addSubItem("items:", level);
		int length_of_items = bw.pop();
		bw.mark();
		while (bw.getByteCount() < length_of_items) {
			addSubItem(readText(bw) + ": " + readText(bw), itemLvl);
		}
		evtDesc = readText(bw);
		addSubItem("text: " + evtDesc, level);
	}
}
