package dsmcc;

import gui.MainPanel;
import sys.BitWise;
import sys.Messages;

public class DdbCache {

	byte[] data;
	public int moduleId, blockNumber;
	int dataOffset, datLenght;

	public DdbCache(byte[] data, int moduleId, int dataOffset, int datLenght, int blockNumber) {
		this.data = data;
		this.moduleId = moduleId;
		this.dataOffset = dataOffset;
		this.datLenght = datLenght;
		this.blockNumber = blockNumber;
	}

	public void load(ModuleList ml) {
		Module m = ml.getById(new Integer(moduleId));
		if (m == null)
			return;
		int ddmLvl = MainPanel.addTreeItem("downloadDataMessage", m.partLvl, MainPanel.DSMCC_TREE); //$NON-NLS-1$
		MainPanel.addTreeItem(
				Messages.getString("DdbCache.block") + BitWise.toHex(blockNumber), ddmLvl, MainPanel.DSMCC_TREE); //$NON-NLS-1$
		ml.feedData(m, data, dataOffset, datLenght, blockNumber, ddmLvl);
	}

	public boolean equals(DdbCache m) {
		return moduleId == m.moduleId && m.blockNumber == blockNumber;
	}
}
