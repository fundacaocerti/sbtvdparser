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

import sys.Messages;

public class Save implements SelectionListener {

	private Shell s;

	public Save(Shell shell) {
		this.s = shell;
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		FileDialog fd = new FileDialog(s, SWT.SAVE);
		fd.setText(Messages.getString("MenuSave.save")); //$NON-NLS-1$
		// fd.setFilterPath("C:/");
		String[] filterExt = { "*.txt", "*.htm" }; //$NON-NLS-1$ //$NON-NLS-2$
		fd.setFilterExtensions(filterExt);
		String selected = fd.open();
		if (selected != null)
			MainPanel.saveTree(selected);
	}

}
