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

import sys.BitWise;

public class DataComponent extends Descriptor {

	public static int tag = 0xfd;

	static String name = "Data Component Descriptor";

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		// data_component_descriptor(){
		// descriptor_tag 8 uimsbf
		// descriptor_length 8 uimsbf
		// data_component_id 16 uimsbf
		int id = bw.pop16();
		addSubItem("data_component_id: " + BitWise.toHex(id), level);
		// for(i=0;i<N;i++){
		// additional_data_component_info 8 uimsbf
		// if (id == 8) {
		// String[] dispType = {"Auto", "Non", "Select", "Specific"};
		// addSubItem("dmf: "+dispType[bw.consumeBits(4) >> 2]+" display",
		// level);
		// bw.consumeBits(2);
		// String[] timing = {"Asynchronous", "Program sync.", "Time sync",
		// "reserved"};
		// addSubItem("timing: "+timing[bw.consumeBits(2)], level);
		// }
		// else
		addSubItem("additional_data_component_info: "
				+ bw.getHexSequence(descriptor_length - 2), level);
	}
}
