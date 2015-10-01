package mpeg.es;

import mpeg.pes.PES;
import mpeg.pes.PESList;
import sys.BitWise;

public class H264 extends PES {

	BitWise bw = null;

	public H264(final int esPid, final int esLoopLevel) {
		pid = esPid;
		id = 0x0b;
		treeIndx = addSubItem("H.264 elementary stream", esLoopLevel);
		PESList.addElementaryStream(this);
	}

	@Override
	public void startPacket(final byte[] source, final int srcOffset, final int writeSize) {
		if (bw == null) bw = new BitWise(source);
		else bw.buf = source;
		bw.setOffset(srcOffset);

		if (bw.pop() != 0 & bw.pop() != 0 & bw.pop() != 1) return; // PES start prefix
		esId = bw.pop();
		if (esId >> 4 != 0xe) {
			addSubItem("Not a video stream: esID: " + BitWise.toHex(esId));
			return;
		}// ABNT 15602-3:2007 pg 6
		addSubItem("video ES id: " + BitWise.toHex(esId & 0xf));
		packetLenght = bw.pop16();
		if (packetLenght != 0) // video ESs have no predefined size
		return;
		feedPart(source, srcOffset + 6, writeSize - 6);
	}

	int removeMagicScaping(final byte[] buf, final int from, final int size) {
		int offset = 0, z = 0;
		// remove emulation prevention bytes
		for (int i = from; i < size + from; i++) {
			if (z < 2 || buf[i] != 3) buf[i - offset] = buf[i];
			else offset++;
			if (buf[i] == 0) z++;
			else z = 0;
		}
		return size - offset;
	}

	final byte[] nalStartCode = { 0, 0, 1 };

	@Override
	public void feedPart(final byte[] source, final int srcOffset, final int writeSize) {
		bw.setOffset(srcOffset);
		bw.setBufferSize(writeSize);
		bw.printBuffer(bw.getAbsolutePosition(), bw.getAvailableSize());
		int lastNALIdx = 0;
		for (int i = srcOffset; i < srcOffset + writeSize - 3; i++)
			if (source[i] == 0 && source[i + 1] == 0 && source[i + 2] == 1) {
				if (lastNALIdx > 0) {
					bw.setOffset(lastNALIdx);
					bw.setBufferSize(i);
					parseNAL();
				}
				lastNALIdx = i;
			}
	}

	String[] nalTypes = { "slice of non-IDR picture", "slice of data partition A", "slice of data partition B",
			"slice of data partition C", "slice of IDR picture", "Supplemental enhancement information",
			"Sequence parameter set", "Picture parameter set", "Access unit delimiter", "End of sequence",
			"End of stream", "Filler data" };

	void parseNAL() {
		System.out.println("NAL unit start " + bw.getHexSequence(3));
		System.out.println("zero " + bw.consumeBits(1));
		System.out.println("nal_ref_idc " + bw.consumeBits(2));
		final int i = bw.consumeBits(5);
		System.out.println("nal_unit_type " + (i > 0 && i < 13 ? nalTypes[i - 1] : i + "?"));
	}
}
