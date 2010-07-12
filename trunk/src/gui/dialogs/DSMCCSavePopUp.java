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

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import sys.LogicTree;
import dsmcc.DSMCCObject;
import dsmcc.Module;
import dsmcc.ModuleList;

public class DSMCCSavePopUp implements Listener, SelectionListener {

	static int mouseButton, x, y;

	Object data;

	Shell s;

	int yBias;

	static String save = "Salvar item", open = "Abrir";

	public DSMCCSavePopUp(Shell s, int yBias) {
		this.s = s;
		this.yBias = yBias;
	}

	public void handleEvent(Event event) {
		if (event.type == SWT.MouseDown) {
			mouseButton = event.button;
			x = event.x;
			y = event.y;
		}
		if (event.item != null // && event.type == SWT.Selection
				&& mouseButton == 3) {
			// System.out.println("click"+((Integer)event.item.getData()).toString());
			Menu menu = new Menu(s, SWT.POP_UP);
			LogicTree lt = (LogicTree) ((TreeItem) event.item).getData();
			data = lt.getData();
			if (data == null)
				return;
			MenuItem saveOpt = new MenuItem(menu, SWT.PUSH);
			saveOpt.setText(save);
			saveOpt.addSelectionListener(this);
			if (data instanceof DSMCCObject && !((DSMCCObject) data).isDirectory()) {
				MenuItem openOpt = new MenuItem(menu, SWT.PUSH);
				openOpt.setText(open);
				openOpt.addSelectionListener(this);
			}
			menu.setLocation(x + s.getLocation().x, y + yBias + s.getLocation().y);
			menu.setVisible(true);
			while (!menu.isDisposed() && menu.isVisible()) {
				if (!Display.getDefault().readAndDispatch())
					Display.getDefault().sleep();
			}
			menu.dispose();
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if (((MenuItem) e.getSource()).getText() == save) {
			FileDialog fd = new FileDialog(s, SWT.SAVE);
			fd.setText("Salvar");
			fd.setFileName(data.toString());
			String selected = fd.open();
			if (selected == null)
				return;
			File f = new File(selected);
			if (data instanceof Module)
				((Module) data).save(f);
			else if (data instanceof ModuleList)
				((ModuleList) data).save(f);
			else
				((DSMCCObject) data).save(f);
		} else
			((DSMCCObject) data).open();
	}

}
