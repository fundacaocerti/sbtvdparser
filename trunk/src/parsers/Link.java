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
package parsers;

/*
 * @author Dan Andrews dan.and...@home.com
 */
import java.io.File;
import java.io.FileInputStream;

import sys.BitWise;
import sys.Log;

public class Link {

	private String filePath = "", alternatePath = "";

	public void parse(String fName) {
		File f = new File(fName);
		int fSize = (int) f.length();
		byte[] lnkHeader = { 0x4c, 0x00, 0x00, 0x00, 0x01, 0x14, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xc0, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x46 };
		if (!f.canRead() || !f.isFile() || fSize > 5000)
			return;
		try {
			FileInputStream in = new FileInputStream(f);
			byte[] ba = new byte[lnkHeader.length];
			in.read(ba);
			for (int i = 0; i < ba.length; i++)
				if (ba[i] != lnkHeader[i])
					return; // not a MS .lnk file
			ba = new byte[fSize - lnkHeader.length];
			in.read(ba);
			BitWise bw = new BitWise(ba);
			int flags = bw.pop16();
			boolean hasShellItems = BitWise.stripBits(flags, 16, 1) == 1;
			bw.pop(54);
			if (hasShellItems)
				bw.pop(bw.pop() + bw.pop() * 256); // skip the itemlist
			bw.mark();
			bw.pop(8);
			flags = bw.pop();
			bw.pop(3);

			// int localVinfo = bw.pop()+bw.pop()*256;
			// bw.pop16();
			bw.pop(4);
			int localFname = bw.pop() + bw.pop() * 256;
			bw.pop16();
			// int remoteVinfo = bw.pop()+bw.pop()*256;
			// bw.pop16();
			bw.pop(4);
			int remoteFname = bw.pop() + bw.pop() * 256;
			bw.pop16();
			// System.out.println("localVinfo "+BitWise.toHex(localVinfo));
			// System.out.println("localFname "+BitWise.toHex(localFname));
			// System.out.println("remoteVinfo "+BitWise.toHex(remoteVinfo));
			// System.out.println("remoteFname "+BitWise.toHex(remoteFname));
			// System.out.println("off "+BitWise.toHex(bw.getByteCount()));

			// System.out.println("slen: "+(bw.pop()+bw.pop()*256));
			int c = 0;
			while (bw.getAvailableSize() > 0) {
				if ((flags & 1) > 0 && bw.getByteCount() == localFname)
					filePath = getStr(bw);
				if ((flags & 2) > 0) {
					if (bw.getByteCount() == 0x30 && bw.buf[bw.getAbsolutePosition()] != 0)
						filePath = getStr(bw);
					if (bw.getByteCount() == remoteFname) {
						String tmp = getStr(bw);
						if (filePath.endsWith("\\"))
							filePath += tmp;
						else
							filePath += "\\" + tmp;
						if (alternatePath.endsWith("\\"))
							alternatePath += tmp;
						else
							alternatePath += "\\" + tmp;
					}
				}
				c = bw.pop();
				if (alternatePath.length() == 0 && c == (int) '\\')
					alternatePath = '\\' + getStr(bw);
			}
			System.out.println(filePath);
		} catch (Exception e) {
			Log.printStackTrace(e);
			return;
		}
	}

	private String getStr(BitWise bw) {
		char c = (char) bw.pop();
		StringBuffer sb = new StringBuffer();
		while (bw.getAvailableSize() > 0 && c != 0) {
			sb.append(c);
			c = (char) bw.pop();
		}
		return sb.toString();
	}

	public String getAlternatePath() {
		return alternatePath;
	}

	public String getFilePath() {
		return filePath;
	}
}