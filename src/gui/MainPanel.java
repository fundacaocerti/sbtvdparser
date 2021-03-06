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

import gui.dialogs.About;
import gui.dialogs.CopyPopUp;
import gui.dialogs.Crop;
import gui.dialogs.DSMCCSavePopUp;
import gui.dialogs.Demux;
import gui.dialogs.Open;
import gui.dialogs.Save;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Vector;

import mpeg.pes.CC;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import parsers.Packet;
import parsers.Parameters;
import sys.BitWise;
import sys.Log;
import sys.LogicTree;
import sys.Messages;
import sys.Persistence;

public class MainPanel {

	private static Vector<String> msgCache = new Vector<String>();

	public static Shell sShell = null; // @jve:decl-index=0:visual-constraint="10,10"

	static Tree mainTree = null;

	private static Tree epgTree = null;

	public static Tree statsTree = null;

	public static Tree dsmccTree = null;

	public static Tree ccTree = null;

	public static boolean isOpen = true;

	private static Display display;

	private static Vector<LogicTree> items = new Vector<LogicTree>(); // @jve:decl-index=0:

	private static LogicTree[] trees = new LogicTree[5]; // @jve:decl-index=0:

	private Menu menuBar = null;

	public static MultiProgressBar progressBar = null;

	public static Text inputLimit = null;

	private static Label limitLabel = null;

	public static Text searchText = null;

	private static Label searchLabel = null;

	static GuiMethods gm = new GuiMethods(); // @jve:decl-index=0:

	public static boolean targetOK = false;

	private static Button btStop = null, btPause = null;

	private static String targetFilter; // @jve:decl-index=0:

	private static FileTransfer fileTransfer = FileTransfer.getInstance();

	static Image imPause, imPlay, imCrop, imStop;

	private TabFolder tabFolder = null;

	private static TabItem psiTab = null;
	private static TabItem epgTab = null;
	private static TabItem statsTab = null;
	private static TabItem ccTab = null;
	private static TabItem dsmccTab = null;
	private static TabItem logTab = null;
	private static TabItem graphTab = null;

	public static Label statusBar = null;

	public static Text log = null;

	public static Graph brGraph = null;

	public static Group bitrateArea = null;

	public static ScrolledComposite scrComp = null;

