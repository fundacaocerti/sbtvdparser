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
//EIA708;
package mpeg.es;

import gui.GuiMethods;
import gui.MainPanel;
import sys.BitWise;

public class CCPresentation {

	static final String[] ISO8859_15 = {
	// ______0____1____2____3____4____5____6____7____8____9____a____b____c____d____e____f
			" ", " ", " ", "0", "@", "P", "`", "p", " ", " ", " ", "°", "À", "Ð", "à", "ð", // _0
			" ", " ", "!", "1", "A", "Q", "a", "q", " ", " ", "¡", "±", "Á", "Ñ", "á", "ñ", // _1
			" ", " ", "\"", "2", "B", "R", "b", "r", " ", " ", "¢", "²", "Â", "Ò", "â", "ò", // _2
			" ", " ", "#", "3", "C", "S", "c", "s", " ", " ", "£", "³", "Ã", "Ó", "ã", "ó", // _3
			" ", " ", "$", "4", "D", "T", "d", "t", " ", " ", "€", "Ž", "Ä", "Ô", "ä", "ô", // _4
			" ", " ", "%", "5", "E", "U", "e", "u", " ", " ", "¥", "µ", "Å", "Õ", "å", "õ", // _5
			" ", " ", "&", "6", "F", "V", "f", "v", " ", " ", "Š", "¶", "Æ", "Ö", "æ", "ö", // _6
			" ", " ", "'", "7", "G", "W", "g", "w", " ", " ", "§", "·", "Ç", "×", "ç", "÷", // _7
			" ", " ", "(", "8", "H", "X", "h", "x", " ", " ", "š", "ž", "È", "Ø", "è", "ø", // _8
			" ", " ", ")", "9", "I", "Y", "i", "y", " ", " ", "©", "¹", "É", "Ú", "é", "ù", // _9
			" ", " ", "*", ":", "J", "Z", "j", "z", " ", " ", "ª", "º", "Ê", "Ú", "ê", "ú", // _a
			" ", " ", "+", ";", "K", " ", "k", "{", " ", " ", "«", "»", "Ë", "Û", "ë", "û", // _b
			" ", " ", ",", "<", "L", " ", "l", "|", " ", " ", "¬", "Œ", "Ì", "Ü", "ì", "ü", // _c
			" ", " ", "-", "=", "M", " ", "m", "}", " ", " ", "ÿ", "œ", "Í", "Ý", "í", "ý", // _d
			" ", " ", ".", ">", "N", " ", "n", "¯", " ", " ", "®", "Ÿ", "Î", "Þ", "î", "þ", // _e
			" ", " ", "/", "?", "O", " ", "o", " ", " ", " ", "¯", "¿", "Ï", "ß", "ï", " " // _f
	};

	static final String[] extraCP = { "☼", "¦", "¨", "´", "¸", "¼", "½", "¾", " ", " ", " ", " ", " ", " ", " ", " ",
			"…", "█", "‘", "’", "“", "”", "•", "™", "⅛", "⅜", "⅝", "⅞", "♪" };

	private boolean extraCodepage = false;

	StringBuffer sb;

	int lvl = 0, pktLvl = 0;

	public CCPresentation(int captLvl, int packetLvl) {
		lvl = captLvl;
		pktLvl = packetLvl;
	}

	public void parse(BitWise bw, int frameSize) {
		if (frameSize == 0)
			return;
		StringBuffer plainText = new StringBuffer();
		String aChar;
		bw.mark();
		char c;
		boolean cmdSequence = false;
		while (bw.getByteCount() < frameSize && bw.getAvailableSize() > 0) {
			c = (char) bw.pop();
			if (c < 0x20 || (c > 0x7e && c < 0xa0)) {
				if (!cmdSequence) {
					if (sb != null) {
						sb.append("}");
						addSubItem(sb.toString());
					}
					sb = new StringBuffer();
					sb.append("cmd: {");
				}
				cmdSequence = true;
				parseCodeChar(c, bw);
			} else {
				if (cmdSequence) {
					if (sb != null) {
						sb.delete(sb.length() - 3, sb.length());
						sb.append("}");
						addSubItem(sb.toString());
					}
					sb = new StringBuffer();
					sb.append("text: {");
					plainText.append("|");
				}
				cmdSequence = false;
				if (extraCodepage) {
					extraCodepage = false;
					if (c > 0x2f && c < 0x4c)
						aChar = extraCP[c - 0x30];
					else if (c == 0x21)
						aChar = extraCP[0x1c];
					else
						aChar = " ";
				} else
					aChar = ISO8859_15[(c >> 4) + ((c & 0xf) << 4)];
				sb.append(aChar);
				plainText.append(aChar);
			}
		}
		sb.append('}');
		addSubItem(sb.toString());
		plainText.append("|");
		GuiMethods.runMethod(GuiMethods.CHANGEITEM, new Object[] { plainText.toString(), new Integer(pktLvl) }, true);
	}

