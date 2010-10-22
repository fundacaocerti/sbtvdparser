package gui;

import mpeg.PCR;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import parsers.Packet;

public class MultiProgressBar extends Composite implements Listener {

	static Shell sShell = null;

	GridData[] labelsGd = new GridData[3];
	Label[] labels = new Label[3];
	GridLayout gridLayout;
	float start, stop, cursor, totalTime;
	String tooltip, text;

	public float getStart() {
		return start;
	}

	public float getStop() {
		return stop;
	}

	MultiProgressBar(Composite parent, int style, int width) {
		super(parent, style);
		Display display = Display.getDefault();
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.makeColumnsEqualWidth = false;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;

		GridData pbGd = new GridData();
		pbGd.widthHint = width - 2;
		setSize(width, 12);
		pbGd.heightHint = 12;
		setLayoutData(pbGd);
		setLayout(gridLayout);

		for (int i = 0; i < labels.length; i++) {
			Label label = new Label(this, SWT.NONE);
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
			gd.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
			label.setLayoutData(gd);
			labelsGd[i] = gd;
			labels[i] = label;
			label.addListener(SWT.MouseMove, this);
		}
		labels[1].setBackground(display.getSystemColor(SWT.COLOR_DARK_BLUE));
		start = 0;
		stop = 0;
		layout();
	}

	public void setText(String s) {

	}

	public void setStartPoint(float f) {
		if (f < 0 || f > 1)
			return;
		start = f;
		if (stop < f)
			stop = f;
	}

	public void layout() {
		float w = getSize().x;
		labelsGd[0].widthHint = Math.round(w * start * (1f - 3f / w));
		labelsGd[1].widthHint = Math.round(w * (stop * (1f - 1f / w) - start * (1f - 3f / w)));
		labelsGd[2].widthHint = Math.round(w * (1 - stop * (1f - 1f / w)));
		super.layout();
	}

	public void setStopPoint(float f) {
		if (f < start || f > 1)
			return;
		stop = f;
		setTotalTime((Packet.estimate * Packet.realPktLenght * 8l) / (PCR.getAverageBitrate() * 1e6f));
	}

	public void setTotalTime(float t) {
		totalTime = t;
	}

	public void setEditMode(boolean editable) {
		if (editable)
			for (int i = 0; i < labels.length; i++)
				labels[i].addListener(SWT.MouseDown, this);
		else
			for (int i = 0; i < labels.length; i++)
				labels[i].removeListener(SWT.MouseDown, this);
	}

	public void handleEvent(Event e) {
		if (e.type == SWT.MouseMove) {
			int abs = e.x + ((Label) e.widget).getLocation().x;
			cursor = (float) abs / (getSize().x - 3);
			tooltip = Float.toString(totalTime * cursor) + "000";
			tooltip = tooltip.substring(0, tooltip.indexOf('.') + 4);
			tooltip += 's';
			if (abs < labels[1].getLocation().x)
				labels[0].setToolTipText(tooltip);
			if (abs < labels[2].getLocation().x)
				labels[1].setToolTipText(tooltip);
			if (abs >= labels[2].getLocation().x)
				labels[2].setToolTipText(tooltip);
		}
		if (e.type == SWT.MouseDown) {
			if (e.button == 1)
				setStartPoint(cursor);
			else
				setStopPoint(cursor);
			layout();
		}
	}

	public static void main(String[] args) {
		sShell = new Shell();
		Display display = Display.getDefault();
		GridLayout gd = new GridLayout();
		gd.numColumns = 1;
		sShell.setLayout(gd);
		sShell.setSize(new Point(700, 80));

		MultiProgressBar mpb = new MultiProgressBar(sShell, SWT.BORDER, 600);
		mpb.setTotalTime(22.3f);
		mpb.setEditMode(true);
		sShell.open();
		sShell.pack();
		while (!sShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

}
