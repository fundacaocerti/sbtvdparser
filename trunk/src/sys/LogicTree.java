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
package sys;

import gui.MainPanel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeItem;

import parsers.Packet;

public class LogicTree {

	public String text;

	public TreeItem treeitem;

	public LogicTree parent;

	public Vector<LogicTree> sons = new Vector<LogicTree>();

	public Object contents;

	public int indx;

	public long creationTimestamp;

	public boolean isVisible = false;

	public LogicTree(String text, LogicTree parent, int indx) {
		this.text = text;
		this.parent = parent;
		this.indx = indx;
		creationTimestamp = Packet.packetCount;
		if (parent != null) {
			parent.sons.add(this);
		}
	}

	public Object getData() {
		return contents;
	}

	public String toString() {
		if (contents != null) {
			return text + " " + contents; //$NON-NLS-1$
		}
		return text;
	}

	private static final String sysEncoding = "UTF-8"; //$NON-NLS-1$

	private int iterator = -1;

	public LogicTree getNext() {
		if (iterator == -1) {
			iterator++;
			return this;
		}
		while (iterator < sons.size()) {
			LogicTree lt = (sons.get(iterator)).getNext();
			if (lt == null)
				iterator++;
			else
				return lt;
		}
		return null;
	}

	public void print(OutputStream out) throws UnsupportedEncodingException, IOException {
		for (int i = 0; i < sons.size(); i++)
			(sons.get(i)).print("  ", i == sons.size() - 1, out); //$NON-NLS-1$
	}

	public String print(String ident, boolean isTheLast, OutputStream out) throws UnsupportedEncodingException,
			IOException {
		out.write(ident.getBytes(sysEncoding));
		if (isTheLast)
			out.write("  └─".getBytes(sysEncoding)); //$NON-NLS-1$
		else
			out.write("  ├─".getBytes(sysEncoding)); //$NON-NLS-1$
		out.write(text.getBytes(sysEncoding));
		out.write("\r\n".getBytes(sysEncoding)); //$NON-NLS-1$
		for (int i = 0; i < sons.size(); i++)
			if (isTheLast)
				(sons.get(i)).print(ident + "  ", i == sons.size() - 1, out); //$NON-NLS-1$
			else
				(sons.get(i)).print(ident + "  │", i == sons.size() - 1, out); //$NON-NLS-1$
		return text;
	}

	public void addToUI(int root) {
		if (isVisible)
			return;
		TreeItem t;
		if (parent.treeitem != null)
			t = new TreeItem(parent.treeitem, SWT.NONE);
		else
			t = new TreeItem(MainPanel.getTree(root), SWT.NONE);
		t.setText(text);
		t.setData(this);
		treeitem = t;
		isVisible = true;
	}

	// Bonsai HTML viewer stuff
	public int bonsaiIndx = 0, bonsaiFDI = -1, bonsaiFFI = -1;

	public static int dirIndxCounter = 0, fileIndxCounter = 0;

	public static Vector<LogicTree> fileList = new Vector<LogicTree>();

	private void printInt(int i, OutputStream out) throws IOException {
		out.write(Integer.toString(i).getBytes());
		out.write('*');
	}

	private void printString(String s, OutputStream out) throws IOException {
		s = s.replace('\n', ' ');
		s = s.replace('\\', '/');
		s = s.replace('*', '#');
		out.write(s.getBytes());
		out.write('*');
	}

	private void printNode(OutputStream out) throws IOException {
		printString(text, out);
		if (parent != null)
			printInt(parent.bonsaiIndx, out);
		else
			printInt(-1, out);
		printInt(bonsaiFDI, out);
		printInt(bonsaiFFI, out);
	}

	private void createBonsaiIndexes() {
		LogicTree lt;
		for (int i = 0; i < sons.size(); i++) {
			lt = sons.get(i);
			if (lt.sons.size() != 0) {
				lt.bonsaiIndx = ++dirIndxCounter;
				if (bonsaiFDI == -1)
					bonsaiFDI = dirIndxCounter;
			} else {
				lt.bonsaiIndx = fileList.size();
				fileList.add(lt);
				if (bonsaiFFI == -1)
					bonsaiFFI = lt.bonsaiIndx;
			}
		}
		for (int i = 0; i < sons.size(); i++)
			(sons.get(i)).createBonsaiIndexes();
	}

	private void recursiveBPrint(OutputStream out) throws IOException {
		LogicTree lt;
		for (int i = 0; i < sons.size(); i++) {
			lt = sons.get(i);
			if (lt.sons.size() != 0)
				lt.printNode(out);
		}
		for (int i = 0; i < sons.size(); i++)
			(sons.get(i)).recursiveBPrint(out);
	}

	public void printBonsai(OutputStream out, String id) throws IOException {
		out.write(("<!--\n\tvar " + id + " = \"").getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
		createBonsaiIndexes();
		printInt(++dirIndxCounter, out);
		printNode(out);
		recursiveBPrint(out);
		printInt(fileList.size(), out);
		LogicTree lt;
		for (int i = 0; i < fileList.size(); i++) {
			lt = fileList.get(i);
			printString(lt.text, out);
			printInt(lt.parent.bonsaiIndx, out);
		}
		out.write("\"\n//-->".getBytes()); //$NON-NLS-1$
	}

	public void printXML(OutputStream out) throws IOException {
		PrintWriter pw = new PrintWriter(out);
		pw.write("<TransportStream Name=\"");
		String tmp = searchItem("NIT.*;.*loop.*;TS_id.*;descri.*;.*information;ts_name.*", 0, this);
		pw.write(tmp.substring(tmp.indexOf('[') + 1, tmp.indexOf(']')));
		pw.write("\" Id=\"");
		tmp = searchItem("NIT.*;.*loop.*;TS_id.*", 0, this);
		pw.write(tmp.substring(tmp.indexOf("0x") + 2));
		pw.write("\" NetworkId=\"");
		tmp = searchItem("NIT.*;.*loop.*;TS_id.*;Orig.*", 0, this);
		pw.write(tmp.substring(tmp.indexOf("0x") + 2, tmp.indexOf('(') - 1));
		pw.write("\"> \n\t<TSDesc>");
		tmp = searchItem(".*\\[.*\\]", 0, this);
		pw.write(tmp);
		pw.write("</TSDesc>\n\t<Services>\n\t\t<Service ServiceName=\"");
		tmp = searchItem("SDT.*;Services;service.*", 0, this);
		pw.write(tmp.substring(tmp.indexOf("0x") + 2));

		pw.flush();
		pw.close();
	}

	private String searchItem(String string, int level, LogicTree root) {
		int end = string.indexOf(';', level);
		String toSearch;
		if (end > 0)
			toSearch = string.substring(level, end);
		else
			toSearch = string.substring(level);
		LogicTree lt;
		for (int i = 0; i < root.sons.size(); i++) {
			lt = root.sons.get(i);
			if (lt.text.matches(toSearch))
				if (end < 0)
					return lt.text;
				else
					return searchItem(string, end + 1, lt);
		}
		return "!";
	}
}
