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
package parsers;

import sys.Log;
import mpeg.TSP;
import mpeg.psi.DSMCC;
import mpeg.psi.EIT;
import mpeg.psi.Table;
import mpeg.psi.TableList;

public class Section {

	Table table;

	byte[] sectionPayload;

	void parseTable() {
		table = TableList.getByPid(TSP.pid);
		if (table == null)
			return;
		
		int matches = 0;
		if (table.searchBytes != null) {
			int i;
			for (i = 0; i < Packet.buffer.length && matches < table.searchBytes.length; i++)
				if (Packet.buffer[i] == table.searchBytes[matches])
					matches++;
				else
					matches = 0;
			if (matches == table.searchBytes.length)
				System.out.println("lowLevelSearch found at filepos: 0x"+
						Long.toHexString((Packet.byteCount+i-matches-Packet.realPktLenght+1))
						+" packet: 0x"+Long.toHexString(Packet.packetCount));
		}
		
		//TODO: debug the continuity counter
//		if (TSP.pid == 0)
//			System.out.println(TSP.continuityCounter+" "+continuityOld+" "+this);
		if ((TSP.continuityCounter - table.continuityOld != 1)
				&& (table.continuityOld - TSP.continuityCounter != 15)
				&& (table.continuityOld != -1))
			TableList.continuityErrorCounters[TableList.tableIndex]++;
		table.continuityOld = TSP.continuityCounter;
		
		if (Packet.layer > 0 && Packet.layer < 4)
			table.layer = Packet.layer;

		if (TSP.payloadUnitStartIndicator == 1) {
			// inicio da seção
			if (TSP.adaptationFieldControl != 1
					&& TSP.adaptationFieldControl != 3)
				System.out.println("adaptation present");
			int pointer_field = 0;
			if (TSP.dataOffset < Packet.buffer.length)
				pointer_field = Packet.buffer[TSP.dataOffset];
			if (pointer_field < 0)
				pointer_field += 256;
			int srcPosition = TSP.dataOffset + 1;// 1 is the
			// pointer_field
			// System.out.println(table.name + ":
			// "+Integer.toHexString(versionNumber));

			if (pointer_field > 0) { // remainder of the last packet
				// System.out.println("packed tables");
				table.feedPart(Packet.buffer, srcPosition, pointer_field);
				srcPosition += pointer_field;
			}
			if (srcPosition < 182) {
				int versionNumber = (Packet.buffer[srcPosition + 5] & 0x3f) >> 1;
				//TODO: opção pra filtrar tabelas repetidas por
				if (table.versionNumber == versionNumber
						&& !(table instanceof EIT) && !(table instanceof DSMCC))
					return;
			}
			int length = TSP.TS_PACKET_LEN - 1 - srcPosition;
			if (length < 1)
				return;
			sectionPayload = new byte[length];
			if (srcPosition + length <= Packet.buffer.length)
				try {
					System.arraycopy(Packet.buffer, srcPosition,
							sectionPayload, 0, length);
				} catch (ArrayIndexOutOfBoundsException e) {
					Log.printStackTrace(new Exception("StreamParser err: "
							+ srcPosition + ", " + length + ", "
							+ pointer_field + ", " + TSP.dataOffset));
					Log.printStackTrace(e);
				}
			// MainPanel.addTreeItem("Erros de continuidade:
			// "+list.continuityErrorCounters[list.tableIndex],
			// 0);
			table.resetMultissection();
			TableList.tablesParsed[TableList.tableIndex] = table
					.printDescription(sectionPayload);
		} else {
			if (TSP.adaptationFieldControl != 1
					&& TSP.adaptationFieldControl != 3)
				System.out.println("adaptation present");
			table.feedPart(Packet.buffer, TSP.dataOffset, Packet.buffer.length
					- TSP.dataOffset);
		}
	}
}
