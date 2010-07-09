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

import gui.dialogs.ButtonListener;
import gui.dialogs.CopyPopUp;
import gui.dialogs.DSMCCSavePopUp;
import gui.dialogs.FileDropListener;
import gui.dialogs.MenuAbout;
import gui.dialogs.MenuOpen;
import gui.dialogs.MenuSave;
import gui.dialogs.PIDSelection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import parsers.Packet;
import parsers.Parameters;
import sys.CRC32;
import sys.Log;
import sys.LogicTree;

public class MainPanel {

	public static Shell sShell = null; // @jve:decl-index=0:visual-constraint="10,10"

	private static Tree mainTree = null;
	
	private static Tree epgTree = null;
	
	public static Tree statsTree = null;
	
	public static Tree dsmccTree = null;
	
	public static Tree ccTree = null;

	public static boolean isOpen = true;

	private static Display display;

	private static Vector items = new Vector(); // @jve:decl-index=0:

	private static LogicTree[] trees = new LogicTree[5]; // @jve:decl-index=0:

	private Menu menuBar = null;

	private static boolean noGui = false;

	public static ProgressBar progressBar = null;

	public static Text inputLimit = null;

	private CLabel limitLabel = null;

	static GuiMethods gm = new GuiMethods(); // @jve:decl-index=0:

	public static boolean targetOK = false;

	public static Button btStop = null;
	public static Button btPause = null;

	private static String targetFilter;  //  @jve:decl-index=0:

	private static FileTransfer fileTransfer = FileTransfer.getInstance();

	public static Image imPause, imPlay; //VE idiota, não suporta declarações com vírgula.
	public static Image imStop;

	private TabFolder tabFolder = null;
	
	private TabItem psiTab = null;
	private TabItem epgTab = null;
	private TabItem statsTab = null;
	private TabItem ccTab = null;
	private TabItem dsmccTab = null;
	private TabItem logTab = null;
	private TabItem graphTab = null;
	
	public static Text log = null;
	
	public static Graph brGraph = null;
	
	public static Group bitrateArea = null;
	
	public static ScrolledComposite scrComp = null;

