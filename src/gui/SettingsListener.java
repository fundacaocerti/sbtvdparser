package gui;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MenuItem;

import sys.Messages;
import sys.Persistence;

public class SettingsListener implements SelectionListener {

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		Persistence.set(Persistence.UI_LANG_IDIOM, ((MenuItem)e.getSource()).getData().toString());
		if (Persistence.get(Persistence.UI_LANG_IDIOM).equals("pt"))
			Persistence.set(Persistence.UI_LANG_REGION, "BR");
		else
			Persistence.set(Persistence.UI_LANG_REGION, "US");
		Messages.load();
		MainPanel.setTexts();
	}

}
