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
import sys.Log;
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
		remainingParts = lenght / blockSize;
		if (lenght % blockSize > 0)
			remainingParts++;
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
		if ((data.length < dataOffset + dataLenght) || (this.data.length < blockNumber * blockSize + dataLenght)) {
			StringBuffer sb = new StringBuffer();
			sb.append("Module "); //$NON-NLS-1$
			sb.append(id);
			sb.append(" data feed err: inserting "); //$NON-NLS-1$
			sb.append(dataLenght);
			sb.append("b of "); //$NON-NLS-1$
			sb.append(data.length);
			sb.append("b buffer from "); //$NON-NLS-1$
			sb.append(dataOffset);
			sb.append(" into "); //$NON-NLS-1$
			sb.append(this.data.length);
			sb.append("b dest. at "); //$NON-NLS-1$
			sb.append(blockNumber * blockSize);
			Log.printWarning(sb.toString());
			return false;
		}
		System.arraycopy(data, dataOffset, this.data, blockNumber * blockSize, dataLenght);
		remainingParts--;
		receivedParts[blockNumber] = true;
		if (remainingParts == 0) {
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
