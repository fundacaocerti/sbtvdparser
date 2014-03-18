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

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import sys.BitWise;

public class ServiceList extends Service {

	public static int tag = 0x41;

	static String name = "Service List Descriptor";

	/*
	 * service_descriptor(){ service_type 8 uimsbf service_provider_name_length 8 uimsbf for(i=0;i<N;i++){ char 8 uimsbf
	 * } service_name_length 8 uimsbf for(i=0;i<N;i++){ Char 8 uimsbf } }
	 */

	@Override
	public void printDescription() {
		final int level = addSubItem(name, tableIndx);
		while (descriptor_length > 0) {
			final int id = bw.pop16();
			String serviceType = "Não definido";
			final int typeTag = bw.pop();
			for (int i = 0; i < typeTags.length; i++)
				if (typeTags[i] == typeTag) serviceType = typeNames[i];
			addSubItem("id: " + BitWise.toHex(id) + " type: " + serviceType, level);
			descriptor_length -= 3;
		}
	}

	@Override
	public String getText(final int lenght, final BitWise bw) {
		String text = null;
		final byte[] ba = new byte[lenght];
		for (int i = 0; i < ba.length; i++)
			ba[i] = (byte) bw.pop();
		try {
			final InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(ba), "ISO8859_15_FDIS");
			final char[] ca = new char[lenght];
			isr.read(ca);
			text = new String(ca);
		} catch (final Exception e) {
			text = "codificaçao desconhecida";
		}
		return text;
	}
}
