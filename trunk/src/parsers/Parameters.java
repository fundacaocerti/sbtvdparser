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
import java.lang.reflect.InvocationTargetException;

import mpeg.TSP;
import mpeg.pes.CC;
import mpeg.psi.EIT;
import mpeg.psi.TOT;
import mpeg.psi.Table;
import mpeg.psi.TableList;
import sys.Log;
import sys.Messages;

public class Parameters {

	public static String initialMessage = ""; //$NON-NLS-1$

	static File srcFile;

	static InputStream bis = null;

	static public boolean noGui = false, noTree = false, noStats = false, batchResults = false;

	static String[] startArgs;

	public static InputStream getStream() {
		try {
			bis = new BufferedInputStream(new FileInputStream(srcFile), 4000000);
		} catch (FileNotFoundException e) {
			initialMessage = Messages.getString("Parameters.fopenErr") + srcFile.getAbsolutePath() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			Log.printWarning(initialMessage);
			Log.printStackTrace(new Exception(initialMessage));
			return null;
		}
		return bis;
	}

	public static void startParser() {
		if (startArgs != null)
			startParser(startArgs);
	}

	public static void startParser(String outFile, String[] pids, String op) {
		if (startArgs != null) {
			String[] newParms = new String[startArgs.length + 2 + pids.length];
			System.arraycopy(startArgs, 0, newParms, 0, startArgs.length);
			String[] oldParms = startArgs;
			newParms[startArgs.length] = op;
			newParms[startArgs.length + 1] = outFile;
			System.arraycopy(pids, 0, newParms, startArgs.length + 2, pids.length);
			startParser(newParms);
			startArgs = oldParms;
		}
	}

