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
package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class QueryDialog extends Dialog implements SelectionListener, KeyListener {

	public QueryDialog(Shell parent, boolean showDirOptions) {
		super(parent);
		this.showDirOptions = showDirOptions;
	}

	private Shell sShell = null; // @jve:decl-index=0:visual-constraint="10,62"
	private Text filter = null;
	private Text maxResults = null;
	private Label ftrLabel = null;
	private Label maxLabel = null;
	private Button regex = null;
	private Button recursive = null;
	private Label regLabel = null;
	private Label recLabel = null;
	private String result; // @jve:decl-index=0:
	private Button okButton = null;
	private boolean isRegex = false, isRecursive = false, listAllFiles = true;
	private static int filterLimit = 0;
	private Label labelList = null;
	private Button chkListAllFiles = null;
	private boolean showDirOptions = false;

	public String open() {
		Shell parent = getParent();
		createSShell();
		sShell.open();
		Display display = parent.getDisplay();
		while (!sShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return result;
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		if (getParent() == null)
			sShell = new Shell(); // to use the visual editor
		else {
			sShell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			sShell.setLocation(getParent().getLocation().x + 50, getParent().getLocation().y + 50);
		}
		sShell.setText("Filtro de resultados");

		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridData gridData21 = new GridData();
		gridData21.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridData gridData2 = new GridData();
		gridData2.widthHint = 150;
		gridData2.horizontalSpan = 3;
		GridData gridData1 = new GridData();
		gridData1.widthHint = 25;
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		gridData.widthHint = 100;
		gridData.horizontalSpan = 4;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		sShell.setText("Shell");
		sShell.setLayout(gridLayout);
		ftrLabel = new Label(sShell, SWT.NONE);
		ftrLabel.setText("Filtro");
		ftrLabel.setLayoutData(gridData4);
		filter = new Text(sShell, SWT.BORDER);
		filter.setLayoutData(gridData2);
		regLabel = new Label(sShell, SWT.NONE);
		regLabel.setText("Filtro é uma regex");
		regLabel.setLayoutData(gridData3);
		regex = new Button(sShell, SWT.CHECK);
		regex.setSelection(false);
		maxLabel = new Label(sShell, SWT.RIGHT);
		maxLabel.setText("Lim. resultados");
		maxLabel.setLayoutData(gridData11);
		maxResults = new Text(sShell, SWT.BORDER);
		maxResults.setTextLimit(3);
		maxResults.setText("1");
		maxResults.setLayoutData(gridData1);
		maxResults.setToolTipText("Use 0 to grab all occurences.");
		maxResults.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				Text t = (Text) e.widget;
				if (e.text.length() > 0) {
					String string = t.getText() + e.text;
					int i = 0;
					try {
						i = Integer.parseInt(string);
					} catch (NumberFormatException ex) {
						e.doit = false;
						return;
					}
					if (i < 0 || i > 500) {
						e.doit = false;
						return;
					}
				}
			}
		});
		maxResults.addKeyListener(this);
		filter.addKeyListener(this);

		labelList = new Label(sShell, SWT.NONE);
		labelList.setText("Listar os arquivos");
		chkListAllFiles = new Button(sShell, SWT.CHECK);
		recLabel = new Label(sShell, SWT.NONE);
		recLabel.setText("Incluir sub-diretórios");
		recLabel.setLayoutData(gridData21);
		recursive = new Button(sShell, SWT.CHECK);

		if (!showDirOptions) {
			recursive.setVisible(false);
			recLabel.setVisible(false);
			labelList.setVisible(false);
			chkListAllFiles.setVisible(false);
		}

		okButton = new Button(sShell, SWT.NONE);
		okButton.setText("OK");
		okButton.setLayoutData(gridData);
		okButton.addSelectionListener(this);
		sShell.pack();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e) {
		result = filter.getText();
		isRecursive = recursive.getSelection();
		isRegex = regex.getSelection();
		listAllFiles = chkListAllFiles.getSelection();
		System.out.println(maxResults.getText());
		filterLimit = Integer.parseInt(maxResults.getText());
		sShell.close();
	}

	public void keyPressed(KeyEvent e) {
		if (e.character == SWT.CR) {
			widgetSelected(null);
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public boolean isRecursive() {
		return isRecursive;
	}

	public boolean listAllFiles() {
		return listAllFiles;
	}

	public boolean isRegex() {
		return isRegex;
	}

	public int getFilterLimit() {
		return filterLimit;
	}
}
