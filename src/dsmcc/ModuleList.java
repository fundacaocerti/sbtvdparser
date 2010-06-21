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

import java.util.Vector;

import mpeg.psi.DSMCC;
import sys.BitWise;

public class ModuleList {
	
	Vector moduleList = new Vector();
	Module lastOne;
	int completeModules = 0;
	
	DSMCC parent;
	
	public ModuleList(DSMCC parent) {
		super();
		this.parent = parent;
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
	
	public void increaseCompleted() {
		completeModules++;
	}
	
	public void feedData(int id, byte[] data, int dataOffset, int dataLenght, int blockNumber, int treeLvl) {
		parent.updateProgress(dataLenght);
		Module m = getById(id);
		if (m != null) {
			if (m.isComplete())
				return;
			m.feedPart(data, dataOffset, dataLenght, blockNumber, treeLvl);
		}
		else
			System.out.println("unknownModule");
	}
	
	public boolean isReadyToMount() {
		return completeModules > 0 && completeModules == moduleList.size();
	}
	
	public void createModule(BitWise bw, int treeLvl) {
		bw.printBuffer(bw.getAbsolutePosition(), bw.getAbsolutePosition()+6);
		int moduleId = bw.pop16();
		int aModuleLvl = MainPanel.addTreeItem("moduleId: "+bw.toHex(moduleId), treeLvl);
		int moduleSize = bw.pop16()<<16 | bw.pop16();
		parent.updateDlSize(moduleSize);
		MainPanel.addTreeItem("moduleSize: "+bw.toHex(moduleSize), aModuleLvl);
		int moduleVersion = bw.pop();
		MainPanel.addTreeItem("moduleVersion: "+bw.toHex(moduleVersion), aModuleLvl);
		int moduleInfoLength = bw.pop();
//		int milLvl = MainPanel.addTreeItem("moduleInfoLength: "+bw.toHex(moduleInfoLength), aModuleLvl);
//		bw.mark();
//		// for(i=0,i<N,i++){ uimsbf
//		// descriptor()
//		while ((bw.getByteCount() < moduleInfoLength)
//				&& (bw.getAvailableSize() > 0)) {
//			DSMCCDescriptorList.print(bw, milLvl);
//		}
		MainPanel.addTreeItem("moduleInfo: "+bw.getHexSequence(moduleInfoLength), aModuleLvl);
		moduleList.add(new Module(moduleId, moduleVersion, moduleSize, aModuleLvl, this));
	}
}
