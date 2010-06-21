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

/*
 cd 17 07 2e 89 0e 54 56 52 45 43 4f 52 44 00 0f 02 04 30 04 31 af 01 05 b0
 cd 15 05 26 0e 54 56 20 47 6c 6f 62 6f 0f 02 e7 40 e7 41 af 01 e7 58
 cd 0d 21 54 65 73 74 65 20 56 43 00 01 12 34 
 */
public class TSinformation extends Descriptor {

	public static int tag = 0xCD;

	static String name = "TS information";

	public static String[] svcTypes = { "TV", "Data", "Data", "1-seg" };

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		addSubItem("descriptor_length: " + descriptor_length, level);
		// remote_control_key_id 8 uimsbf
		addSubItem("remote_control_key_id: " + bw.pop(), level);

		// length_of_ts_name 6 uimsbf
		int aux = bw.pop();
		int length_of_ts_name = bw.stripBits(aux, 8, 6);

		// transmission_type_count 2 uimsbf
		int transmission_type_count = bw.stripBits(aux, 2, 2);

		// ts_name
		char[] str = new char[length_of_ts_name];
		for (int i = 0; i < length_of_ts_name; i++) {
			str[i] = (char) bw.pop();
		}
		addSubItem("ts_name: [" + new String(str) + "]", level);

		// transmission type loop
		int loopLevel = addSubItem("Transmission loop", level);
		for (int i = 0; i < transmission_type_count; i++) {
			// Transmission_type_info 8 bslbf -- Specified and operated by the
			// broadcaster
			tableIndx = aux;
			int ttInfo = bw.pop();
			// num_of_service 8 uimsbf
			int num_of_service = bw.pop();
			// service loop
			int svcLoopLevel = addSubItem("Service loop", loopLevel);
			String type = "ABC-";
			addSubItem("transmission_type: "
					+ type.charAt(bw.stripBits(ttInfo, 8, 2)), svcLoopLevel);
			String[] mod = { "64QAM", "16QAM", "QPSK", "reserved" };
			addSubItem("modulation: " + mod[bw.stripBits(ttInfo, 6, 2)],
					svcLoopLevel);
			for (int k = 0; k < num_of_service; k++) {
				// service_id 16 uimsbf
				int service_id = bw.pop16();
				int svcIdLevel = addSubItem("service_id: "
						+ bw.toHex(service_id), svcLoopLevel);
				addSubItem("type: " + svcTypes[bw.stripBits(service_id, 5, 2)],
						svcIdLevel);
				addSubItem("number: " + (bw.stripBits(service_id, 3, 3) + 1),
						svcIdLevel);
			}
		}
	}
}