	void parseActivePosition(BitWise bw) {
		char c = (char) bw.pop();
		sb.append(" Ln:" + (c & 0x3f));
		c = (char) bw.pop();
		sb.append(" Col:" + (c & 0x3f));
	}

	void parseEscapeSeq(BitWise bw) {
		char c = (char) bw.pop();
		sb.append(Integer.toHexString(c));
		c = (char) bw.pop();
		sb.append(Integer.toHexString(c));
	}

	void parseCodeChar(char c, BitWise bw) {
		if (c == 0)
			return;
		String codeName = null;
		if (c > 6)
			if (c < 0x20)
				codeName = c0Names[c - 7];
			else
				codeName = c1Names[c - 0x7f];
		if (codeName == null)
			sb.append(Integer.toHexString(c));
		else
			sb.append(codeName);

		if (c == APS)
			parseActivePosition(bw);
		if (c == ESC)
			parseEscapeSeq(bw);
		if (c == CSI)
			parseCommand(bw);
		if (c == COL)
			parseColor(bw);
		if (c == SS3)
			extraCodepage = true;
		sb.append(" | ");
	}

	private void parseColor(BitWise bw) {
		char c;
		c = (char) bw.pop();
		sb.append(" ");
		String[] area = { "FG", "BG", "HFG", "HBG" };
		int i = (c >> 4) & 0x7;
		if (i > 3)
			sb.append(area[i - 4]);
		String[] colors = { "black", "red", "green", "yellow", "blue", "magenta", "cyan", "whyte", "transparent" };
		i = c & 0xf;
		sb.append(">");
		if (i > 8) {
			sb.append("H-");
			i -= 8;
		}
		if (i < 9)
			sb.append(colors[i]);
	}

	private void parseCommand(BitWise bw) {
		sb.append(" ");
		int cmdMaxLen = 8;
		char c = (char) bw.pop();
		while (c != 0x20 && cmdMaxLen > 0 && bw.getAvailableSize() > 0) {
			sb.append(c);
			cmdMaxLen--;
			c = (char) bw.pop();
		}
		c = (char) bw.pop();
		for (int i = 0; i < csiIds.length; i++)
			if (csiIds[i] == c)
				sb.append(" " + csiNames[i]);
		// sb.append("-"+(c / 16)+"/"+(c & 0xf)+")");
	}

	public static final char CSI = 0x9b, ESC = 0x1b, COL = 0x90, APS = 0x1c, SS3 = 0x1d;

	public static final String[] c0Names = { "BEL", "APB", "APF", "APD", "APU", "CS", "APR", "LS1", "LS0", null, null,
			null, null, null, null, "PAPF", null, "CAN", "SS2", null, "ESC", "APS", "SS3", "RS", "US" };

	public static final String[] c1Names = { "DEL", "BKF", "RDF", "GRF", "YLF", "BLF", "MGF", "CNF", "WHF", "SSZ",
			"MSZ", "NSZ", "SZX", null, null, null, null, "COL", "FLC", "CDC", "POL", "WMM", "MACRO", null, "HLC",
			"RPC", "SPL", "STL", "CSI", null, "TIME", null, null };

	public static final String[] csiNames = { "SWF", "CCC", "RCS", "ACPS", "SDF", "SDP", "SSM", "PLD", "PLU", "SHS",
			"SVS", "GSM", "GAA", "SRC", "TCC", "CFS", "ORN", "MDF", "XCS", "PRA", "ACS", "SCS" };

	public static final char[] csiIds = { 83, 84, 110, 97, 86, 95, 87, 75, 92, 88, 89, 66, 93, 94, 98, 101, 99, 100,
			102, 104, 105 };

	public int addSubItem(String msg, int parent) {
		return MainPanel.addTreeItem(msg, parent, MainPanel.CC_TREE);
	}

	public int addSubItem(String msg) {
		return addSubItem(msg, lvl);
	}

	// char[] ccIds = { 0x00, 0x98, 0x99, 0x9a, 0x9b, 0x9c, 0x9d, 0x9e, 0x9f };
	//
	// String[] ccNames = { "NUL", "priority", "anchor", "number",
	// "anchor vertical", "anchor horizontal", "row count",
	// "column count", "locked, visible", "centered", "style ID" };
}
