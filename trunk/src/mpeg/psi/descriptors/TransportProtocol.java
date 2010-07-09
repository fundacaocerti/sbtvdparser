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


public class TransportProtocol extends AITDescriptor {

	public static int tag = 0x02;

	static String name = "Transport Protocol Descriptor";

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		String[] ids = {"reserved", "object carousel", "IP over DVB", "data piping", "data carousel"};
		int id = bw.pop16();
		if (id < ids.length)
			addSubItem("protocol_id: "+BitWise.toHex(id)+" - "+ids[id], level);
		else
			addSubItem("protocol_id: "+BitWise.toHex(id)+" - "+ids[0], level);
		addSubItem("transport_protocol_label: "+BitWise.toHex(bw.pop()), level);
		if (id == 1 || id == 4 || id == 2) {
			boolean rc = (BitWise.stripBits(bw.pop(), 8, 1) & 1) > 0;
			addSubItem("remote connection: "+(rc ? "true" : "false"), level);
			if (rc) {
				addSubItem("onid: "+BitWise.toHex(bw.pop16()), level);
				addSubItem("tsid: "+BitWise.toHex(bw.pop16()), level);
				addSubItem("service_id: "+BitWise.toHex(bw.pop16()), level);
			}
			addSubItem("component_tag: "+BitWise.toHex(bw.pop()), level);
		}
		if (id == 2) {
			addSubItem("alignment_indication: "+(((bw.pop() & 1) > 0) ? "true" : "false"), level);
			
		}
		
		addSubItem("selector_byte: "+bw.getHexSequence(descriptor_length-3), level);
//		for (int i = 0; i < descriptor_length-3; i++) {
//
//		}
	}
}
