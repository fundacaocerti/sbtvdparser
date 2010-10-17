package gui;

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

public class MultiProgressBar extends Composite implements Listener {

	static Shell sShell = null;

	GridData[] labelsGd = new GridData[3];
	Label[] labels = new Label[3];
	GridLayout gridLayout;
	float start, stop, cursor;

	MultiProgressBar(Composite parent, int style) {
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
		pbGd.widthHint = 600;
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
			label.addListener(SWT.MouseDown, this);

		}
		labels[1].setBackground(display.getSystemColor(SWT.COLOR_DARK_BLUE));
		// setBackground(display.getSystemColor( SWT.COLOR_GREEN));
		start = 0.29039997f;
		stop = 0.40417075f;
		layout();
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
		System.out.println(w);
		labelsGd[0].widthHint = (int)(w * start);
		labelsGd[1].widthHint = (int)Math.ceil(w * (stop - start));
		labelsGd[2].widthHint = (int)(w * (1 - stop));
//		System.out.println(start);
//		System.out.println(stop);
		System.out.println(labelsGd[0].widthHint);
		System.out.println(labelsGd[1].widthHint);
		System.out.println(labelsGd[2].widthHint);
//		System.out.println();
		// progressBar.pack();
		super.layout();
	}

	public void setStopPoint(float f) {
		if (f < start || f > 1)
			return;
		System.out.println(f);
		stop = f;
	}

	public void handleEvent(Event e) {
		if (e.type == SWT.MouseMove) {
			cursor = (float)(1+e.x+((Label)e.widget).getLocation().x)/getSize().x;
			labels[1].setToolTipText(cursor+"s");
		}
		if (e.type == SWT.MouseDown) {
			if (e.button == 1)
				setStartPoint(cursor);
			else
				setStopPoint(cursor+1f/getSize().x);
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

		MultiProgressBar mpb = new MultiProgressBar(sShell, SWT.BORDER);
		sShell.open();
		while (!sShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
//				try {
//					Thread.sleep(1500);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				mpb.setStartPoint((float) Math.random());
//				mpb.setStopPoint((float) Math.random());
//				mpb.layout();
			}
		}

	}



}