	private void createTabFolder() {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		gridData.verticalAlignment = GridData.FILL;
				
		tabFolder = new TabFolder(sShell, SWT.NONE);
		tabFolder.setLayoutData(gridData);
		
		mainTree = new Tree(tabFolder, SWT.BORDER);
		mainTree.setLayoutData(gridData);
		
		epgTree = new Tree(tabFolder, SWT.BORDER);
		epgTree.setLayoutData(gridData);
		
		log = new Text(tabFolder, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		log.setEditable(false);
		
		bitrateArea = new Group(tabFolder, SWT.BORDER);
		brGraph = new Graph(bitrateArea, SWT.BORDER);
		brGraph.setBounds(new Rectangle(3, 28, 288, 211));
		bitrateArea.setText("Gráficos de bitrates para PIDs relevantes");
//		brGraph.setLayoutData(gridData);
		createPidSelector();
		pidLabel = new CLabel(bitrateArea, SWT.NONE);
		pidLabel.setText("PID para análise:");
		pidLabel.setBounds(new Rectangle(3, 268, 93, 19));
		graphInfo = new CLabel(bitrateArea, SWT.NONE);
		graphInfo.setText("0.0Mbps - 20.0s");
		graphInfo.setBounds(new Rectangle(3, 238, 289, 23));
		
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
		
		psiTab.setText("Estrutura");
		epgTab.setText("EPG");
		statsTab.setText("Stats");
		ccTab.setText("Caption");
		dsmccTab.setText("DSM-CC");
		logTab.setText("Log");
		graphTab.setText("Bitrates");
		
		psiTab.setControl(mainTree);
		epgTab.setControl(epgTree);
		statsTab.setControl(scrComp);
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
		GridData gridData3 = new GridData();
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData3.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData3.verticalSpan = 0;
		gridData3.grabExcessVerticalSpace = true;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.widthHint = 200;
		gridData.verticalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;

		scrComp = new ScrolledComposite(tabFolder, SWT.V_SCROLL);
		scrComp.setLayout(new GridLayout());
		scrComp.setMinSize(400, 400);
		scrComp.setExpandHorizontal(true);
		scrComp.setExpandVertical(true);
		statsGroup = new Composite(scrComp, SWT.NONE);
		scrComp.setContent(statsGroup);
		statsGroup.setLayout(new GridLayout());
		statsTree = new Tree(statsGroup, SWT.BORDER);
		statsTree.setLayoutData(gridData);
		
		pidStats = new Group(statsGroup, SWT.V_SCROLL);
		pidStats.setText("PID bitrates");
		pidStats.setLayoutData(gridData3);
		pidStats.setLayout(gridLayout1);
	}

	/**
	 * This method initializes pidSelector	
	 *
	 */
	private void createPidSelector() {
		pidSelector = new Combo(bitrateArea, SWT.READ_ONLY);
		pidSelector.setBounds(new Rectangle(93, 268, 195, 21));
		pidSelector.addSelectionListener(new PIDSelection());
	}

	public static void main(String[] cmdArgs) {
		try {
			MainPanel thisClass = null;
//			if (cmdArgs.length == 0)
//				return;
			if (Parameters.noGui)
				noGui = true;
			if (!noGui) {
				display = Display.getDefault();
				thisClass = new MainPanel();
				thisClass.createSShell();
				sShell.open();
				thisClass.createDND();
			}
			// s.setPriority(3);
			CRC32.makeTable();
			Parameters.startParser(cmdArgs);

			while (thisClass != null && !sShell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			if (display != null)
				display.dispose();
			isOpen = false;
		} catch (RuntimeException e) {
			Log.printStackTrace(e);
		}
		System.exit(0);
	}

	private void createDND() {
		int dndOps = DND.DROP_LINK | DND.DROP_COPY | DND.DROP_DEFAULT;
		DropTarget target = new DropTarget(sShell, dndOps);
		target.setTransfer(new Transfer[] { fileTransfer });
		target.addDropListener(new FileDropListener());
	}

	private void createSShell() {
		GridData progressGridData = new GridData();
		progressGridData.grabExcessHorizontalSpace = true;
		progressGridData.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		progressGridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;

		sShell = new Shell();
		sShell.setText("SBTVD Stream Parser");
		sShell.setLayout(gridLayout);
		sShell.setSize(new Point(800, 600));
	
		menuBar = new Menu(sShell, SWT.BAR);
		sShell.setMenuBar(menuBar);
		MenuItem file = new MenuItem(menuBar, SWT.CASCADE);
		file.setText("&Arquivo");
		final Menu fileMenu = new Menu(sShell, SWT.DROP_DOWN);
		file.setMenu(fileMenu);
		
		MenuItem openItem = new MenuItem(fileMenu, SWT.PUSH);
		MenuItem openFilterItem = new MenuItem(fileMenu, SWT.PUSH);
		openItem.setText("&Abrir\tCTRL+A");
		openItem.setAccelerator(SWT.CTRL + 'A');
		MenuOpen openFileListener = new MenuOpen(sShell, openItem, openFilterItem);
		openItem.addSelectionListener(openFileListener);

		openFilterItem.setText("Abrir e &Filtrar\tCTRL+F");
		openFilterItem.setAccelerator(SWT.CTRL + 'F');
		openFilterItem.addSelectionListener(openFileListener);

		MenuItem openDirItem = new MenuItem(fileMenu, SWT.PUSH);
		openDirItem.setText("Analisar &Diretório\tCTRL+D");
		openDirItem.setAccelerator(SWT.CTRL + 'D');
		openDirItem.addSelectionListener(openFileListener);

		MenuItem saveItem = new MenuItem(fileMenu, SWT.PUSH);
		saveItem.setText("&Salvar\tCTRL+S");
		saveItem.setAccelerator(SWT.CTRL + 'S');
		MenuSave saveFileListener = new MenuSave(sShell);
		
		MenuItem about = new MenuItem(fileMenu, SWT.PUSH);
		about.setText("S&obre");
		about.addSelectionListener(new MenuAbout(sShell));
		
		limitLabel = new CLabel(sShell, SWT.NONE);
		limitLabel.setText("Limite:");
		inputLimit = new Text(sShell, SWT.BORDER);
		inputLimit
				.setToolTipText("Limita o número de pacotes (TSP) a serem lidos da stream. Use 0 ou vazio para ler o arquivo completo.");
		inputLimit.setTextLimit(10);
		btPause = new Button(sShell, SWT.NONE);
		InputStream isPause = this.getClass().getClassLoader()
				.getResourceAsStream("res/bot_pause.png");
		imPause = new Image(Display.getCurrent(), isPause);
		btPause.setImage(imPause);
		btPause.setToolTipText("Pausar");
		btPause.setEnabled(false);
		btPause.addSelectionListener(new ButtonListener());
		btStop = new Button(sShell, SWT.NONE);
		InputStream isStop = this.getClass().getClassLoader()
				.getResourceAsStream("res/bot_stop.png");
		imStop = new Image(Display.getCurrent(), isStop);
		btStop.setImage(imStop);
		btStop.setToolTipText("Parar");
		btStop.setEnabled(false);
		btStop.addSelectionListener(new ButtonListener());
		InputStream isPlay = this.getClass().getClassLoader()
				.getResourceAsStream("res/bot_play.png");
		imPlay = new Image(Display.getCurrent(), isPlay);
		progressBar = new ProgressBar(sShell, SWT.SMOOTH);
		progressBar.setMaximum(100);
		progressBar.setLayoutData(progressGridData);
		progressBar.setMinimum(0);

		createTabFolder();
		saveItem.addSelectionListener(saveFileListener);

		CopyPopUp mouseListener = new CopyPopUp(sShell, 80);
		mainTree.addListener(SWT.Selection, mouseListener);
		mainTree.addListener(SWT.MouseDown, mouseListener);
		
		DSMCCSavePopUp savePopUp = new DSMCCSavePopUp(sShell, 80);
		dsmccTree.addListener(SWT.Selection, savePopUp);
		dsmccTree.addListener(SWT.MouseDown, savePopUp);
		progressBar.addListener(SWT.MouseDown, mouseListener);
		clearTree();
	}

	public static void setProgress(int progress) {
		GuiMethods.runMethod(GuiMethods.SETPROGRESSBAR, new Integer(progress),
				true);
	}

	public static void getLimit() {
		if (noGui)
			return;
		GuiMethods.runMethod(GuiMethods.GETLIMITBOX, null, false);
	}

	public static void setLimit(long limit) {
		if (noGui)
			return;
		GuiMethods.runMethod(GuiMethods.SETLIMITBOX, new Long(limit), false);
	}

	public static void setTreeData(int indx, Object data) {
		((LogicTree) items.get(indx)).contents = data;
	}

	public static int addTreeItem(String content, int parent) {
		return addTreeItem(content, parent, PSI_TREE);
	}
	
	public static void changeTreeItem(String content, int index) {
		if (targetFilter != null)
			return;
		LogicTree lt = (LogicTree)(items.get(index));
		lt.text = content;
		lt.treeitem.setText(content);
	}
	
	static int filteredId = 0;
	public static String tsNameId = null;
	public static int tsNameIndex = 0;
	
	public static int addTreeItem(String content, int parent, int rootIndx) {
		if (targetOK) {
			int ancestor = parent;
			while (ancestor != filteredId && ancestor != 0) {
				LogicTree elderOne = ((LogicTree) items.get(ancestor)).parent;
				if (ancestor == elderOne.indx)
					break; //an item should not point to itself, but it does
				ancestor = elderOne.indx;
			}
			if (ancestor == 0) {
				targetOK = false;
				if (content.toLowerCase().indexOf(targetFilter.toLowerCase()) == -1)
					return 0;
			}
		}
		if (targetFilter != null)
			//if (filterIsRegex)
			if (content.toLowerCase().indexOf(targetFilter.toLowerCase()) != -1
					&& filterLimit != 0) {
				filteredId = items.size();
				targetOK = true;
				Vector reverse = new Vector();
				int p = parent;
				LogicTree topItem = null;
				int limit = 30;
				while (limit > 0 && p != 0) {
					topItem = (LogicTree) items.get(p);
					reverse.add(topItem);
					limit--;
					p = topItem.parent.indx;
				}
				if (topItem != null && p == 0) {
//					topItem.parent.indx = tsNameIndex;
					topItem.parent = (LogicTree) items.get(tsNameIndex);
				}
				for (int i = reverse.size(); i > 0; i--) {
					LogicTree lt = (LogicTree) reverse.get(i-1);
					if (!lt.isVisible)
						GuiMethods.runMethod(GuiMethods.ADDTREEITEM,  
							new Object[] {lt, new Integer(rootIndx)}, true);
//					((LogicTree) reverse.get(i-1)).treeitem.setExpanded(true);
				}
				filterLimit--;
			}
			//else if (!content.startsWith("Parsing"))
			//	return 0;

		LogicTree tit;
		if (parent == 0)
			tit = new LogicTree(content, trees[rootIndx], items.size());
		else
			tit = new LogicTree(content, (LogicTree) items.get(parent), items
					.size());
		items.add(tit);

		if (!noGui && (targetFilter == null || targetOK || 
				(content == tsNameId && !listOnlyMatches)))
			GuiMethods.runMethod(GuiMethods.ADDTREEITEM,  new Object[] {tit, new Integer(rootIndx)}, true);

		if (filterLimit == 0)
			Packet.limitNotReached = false;
		
		return items.size() - 1;
	}
//
//	public static LogicTree getTreeRoot() {
//		return treeRoot;
//	}

	public final static int PSI_TREE = 0, EPG_TREE = 1, STATS_TREE = 2, DSMCC_TREE = 3, CC_TREE = 4;

	public static Composite statsGroup = null;

	public static Group pidStats = null;

	public static Tree getTree(int rootIndx) {
		Tree trees[] = {mainTree, epgTree, statsTree, dsmccTree, ccTree};
		return trees[rootIndx];
	}

	public static void printTree() {
		if (display != null && !display.isDisposed())
			display.asyncExec(null);
		else
			try {
				trees[PSI_TREE].print(System.out);
				return;
			} catch (UnsupportedEncodingException e1) {
				Log.printStackTrace(e1);
			} catch (IOException e1) {
				Log.printStackTrace(e1);
			}
	}

	public static void guiThreadExec(Runnable r, boolean sync) {
		if (!noGui && display != null && !display.isDisposed())
			if (sync)
				display.syncExec(r);
			else
				display.asyncExec(r);
	}

	public static void clearTree() {
		if (!noGui) {
			Tree tmp[] = {mainTree, epgTree, statsTree, dsmccTree, ccTree};
			for (int i = 0; i < tmp.length; i++)
				GuiMethods.runMethod(GuiMethods.CLEARTREE, tmp[i], true);
			items.removeAllElements();
		}
		for (int i = 0; i < trees.length; i++)
			trees[i] = new LogicTree("root", null, 0);
//		TableList.resetList();
		GuiMethods.runMethod(GuiMethods.SETPROGRESSBAR, new Integer(0), false);
		PIDStats.clear();
	}

	public static void saveTree(String filePth) {
		try {
			File f = new File(filePth);
			if (f.exists())
				f.delete();
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			if (filePth.endsWith("htm"))
				trees[PSI_TREE].printBonsai(fos);
			else
				trees[PSI_TREE].print(fos);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			if (noGui)
				System.err.println(e.getMessage());
			else
				new TreeItem(mainTree, SWT.NONE).setText(e.getMessage());
		}
	}

	public static void setFilter(String filter) {
		targetFilter = filter;
		targetOK = false;
		filteredId = 0;
		tsNameId = null;
		filterIsRegex = false;
		listOnlyMatches = false;
		filterLimit = 1;
	}

	public static void setCursor(int type) {
		GuiMethods.runMethod(GuiMethods.SETCURSOR, new Integer(type), false);
	}
	
	public static void setTitle(String text) {
		GuiMethods.runMethod(GuiMethods.SETTITLE, text, true);
	}
	
	private static int filterLimit = 1;
	
	public static void setFilterLimit(int fLimit) {
		filterLimit = fLimit;
	}
	
	private static boolean filterIsRegex = false, listOnlyMatches = false;

	public static Combo pidSelector = null;

	private CLabel pidLabel = null;

	public static CLabel graphInfo = null;
	
	public static void setFilterAsRegex() {
		filterIsRegex = true;
		System.out.println("filterIsRegex "+filterIsRegex);
	}
	
	public static void listOnlyMatches() {
		listOnlyMatches = true;
	}
}
