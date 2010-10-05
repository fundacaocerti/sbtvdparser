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
import java.io.OutputStream;

import mpeg.AdaptationField;
import mpeg.PCR;
import mpeg.TSP;
import mpeg.pes.PESList;
import mpeg.psi.IIP;
import mpeg.psi.TOT;
import mpeg.psi.TableList;

import org.eclipse.swt.SWT;

import sys.Log;
import sys.Messages;
import sys.PIDStats;

public class Packet extends Thread {

	InputStream bis = null;

	public OutputStream bos = null;

	public static long limit = 0, estimate = 0, TEIerrors = 0, byteCount = 0, packetCount = 0, syncLosses = 0;

	public static boolean is204b = false, iipAdded = false;

	public int[] filterPIDs = null;

	int btsPackets = 0, skipSize = 0, customPktCount = 0;

	public static int realPktLenght = TSP.TS_PACKET_LEN;

	public static int layer = 0;

	final int skipsToSetPktLen = 250;
	int synclossesToRecalc = 500000;

	int[] skips = new int[skipsToSetPktLen];
	int[] skipsHisto = new int[skipsToSetPktLen];

	public static byte[] buffer = new byte[TSP.TS_PACKET_LEN - 1];

	public Packet(InputStream bis) {
		super();
		this.bis = bis;
	}

	void parsePacket() throws IOException {
		int sync = 0;
		int i = 0;
		if (is204b) {
			bis.read();
			layer = bis.read() >> 4;
			byteCount += 2;
			byteCount += bis.skip(14);
			if (layer == 8 && !iipAdded) {
				TableList.addTable(new IIP(TSP.pid));
				iipAdded = true;
			}
		} else
			byteCount += bis.skip(skipSize);
		while (sync != TSP.SYNC_BYTE && (MainPanel.isOpen || Parameters.noGui) && sync != -1) {
			sync = bis.read();
			i++;
		}
		// if (sync == -1)
		// counter = limit;
		byteCount += i;
		if (i != 1) {
			syncLosses++;
			if (syncLosses % 500 == 0)
				Log.printWarning(Messages.getString("Packet.warning") + packetCount + "-" + realPktLenght + "-" //$NON-NLS-1$
						+ skipSize);
			if (syncLosses > synclossesToRecalc) {
				customPktCount = 0;
				synclossesToRecalc += syncLosses;
				Log.printWarning(Messages.getString("Packet.syncloss")); //$NON-NLS-1$
			}
		}
		if (customPktCount < skipsToSetPktLen) {
			if (i > skipsToSetPktLen)
				i = skipsToSetPktLen;
			skips[customPktCount] = i - 1;
			customPktCount++;
			if (customPktCount == skipsToSetPktLen) {
				for (int j = 0; j < skipsToSetPktLen; j++)
					skipsHisto[skips[j]]++;
				int max = 0;
				for (int j = 0; j < skipsToSetPktLen; j++)
					if (skipsHisto[j] > max) {
						max = skipsHisto[j];
						skipSize = j;
					}

				realPktLenght = TSP.TS_PACKET_LEN + skipSize;
				if (realPktLenght % TSP.TS_PACKET_LEN == 0)
					realPktLenght = TSP.TS_PACKET_LEN;
				MainPanel.addTreeItem(Messages.getString("Packet.set") + realPktLenght + " bytes.", 0); //$NON-NLS-1$
				syncLosses = 0;
				estimate = (long) (estimate * (float) TSP.TS_PACKET_LEN / realPktLenght);
				if (skipSize == 16) {
					is204b = true;
					MainPanel.addTreeItem(Messages.getString("Packet.layer"), 0); //$NON-NLS-1$
				}
			}
		}
		byteCount += bis.read(buffer); // TODO: skip PIDs not used
		packetCount++;
		TSP.parse(buffer);
		if (TSP.transportErrorIndicator == 1)
			TEIerrors++;
	}

	public void run() {
		try {
			is204b = false;
			if (bis == null)
				return;
			packetCount = 0;
			byteCount = 0;
			AdaptationField.pcrPid = -1;
			PCR.firstTimestamp = -1;
			TableList.resetList();
			PESList.resetList();
			PIDStats.reset();
			System.gc();
			if (limit > 0)
				MainPanel.setLimit(limit);
			MainPanel.getLimit();
			try {
				if (filterPIDs != null)
					pidFilterLoop();
				else
					mainLoop();
				bis.close();
			} catch (Exception e) {
				e.printStackTrace();
				Log.printStackTrace(new Exception("dataOffset: " + TSP.dataOffset));
				Log.printStackTrace(e);
			}
			Parameters.printStats();
			MainPanel.setProgress(100);
		} catch (RuntimeException e) {
			Log.printStackTrace(e);
			e.printStackTrace();
		}
	}

