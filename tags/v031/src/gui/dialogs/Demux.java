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
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import parsers.Parameters;
import sys.BitWise;
import sys.Messages;
import sys.PIDStats;

public class Demux extends Dialog implements SelectionListener {

	private Shell sShell = null; // @jve:decl-index=0:visual-constraint="132,16"
	private Button btOK = null;
	private Button btCancel = null;
	private Button btInvert = null;
	private Composite pidList = null;
	private ScrolledComposite sc = null;
	Shell parent;
	PIDStats.Set[] pid;

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if (e.widget == btCancel)
			sShell.close();
		else if (e.widget == btOK) {
			int pidCount = 0;
			for (int i = 0; i < PIDStats.getPidCount(); i++) {
				Button b = (Button) pid[i].data;
				if (b.getSelection())
					pidCount++;
			}
			if (pidCount == 0)
				return;
			String[] sa = new String[pidCount];
			for (int i = 0; i < PIDStats.getPidCount(); i++) {
				Button b = (Button) pid[i].data;
				if (b.getSelection()) {
					pidCount--;
					sa[pidCount] = BitWise.toHex(pid[i].pid);
				}
			}
			sShell.close();
			FileDialog fd = new FileDialog(getParent(), SWT.SAVE);
			fd.setText(Messages.getString("MenuSave.save")); //$NON-NLS-1$
			String selected = fd.open();
			if (selected != null)
				Parameters.startParser(selected, sa, "-demux"); //$NON-NLS-1$
		} else if (e.widget == btInvert)
			for (int i = 0; i < PIDStats.getPidCount(); i++) {
				Button b = (Button) pid[i].data;
				b.setSelection(!b.getSelection());
			}
		else
			open();
	}

	public Demux(Shell parent) {
		super(parent);
		this.parent = parent;
	}

	public static void open() {
		class Starter implements Runnable {
			public void run() {
				Demux thisInstance = new Demux(MainPanel.sShell);
				thisInstance.initialize();
				thisInstance.sShell.open();
				Display display = thisInstance.sShell.getDisplay();
				while (!thisInstance.sShell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			}
		}
		MainPanel.guiThreadExec(new Starter(), false);
	}

	private void initialize() {
		if (getParent() == null)
			sShell = new Shell(SWT.APPLICATION_MODAL | SWT.SHELL_TRIM);
		// to use the visual editor
		else {
			sShell = new Shell(getParent(), SWT.APPLICATION_MODAL | SWT.SHELL_TRIM);
			sShell.setLocation(getParent().getLocation().x + 50, getParent().getLocation().y + 50);
		}
		GridLayout shellGd = new GridLayout();
		shellGd.numColumns = 3;
		sShell.setLayout(shellGd);

		GridData listGd = new GridData();
		listGd.horizontalSpan = 3;
		listGd.heightHint = 300;
		listGd.widthHint = 320;

		GridData btGd = new GridData();
		btGd.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		btGd.widthHint = 100;

		sc = new ScrolledComposite(sShell, SWT.H_SCROLL | SWT.V_SCROLL);
		pidList = new Composite(sc, SWT.BORDER_DASH);
		sc.setContent(pidList);
		sc.setLayoutData(listGd);
		GridLayout gl = new GridLayout();

		gl.numColumns = 1;
		pidList.setLayout(gl);
		sc.setLayout(gl);

		pid = PIDStats.getPids();

		for (int i = 0; i < PIDStats.getPidCount(); i++) {
			Button b = new Button(pidList, SWT.CHECK);
			b.setText(BitWise.toHex(pid[i].pid) + " (" + pid[i].name + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			pid[i].data = b;
		}
		pidList.pack();
		pidList.layout();

		btOK = new Button(sShell, SWT.NONE);
		btOK.setText("Demux"); //$NON-NLS-1$
		btOK.setLayoutData(btGd);
		btOK.addSelectionListener(this);

		btInvert = new Button(sShell, SWT.NONE);
		btInvert.setText("Invert"); //$NON-NLS-1$
		btInvert.setLayoutData(btGd);
		btInvert.addSelectionListener(this);

		btCancel = new Button(sShell, SWT.NONE);
		btCancel.setText("Cancel"); //$NON-NLS-1$
		btCancel.setLayoutData(btGd);
		btCancel.addSelectionListener(this);

		sShell.setText("Select PIDs to maintain"); //$NON-NLS-1$
		sShell.pack();
	}
} // @jve:decl-index=0:visual-constraint="79,26"
