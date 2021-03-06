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
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;

import parsers.Packet;
import sys.LogicTree;

public class GuiMethods implements Runnable {
	int type = 0;

	Object[] param = null;

	Object returnVal = null;

	private boolean executed;

	public static final int CLEARTREE = 1, ADDTREEITEM = 2, GETLIMITBOX = 3, SETLIMITBOX = 4, SETPROGRESSBAR = 5,
			SETCURSOR = 6, SETTITLE = 7, ADDTOLOG = 8, ADDPIDBAR = 9, CHANGEITEM = 10;

	private static GuiMethods thisClass = new GuiMethods();

	public static final Cursor normalCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_ARROW);

	public static final Cursor busyCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_WAIT);

	public static final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);

	public static synchronized void runMethod(int type, Object[] param, boolean sync) {
		thisClass.type = type;
		thisClass.param = param;
		thisClass.executed = false;
		MainPanel.guiThreadExec(thisClass, sync);
	}

	public static synchronized void runMethod(int type, Object param, boolean sync) {
		thisClass.type = type;
		thisClass.param = new Object[] { param };
		thisClass.executed = false;
		MainPanel.guiThreadExec(thisClass, sync);
	}

	public static Object getReturn() {
		return thisClass.returnVal;
	}

	public void run() {
		try {
			if (executed)
				return;
			executed = true;
			switch (type) {
			case CLEARTREE:
				((Tree) param[0]).removeAll();
				break;
			case ADDPIDBAR:
				PIDStats.addBar(((Integer) param[0]).intValue(), ((Integer) param[1]).intValue(), ((Integer) param[2])
						.intValue(), param[3].toString());
				break;
			case ADDTREEITEM:
				((LogicTree) param[0]).addToUI(((Integer) param[1]).intValue());
				break;
			case CHANGEITEM:
				MainPanel.changeTreeItem(param[0].toString(), ((Integer) param[1]).intValue());
				break;
			case GETLIMITBOX:
				if (!MainPanel.inputLimit.isDisposed()) {
					long limit = 0;
					try {
						limit = Long.parseLong(MainPanel.inputLimit.getText());
					} catch (NumberFormatException e) {
					}
					Packet.setPacketLimit(limit);
				}
				break;
			case ADDTOLOG:
				MainPanel.log.append(param[0].toString());
				break;
			case SETTITLE:
				MainPanel.sShell.setText(param[0].toString());
				break;
			case SETLIMITBOX:
				MainPanel.inputLimit.setText(((Long) param[0]).toString());
				break;
			case SETPROGRESSBAR:
				float i = ((Float) param[0]).floatValue();
				MainPanel.progressBar.setStopPoint(i);
				// TODO: MainPanel.progressBar.setText(x%);
				MainPanel.progressBar.layout();
				if (i > 0 && i != 1) {
					MainPanel.setPauseButtonState(MainPanel.PLAING);
				} else {
					MainPanel.setPauseButtonState(MainPanel.STOPPED);
				}
				break;
			case SETCURSOR:
				int type = ((Integer) param[0]).intValue();
				switch (type) {
				case SWT.CURSOR_ARROW:
					MainPanel.sShell.setCursor(normalCursor);
					break;
				case SWT.CURSOR_WAIT:
					MainPanel.sShell.setCursor(busyCursor);
					break;
				case SWT.CURSOR_HAND:
					MainPanel.sShell.setCursor(handCursor);
					break;
				}
				break;
			}
		} catch (NullPointerException e) {
			// may happen when the object is destroyed before the async. exe.
			// It means nothing, so we can just ignore it.
		}
		param = null;
		returnVal = null;
	}
}
