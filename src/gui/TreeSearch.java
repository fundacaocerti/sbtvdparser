package gui;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.TreeItem;

final class TreeSearch implements KeyListener {

	private final MainPanel mainPanel;
	private String searchPattern;
	private final Vector<TreeItem> matches = new Vector<TreeItem>();
	Iterator<TreeItem> it;

	TreeSearch(final MainPanel mainPanel) {
		this.mainPanel = mainPanel;
	}

	public void keyReleased(final KeyEvent arg0) {
	}

	public void keyPressed(final KeyEvent arg0) {
		if (arg0.keyCode == SWT.CR || arg0.keyCode == SWT.KEYPAD_CR) {
			if (matches.size() == 0) {
				searchPattern = MainPanel.searchText.getText();
				final TreeItem[] chds = MainPanel.mainTree.getItems();
				for (int i = 0; i < chds.length; i++)
					recurse(chds[i]);
				MainPanel.statusBar.setText("Found " + matches.size() + " matches");
			}
			if (it == null) it = matches.iterator();
			TreeItem tit;
			try {
				tit = it.next();
				tit.setExpanded(true);
				tit.getParent().setSelection(tit);
			} catch (final NoSuchElementException e) {
				it = matches.iterator();
				MainPanel.statusBar.setText("Wrap search");
			}
		} else {
			matches.removeAllElements();
			it = null;
		}
	}

	private void recurse(final TreeItem tit) {
		if (tit.getText().indexOf(searchPattern) >= 0) matches.add(tit);
		final TreeItem[] chds = tit.getItems();
		for (int i = 0; i < chds.length; i++)
			recurse(chds[i]);
	}
}