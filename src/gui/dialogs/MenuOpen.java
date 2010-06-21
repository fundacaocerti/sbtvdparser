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
import gui.QueryDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import parsers.Parameters;
import sys.BatchAnalisys;

public class MenuOpen implements SelectionListener {

	private Shell shell;

	Widget fileMenu, filterMenu;

	static String lastDir = null;

	public MenuOpen(Shell shell, Widget fileMenu, Widget filterMenu) {
		this.shell = shell;
		this.fileMenu = fileMenu;
		this.filterMenu = filterMenu;
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		String[] filterExt = { "*.trp", "*.ts", ".mpeg", "*.*" };
		if (fileMenu.equals(e.widget)) {
			FileDialog fd = new FileDialog(shell, SWT.OPEN);// +SWT.MULTI
			fd.setText("Abrir");
			// fd.setFilterPath("C:/");
			fd.setFilterExtensions(filterExt);
			String selected = fd.open();
			System.out.println(selected);
			MainPanel.clearTree();
			Parameters.startParser(new String[] { selected });
		} else {
			if (filterMenu.equals(e.widget)) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);// +SWT.MULTI
				fd.setText("Abrir e filtrar");
				// fd.setFilterPath("C:/");
				fd.setFilterExtensions(filterExt);
				String selected = fd.open();
				if (selected == null)
					return;
				QueryDialog qDialog = new QueryDialog(shell, false);
				String filter = qDialog.open();
				if (filter == null || filter.length() == 0)
					return;
				MainPanel.clearTree();
				String[] parm = new String[] { selected, "-filter", filter, "", "-limitMatches",
						Integer.toString(qDialog.getFilterLimit())};
				if (qDialog.isRegex())
					parm[3] = "-isRegex"; 
				Parameters.startParser(parm);
			} else {
				DirectoryDialog fd = new DirectoryDialog(shell, SWT.OPEN);// +SWT.MULTI
				fd.setFilterPath(lastDir);
				fd.setText("Abrir Diretório");
				lastDir = fd.open();
				if (lastDir == null)
					return;
				QueryDialog qDialog = new QueryDialog(shell, true);
				String filter = qDialog.open();
				if (filter == null || filter.length() == 0)
					return;
				MainPanel.clearTree();
				String[] parm = new String[] { null, "-filter", filter, "", "",
						"-limitMatches", Integer.toString(qDialog.getFilterLimit())};
				if (qDialog.isRegex())
					parm[3] = "-isRegex";
				if (!qDialog.listAllFiles())
					parm[4] = "-listOnlyMatches"; 
				BatchAnalisys ba = new BatchAnalisys(lastDir, qDialog.isRecursive(), parm);
				ba.start();
			}
		}
	}

	private String popQuery() {
		QueryDialog qDialog = new QueryDialog(shell, false);
		qDialog.setText("Filtro de resultados");
		String filter = qDialog.open();
		System.out.println(qDialog.isRegex());
		System.out.println(qDialog.isRecursive());
		System.out.println(qDialog.getFilterLimit());
		return filter;
	}
}