	private void pidFilterLoop() throws InterruptedException, IOException {
		limitNotReached = true;
		boolean hasBytesToRead;
		do {
			parsePacket();
			PIDStats.increasePid(TSP.pid);
			if (TSP.transportErrorIndicator == 1)// check if should copy TSP.pid
				break;
			for (int i = 0; i < filterPIDs.length; i++)
				if (filterPIDs[i] == TSP.pid) {
					bos.write(0x47);
					bos.write(buffer);
					break;
				}

			if (estimate > 100 && !Parameters.noGui && packetCount % (estimate / 100) == 0)
				MainPanel.setProgress((int) (packetCount / (estimate / 100)));
			hasBytesToRead = byteCount < (estimate * realPktLenght);
			if (limitNotReached)
				limitNotReached = (limit == 0 || packetCount < limit);
		} while (hasBytesToRead && (MainPanel.isOpen || Parameters.noGui) && limitNotReached);
		bos.flush();
		bos.close();
		MainPanel.setCursor(SWT.CURSOR_ARROW);
		MainPanel.addTreeItem("Demux complete!", 0);
	}

	public void printBitrate() {
		float bitrate = PCR.getAverageBitrate();
		if (Float.isInfinite(bitrate) || bitrate == 0)
			bitrate = TOT.lastBitrate;
		if (bitrate == 0)
			return;
		PIDStats.printStats(bitrate);
		if (!Float.isInfinite(TOT.lastBitrate)) {
			MainPanel.addTreeItem(Messages.getString("Packet.bitrate") + bitrate + " Mbps", 0, MainPanel.STATS_TREE); //$NON-NLS-1$
			// MainPanel.addTreeItem("TS packets: " + packetCounter, 0);
			int duration = (int) (packetCount / bitrate * realPktLenght * 8 / 1e6);
			String[] hms = new String[3];
			int hmsDuration = duration;
			for (int i = 0; i < hms.length; i++) {
				hms[i] = Integer.toString(hmsDuration % 60);
				if (hms[i].length() == 1)
					hms[i] = "0" + hms[i];
				hmsDuration = hmsDuration / 60;
			}
			MainPanel
					.addTreeItem(
							Messages.getString("Packet.duration") + hms[2] + ":" + hms[1] + ":" + hms[0] + " (" + duration + "s)", 0, //$NON-NLS-1$
							MainPanel.STATS_TREE);
		}
	}

	public static boolean limitNotReached = true;

	private void mainLoop() throws InterruptedException, IOException {
		limitNotReached = true;
		boolean hasBytesToRead;
		Section sp = new Section();
		ESPacket pp = new ESPacket();
		do {
			while (paused)
				Thread.sleep(100);
			parsePacket();
			PIDStats.increasePid(TSP.pid);

			sp.parseTable();
			pp.parsePES();

			if (estimate > 100 && !Parameters.noGui && packetCount % (estimate / 100) == 0)
				MainPanel.setProgress((int) (packetCount / (estimate / 100)));
			hasBytesToRead = byteCount < (estimate * realPktLenght);
			if (limitNotReached)
				limitNotReached = (limit == 0 || packetCount < limit);
			if (jumpOK) {
				MainPanel.setProgress((int) (packetCount / (estimate / 100)));
				MainPanel.setCursor(SWT.CURSOR_ARROW);
				jumpOK = false;
			}
			if (jump)
				jump();
		} while (jump
				|| (hasBytesToRead && !TableList.tablesCaught() && (MainPanel.isOpen || Parameters.noGui) && limitNotReached));
		MainPanel.setCursor(SWT.CURSOR_ARROW);
		// System.out.println(hasBytesToRead + ","
		// + !TableList.tablesCaught() + ","
		// + (MainPanel.isOpen || Core.noGui) + "," + limitNotReached);
		// System.out.println(packetCounter);
		// System.out.println(packetEstimate);
		// System.out.println(byteCount);
		// System.out.println(bis.available());
	}

	boolean bitrateIsValid = true;

	private void jump() throws IOException {
		bitrateIsValid = false;
		if (filePosition > byteCount)
			bis.skip(filePosition - byteCount);
		else {
			bis.close();
			bis = Parameters.getStream();
			// bis.reset();
			bis.skip(filePosition);
		}
		packetCount = filePosition / realPktLenght;
		byteCount = filePosition;
		jump = false;
		jumpOK = true;
	}

	private static long filePosition;

	private static boolean jump = false, jumpOK = false;

	public static void jumpTo(float location) {
		filePosition = (long) ((estimate * realPktLenght) * location);
		jump = true;
	}

	private static boolean paused = false;

	public static boolean isPaused() {
		return paused;
	}

	public static void pause(boolean state) {
		paused = state;
	}

	public static void setPacketLimit(long limit) {
		if (limit > 0) {
			Packet.limit = limit;
			if (limit < estimate)
				estimate = limit;
		}
	}
}
