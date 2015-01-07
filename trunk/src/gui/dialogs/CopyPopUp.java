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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import mpeg.PCR;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import sys.LogicTree;
import sys.Messages;

public class CopyPopUp implements Listener {

	static int mouseButton, x, y;

	static LogicTree lt = null;
	static MenuItem copyRAW, copyTree;

	Shell s;

	int yBias;

	public CopyPopUp(final Shell s, final int yBias) {
		this.s = s;
		this.yBias = yBias;
	}

	public void handleEvent(final Event event) {
		if (event.type == SWT.MouseDown) {
			mouseButton = event.button;
			x = event.x;
			y = event.y;
			if (event.widget.getClass() == Tree.class && mouseButton == 3) {
				// if (event.item != null)
				// lt = (LogicTree) ((TreeItem) event.item).getData();
				final Menu menu = new Menu(s, SWT.POP_UP);
				copyRAW = new MenuItem(menu, SWT.PUSH);
				copyTree = new MenuItem(menu, SWT.PUSH);
				copyRAW.setText(Messages.getString("CopyPopUp.copyQuestion")); //$NON-NLS-1$
				copyRAW.addListener(SWT.Selection, this);
				copyTree.setText(Messages.getString("CopyPopUp.copyTreeQuestion")); //$NON-NLS-1$
				copyTree.addListener(SWT.Selection, this);
				menu.setLocation(x + s.getLocation().x, y + yBias + s.getLocation().y);
				menu.setVisible(true);
				while (!menu.isDisposed() && menu.isVisible())
					if (!Display.getDefault().readAndDispatch()) Display.getDefault().sleep();
				menu.dispose();
			}
		}
		if (event.widget == copyRAW) {
			final Clipboard clipboard = new Clipboard(Display.getDefault());
			clipboard.setContents(new Object[] { lt.toString() }, new Transfer[] { TextTransfer.getInstance() });
		}
		if (event.widget == copyTree) {
			final Clipboard clipboard = new Clipboard(Display.getDefault());
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				baos.write(lt.text.getBytes("UTF-8"));
				baos.write('\n');
				lt.print(baos);
				clipboard.setContents(new Object[] { baos.toString("UTF-8") },
						new Transfer[] { TextTransfer.getInstance() });
			} catch (final UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		// TODO: add subtitles and statistics
		if (event.type == SWT.Selection && event.widget.getClass() == Tree.class) {
			lt = (LogicTree) ((TreeItem) event.item).getData();
			MainPanel.statusBar.setText(PCR.getFormatedTimestamp(lt.creationTimestamp));
		}
	}

}
