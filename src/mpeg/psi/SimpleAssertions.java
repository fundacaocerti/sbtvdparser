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

import gui.MainPanel;
import sys.LogicTree;
import sys.Messages;

public class SimpleAssertions {

	static String[] searchTags = { "remote_control_key_id: ", "ts_name: ", "network_PID: ", "service_id: ", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"service_id: ", "service_id: ", "UTC-3_time: ", "service_name: ", "service_name: ", "service_name: ", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"service_count: ", "component_tag: 0x00", "component_tag: 0x00", "component_tag: 0x10", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"type: 0x1b- H.264", "type: 0x11- ISO/IEC 14496-3 Audio", "component_tag: 0x30" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	static boolean[] tagsFound = new boolean[searchTags.length];

	public static void checkSBTVDConformity(LogicTree root) {
		LogicTree current = root.getNext();
		int digestLevel = MainPanel.addTreeItem(Messages.getString("SimpleAssertions.resume"), 0); //$NON-NLS-1$
		while (current != null) {
			String text = current.toString();
			for (int i = 0; i < searchTags.length; i++)
				if (!tagsFound[i] && (text.startsWith(searchTags[i]) || text.indexOf(searchTags[i]) > 0)) {
					MainPanel.addTreeItem(text, digestLevel);
					tagsFound[i] = true;
					break;
				}
			current = root.getNext();
		}
		digestLevel = MainPanel.addTreeItem(Messages.getString("SimpleAssertions.problems"), digestLevel); //$NON-NLS-1$
		for (int i = 0; i < searchTags.length; i++)
			if (!tagsFound[i]) {
				MainPanel.addTreeItem(searchTags[i], digestLevel);
			}
	}
}
