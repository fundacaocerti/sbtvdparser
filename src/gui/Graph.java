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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;

import sys.Messages;

public class Graph extends Composite {

	private static Canvas canvas = null;
	private static Paint graphArea = new Paint(); // @jve:decl-index=0:
	private static float min = 100, max = 0, ratio = 1;
	private static Zoomer z;

	public Graph(Composite parent, int style) {
		super(parent, style);
		z = new Zoomer();
		initialize();
	}

	private void initialize() {
		setSize(new Point(290, 210));
		GridData gridData2 = new GridData();
		gridData2.horizontalSpan = 3;
		gridData2.widthHint = getSize().x;
		gridData2.grabExcessHorizontalSpace = true;
		GridData gridData1 = new GridData();
		gridData1.widthHint = xRes;
		gridData1.horizontalSpan = 3;
		createCanvas();
		vPos = new Slider(this, SWT.VERTICAL);
		vPos.setSelection(40);
		vPos.setMaximum(40);
		vPos.setToolTipText(Messages.getString("Graph.vpos")); //$NON-NLS-1$
		vPos.addSelectionListener(z);
		vZoom = new Slider(this, SWT.VERTICAL);
		vZoom.setSelection(40);
		vZoom.setMaximum(90);
		vZoom.setToolTipText(Messages.getString("Graph.vzoom")); //$NON-NLS-1$
		vZoom.addSelectionListener(z);
		sampleTime = new Slider(this, SWT.NONE);
		sampleTime.setToolTipText(Messages.getString("Graph.sampletime")); //$NON-NLS-1$
		// sampleTime.setSize(120, sampleTime.getSize().x);
		sampleTime.setMinimum(0);
		sampleTime.setSelection(0);
		sampleTime.setMaximum(8);
		sampleTime.setThumb(2);
		sampleTime.setIncrement(1);
		sampleTime.setPageIncrement(1);
		sampleTime.setLayoutData(gridData1);
		info = new Label(this, SWT.NONE);
		info.setText(Messages.getString("Graph.default")); //$NON-NLS-1$
		info.setLayoutData(gridData2);
		sampleTime.addSelectionListener(z);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		this.setLayout(gridLayout);
	}

	private class Zoomer implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {
			float vz = (float) vZoom.getSelection() / 10 - 4;
			if (vz < 0)
				vz = 1 / (1 - vz);
			else
				vz += 1;
			timescale = (float) Math.pow(2, sampleTime.getSelection() - 3);
			float offs = (30 - vPos.getSelection()) * max / 30;
			StringBuilder sb = new StringBuilder();
			sb.append(Messages.getString("Graph.sample")); //$NON-NLS-1$
			if (timescale < 1)
				sb.append("1:" + Math.round(1 / timescale));
			else
				sb.append(Math.round(timescale));
			sb.append(Messages.getString("Graph.from")); //$NON-NLS-1$
			sb.append((Float.toString(offs) + "000").substring(0, 4)); //$NON-NLS-1$
			sb.append(Messages.getString("Graph.to")); //$NON-NLS-1$
			sb.append((Float.toString(max / vz + offs) + "00").substring(0, 4)); //$NON-NLS-1$
			sb.append("Mbps"); //$NON-NLS-1$
			info.setText(sb.toString());
			graphArea.setScaling(vz, offs, timescale);
			resample();
		}
	}

	private static int xRes = 230, yRes = 160;

	class graphXYLabel implements MouseMoveListener {
		int lastColumn = 0;

		public void mouseMove(MouseEvent e) {
			if (grData == null)
				return;
			if (e.x < grData.length && e.x != lastColumn) {
				graphArea.setActiveColumn(e.x);
				if (lastColumn < e.x)
					canvas.redraw(lastColumn, 0, e.x - lastColumn + 1, canvas.getSize().y, true);
				else
					canvas.redraw(e.x, 0, lastColumn - e.x + 1, canvas.getSize().y, true);
				// canvas.redraw(lastColumn, 0, 1, canvas.getSize().y, true);
				lastColumn = e.x;
			}
		}
	}

	private void createCanvas() {
		GridData gridData = new GridData();
		gridData.widthHint = xRes;
		gridData.heightHint = yRes;
		canvas = new Canvas(this, SWT.NONE);
		canvas.setLayoutData(gridData);
		canvas.addPaintListener(graphArea);
		canvas.addMouseMoveListener(new graphXYLabel());
	}

	static float[] grData = new float[xRes], sourceGrData;
	private Slider vZoom = null;
	private Slider vPos = null;
	private Slider sampleTime = null;
	private Label info = null;
	static float timescale = 1 / 8;

	public static void setPlotData(float[] data) {
		sourceGrData = data;
		z.widgetSelected(null);
	}

	private static void resample() {
		if (sourceGrData == null)
			return;

		int avg = (int) (timescale * 8);
		int i = 0;
		for (i = 0; i < grData.length; i++)
			grData[i] = 0;
		for (i = 0; i < grData.length; i++) {
			grData[i] = 0;
			if ((i + 1) * avg > sourceGrData.length)
				break;
			for (int j = 0; j < avg; j++)
				grData[i] += sourceGrData[i * avg + j] / avg;
		}

		min = 100;
		max = 0;
		for (int j = 0; j < i; j++) {
			if (grData[j] < min)
				min = grData[j];
			if (grData[j] > max)
				max = grData[j];
		}

		ratio = yRes / (max - min);
		graphArea.setData(grData, min, max, ratio);

		canvas.redraw();
	}

} // @jve:decl-index=0:visual-constraint="28,22"
