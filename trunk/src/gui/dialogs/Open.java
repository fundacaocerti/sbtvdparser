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

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import parsers.Parameters;
import sys.BatchAnalisys;
import sys.Messages;
import sys.Persistence;

public class Open implements SelectionListener {

	private Shell shell;

	Widget fileMenu, filterMenu;

	public Open(Shell shell, Widget fileMenu, Widget filterMenu) {
		this.shell = shell;
		this.fileMenu = fileMenu;
		this.filterMenu = filterMenu;
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		String[] filterExt = { "*.*", "*.ts", ".mpeg", "*.trp" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (fileMenu.equals(e.widget)) {
			FileDialog fd = new FileDialog(shell, SWT.OPEN);// +SWT.MULTI
			fd.setText(Messages.getString("MenuOpen.open")); //$NON-NLS-1$
			fd.setFilterPath(Persistence.get(Persistence.LAST_READ_DIR));
			fd.setFilterExtensions(filterExt);
			String selected = fd.open();
			if (selected == null)
				return;
			Persistence.set(Persistence.LAST_READ_DIR, new File(selected).getParent());
			MainPanel.clearTree();
			Parameters.startParser(new String[] { selected });
		} else {
			if (filterMenu.equals(e.widget)) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);// +SWT.MULTI
				fd.setText(Messages.getString("MenuOpen.openFilter")); //$NON-NLS-1$
				fd.setFilterExtensions(filterExt);
				fd.setFilterPath(Persistence.get(Persistence.LAST_READ_DIR));
				String selected = fd.open();
				if (selected == null)
					return;
				Persistence.set(Persistence.LAST_READ_DIR, new File(selected).getParent());
				Query qDialog = new Query(shell, false);
				String filter = qDialog.open();
				if (filter == null || filter.length() == 0)
					return;
				MainPanel.clearTree();
				String[] parm = new String[] { selected, "-filter", filter, "", "-limitMatches", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						Integer.toString(qDialog.getFilterLimit()) };
				if (qDialog.isRegex())
					parm[3] = "-isRegex"; //$NON-NLS-1$
				Parameters.startParser(parm);
			} else {
				DirectoryDialog fd = new DirectoryDialog(shell, SWT.OPEN);// +SWT.MULTI
				fd.setFilterPath(Persistence.get(Persistence.LAST_READ_DIR));
				fd.setText(Messages.getString("MenuOpen.openDir")); //$NON-NLS-1$
				String dir = fd.open();
				if (dir == null)
					return;
				Persistence.set(Persistence.LAST_READ_DIR, dir);
				Query qDialog = new Query(shell, true);
				String filter = qDialog.open();
				if (filter == null || filter.length() == 0)
					return;
				MainPanel.clearTree();
				String[] parm = new String[] { null, "-filter", filter, "", "", "-limitMatches", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						Integer.toString(qDialog.getFilterLimit()) };
				if (qDialog.isRegex())
					parm[3] = "-isRegex"; //$NON-NLS-1$
				if (!qDialog.listAllFiles())
					parm[4] = "-listOnlyMatches"; //$NON-NLS-1$
				BatchAnalisys ba = new BatchAnalisys(Persistence.get(Persistence.LAST_READ_DIR), qDialog.isRecursive(),
						parm);
				ba.start();
			}
		}
	}
}
