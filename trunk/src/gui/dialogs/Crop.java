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

import gui.MainPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import parsers.Parameters;
import sys.Messages;

public class Crop implements SelectionListener {

	static boolean waitingCrop = false;
	static Shell parent;

	public Crop(Shell parent) {
		Crop.parent = parent;
	}

	public static boolean isWaitingCrop() {
		return waitingCrop;
	}

	public static void crop() {
		MainPanel.progressBar.setEditMode(false);
		FileDialog fd = new FileDialog(parent, SWT.SAVE);
		fd.setText(Messages.getString("MenuSave.save")); //$NON-NLS-1$
		String selected = fd.open();
		String[] sa = { Float.toString(MainPanel.progressBar.getStart()),
				Float.toString(MainPanel.progressBar.getStop()) };
		MainPanel.progressBar.setStartPoint(0);
		MainPanel.progressBar.setStopPoint(0);
		MainPanel.progressBar.layout();
		MainPanel.setPauseButtonState(MainPanel.STOPPED);
		if (selected != null)
			Parameters.startParser(selected, sa, "-crop");
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if (!waitingCrop) {
			MainPanel.setPauseButtonState(MainPanel.CROP_WAIT);
			MainPanel.progressBar.setEditMode(true);
			MainPanel.statusBar.setText("Left click on the progress bar to set start point, right click to stop.");
			waitingCrop = true;
		} else {
			MainPanel.progressBar.setEditMode(true);
		}
	}
}
