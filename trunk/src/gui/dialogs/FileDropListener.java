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
package gui.dialogs;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;

import parsers.Parameters;

public class FileDropListener implements DropTargetListener {

	public void dragEnter(DropTargetEvent event) {
		if (event.detail == DND.DROP_DEFAULT)
			if ((event.operations & DND.DROP_COPY) != 0)
				event.detail = DND.DROP_COPY;
			else
				event.detail = DND.DROP_NONE;
	}

	public void dragOver(DropTargetEvent event) {
	}

	public void dragOperationChanged(DropTargetEvent event) {
		if (event.detail == DND.DROP_DEFAULT) {
			if ((event.detail = (event.operations & DND.DROP_COPY)) != 0)
				event.detail = DND.DROP_COPY;
			else
				event.detail = DND.DROP_NONE;
		}
	}

	public void dragLeave(DropTargetEvent event) {
	}

	public void drop(DropTargetEvent event) {
		String[] files = (String[]) event.data;
		Parameters.startParser(new String[] { files[0] });
	}

	public void dropAccept(DropTargetEvent event) {
	}

}
