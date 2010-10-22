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
package parsers;

import gui.MainPanel;

import java.io.IOException;
import java.io.InputStream;

import mpeg.TSP;
import mpeg.pes.CC;
import mpeg.pes.PESList;
import mpeg.psi.TableList;

import org.eclipse.swt.SWT;

import sys.Log;
import sys.PIDStats;

public class IndependentPES extends Thread {

	InputStream bis = null;

	public static long limit = 0, byteCount = 0, counter = 0, syncLosses = 0;

	public static byte[] buffer = new byte[6], bigBuffer;

	public IndependentPES(InputStream bis) {
		super();
		this.bis = bis;
	}

	public void run() {
		try {
			if (bis == null)
				return;
			counter = 0;
			byteCount = 0;
			TableList.resetList();
			PESList.resetList();
			PIDStats.reset();
			if (limit > 0)
				MainPanel.setLimit(limit);
			MainPanel.getLimit();
			try {
				mainLoop();
				bis.close();
			} catch (Exception e) {
				e.printStackTrace();
				Log.printStackTrace(new Exception("dataOffset: " + TSP.dataOffset));
				Log.printStackTrace(e);
			}
			Parameters.printStats();
			MainPanel.setProgress(1);
		} catch (RuntimeException e) {
			Log.printStackTrace(e);
			e.printStackTrace();
		}
	}

	public static boolean limitNotReached = true;

	private void mainLoop() throws InterruptedException, IOException {
		CC pp = new CC(1);
		int readBytes = 0;
		readBytes = bis.read(buffer);
		if (readBytes < 0)
			return;
		do {
			byteCount += readBytes;
			pp.startPacket(buffer, 0, buffer.length);
			bigBuffer = new byte[pp.packetLenght];
			readBytes = bis.read(bigBuffer);
			byteCount += readBytes;
			pp.feedPart(bigBuffer, 0, bigBuffer.length);
			readBytes = bis.read(buffer);
		} while (((MainPanel.isOpen || Parameters.noGui) && readBytes != -1));
		MainPanel.setCursor(SWT.CURSOR_ARROW);
	}
}
