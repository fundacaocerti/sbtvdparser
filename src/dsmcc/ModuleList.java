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
import java.util.Vector;

import mpeg.psi.DSMCC;
import mpeg.psi.descriptors.DSMCCDescriptorList;
import sys.BitWise;
import sys.Log;

public class ModuleList {
	
	Vector moduleList = new Vector();
	Vector cacheList = new Vector();
	Module lastOne;
	int completeModules = 0;
	int origSize;
	FileList fileList;
	
	DSMCC parent;
	
	public ModuleList(DSMCC parent) {
		super();
		this.parent = parent;
		fileList = new FileList();
	}
	
	public Module getById(int id) {
		//version is not used by now
		if (lastOne != null && lastOne.getId() == id)
			return lastOne;
		for (int i = 0; i < moduleList.size(); i++) {
			lastOne = (Module)moduleList.get(i);
			if (lastOne.getId() == id)
				return lastOne;
		}
		return null;
	}
	
	public void loadCache() {
		for (int i = 0; i < cacheList.size(); i++)
			((ModuleCache)cacheList.get(i)).load(this);
		cacheList.removeAllElements();
	}
	
	public void mountFS() {
		DSMCCObject root = fileList.getRoot();
		if (root != null) {
			root.name = "Carrossel "+BitWise.toHex(parent.downloadId);
			root.mountTree(parent.progressLvl);
		}
		fileList.reset();
	}
	
	public void reset() {
		completeModules = 0;
		moduleList.removeAllElements();
		cacheList.removeAllElements();
		mountFS();
	}
	
	public void increaseCompleted() {
		completeModules++;
	}
	
	public void feedData(Module m, byte[] data, int dataOffset, int dataLenght, int blockNumber, int treeLvl) {
		parent.updateProgress(dataLenght);
		m.feedPart(data, dataOffset, dataLenght, blockNumber, treeLvl);
	}
	
	public boolean isReadyToMount() {
		return completeModules > 0 && completeModules == moduleList.size();
	}
	
	public void cacheData(int m, byte[] data, int dataOffset, int dataLenght, int blockNumber) {
		ModuleCache mc = new ModuleCache(data, m, dataOffset, dataLenght, blockNumber);
		if (!cacheList.contains(mc))
			cacheList.add(mc);
		else
			System.out.println("já no cache");
	}
	
	public void save(File f) {
		if (f.exists())
			f.delete();
		f.mkdir();
		for (int i = 0; i < moduleList.size(); i++) {
			Module m = (Module)moduleList.get(i);
			m.save(new File(f, m.toString()));
		}
	}
	
	public String toString() {
		return "ModuleList "+BitWise.toHex(parent.downloadId);
	}
	
	public void createModule(BitWise bw, int treeLvl) {
		bw.printBuffer(bw.getAbsolutePosition(), bw.getAbsolutePosition()+6);
		int moduleId = bw.pop16();
		int aModuleLvl = MainPanel.addTreeItem("moduleId: "+BitWise.toHex(moduleId), treeLvl);
		int moduleSize = bw.pop16()<<16 | bw.pop16();
		if (moduleSize > 0xa00000 || moduleSize < 0) {
			Log.printWarning("DSMCC module is too large (>1Mb) - not creating");
			return;
		}
		if (moduleSize == 0)
			increaseCompleted();
		parent.updateDlSize(moduleSize);
		MainPanel.addTreeItem("moduleSize: "+BitWise.toHex(moduleSize), aModuleLvl);
		int moduleVersion = bw.pop();
		MainPanel.addTreeItem("moduleVersion: "+BitWise.toHex(moduleVersion), aModuleLvl);
		
		int moduleInfoLength = bw.pop();
		int miLvl = MainPanel.addTreeItem("moduleInfo: lenght "+BitWise.toHex(moduleInfoLength), aModuleLvl);
		MainPanel.addTreeItem("ModuleTimeOut: "+BitWise.toHex(bw.pop32()), miLvl);
		MainPanel.addTreeItem("BlockTimeOut: "+BitWise.toHex(bw.pop32()), miLvl);
		MainPanel.addTreeItem("MinBlockTime: "+BitWise.toHex(bw.pop32()), miLvl);
		int taps = bw.pop();
		int tapLvl = MainPanel.addTreeItem("taps_count: "+taps, miLvl);
		for (int i = 0; i < taps; i++) {
			int aTapLvl = MainPanel.addTreeItem("id: "+BitWise.toHex(bw.pop16()), tapLvl);
			MainPanel.addTreeItem("use: "+BitWise.toHex(bw.pop16()), aTapLvl);
			MainPanel.addTreeItem("association_tag: "+BitWise.toHex(bw.pop16()), aTapLvl);
			MainPanel.addTreeItem("selector_length: "+BitWise.toHex(bw.pop()), aTapLvl);
		} 				
		int userInfoLength 	= bw.pop();
		int uiLvl = MainPanel.addTreeItem("userInfo: lenght "+BitWise.toHex(userInfoLength), aModuleLvl);
		
		// for(i=0,i<N,i++){ uimsbf
		// descriptor()
		bw.mark();
		origSize = moduleSize;
		while ((bw.getByteCount() < userInfoLength)
				&& (bw.getAvailableSize() > 0)) {
			DSMCCDescriptorList.print(bw, uiLvl, this);
		}
//		MainPanel.addTreeItem("moduleInfo: "+bw.getHexSequence(moduleInfoLength), aModuleLvl);
		moduleList.add(new Module(moduleId, moduleVersion, moduleSize, aModuleLvl, this, origSize));
	}

	public void setCompression(int origSize) {
		this.origSize = origSize;
	}
}
