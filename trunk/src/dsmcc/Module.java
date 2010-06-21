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

import sys.BitWise;
import sys.Log;


public class Module {
	int id, version, lenght, remainingParts, treeLvl;
	boolean[] receivedParts;
	public int partLvl;
	byte[] data;
	ModuleList moduleList;
	
	public Module(int id, int version, int lenght, int treeLvl, ModuleList moduleList) {
		this.id = id;
		this.version = version;
		this.lenght = lenght;
		this.treeLvl = treeLvl;
		this.moduleList = moduleList;
		data = new byte[lenght];
		remainingParts = lenght / 4066 + 1;
		receivedParts = new boolean[remainingParts];
		partLvl = MainPanel.addTreeItem("parts: "+remainingParts, treeLvl);
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isComplete() {
		return remainingParts == 0;
	}
	
	public void save() {
		try {
			String mId = "000"+Integer.toHexString(id);
			File f = new File("k:\\modules\\module"+mId.substring(mId.length()-4, mId.length()));
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
	
	public void feedPart(byte[] data, int dataOffset, int dataLenght, int blockNumber, int partLvl) {
		if (remainingParts == 0 || blockNumber > receivedParts.length
				|| receivedParts[blockNumber])
			return;
		int pLvl = MainPanel.addTreeItem("part: "+blockNumber+" size: "+dataLenght, partLvl);
//		MainPanel.addTreeItem("["+
//		printHex(contents, startOffset, startOffset+4);
//		System.out.print("...");
//		printHex(contents, startOffset+lenght-4, startOffset+lenght);
//		System.out.println("]");
//		System.out.print("Module: "+Integer.toHexString(id));
//		System.out.print("\tsize: "+Integer.toHexString(lenght));
//		System.out.println("\tfeed: do="+dataOffset+" dl="+dataLenght+" bn="+blockNumber);
		if (data.length < dataOffset+dataLenght) {
//			Log.printWarning("Corrupted);
			System.out.println("Module "+id+" data feed err: inserting "+dataLenght+
					"b of "+data.length+"b buffer from "+dataOffset);
//			System.out.println("Module "+id+" data feed err: inserting "+dataLenght+
//					"b into "+this.data.length+"b buffer from "+blockNumber*4066);
			return;
		}
		System.arraycopy(data, dataOffset, this.data, blockNumber*4066, dataLenght);
		remainingParts--;
		receivedParts[blockNumber] = true;
		//TODO: verificar blocos já recebidos, ignorar
		if (remainingParts == 0) {
			System.out.println("Module complete");
//			save();
			BIOP b = new BIOP();
			BitWise bw = new BitWise(this.data);
			b.parseModule(bw, treeLvl);
			moduleList.increaseCompleted();
			if (moduleList.isReadyToMount()) {
				DSMCCDir root = (DSMCCDir)FileList.getByObjKey(BIOP.svcGatewayObjKey);
				root.name = "Carrossel";//+carroussel_id
				root.mountTree(moduleList.parent.progressLvl);
			}
		}
		
//		StringBuffer sb =  new StringBuffer();
//		sb.append(" [");
//		for (int i = dataOffset; i < dataOffset+5; i++) {
//			if (data[i] < 0x10 && data[i] >= 0)
//				sb.append('0');
//			sb.append(Integer.toHexString(data[i] & 0xff));
//			sb.append(' ');
//		}
//		sb.append("... ");
//		for (int i = dataLenght+dataOffset-5; i < dataLenght+dataOffset; i++) {
//			if (data[i] < 0x10 && data[i] >= 0)
//				sb.append('0');
//			sb.append(Integer.toHexString(data[i] & 0xff));
//			sb.append(' ');
//		}
//		sb.append(']');
//		MainPanel.addTreeItem("content: "+sb.toString(), treeLvl);
	}
}
