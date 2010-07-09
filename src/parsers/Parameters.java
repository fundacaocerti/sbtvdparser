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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import mpeg.pes.CC;
import mpeg.psi.EIT;
import mpeg.psi.TOT;
import mpeg.psi.Table;
import mpeg.psi.TableList;
import sys.Log;

public class Parameters {

	public static String initialMessage = "";

	static File srcFile;

	static InputStream bis = null;

	static public boolean noGui = false, noTree = false, noStats = false,
			batchResults = false;

	public static InputStream getStream() {
		try {
			bis = new BufferedInputStream(new FileInputStream(srcFile), 4000000);
		} catch (FileNotFoundException e) {
			initialMessage = "File cannot be opened: ["
					+ srcFile.getAbsolutePath() + "]";
			Log.printWarning(initialMessage);
			Log.printStackTrace(new Exception(initialMessage));
			return null;
		}
		return bis;
	}

	public static void startParser(String[] args) {
		StringBuffer sb = new StringBuffer();
		sb.append("Command line: [");
		for (int i = 0; i < args.length; i++) {
			sb.append(args[i]);
			sb.append(' ');
		}
		sb.append("]");
		Log.printWarning(sb.toString());
		if (args.length == 0) {
			Log.printWarning("Input file not informed in commandline");
			return;
		}
		String srcPath = args[0];
		if (srcPath == null)
			return;
		srcFile = new File(srcPath);
		Log.setLogFile(new File(srcFile.getParentFile(), "log.txt"));
		Log.setTsFile(srcFile);

		if (srcFile.getName().endsWith(".lnk")) {
			try {
				Link lnkp = new Link();
				lnkp.parse(srcFile.getAbsolutePath());
				srcFile = new File(lnkp.getFilePath());
				if (!srcFile.exists())
					srcFile = new File(lnkp.getAlternatePath());
			} catch (Exception e) {
				initialMessage = "File cannot be opened: ["
						+ srcFile.getAbsolutePath() + "]";
				Log.printStackTrace(new Exception(initialMessage));
				Log.printWarning(initialMessage);
				return;
			}
		}

		if (!srcFile.exists() || !srcFile.isFile()) {
			initialMessage = "File not found: [" + srcFile.getAbsolutePath()
					+ "]";
			Log.printStackTrace(new Exception(initialMessage));
			Log.printWarning(initialMessage);
			return;
		}
		
		bis = getStream();
		
		if (srcFile.getName().endsWith(".pes")) {
			initMainPanel();
			System.out.println("here we go");
			IndependentPES pes = new IndependentPES(bis);
			//TODO: set pes type by user input
			pes.start();
			return;
		}

		Packet.estimate = srcFile.length() / 188;
		Packet.limit = 0;
		int filter = 0;
		int matchLimit = 0;
		MainPanel.setFilter(null);
		CC.reset();
		for (int i = 1; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-noGui"))
				noGui = true;
			if (args[i].equalsIgnoreCase("-noTree"))
				noTree = true;
			if (args[i].equalsIgnoreCase("-noStats"))
				noStats = true;
			if (args[i].equalsIgnoreCase("-filter"))
				filter = i;
			if (args[i].equalsIgnoreCase("-limitInput"))
				Packet.limit = i;
			if (filter > 0 && filter == i - 1)
				MainPanel.setFilter(args[i]);
			if (Packet.limit > 0 && Packet.limit == i - 1) {
				Packet.limit = Long.parseLong(args[i]);
				Packet.estimate = Packet.limit;
			}
			if (matchLimit > 0 && matchLimit == i - 1)
				matchLimit = Integer.parseInt(args[i]);
			if (args[i].equalsIgnoreCase("-limitMatches"))
				matchLimit = i;
			if (args[i].equalsIgnoreCase("-isRegex"))
				MainPanel.setFilterAsRegex();
			if (args[i].equalsIgnoreCase("-listOnlyMatches"))
				MainPanel.listOnlyMatches();
		}
		if (matchLimit > 0)
			MainPanel.setFilterLimit(matchLimit);
		else
			MainPanel.setFilterLimit(-1);
		// if (!noGui)
		if (!batchResults)
			MainPanel.clearTree();
		initMainPanel();
		TOT.reset();
		pp = new Packet(bis);
		pp.start();
	}

	private static void initMainPanel() {
		MainPanel.setProgress(0);
		initialMessage = "Parsing [" + srcFile.getAbsolutePath() + "]";
		MainPanel.tsNameId = initialMessage;
		MainPanel.tsNameIndex = MainPanel.addTreeItem(initialMessage, 0);
		Log.printWarning(initialMessage);
		MainPanel.setTitle(srcFile.getName());
	}

	public static void printStats() {
		if (noTree)
			System.out.println(initialMessage);
		if (!noStats) {
			((EIT) TableList.getByPid(EIT.FULLSEGPID)).printStatistics();
			((EIT) TableList.getByPid(EIT.FULLSEGPID)).printEPG();
			((EIT) TableList.getByPid(EIT.ONESEGPID)).printEPG();
			if (pp != null && pp.bitrateIsValid)
				pp.printBitrate();
			MainPanel.addTreeItem("Sync. losses: " + Packet.syncLosses, 0, MainPanel.STATS_TREE);
			MainPanel.addTreeItem("TEI counter: " + Packet.TEIerrors, 0, MainPanel.STATS_TREE);
			int continuityCount = 0;
			for (int i = 0; i < TableList.continuityErrorCounters.length; i++) {
				continuityCount += TableList.continuityErrorCounters[i];
			}
			int cntLvl = MainPanel.addTreeItem("Continuity error counter: " + continuityCount, 0, MainPanel.STATS_TREE);
			for (int i = 0; i < TableList.continuityErrorCounters.length; i++)
				if (TableList.continuityErrorCounters[i] != 1) {
					Table t = TableList.getByIndex(i);
					if (t != null)
						MainPanel.addTreeItem(t.name + ": " +
								TableList.continuityErrorCounters[i], cntLvl, MainPanel.STATS_TREE);;
					}
		}
		// MainPanel.addTreeItem("parsing done", 0);
		if (!noTree)
			MainPanel.printTree();
		// if (!noStats)
		// SimpleAssertions.checkSBTVDConformity(MainPanel.getTreeRoot());
		if (noGui)
			System.exit(0);
	}

	public static boolean isAlive() {
		return pp.isAlive();
	}

	static Packet pp;
}
