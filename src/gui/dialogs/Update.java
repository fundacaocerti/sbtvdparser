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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import sys.Messages;

public class Update extends Dialog implements SelectionListener {

	private Shell sShell = null; // @jve:decl-index=0:visual-constraint="10,62"
	private Button btOK = null;

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if (e.widget instanceof MenuItem)
			open();
		if (e.widget == btOK)
			sShell.close();
	}

	public Update(Shell parent) {
		super(parent);
		initialize();
	}

	public void open() {
		initialize();
		sShell.open();
		Display display = sShell.getDisplay();
		while (!sShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	private void initialize() {
		if (getParent() == null)
			sShell = new Shell(); // to use the visual editor
		else {
			sShell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			sShell.setLocation(getParent().getLocation().x + 50, getParent().getLocation().y + 50);
		}
		GridData gridData = new GridData();

		btOK = new Button(sShell, SWT.NONE);
		btOK.setText(Messages.getString("MenuAbout.ok")); //$NON-NLS-1$
		btOK.setLayoutData(gridData);
		sShell.setLayout(new GridLayout());
		btOK.addSelectionListener(this);
		sShell.setSize(new Point(345, 154));
		sShell.setText("Update");
		sShell.pack();
	}

}