	public static void startParser(String[] args) {
		startArgs = args;
		StringBuffer sb = new StringBuffer();
		sb.append(Messages.getString("Parameters.command")); //$NON-NLS-1$
		for (int i = 0; i < args.length; i++) {
			sb.append(args[i]);
			sb.append(", ");//$NON-NLS-1$
		}
		sb.append("]"); //$NON-NLS-1$
		Log.printWarning(sb.toString());
		if (args.length == 0) {
			Log.printWarning(Messages.getString("Parameters.noInput")); //$NON-NLS-1$
			return;
		}
		String srcPath = args[0];
		if (srcPath == null)
			return;
		srcFile = new File(srcPath);
		Log.setLogFile(new File(srcFile.getParentFile(), "log.txt")); //$NON-NLS-1$
		Log.setTsFile(srcFile);

		if (srcFile.getName().endsWith(".lnk")) { //$NON-NLS-1$
			try {
				Link lnkp = new Link();
				lnkp.parse(srcFile.getAbsolutePath());
				srcFile = new File(lnkp.getFilePath());
				if (!srcFile.exists())
					srcFile = new File(lnkp.getAlternatePath());
			} catch (Exception e) {
				initialMessage = Messages.getString("Parameters.fopenErr") + srcFile.getAbsolutePath() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
				Log.printStackTrace(new Exception(initialMessage));
				Log.printWarning(initialMessage);
				return;
			}
		}

		if (!srcFile.exists() || !srcFile.isFile()) {
			initialMessage = Messages.getString("Parameters.fNotFound") + srcFile.getAbsolutePath() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			Log.printStackTrace(new Exception(initialMessage));
			Log.printWarning(initialMessage);
			return;
		}

		bis = getStream();

		if (srcFile.getName().endsWith(".pes")) { //$NON-NLS-1$
			initMainPanel();
			IndependentPES pes = new IndependentPES(bis);
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
			if (args[i].equalsIgnoreCase("-noGui")) //$NON-NLS-1$
				noGui = true;
			if (args[i].equalsIgnoreCase("-noTree")) //$NON-NLS-1$
				noTree = true;
			if (args[i].equalsIgnoreCase("-noStats")) //$NON-NLS-1$
				noStats = true;
			if (args[i].equalsIgnoreCase("-forcePid")) //$NON-NLS-1$
				forcePid = i;
			if (args[i].equalsIgnoreCase("-filter")) //$NON-NLS-1$
				filter = i;
			if (args[i].equalsIgnoreCase("-limitInput")) //$NON-NLS-1$
				Packet.limit = i;
			if (forcePid > 0 && forcePid == i - 2) {
				if (args[i - 1].startsWith("0x")) {
					int pid = Integer.parseInt(args[i - 1].substring(2), 16);
					String table = "mpeg.psi." + args[i];
					System.out.println(table + " for pid " + pid);
					try {
						Class<?> cl = Class.forName(table);
						if (cl.getSuperclass() == Table.class) {
							Constructor<?> c = cl.getConstructor(new Class[] { int.class });
							TableList.forceTable((Table) c.newInstance(new Object[] { pid }));
							break;
						} else
							System.out.println("Not a table");
					} catch (ClassNotFoundException e) {
						System.out.println("ClassNotFoundException: " + e.getMessage());
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Class<?> cl;
					try {
						cl = Class.forName(table);
						TableList.forceTable((Table) cl.newInstance());
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (filter > 0 && filter == i - 1)
				MainPanel.setFilter(args[i]);
			if (Packet.limit > 0 && Packet.limit == i - 1) {
				Packet.limit = Long.parseLong(args[i]);
				Packet.estimate = Packet.limit;
			}
			if (matchLimit > 0 && matchLimit == i - 1)
				matchLimit = Integer.parseInt(args[i]);
			if (args[i].equalsIgnoreCase("-limitMatches")) //$NON-NLS-1$
				matchLimit = i;
			if (args[i].equalsIgnoreCase("-isRegex")) //$NON-NLS-1$
				MainPanel.setFilterAsRegex();
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
				File f = new File(args[i + 1]);
				if (f.exists())
					f.delete();
				try {
					f.createNewFile();
					FileOutputStream fos = new FileOutputStream(f);
					bos = new BufferedOutputStream(fos);
				} catch (IOException e) {
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
				} catch (NumberFormatException e1) {
					initialMessage = Messages.getString("Parameters.cropRange"); //$NON-NLS-1$
					return;
				}

				File f = new File(args[i + 1]);
				if (f.exists())
					f.delete();
				try {
					f.createNewFile();
					FileOutputStream fos = new FileOutputStream(f);
					bos = new BufferedOutputStream(fos);
				} catch (IOException e) { // TODO handle it
					e.printStackTrace();
				}
				i += 3;

			}
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
		if (demuxPids != null) {
			pp.filterPIDs = demuxPids;
			pp.bos = bos;
		}
		if (cropPoints != null) {
			pp.cropPoints = cropPoints;
			pp.bos = bos;
		}
		pp.start();
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
		if (noTree)
			System.out.println(initialMessage);
		if (!noStats) {
			((EIT) TableList.getByPid(EIT.FULLSEGPID)).printStatistics();
			((EIT) TableList.getByPid(EIT.FULLSEGPID)).printEPG();
			((EIT) TableList.getByPid(EIT.ONESEGPID)).printEPG();
			if (pp != null && pp.bitrateIsValid)
				pp.printBitrate();
			MainPanel.addTreeItem(
					Messages.getString("Parameters.syncLoss") + Packet.syncLosses, 0, MainPanel.STATS_TREE); //$NON-NLS-1$
			MainPanel.addTreeItem(Messages.getString("Parameters.tei") + Packet.TEIerrors, 0, MainPanel.STATS_TREE); //$NON-NLS-1$
			int continuityCount = 0;
			for (int i = 0; i < TableList.continuityErrorCounters.length; i++) {
				continuityCount += TableList.continuityErrorCounters[i];
			}
			int cntLvl = MainPanel.addTreeItem(
					Messages.getString("Parameters.continuity") + continuityCount, 0, MainPanel.STATS_TREE); //$NON-NLS-1$
			for (int i = 0; i < TableList.continuityErrorCounters.length; i++)
				if (TableList.continuityErrorCounters[i] != 1) {
					Table t = TableList.getByIndex(i);
					if (t != null)
						MainPanel.addTreeItem(t.name + ": " + TableList.continuityErrorCounters[i], cntLvl, //$NON-NLS-1$
								MainPanel.STATS_TREE);
					;
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
