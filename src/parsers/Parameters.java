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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import mpeg.PCR;
import mpeg.TSP;
import mpeg.pes.CC;
import mpeg.psi.EIT;
import mpeg.psi.TOT;
import mpeg.psi.Table;
import mpeg.psi.TableList;
import sys.Log;
import sys.Messages;
import sys.Persistence;

public class Parameters {

	public static String initialMessage = ""; //$NON-NLS-1$

	static File srcFile;

	static InputStream bis = null;

	static public boolean noGui = false, noTree = false, noStats = false, batchResults = false;

	static String[] startArgs;

	private static FileInputStream fis;

	public static long skip(final long i) {
		try {
			return fis.skip(i);
		} catch (final IOException e) {
		}
		return 0;
	}

	public static InputStream getStream() {
		try {
			fis = new FileInputStream(srcFile);
			bis = new BufferedInputStream(fis, 4000000);
		} catch (final FileNotFoundException e) {
			initialMessage = Messages.getString("Parameters.fopenErr") + srcFile.getAbsolutePath() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			Log.printWarning(initialMessage);
			Log.printStackTrace(new Exception(initialMessage));
			return null;
		}
		return bis;
	}

	public static void startParser() {
		if (startArgs != null) startParser(startArgs);
	}

	public static void startParser(final String outFile, final String[] pids, final String op) {
		if (startArgs != null) {
			final String[] newParms = new String[startArgs.length + 2 + pids.length];
			System.arraycopy(startArgs, 0, newParms, 0, startArgs.length);
			final String[] oldParms = startArgs;
			newParms[startArgs.length] = op;
			newParms[startArgs.length + 1] = outFile;
			System.arraycopy(pids, 0, newParms, startArgs.length + 2, pids.length);
			preParse(newParms);
			startParser(newParms);
			startArgs = oldParms;
		}
	}

	public static void preParse(final String[] args) {
		if (args.length != 0 && args[0].equalsIgnoreCase("-help")) {
			printHelp();
			System.exit(0);
		}
		startArgs = args;
		final StringBuffer sb = new StringBuffer();
		sb.append(Messages.getString("Parameters.command")); //$NON-NLS-1$
		for (int i = 0; i < args.length; i++) {
			sb.append(args[i]);
			sb.append(", ");//$NON-NLS-1$
		}
		sb.append("]"); //$NON-NLS-1$
		for (int i = 1; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-noGui") || args[i].equalsIgnoreCase("-subtitles")) //$NON-NLS-1$
			noGui = true;
			if (args[i].equalsIgnoreCase("-noTree")) //$NON-NLS-1$
			noTree = true;
			if (args[i].equalsIgnoreCase("-onTheFly")) //$NON-NLS-1$
			onTheFly = true;
			if (args[i].equalsIgnoreCase("-noBlackList")) //$NON-NLS-1$
			noBlackList = true;
			if (args[i].equalsIgnoreCase("-noStats")) //$NON-NLS-1$
			noStats = true;
			if (args[i].equalsIgnoreCase("-help")) {
				printHelp();
				System.exit(0);
			}
		}
		Log.printWarning(sb.toString());
	}

	private static void printHelp() {
		System.out
				.println("SBTVD Transport Stream Parser v" + Persistence.CURRENT_SW_VERSION + "\nCopyright © 2010 Gabriel A. G. Marques\n" //$NON-NLS-1$ //$NON-NLS-2$
						+ "gabriel.marques@gmail.com");
		System.out.println("usage: java -Djava.library.path=/usr/lib/jni -jar tsp.jar [filename] [options]");
		System.out.println("\n[filename] is the TransportStream file to be parsed");
		System.out.println("options are:\n");
		System.out
				.println("\t-noGui: do not open a graphical interface, print parsing resylts to stdout - limited functionality");
		System.out.println("\t-noTree: supress PSI tree from the output");
		System.out.println("\t-noStats: supress table/bitrate statistics from the output");
		System.out
				.println("\t-forcePid 0xNNN TableName: force the informed PID to be parsed as TableName, if known - in case it's not referenced by other tables");
		System.out.println("\t\tTableName: one of AIT, CAT, DSMCC, EIT, IIP, NIT, PAT, PMT, SDT, SDTT, TOT or TSDT");
		System.out
				.println("\t-limitInput 'int': maximum number of TS packets to process, else the whole file is processed");
		System.out
				.println("\t-filter \"stringPattern\" to reduce the amount of displayed info, when present, the following tags will be considered");
		System.out
				.println("\t\t-limitMatches 'int': when present, only the informed N maches are shown (on GUI or stdOut)");
		System.out
				.println("\t\t-listOnlyMatches: when present, only the exact maches will be displayed, with no context info");
		System.out.println("\t\t-isRegex: when present, \"stringPattern\" is applied as regex");
		System.out.println("\t-demux: outputFileName PidToKeep1 PidToKeep2...");
		System.out
				.println("\t-crop: outputFileName startPos endPos //start and end are floats between 0.0 and 1.0, relative to file length");
		System.out
				.println("\t-subtitle 'int': will run the parser in noGui mode and output only the closed caption from the"
						+ " indicated program number (1 to 8) - the timestamps are relative to the PCR of the same program.");
		System.out.println("If the program number is zero, all the CCs will be parsed and printed, and the first PCR "
				+ "found will be used for all.");
		System.out.println("\t-onTheFly: print everithing as it is parsed");
		System.out.println("\t-skip 'int': skip this amout of seconds of the input once a PCR is found");
		System.out.println("\t-noBlackList: the parser will keep analysing a PID even in case of errors");
	}

