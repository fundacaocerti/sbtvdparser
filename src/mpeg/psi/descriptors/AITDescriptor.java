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

public class AITDescriptor extends Descriptor {

	public AITDescriptor() {
		predefinedTags = new int[] { 0x00, 0x01, 0x0b, 0x05, 0x02, 0x11, 0x0c, 0x0d, 0x03, 0x07, 0x06 };
		predefinedNames = new String[] { "application", "application_name", "application_icon",
				"external_application_authorisation", "transport_protocol", "ip_signalling", "prefetch",
				"DII_location", "ginga_J_application", "ginga_J_application_location", "ginga_NCL_application" };
	}

	protected String checkRange(String descriptorName) {
		if (parsedTag > 0x79 && parsedTag < 0xC0)
			descriptorName = "Broadcaster defined";
		if (parsedTag > 0x07 && parsedTag < 0x80 || parsedTag > 0xbf && parsedTag < 0xc2 || parsedTag > 0xc2)
			descriptorName = "Reserved";
		return descriptorName;
	}
}
