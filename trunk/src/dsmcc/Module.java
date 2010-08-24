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
package dsmcc;

import gui.MainPanel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import sys.BitWise;
import sys.Messages;

public class Module {
	int id, version, lenght, remainingParts, treeLvl, origSize, blockSize = 4066;
	boolean[] receivedParts;
	public int partLvl;
	byte[] data;
	ModuleList moduleList;

	public Module(int id, int version, int lenght, int treeLvl, ModuleList moduleList, int origSize, int blockSize) {
		this.id = id;
		this.version = version;
		this.lenght = lenght;
		this.origSize = origSize;
		this.treeLvl = treeLvl;
		this.moduleList = moduleList;
		this.blockSize = blockSize;
		data = new byte[lenght];
		remainingParts = lenght / blockSize + 1;
		receivedParts = new boolean[remainingParts];
		partLvl = MainPanel.addTreeItem(Messages.getString("Module.parts") + remainingParts, treeLvl); //$NON-NLS-1$
	}

	public int getId() {
		return id;
	}

	public boolean isComplete() {
		return remainingParts == 0;
	}

	public String toString() {
		return "000" + Integer.toHexString(id); //$NON-NLS-1$
	}

	public void save(File f) {
		try {
			// File f = new
			// File("k:\\modules\\module"+mId.substring(mId.length()-4,
			// mId.length()));
			if (f.exists())
				f.delete();
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(data);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean feedPart(byte[] data, int dataOffset, int dataLenght, int blockNumber, int partLvl) {
		if (remainingParts == 0 || blockNumber >= receivedParts.length || receivedParts[blockNumber])
			return false;
		MainPanel
				.addTreeItem(
						Messages.getString("Module.part") + blockNumber + Messages.getString("Module.size") + dataLenght, partLvl); //$NON-NLS-1$ //$NON-NLS-2$
		// MainPanel.addTreeItem("["+
		// printHex(contents, startOffset, startOffset+4);
		// System.out.print("...");
		// printHex(contents, startOffset+lenght-4, startOffset+lenght);
		// System.out.println("]");
		// System.out.print("Module: "+Integer.toHexString(id));
		// System.out.print("\tsize: "+Integer.toHexString(lenght));
		// System.out.println("\tfeed: do="+dataOffset+" dl="+dataLenght+" bn="+blockNumber);
		if ((data.length < dataOffset + dataLenght) || (this.data.length < blockNumber * blockSize + dataLenght)) {
			System.out
					.println("Module " + id + " data feed err: inserting " + dataLenght + "b of " + data.length //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ "b buffer from " + dataOffset + " into " + this.data.length + "b dest. at " + blockNumber * blockSize //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ dataLenght);
			return false;
		}
		System.arraycopy(data, dataOffset, this.data, blockNumber * blockSize, dataLenght);
		remainingParts--;
		receivedParts[blockNumber] = true;
		if (remainingParts == 0) {
			System.out.println("Module complete"); //$NON-NLS-1$
			if (origSize != lenght) {
				Inflater decompresser = new Inflater();
				decompresser.setInput(this.data, 0, lenght);
				byte[] result = new byte[origSize];
				try {
					int resultLength = decompresser.inflate(result);
					decompresser.end();
					if (resultLength == origSize)
						this.data = result;
				} catch (DataFormatException e) {
					e.printStackTrace();
				}
			}
			MainPanel.setTreeData(treeLvl, this);
			BIOP b = new BIOP(moduleList.fileList);
			BitWise bw = new BitWise(this.data);
			b.parseModule(bw, treeLvl);
			moduleList.increaseCompleted();
			if (moduleList.isReadyToMount())
				moduleList.mountFS();
		}
		return true;
		// StringBuffer sb = new StringBuffer();
		// sb.append(" [");
		// for (int i = dataOffset; i < dataOffset+5; i++) {
		// if (data[i] < 0x10 && data[i] >= 0)
		// sb.append('0');
		// sb.append(Integer.toHexString(data[i] & 0xff));
		// sb.append(' ');
		// }
		// sb.append("... ");
		// for (int i = dataLenght+dataOffset-5; i < dataLenght+dataOffset; i++)
		// {
		// if (data[i] < 0x10 && data[i] >= 0)
		// sb.append('0');
		// sb.append(Integer.toHexString(data[i] & 0xff));
		// sb.append(' ');
		// }
		// sb.append(']');
		// MainPanel.addTreeItem("content: "+sb.toString(), treeLvl);
	}
}
