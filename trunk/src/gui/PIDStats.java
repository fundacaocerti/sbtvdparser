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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

public class PIDStats {

	static int barCount = 0;

	public static void addBar(int pid, int percent, int maxPercent, String name) {
		// percent = (int)(Math.log(percent)*100);
		// maxPercent = (int)(Math.log(maxPercent)*100);
		// System.out.println(Math.log(percent)+"/"+Math.log(maxPercent)+" "+name);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		Label label = new Label(MainPanel.pidStats, SWT.NONE);
		String pidText = "000" + Integer.toHexString(pid); //$NON-NLS-1$
		pidText = "0x" + pidText.substring(pidText.length() - 4, pidText.length()); //$NON-NLS-1$
		label.setText(pidText);
		ProgressBar pb = new ProgressBar(MainPanel.pidStats, SWT.SMOOTH);
		pb.setMaximum(maxPercent);
		pb.setSelection(percent);
		pb.setToolTipText(name);
		pb.setLayoutData(gd);
		barCount++;
		MainPanel.statsTree.pack(true);
		MainPanel.statsGroup.pack(true);
		MainPanel.scrComp.setMinSize(400, 100 + barCount * 24);
		MainPanel.pidSelector.add(pidText + " - " + name); //$NON-NLS-1$
	}

	// public static void scaleBars() {
	// for (int i = 0; i < bars.size(); i++) {
	// ((ProgressBar)bars.elementAt(i)).setSelection(10);
	//			
	// }
	// }

	public static void clear() {
		barCount = 0;
		Control[] c = MainPanel.pidStats.getChildren();
		for (int i = 0; i < c.length; i++)
			c[i].dispose();
	}
}
