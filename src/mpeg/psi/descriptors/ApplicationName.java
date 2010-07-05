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


public class ApplicationName extends AITDescriptor {

	public static int tag = 0x01;

	static String name = "Application Name Descriptor";

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		// application_signalling_descriptor() {
		// for( i=0; i<N; i++ ){
		int name_lenght = 0;
		for (int i = 0; i < descriptor_length; i += 4+name_lenght) {
			// ISO_639_language_code
			addSubItem(Component.parseISO639(bw), level);
			// text 
			name_lenght = bw.pop();
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < name_lenght; j++)
				sb.append((char) bw.pop());
			addSubItem("name: ["+sb.toString()+"]", level);
		}
	}
}
