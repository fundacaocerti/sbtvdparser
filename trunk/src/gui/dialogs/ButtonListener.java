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

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import parsers.Packet;
import sys.BatchAnalisys;

public class ButtonListener implements SelectionListener {

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if (e.widget == MainPanel.btPause)
			if (BatchAnalisys.stopThread)
				Packet.pause(!Packet.isPaused());
			else {
				stopCurrentTS();
				return;
			}
		else {
			BatchAnalisys.stopThread = true;
			stopCurrentTS();
		}
		if (Packet.isPaused())
			MainPanel.btPause.setImage(MainPanel.imPlay);
		else
			MainPanel.btPause.setImage(MainPanel.imPause);
	}

	private void stopCurrentTS() {
		Packet.setPacketLimit(Packet.packetCount);
		Packet.pause(false);
		if (BatchAnalisys.stopThread)
			MainPanel.btStop.setEnabled(false);
		MainPanel.btPause.setEnabled(false);
		MainPanel.progressBar.setSelection(100);
		MainPanel.progressBar.setToolTipText("100%");
	}

}
