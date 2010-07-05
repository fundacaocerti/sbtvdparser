package mpeg.psi.descriptors;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import sys.Log;

public class GingaJAppLocation extends AITDescriptor {

	public static int tag = 0x04;

	static String name = "Ginga-J Application Location";

	private int level;

	public void printDescription() {

		this.level = addSubItem(name, tableIndx);

		int base_directory_lenght = bw.pop();
		byte[] base_directory = new byte[base_directory_lenght];

		for (int i = 0; i < base_directory_lenght; i++) {
			base_directory[i] = (byte) bw.pop();
		}

		this.printSubItem("Base Directory: ", base_directory);

		int classpath_extension_length = bw.pop();
		byte[] classpath_extension = new byte[classpath_extension_length];

		for (int i = 0; i < classpath_extension_length; i++) {
			classpath_extension[i] = (byte) bw.pop();
		}

		this.printSubItem("Classpath Extension: ", classpath_extension);

		int initial_class_lenght = descriptor_length - base_directory_lenght
				- classpath_extension_length - 2;
		byte[] initial_class = new byte[initial_class_lenght];

		for (int i = 0; i < initial_class_lenght; i++) {
			initial_class[i] = (byte) bw.pop();
		}

		this.printSubItem("Initial Class: ", initial_class);

	}

	private void printSubItem(String label, byte[] value) {
		try {
			InputStreamReader isr = new InputStreamReader(
					new ByteArrayInputStream(value), "ISO8859_15_FDIS");
			char[] ca = new char[value.length];
			isr.read(ca);
			String valueS = new String(ca);
			addSubItem(label.concat(valueS), level);
		} catch (Exception e) {
			Log.printStackTrace(e);
		}
	}

}