	private void createTabFolder() {
		final GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 7;
		gridData.verticalAlignment = GridData.FILL;

		tabFolder = new TabFolder(sShell, SWT.NONE);
		tabFolder.setLayoutData(gridData);

		mainTree = new Tree(tabFolder, SWT.BORDER);
		mainTree.setLayoutData(gridData);

		epgTree = new Tree(tabFolder, SWT.BORDER);
		epgTree.setLayoutData(gridData);

		log = new Text(tabFolder, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		log.setEditable(false);

		createBitRateGraphTab();

		createStatsGroup();

		ccTree = new Tree(tabFolder, SWT.BORDER);
		ccTree.setLayoutData(gridData);

		dsmccTree = new Tree(tabFolder, SWT.BORDER);
		dsmccTree.setLayoutData(gridData);

		psiTab = new TabItem(tabFolder, SWT.NULL);
		epgTab = new TabItem(tabFolder, SWT.NULL);
		statsTab = new TabItem(tabFolder, SWT.NULL);
		ccTab = new TabItem(tabFolder, SWT.NULL);
		dsmccTab = new TabItem(tabFolder, SWT.NULL);
		logTab = new TabItem(tabFolder, SWT.NULL);
		graphTab = new TabItem(tabFolder, SWT.NULL);

		psiTab.setControl(mainTree);
		epgTab.setControl(epgTree);
		statsTab.setControl(statsTree);
		ccTab.setControl(ccTree);
		dsmccTab.setControl(dsmccTree);
		logTab.setControl(log);
		graphTab.setControl(bitrateArea);
	}

	/**
	 * This method initializes statsGroup
	 * 
	 */
	private void createStatsGroup() {
		final GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.widthHint = 200;
		gridData.verticalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;

		statsGroup = new Composite(scrComp, SWT.NONE);

		statsGroup.setLayout(new GridLayout());
		statsTree = new Tree(tabFolder, SWT.BORDER);
		statsTree.setLayoutData(gridData);
	}

	private void createBitRateGraphTab() {
		bitrateArea = new Group(tabFolder, SWT.BORDER);
		bitrateArea.setLayout(new GridLayout());
		// ((RowLayout) bitrateArea.getLayout()).type = SWT.VERTICAL;
		brGraph = new Graph(bitrateArea, SWT.BORDER);
		brGraph.setLayoutData(new GridData(300, 240));
		pidSelector = new Combo(bitrateArea, SWT.READ_ONLY);
		pidSelector.setLayoutData(new GridData(260, 30));
		pidSelector.addSelectionListener(new PIDSelection());
		pidLabel = new Label(bitrateArea, SWT.NONE);
		graphInfo = new Label(bitrateArea, SWT.NONE);

		scrComp = new ScrolledComposite(bitrateArea, SWT.V_SCROLL);
		scrComp.setLayout(new GridLayout());
		scrComp.setMinSize(400, 400);
		scrComp.setExpandHorizontal(true);
		scrComp.setExpandVertical(true);
		pidStats = new Group(scrComp, SWT.V_SCROLL);
		scrComp.setContent(pidStats);
		final GridData gridData3 = new GridData();
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData3.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData3.verticalSpan = 0;
		gridData3.grabExcessVerticalSpace = true;
		final GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		pidStats.setLayout(gridLayout1);
		pidStats.setLayoutData(gridData3);
		scrComp.setLayoutData(gridData3);
	}

	public void initialize() {
		if (!Parameters.noGui) {
			display = Display.getDefault();
			createSShell();
			sShell.open();
			sShell.layout(true, true);
			sShell.pack();
			createDND();
		}
		for (int i = 0; i < msgCache.size(); i++)
			addTreeItem(msgCache.get(i).toString(), 0);
		msgCache.removeAllElements();
	}

	public void handleEvents() {
		while (!sShell.isDisposed())
			if (!display.readAndDispatch()) display.sleep();
	}

	public void dispose() {
		try { // in case the display is already disposed
			if (display != null) display.dispose();
		} catch (final org.eclipse.swt.SWTException e) {
		}
		isOpen = false;
	}

	private void createDND() {
		final int dndOps = DND.DROP_LINK | DND.DROP_COPY | DND.DROP_DEFAULT;
		final DropTarget target = new DropTarget(sShell, dndOps);
		target.setTransfer(new Transfer[] { fileTransfer });
		target.addDropListener(new FileDropListener());
	}

	private static MenuItem file, settings, openItem, openFilterItem, openDirItem, saveItem, about, langPtItem,
			langEnItem, tools, demux, crop;

	private void createSShell() {
		final GridData progressGridData = new GridData();
		progressGridData.grabExcessHorizontalSpace = true;
		progressGridData.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		progressGridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 7;

		sShell = new Shell();
		sShell.setLayout(gridLayout);
		sShell.setSize(new Point(800, 400));

		menuBar = new Menu(sShell, SWT.BAR);
		sShell.setMenuBar(menuBar);
		file = new MenuItem(menuBar, SWT.CASCADE);
		final Menu fileMenu = new Menu(sShell, SWT.DROP_DOWN);
		file.setMenu(fileMenu);

		settings = new MenuItem(menuBar, SWT.CASCADE);
		final Menu setMenu = new Menu(sShell, SWT.DROP_DOWN);
		settings.setMenu(setMenu);
		langPtItem = new MenuItem(setMenu, SWT.PUSH);
		langEnItem = new MenuItem(setMenu, SWT.PUSH);

		tools = new MenuItem(menuBar, SWT.CASCADE);
		final Menu toolsMenu = new Menu(sShell, SWT.DROP_DOWN);
		tools.setMenu(toolsMenu);
		demux = new MenuItem(toolsMenu, SWT.PUSH);
		crop = new MenuItem(toolsMenu, SWT.PUSH);
		final Demux demuxListener = new Demux(sShell);
		demux.addSelectionListener(demuxListener);
		final Crop cropListener = new Crop(sShell);
		crop.addSelectionListener(cropListener);

		final SettingsListener setLstnr = new SettingsListener();
		langPtItem.addSelectionListener(setLstnr);
		langEnItem.addSelectionListener(setLstnr);
		langEnItem.setData("en"); //$NON-NLS-1$
		langPtItem.setData("pt"); //$NON-NLS-1$

		openItem = new MenuItem(fileMenu, SWT.PUSH);
		openFilterItem = new MenuItem(fileMenu, SWT.PUSH);
		openItem.setAccelerator(SWT.CTRL + 'A');
		final Open openFileListener = new Open(sShell, openItem, openFilterItem);
		openItem.addSelectionListener(openFileListener);

		openFilterItem.setAccelerator(SWT.CTRL + 'F');
		openFilterItem.addSelectionListener(openFileListener);

		openDirItem = new MenuItem(fileMenu, SWT.PUSH);
		openDirItem.setAccelerator(SWT.CTRL + 'D');
		openDirItem.addSelectionListener(openFileListener);

		saveItem = new MenuItem(fileMenu, SWT.PUSH);
		saveItem.setAccelerator(SWT.CTRL + 'S');
		final Save saveFileListener = new Save(sShell);

		about = new MenuItem(fileMenu, SWT.PUSH);
		about.addSelectionListener(new About(sShell));

		limitLabel = new Label(sShell, SWT.NONE);
		inputLimit = new Text(sShell, SWT.BORDER);
		inputLimit.setTextLimit(10);
		btPause = new Button(sShell, SWT.NONE);
		final InputStream isPause = this.getClass().getClassLoader().getResourceAsStream("res/bot_pause.png"); //$NON-NLS-1$
		imPause = new Image(Display.getCurrent(), isPause);
		btPause.addSelectionListener(new ButtonListener());
		btPause.setData("pause"); //$NON-NLS-1$
		btStop = new Button(sShell, SWT.NONE);
		btStop.setData("stop"); //$NON-NLS-1$
		final InputStream isStop = this.getClass().getClassLoader().getResourceAsStream("res/bot_stop.png"); //$NON-NLS-1$
		imStop = new Image(Display.getCurrent(), isStop);
		btStop.setImage(imStop);
		btStop.setEnabled(false);
		btStop.addSelectionListener(new ButtonListener());
		final InputStream isPlay = this.getClass().getClassLoader().getResourceAsStream("res/bot_play.png"); //$NON-NLS-1$
		imPlay = new Image(Display.getCurrent(), isPlay);
		final InputStream isCrop = this.getClass().getClassLoader().getResourceAsStream("res/bot_crop.png"); //$NON-NLS-1$
		imCrop = new Image(Display.getCurrent(), isCrop);
		progressBar = new MultiProgressBar(sShell, SWT.BORDER, 500);
		// progressBar.setLayoutData(progressGridData);

		searchLabel = new Label(sShell, SWT.NONE);
		searchText = new Text(sShell, SWT.BORDER);
		searchText.addKeyListener(new TreeSearch(this));

		createTabFolder();
		saveItem.addSelectionListener(saveFileListener);

		final CopyPopUp mouseListener = new CopyPopUp(sShell, 80);
		mainTree.addListener(SWT.Selection, mouseListener);
		mainTree.addListener(SWT.MouseDown, mouseListener);

		final DSMCCSavePopUp savePopUp = new DSMCCSavePopUp(sShell, 80);
		dsmccTree.addListener(SWT.Selection, savePopUp);
		dsmccTree.addListener(SWT.MouseDown, savePopUp);
		progressBar.addListener(SWT.MouseDown, mouseListener);

		statusBar = new Label(sShell, SWT.LEFT);
		statusBar.setText(" ");
		statusBar.computeSize(50, 50);
		final GridData statusGd = new GridData();
		statusGd.horizontalSpan = 7;
		statusGd.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;

		statusBar.setLayoutData(statusGd);
		clearTree();
		setTexts();
	}

	public static final int PLAING = 0, PAUSED = 1, CROP_WAIT = 2, STOPPED = 3;

	public static void setPauseButtonState(final int state) {
		final String[] tooltips = { Messages.getString("MainPanel.pauseTip"), //$NON-NLS-1$
				Messages.getString("MainPanel.playTip"), //$NON-NLS-1$
				Messages.getString("MainPanel.cropTip"), //$NON-NLS-1$
				Messages.getString("MainPanel.openPlayTip") //$NON-NLS-1$
		};
		final Image[] images = { imPause, imPlay, imCrop, imPlay };
		btPause.setToolTipText(tooltips[state]);
		btPause.setImage(images[state]);
		if (state == PLAING || state == PAUSED) btStop.setEnabled(true);
		else btStop.setEnabled(false);
	}

	public static void setTexts() {
		limitLabel.setText(Messages.getString("MainPanel.limit")); //$NON-NLS-1$
		searchLabel.setText(Messages.getString("MainPanel.search")); //$NON-NLS-1$
		sShell.setText(Messages.getString("MainPanel.shellTitle")); //$NON-NLS-1$
		inputLimit.setToolTipText(Messages.getString("MainPanel.limitTip")); //$NON-NLS-1$
		setPauseButtonState(STOPPED);
		btStop.setToolTipText(Messages.getString("MainPanel.stopTip")); //$NON-NLS-1$

		pidStats.setText(Messages.getString("MainPanel.pidRates")); //$NON-NLS-1$
		bitrateArea.setText(Messages.getString("MainPanel.grafTitle")); //$NON-NLS-1$
		pidLabel.setText(Messages.getString("MainPanel.pid")); //$NON-NLS-1$
		graphInfo.setText("0.0Mbps - 20.0s"); //$NON-NLS-1$
		psiTab.setText(Messages.getString("MainPanel.struct")); //$NON-NLS-1$
		epgTab.setText(Messages.getString("MainPanel.epg")); //$NON-NLS-1$
		statsTab.setText(Messages.getString("MainPanel.stats")); //$NON-NLS-1$
		ccTab.setText(Messages.getString("MainPanel.caption")); //$NON-NLS-1$
		dsmccTab.setText(Messages.getString("MainPanel.dsmcc")); //$NON-NLS-1$
		logTab.setText(Messages.getString("MainPanel.log")); //$NON-NLS-1$
		graphTab.setText(Messages.getString("MainPanel.bitrates")); //$NON-NLS-1$
		file.setText(Messages.getString("MainPanel.fileMenu")); //$NON-NLS-1$
		settings.setText(Messages.getString("MainPanel.settingsMenu")); //$NON-NLS-1$
		openItem.setText(Messages.getString("MainPanel.open")); //$NON-NLS-1$
		openFilterItem.setText(Messages.getString("MainPanel.openFilter")); //$NON-NLS-1$
		openDirItem.setText(Messages.getString("MainPanel.openDir")); //$NON-NLS-1$
		saveItem.setText(Messages.getString("MainPanel.save")); //$NON-NLS-1$
		about.setText(Messages.getString("MainPanel.about")); //$NON-NLS-1$
		if (Persistence.get(Persistence.UI_LANG_IDIOM).equals("pt")) { //$NON-NLS-1$
			langPtItem.setText("● " + Messages.getString("MainPanel.langPt")); //$NON-NLS-1$ //$NON-NLS-2$
			langEnItem.setText("   " + Messages.getString("MainPanel.langEn")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			langEnItem.setText("● " + Messages.getString("MainPanel.langEn")); //$NON-NLS-1$ //$NON-NLS-2$
			langPtItem.setText("   " + Messages.getString("MainPanel.langPt")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		tools.setText(Messages.getString("MainPanel.toolsMenu")); //$NON-NLS-1$
		demux.setText(Messages.getString("MainPanel.demux")); //$NON-NLS-1$
		crop.setText(Messages.getString("MainPanel.crop")); //$NON-NLS-1$
	}

	public static void setProgress(final float progress) {
		GuiMethods.runMethod(GuiMethods.SETPROGRESSBAR, new Float(progress), true);
	}

	public static void getLimit() {
		if (Parameters.noGui) return;
		GuiMethods.runMethod(GuiMethods.GETLIMITBOX, null, false);
	}

	public static void setLimit(final long limit) {
		if (Parameters.noGui) return;
		GuiMethods.runMethod(GuiMethods.SETLIMITBOX, new Long(limit), false);
	}

	public static void setTreeData(final int indx, final Object data) {
		items.get(indx).contents = data;
	}

	public static int addTreeItem(final String content, final int parent) {
		return addTreeItem(content, parent, PSI_TREE);
	}

	// this messages can be added before the MainPanel is created, and will be
	// shown later
	public static void cacheMessage(final String content) {
		msgCache.add(content);
	}

	public static void changeTreeItem(final String content, final int index) {
		if (targetFilter != null) return;
		final LogicTree lt = items.get(index);
		lt.text = content;
		lt.treeitem.setText(content);
	}

	static int filteredId = 0;
	public static String tsNameId = null;
	public static int tsNameIndex = 0;

	public static int addTreeItem(String content, final int parent, final int rootIndx) {
		if (targetOK) {
			int ancestor = parent;
			while (ancestor != filteredId && ancestor != 0) {
				final LogicTree elderOne = items.get(ancestor).parent;
				if (ancestor == elderOne.indx) break; // an item should not point to itself, but it does
				ancestor = elderOne.indx;
			}
			if (ancestor == 0) {
				targetOK = false;
				if (content.toLowerCase().indexOf(targetFilter.toLowerCase()) == -1) return 0;
			}
		}
		if (targetFilter != null) if (filterIsRegex) {
			if (content.matches(targetFilter) && filterLimit != 0) {
				content += Messages.getString("MainPanel.match"); //$NON-NLS-1$
				addMatchingItem(parent, rootIndx);
			}
		} else
			if (content.toLowerCase().indexOf(targetFilter.toLowerCase()) != -1 && filterLimit != 0) {
				content += Messages.getString("MainPanel.match"); //$NON-NLS-1$
				addMatchingItem(parent, rootIndx);
			}

		LogicTree tit;
		if (parent == 0) tit = new LogicTree(content, trees[rootIndx], items.size());
		else tit = new LogicTree(content, items.get(parent), items.size());
		items.add(tit);

		if (!Parameters.noGui && (targetFilter == null || targetOK || content == tsNameId && !listOnlyMatches)) GuiMethods
				.runMethod(GuiMethods.ADDTREEITEM, new Object[] { tit, new Integer(rootIndx) }, true);

		// TODO: remove this flag and add a listener for the limit-box
		if (filterLimit == 0) Packet.limitNotReached = false;
		if (Parameters.onTheFly) System.out.println(tit);
		return items.size() - 1;
	}

	private static void addMatchingItem(final int parent, final int rootIndx) {
		filteredId = items.size();
		targetOK = true;
		final Vector<LogicTree> reverse = new Vector<LogicTree>();
		int p = parent;
		LogicTree topItem = null;
		int limit = 30;
		while (limit > 0 && p != 0) {
			topItem = items.get(p);
			reverse.add(topItem);
			limit--;
			p = topItem.parent.indx;
		}
		if (topItem != null && p == 0) topItem.parent = items.get(tsNameIndex);
		for (int i = reverse.size(); i > 0; i--) {
			final LogicTree lt = reverse.get(i - 1);
			if (!lt.isVisible) GuiMethods.runMethod(GuiMethods.ADDTREEITEM, new Object[] { lt, new Integer(rootIndx) },
					true);
		}
		filterLimit--;
	}

	public final static int PSI_TREE = 0, EPG_TREE = 1, STATS_TREE = 2, DSMCC_TREE = 3, CC_TREE = 4;

	public static Composite statsGroup = null;

	public static Group pidStats = null;

	public static Tree getTree(final int rootIndx) {
		final Tree trees[] = { mainTree, epgTree, statsTree, dsmccTree, ccTree };
		return trees[rootIndx];
	}

	public static void printTree(final OutputStream fos, final String filePth) throws IOException {
		fos.write(0xEF);
		fos.write(0xBB);
		fos.write(0xBF);
		final String[] treeNames = { Messages.getString("MainPanel.struct"), Messages.getString("MainPanel.epg"), //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("MainPanel.stats"), Messages.getString("MainPanel.dsmcc"), //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("MainPanel.caption") }; //$NON-NLS-1$
		if (filePth.endsWith("htm")) //$NON-NLS-1$
		for (int i = 0; i < trees.length; i++)
			trees[i].printBonsai(fos, treeNames[i]);
		else
			if (filePth.endsWith("xml")) //$NON-NLS-1$
			trees[PSI_TREE].printXML(fos);
			else
				if (!Parameters.onTheFly) if (CC.onlyCC) trees[MainPanel.CC_TREE].print(fos);
				else {
					for (int i = 0; i < trees.length; i++) {
						fos.write("****".getBytes()); //$NON-NLS-1$
						fos.write(treeNames[i].getBytes());
						fos.write("****\n".getBytes()); //$NON-NLS-1$
						trees[i].print(fos);
						fos.write("\n\n\n".getBytes()); //$NON-NLS-1$
					}
					fos.write("**** Bitrates ****\n".getBytes());
					final Iterator<Object[]> it = sys.PIDStats.bars.iterator();
					while (it.hasNext()) {
						final Object[] bar = it.next();
						final float percent = (Integer) bar[1] * 1.0f / (Integer) bar[2];
						final int i = Math.round(percent * 32);
						for (int j = 0; j < 32; j++)
							if (j < i) fos.write('#');
							else fos.write('_');
						fos.write((" pid " + BitWise.toHex((Integer) bar[0]) + ": " + bar[3] + "\n").getBytes());
					}
				}
		fos.flush();
		fos.close();
	}

	public static void printTabsAsText() {
		try {
			printTree(System.out, ".txt");
		} catch (final UnsupportedEncodingException e1) {
			Log.printStackTrace(e1);
		} catch (final IOException e1) {
			Log.printStackTrace(e1);
		}
	}

	public static void guiThreadExec(final Runnable r, final boolean sync) {
		if (!Parameters.noGui && display != null && !display.isDisposed() && r != null) if (sync) display.syncExec(r);
		else display.asyncExec(r);
	}

	public static void clearTree() {
		if (!Parameters.noGui) {
			final Tree tmp[] = { mainTree, epgTree, statsTree, dsmccTree, ccTree };
			for (int i = 0; i < tmp.length; i++)
				GuiMethods.runMethod(GuiMethods.CLEARTREE, tmp[i], true);
			items.removeAllElements();
			GuiMethods.runMethod(GuiMethods.SETPROGRESSBAR, new Integer(0), false);
			PIDStats.clear();
		}
		for (int i = 0; i < trees.length; i++)
			trees[i] = new LogicTree("root", null, 0); //$NON-NLS-1$
		// TableList.resetList();
	}

	public static void saveTree(final String filePth) {
		try {
			final File f = new File(filePth);
			if (f.exists()) f.delete();
			f.createNewFile();
			final FileOutputStream fos = new FileOutputStream(f);
			printTree(fos, filePth);
		} catch (final Exception e) {
			if (Parameters.noGui) System.err.println(e.getMessage());
			else new TreeItem(mainTree, SWT.NONE).setText(e.getMessage());
		}
	}

	public static void setFilter(final String filter) {
		targetFilter = filter;
		targetOK = false;
		filteredId = 0;
		tsNameId = null;
		filterIsRegex = false;
		listOnlyMatches = false;
		filterLimit = 1;
	}

	public static void setCursor(final int type) {
		GuiMethods.runMethod(GuiMethods.SETCURSOR, new Integer(type), false);
	}

	public static void setTitle(final String text) {
		GuiMethods.runMethod(GuiMethods.SETTITLE, text, true);
	}

	private static int filterLimit = 1;

	public static void setFilterLimit(final int fLimit) {
		filterLimit = fLimit;
	}

	private static boolean filterIsRegex = false, listOnlyMatches = false;

	public static Combo pidSelector = null;

	private static Label pidLabel = null;

	public static Label graphInfo = null;

	public static void setFilterAsRegex() {
		filterIsRegex = true;
	}

	public static void listOnlyMatches() {
		listOnlyMatches = true;
	}
}