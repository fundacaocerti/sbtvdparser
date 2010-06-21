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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class MenuAbout  extends Dialog implements SelectionListener {

	private Label imageLabel = null;
	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,62"
	private Label infoLabel = null;
	private Button btOK = null;
	private Link warrantyLink = null;
	private Link condLink = null;
	private Label gplImg = null;
	private Link certiLink = null;
	private Label certiLogoLabel = null;
	
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if (e.widget instanceof MenuItem)
			open();
		if (e.widget == btOK)
			sShell.close();
		try {
			if (e.widget == warrantyLink)
				java.awt.Desktop.getDesktop().browse(new URI("http://www.gnu.org/licenses/gpl.html#section15")); 
			if (e.widget == condLink) {
				Desktop desk = Desktop.getDesktop();
				if (!java.awt.Desktop.isDesktopSupported())
					return;
				File license = new File("COPYNG.txt");
				System.out.println(license.getAbsolutePath());
				if (license.exists() && license.canRead())
					try {
						desk.open(license);
					} catch (IOException e2) {
						e2.printStackTrace();
					}
			}
			if (e.widget == certiLink)
				java.awt.Desktop.getDesktop().browse(new URI("http://www.certi.org.br")); 
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException f) {
			f.printStackTrace();
		}
	}

	public MenuAbout(Shell parent) {
		super(parent);
//		sShell = parent;
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
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		GridData infoImgGd = new GridData();
		GridData gplGd = new GridData();
		GridData condLinkGd = new GridData();
		GridData wrLinkGd = new GridData();
		GridData info1gd = new GridData();
		GridData btOkGd = new GridData();
		GridLayout gridLayout = new GridLayout();
		
		gridLayout.numColumns = 3;
		infoImgGd.widthHint = 60;
		info1gd.horizontalSpan = 1;
		info1gd.widthHint = 250;
		info1gd.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		wrLinkGd.horizontalSpan = 3;
		condLinkGd.horizontalSpan = 3;
		gplGd.verticalSpan = 1;
		gplGd.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		btOkGd.horizontalSpan = 3;
		btOkGd.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		
		if (getParent() == null)
			sShell = new Shell(); //to use the visual editor
		else {
			sShell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			sShell.setLocation(getParent().getLocation().x + 50,
				getParent().getLocation().y + 50);
		}
		imageLabel = new Label(sShell, SWT.NONE);
		imageLabel.setImage(sShell.getDisplay().getSystemImage(SWT.ICON_WORKING));
		imageLabel.setLayoutData(infoImgGd);
		infoLabel = new Label(sShell, SWT.NONE);
		infoLabel.setText("SBTVD Transport Stream Parser\n" +
        		"Copyright © 2010 Gabriel A. G. Marques\n" +
        		"gabriel.marques@gmail.com");
		infoLabel.setLayoutData(info1gd);
		gplImg = new Label(sShell, SWT.NONE);
		//License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>
		certiLink = new Link(sShell, SWT.NONE);
		certiLink.setText("Initial development kindly supported by <a>CERTI Foundation</a>");
		certiLink.setLayoutData(gridData);
		certiLink.addSelectionListener(this);
		certiLogoLabel = new Label(sShell, SWT.NONE);
		certiLogoLabel.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream(
		"/res/certi.png")));
		warrantyLink = new Link(sShell, SWT.NONE);
		warrantyLink.setText("This program comes with <a>ABSOLUTELY NO WARRANTY.</a>");
		warrantyLink.setLayoutData(wrLinkGd);
		warrantyLink.addSelectionListener(this);
		gplImg.setText("Label");
		gplImg.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream(
				"/res/gplv3-88x31.png")));
		gplImg.setLayoutData(gplGd);
		condLink = new Link(sShell, SWT.NONE);
		condLink.setText("This is free software, and you are welcome to redistribute " +
				"it under <a>certain conditions.</a>");
		condLink.setLayoutData(condLinkGd);
		condLink.addSelectionListener(this);
		btOK = new Button(sShell, SWT.NONE);
		btOK.setText("OK");
		btOK.setLayoutData(btOkGd);
		btOK.addSelectionListener(this);
		sShell.setLayout(gridLayout);
		sShell.setSize(new Point(428, 199));
		sShell.setText("Sobre");
		sShell.pack();
	}

}
