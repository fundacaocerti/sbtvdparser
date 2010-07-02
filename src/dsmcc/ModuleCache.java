package dsmcc;

import sys.BitWise;
import gui.MainPanel;


public class ModuleCache {

	byte[] data;
	public int moduleId, blockNumber;
	int dataOffset, datLenght;
	public ModuleCache(byte[] data, int moduleId, int dataOffset, int datLenght, int blockNumber) {
		this.data = data;
		this.moduleId = moduleId;
		this.dataOffset = dataOffset;
		this.datLenght = datLenght;
		this.blockNumber = blockNumber;
	}
	
	public void load(ModuleList ml) {
		Module m = ml.getById(moduleId);
		if (m == null)
			return;
		int ddmLvl = MainPanel.addTreeItem("downloadDataMessage", 
				m.partLvl, MainPanel.DSMCC_TREE);
		MainPanel.addTreeItem("blockNumber: "+BitWise.toHex(blockNumber), ddmLvl, MainPanel.DSMCC_TREE);
		ml.feedData(m, data, dataOffset,
			datLenght, blockNumber, ddmLvl);
	}
	
	public boolean equals(ModuleCache m) {
		return moduleId == m.moduleId && m.blockNumber == blockNumber;
	}
}
