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

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sys.Messages;
import sys.Persistence;

public class Update extends Dialog implements SelectionListener {

	private Shell sShell = null; // @jve:decl-index=0:visual-constraint="10,62"
	private Button btOK = null;
	private Label msgLabel = null;
	private Label cvLabel = null;
	private Text cvText = null;
	private Label nvLabel = null;
	private Text nvText = null;
	private Label rnLabel = null;
	private Text rnArea = null;
	private Link link = null, linkb = null;
	Shell parent;
	static String updVer, updURL, updInfo;  //  @jve:decl-index=0:

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if (e.widget == btOK)
			sShell.close();
	}
	
	public Update(Shell parent) {
		super(parent);
		this.parent = parent;
	}

	public static void open(String _updVer, String _updURL, String _updInfo) {
		updVer = _updVer;
		updURL = _updURL;
		updInfo = _updInfo;
		class Starter implements Runnable {
			public void run() {
				Update thisInstance = new Update(MainPanel.sShell);
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
			sShell = new Shell(SWT.APPLICATION_MODAL | SWT.SHELL_TRIM); // to use the visual editor
		else {
			sShell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			sShell.setLocation(getParent().getLocation().x + 50, getParent().getLocation().y + 50);
		}
		GridData defaultGd = new GridData();
		defaultGd.horizontalSpan = 3;
		defaultGd.widthHint = 300;
		defaultGd.grabExcessHorizontalSpace = true;
		
		GridData gridData2 = new GridData();
		gridData2.horizontalSpan = 3;
		
		GridData textGd = new GridData();
		textGd.horizontalSpan = 2;
		textGd.widthHint = 120;
		
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData2.widthHint = 300;
		gridData2.heightHint = 100;
	
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		
		
		msgLabel = new Label(sShell, SWT.NONE);
		msgLabel.setText("A new version of this program is available!");
		msgLabel.setLayoutData(defaultGd);
		cvLabel = new Label(sShell, SWT.NONE);
		cvLabel.setText("Current version:");
		cvText = new Text(sShell, SWT.BORDER);
		cvText.setEditable(false);
		long currSwDate = Long.parseLong(Persistence.CURRENT_SW_DATE, 16);
		cvText.setText(Persistence.CURRENT_SW_VERSION+" - "+
				DateFormat.getDateInstance().format(new Date(currSwDate)));
		cvText.setLayoutData(textGd);
		nvLabel = new Label(sShell, SWT.NONE);
		nvLabel.setText("New version:");
		nvText = new Text(sShell, SWT.BORDER);
		nvText.setEditable(false);
		nvText.setLayoutData(textGd);
		nvText.setText(updVer);
		rnLabel = new Label(sShell, SWT.NONE);
		rnLabel.setText("Release notes:");
		rnLabel.setLayoutData(defaultGd);
		rnArea = new Text(sShell, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		rnArea.setEditable(false);
		rnArea.setText(updInfo);
		rnArea.setLayoutData(gridData2);
		cvText = new Text(sShell, SWT.BORDER);
		cvText.setText(updURL);
		cvText.setLayoutData(defaultGd);
		cvText.setEditable(false);
		cvText.setToolTipText(updURL);
		btOK = new Button(sShell, SWT.NONE);
		btOK.setText(Messages.getString("MenuAbout.ok")); //$NON-NLS-1$
		btOK.setLayoutData(gridData);
		sShell.setLayout(gridLayout);
		btOK.addSelectionListener(this);
		sShell.setText("Update notification");
		sShell.pack();
	}
}
