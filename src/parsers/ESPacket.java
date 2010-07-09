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

import mpeg.TSP;
import mpeg.pes.PES;
import mpeg.pes.PESList;
import mpeg.psi.TableList;

public class ESPacket {
	int continuityOld = -1;

	PES pes;

	void parsePES() {
		pes = PESList.getByPid(TSP.pid);
		if (pes == null)
			return;
		// TSP.print();
		if ((TSP.continuityCounter - continuityOld != 1)
				&& (continuityOld - TSP.continuityCounter != 15)
				&& (continuityOld != -1))
			TableList.continuityErrorCounters[PESList.pesCount]++;
		continuityOld = TSP.continuityCounter;

		if (Packet.layer > 0 && Packet.layer < 4)
			pes.layer = Packet.layer;

		if (TSP.payloadUnitStartIndicator == 1) {
			// início da seção
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
			if (pointer_field > 0) { // remainder of the last packet
				// System.out.println("packed tables");
				pes.feedPart(Packet.buffer, srcPosition, pointer_field);
				srcPosition += pointer_field;
			} else
				srcPosition = TSP.dataOffset;
			int length = TSP.TS_PACKET_LEN - 1 - srcPosition;
			if (length < 1)
				return;
			if (srcPosition + length <= Packet.buffer.length)
				pes.startPacket(Packet.buffer, srcPosition, length);
			// }
		} else {
			// if (TSP.adaptationFieldControl == 1
			// || TSP.adaptationFieldControl == 3) {
			int pointer_field = 0;
			if (TSP.dataOffset < Packet.buffer.length)
				pointer_field = Packet.buffer[TSP.dataOffset];
			if (pointer_field < 0)
				pointer_field += 256;
			int srcPosition = TSP.dataOffset + 1 + pointer_field;
			pes.feedPart(Packet.buffer, srcPosition, Packet.buffer.length
					- srcPosition);
			// }
		}
	}
}
