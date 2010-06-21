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
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import parsers.Packet;
import sys.LogicTree;

public class CopyPopUp implements Listener {

	static int mouseButton, x, y;

	String text;

	Shell s;

	int yBias;

	public CopyPopUp(Shell s, int yBias) {
		this.s = s;
		this.yBias = yBias;
	}

	public void handleEvent(Event event) {
		if (event.type == SWT.MouseDown) {
			mouseButton = event.button;
			x = event.x;
			y = event.y;
			if (event.widget.getClass() == ProgressBar.class) {
				ProgressBar pb = (ProgressBar) event.widget;
				if (pb.getSelection() != 100) {
					MainPanel.setCursor(SWT.CURSOR_WAIT);
					Packet.jumpTo((float) x / (pb.getSize().x - 3));
				}
			}
		}
		if (event.item != null && event.type == SWT.Selection
				&& mouseButton == 3) {
			// System.out.println("click"+((Integer)event.item.getData()).toString());
			Menu menu = new Menu(s, SWT.POP_UP);
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			LogicTree lt = (LogicTree) ((TreeItem) event.item).getData();
			text = lt.toString();
			item.setText("copiar conteúdo?");
			item.addListener(SWT.Selection, this);
			menu.setLocation(x + s.getLocation().x, y
					+ yBias + s.getLocation().y);
			menu.setVisible(true);
			while (!menu.isDisposed() && menu.isVisible()) {
				if (!Display.getDefault().readAndDispatch())
					Display.getDefault().sleep();
			}
			menu.dispose();
		}
		if (event.item == null && event.type == SWT.Selection) {
			Clipboard clipboard = new Clipboard(Display.getDefault());
			clipboard.setContents(new Object[] { text },
					new Transfer[] { TextTransfer.getInstance() });
		}
	}

}
