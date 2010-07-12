package mpeg.psi.descriptors;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import sys.Log;

public class GingaNCLApp extends AITDescriptor {

	public static int tag = 0x06;

	static String name = "Ginga-NCL Application";

	private int level;

	public void printDescription() {

		this.level = addSubItem(name, tableIndx);

		int readedBytes = 0;
		while (readedBytes < descriptor_length) {
			int parameter_lenght = bw.pop();
			byte[] parameter = new byte[parameter_lenght];
			for (int i = 0; i < parameter_lenght; i++) {
				parameter[i] = (byte) bw.pop();
				readedBytes++;
			}

			this.printSubItem("Parameter: ", parameter);
			readedBytes++;
		}

	}

	private void printSubItem(String label, byte[] value) {
		try {
			InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(value), "ISO8859_15_FDIS");
			char[] ca = new char[value.length];
			isr.read(ca);
			String valueS = new String(ca);
			addSubItem(label.concat(valueS), level);
		} catch (Exception e) {
			Log.printStackTrace(e);
		}
	}

}
