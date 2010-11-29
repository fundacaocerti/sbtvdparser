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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;

import sys.PIDStats;

public class Paint implements PaintListener {

	public void paintControl(PaintEvent e) {
		e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_BLACK));

		e.gc.fillRectangle(e.x, e.y, e.width, e.height);
		if (data == null)
			return;
		int maxPlot = e.x + e.width;
		if (data.length < maxPlot)
			maxPlot = data.length;
		if (e.x + e.width < maxPlot)
			maxPlot = e.x + e.width;
		for (int i = e.x; i < maxPlot; i++) {
			if (i == selection) {
				e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_RED));
				e.gc
						.drawLine(selection, e.height, selection, e.height
								- (int) ((data[i] - min - vPos) * ratio * vZoom));
				MainPanel.graphInfo.setText((Float.toString(i * hZoom) + "000").substring(0, 5)//$NON-NLS-1$
						+ "s - " + PIDStats.formatScaleFactor(data[i])); //$NON-NLS-1$
				e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_GREEN));
			} else {
				int lineLenght = (int) ((data[i] - min - vPos) * ratio * vZoom);
				e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_GREEN));
				e.gc.drawLine(i, e.height, i, e.height - lineLenght);
				if (lineLenght > 3) {
					e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_DARK_GREEN));
					e.gc.drawLine(i, e.height, i, e.height - lineLenght + 3);
				}
			}
		}
		// selection = e.x;
	}

	float[] data = null;
	float min, max, ratio;
	int selection = 0;
	float vZoom = 1, vPos = 0, hZoom = 1;

	public void setScaling(float vZoom, float vPos, float hZoom) {
		this.vZoom = vZoom;
		this.vPos = vPos;
		this.hZoom = hZoom;
	}

	public void setData(float[] data, float min, float max, float ratio) {
		this.data = data;
		this.min = min;
		this.max = max;
		this.ratio = ratio;
	}

	public void setActiveColumn(int x) {
		selection = x;
	}
}