	public static void startParser(final String[] args) {
		startArgs = args;
		if (args.length == 0) {
			Log.printWarning(Messages.getString("Parameters.noInput")); //$NON-NLS-1$
			printHelp();
			return;
		}
		final String srcPath = args[0];
		if (srcPath == null) return;
		srcFile = new File(srcPath);
		Log.setLogFile(new File(srcFile.getParentFile(), "log.txt")); //$NON-NLS-1$
		Log.setTsFile(srcFile);

		if (srcFile.getName().endsWith(".lnk")) try {
			final Link lnkp = new Link();
			lnkp.parse(srcFile.getAbsolutePath());
			srcFile = new File(lnkp.getFilePath());
			if (!srcFile.exists()) srcFile = new File(lnkp.getAlternatePath());
		} catch (final Exception e) {
			initialMessage = Messages.getString("Parameters.fopenErr") + srcFile.getAbsolutePath() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			Log.printStackTrace(new Exception(initialMessage));
			Log.printWarning(initialMessage);
		}

		bis = getStream();

		if (srcFile.getName().endsWith(".pes")) { //$NON-NLS-1$
			initMainPanel();
			final IndependentPES pes = new IndependentPES(bis);
			// TODO: set pes type by user input
			pes.start();
			return;
		}

		BufferedOutputStream bos = null;
		Packet.fileLenght = srcFile.length();
		Packet.estimate = Packet.fileLenght / TSP.TS_PACKET_LEN;
		// redefined when packet size is assured
		Packet.limit = 0;
		int filter = 0, forcePid = 0;
		int matchLimit = 0;
		MainPanel.setFilter(null);
		CC.reset();
		int[] demuxPids = null;
		float[] cropPoints = null;
		for (int i = 1; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-forcePid")) //$NON-NLS-1$
			forcePid = i;
			if (args[i].equalsIgnoreCase("-filter")) //$NON-NLS-1$
			filter = i;
			if (args[i].equalsIgnoreCase("-limitInput")) //$NON-NLS-1$
			Packet.limit = i;
			if (forcePid > 0 && forcePid == i - 2) if (args[i - 1].startsWith("0x")) {
				final int pid = Integer.parseInt(args[i - 1].substring(2), 16);
				final String table = "mpeg.psi." + args[i];
				System.out.println("assigned " + table + " for pid " + pid);
				try {
					final Class<?> cl = Class.forName(table);
					if (cl.getSuperclass() == Table.class) {
						final Constructor<?> c = cl.getConstructor(new Class[] { int.class });
						TableList.forceTable((Table) c.newInstance(new Object[] { pid }));
					} else System.out.println("Not a table");
				} catch (final Exception e) {
					System.out.println("ClassNotFoundException: " + e.getMessage());
				}
			}
			if (filter > 0 && filter == i - 1) MainPanel.setFilter(args[i]);
			if (Packet.limit > 0 && Packet.limit == i - 1) {
				Packet.limit = Long.parseLong(args[i]);
				Packet.estimate = Packet.limit;
			}
			if (matchLimit > 0 && matchLimit == i - 1) matchLimit = Integer.parseInt(args[i]);
			if (args[i].equalsIgnoreCase("-limitMatches")) //$NON-NLS-1$
			matchLimit = i;
			if (args[i].equalsIgnoreCase("-isRegex")) //$NON-NLS-1$
			MainPanel.setFilterAsRegex();
			if (args[i].equalsIgnoreCase("-subtitles")) {//$NON-NLS-1$
				CC.onlyCC = true;
				CC.singleProgramId = 1;
				// noGui = true; set on preparse
				if (args.length > i + 1) try {
					i++;
					CC.singleProgramId = Integer.parseInt(args[i]);
				} catch (final NumberFormatException e) {
				}
			}
			if (args[i].equalsIgnoreCase("-skip")) //$NON-NLS-1$
			if (args.length > i + 1) try {
				i++;
				PCR.skipSize = Integer.parseInt(args[i]);
			} catch (final NumberFormatException e) {
			}
			if (args[i].equalsIgnoreCase("-listOnlyMatches")) //$NON-NLS-1$
			MainPanel.listOnlyMatches();
			if (args[i].equalsIgnoreCase("-demux")) { //$NON-NLS-1$
				int j;
				for (j = i + 2; j < args.length && args[j].startsWith("0x"); j++) //$NON-NLS-1$
					;
				if (j == i + 2) {
					initialMessage = Messages.getString("Parameters.pidNotInformed"); //$NON-NLS-1$
					return;
				}
				demuxPids = new int[j - i - 2];
				for (int k = 0; k < demuxPids.length; k++)
					demuxPids[k] = Integer.parseInt(args[k + i + 2].substring(2), 16);
				// TODO:
				final File f = new File(args[i + 1]);
				if (f.exists()) f.delete();
				try {
					f.createNewFile();
					final FileOutputStream fos = new FileOutputStream(f);
					bos = new BufferedOutputStream(fos);
				} catch (final IOException e) {
					// TODO handle it
					e.printStackTrace();
				}
				i = j - 1;
			}
			if (args[i].equalsIgnoreCase("-crop")) { //$NON-NLS-1$
				cropPoints = new float[2];
				try {
					cropPoints[0] = Float.parseFloat(args[i + 2]);
					cropPoints[1] = Float.parseFloat(args[i + 3]);
				} catch (final NumberFormatException e1) {
					initialMessage = Messages.getString("Parameters.cropRange"); //$NON-NLS-1$
					return;
				}

				final File f = new File(args[i + 1]);
				if (f.exists()) f.delete();
				try {
					f.createNewFile();
					final FileOutputStream fos = new FileOutputStream(f);
					bos = new BufferedOutputStream(fos);
				} catch (final IOException e) { // TODO handle it
					e.printStackTrace();
				}
				i += 3;

			}
		}
		if (matchLimit > 0) MainPanel.setFilterLimit(matchLimit);
		else MainPanel.setFilterLimit(-1);
		// if (!noGui)
		if (!batchResults) MainPanel.clearTree();
		initMainPanel();
		TOT.reset();
		pp = new Packet(bis);
		if (demuxPids != null) {
			pp.filterPIDs = demuxPids;
			pp.bos = bos;
		}
		if (cropPoints != null) {
			pp.cropPoints = cropPoints;
			pp.bos = bos;
		}
		pp.start();
		if (noGui) synchronized (pp) {
			try {
				pp.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void initMainPanel() {
		MainPanel.setProgress(0);
		initialMessage = Messages.getString("Parameters.parsing") + srcFile.getAbsolutePath() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		MainPanel.tsNameId = initialMessage;
		MainPanel.tsNameIndex = MainPanel.addTreeItem(initialMessage, 0);
		Log.printWarning(initialMessage);
		MainPanel.setTitle(srcFile.getName());
	}

	public static void printStats() {
		if (noTree) System.out.println(initialMessage);
		if (!noStats) {
			final int statLevel = MainPanel.addTreeItem(
					"TS bitrate (bps): " + Math.round(PCR.getAverageBitrate() * 1000000), 0, MainPanel.STATS_TREE);
			// MainPanel.addTreeItem("TS bitrate (bps): ", statLevel,
			// MainPanel.STATS_TREE);
			((EIT) TableList.getByPid(EIT.FULLSEGPID)).printStatistics();
			((EIT) TableList.getByPid(EIT.FULLSEGPID)).printEPG();
			((EIT) TableList.getByPid(EIT.ONESEGPID)).printEPG();
			if (pp != null && pp.bitrateIsValid) pp.printBitrate();
			MainPanel.addTreeItem(Messages.getString("Parameters.syncLoss") + Packet.syncLosses, 0, MainPanel.STATS_TREE); //$NON-NLS-1$
			MainPanel.addTreeItem(Messages.getString("Parameters.tei") + Packet.TEIerrors, 0, MainPanel.STATS_TREE); //$NON-NLS-1$
			int continuityCount = 0;
			for (int i = 0; i < TableList.continuityErrorCounters.length; i++)
				continuityCount += TableList.continuityErrorCounters[i];
			final int cntLvl = MainPanel.addTreeItem(
					Messages.getString("Parameters.continuity") + continuityCount, 0, MainPanel.STATS_TREE); //$NON-NLS-1$
			for (int i = 0; i < TableList.continuityErrorCounters.length; i++)
				if (TableList.continuityErrorCounters[i] != 1) {
					final Table t = TableList.getByIndex(i);
					if (t != null) MainPanel.addTreeItem(t.name + ": " + TableList.continuityErrorCounters[i], cntLvl, //$NON-NLS-1$
							MainPanel.STATS_TREE);
					;
				}
		}
		// MainPanel.addTreeItem("parsing done", 0);
		// if (!noTree) MainPanel.printTree();
		// if (!noStats)
		// SimpleAssertions.checkSBTVDConformity(MainPanel.getTreeRoot());
		if (noGui) MainPanel.printTabsAsText();
		// System.exit(0);
	}

	public static boolean isAlive() {
		return pp.isAlive();
	}

	static Packet pp;

	public static boolean onTheFly = false;

	public static boolean noBlackList = false;
}
