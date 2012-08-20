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
package sys;

public class BitWise {

	public byte[] buf = null;

	int bufSize = 0, readPtr = 0, readCount;

	public BitWise(byte[] ba) {
		buf = ba;
		if (ba != null)
			bufSize = buf.length;
	}

	public void setOffset(int offset) {
		readPtr = offset;
	}

	/**
	 * @param offset
	 *            1 to 8
	 */
	public static int stripBits(int b, int offset, int count) {
		if (b < 0)
			b += 256;
		int mask = (1 << count) - 1;
		if (offset >= count) {
			mask = mask << (offset - count);
			return (b & mask) >>> (offset - count);
		}
		return -1;
	}

	public static int toInt(byte b) {
		int i = b;
		if (i < 0)
			i += 256;
		return i;
	}

	public String printBin(int n, int digits) {
		int mask = 1 << digits;
		return Integer.toBinaryString(n & (mask - 1) | mask).substring(1, digits + 1);
	}

	public void push(int i, int pos) {
		if (i < 256)
			buf[pos] = (byte) i;
		else {
			buf[pos] = (byte) (i >> 8);
			buf[pos + 1] = (byte) i;
		}
	}

	public void push(int i) {
		push(i, bufSize);
		bufSize++;
		if (i > 255)
			bufSize++;
	}

	public int pop() {
		if (readPtr >= bufSize) {
			loopTrap();
			return 0;
		}
		int i = buf[readPtr];
		if (i < 0)
			i += 256;
		readPtr++;
		return i;
	}

	int maxLoops = 30;

	private void loopTrap() {
		maxLoops--;
		if (maxLoops == 0)
			throw (new RuntimeException("Infinite loop err:")); //$NON-NLS-1$
	}

	public int pop32() {
		return (pop16() << 16) | pop16();
	}

	public int pop16() {
		return (pop() << 8) | pop();
	}

	public static String toHex(int i) {
		String s = Integer.toHexString(i);
		if (s.length() % 2 == 1)
			return "0x0" + Integer.toHexString(i); //$NON-NLS-1$
		return "0x" + Integer.toHexString(i); //$NON-NLS-1$
	}

	public int pop(int bytes) {
		if (readPtr > bufSize - bytes || readPtr == bufSize) {
			loopTrap();
			return 0;
		}
		int i = buf[readPtr];
		if (i < 0)
			i += 256;
		readPtr += bytes;
		return i;
	}

	public void pushArray(byte[] a) {
		System.arraycopy(a, 0, buf, bufSize, a.length);
		bufSize += a.length;
	}

	static int auxPB, indxPB = 0;

	public int mergeBits(int i, int nBits) {
		if (indxPB == 0)
			auxPB = 0;
		indxPB += nBits;
		int mask = (1 << nBits) - 1;
		auxPB = auxPB | ((i & mask) << (8 - indxPB));
		if (indxPB > 7) {
			indxPB = 0;
			return auxPB;
		}
		return -1;
	}

	public void printBuffer() {
		if (bufSize < buf.length)
			printBuffer(0, bufSize);
		else
			printBuffer(0, buf.length);
	}

	public void printBuffer(int from, int to) {
		int b;
		for (int i = from; i < to; i++) {
			b = buf[i];
			if (b < 0)
				b += 256;
			System.out.print((char) b);
			System.out.print("  "); //$NON-NLS-1$
		}
		System.out.println();
		for (int i = from; i < to; i++) {
			b = buf[i];
			if (b < 0)
				b += 256;
			if (b < 0x10)
				System.out.print("0"); //$NON-NLS-1$
			System.out.print(Integer.toHexString(b));
			System.out.print(" "); //$NON-NLS-1$
		}
		System.out.println();
	}

	public int getBufferSize() {
		return bufSize;
	}

	public int getAvailableSize() {
		return bufSize - readPtr;
	}

	public void setBufferSize(int size) {
		bufSize = size;
		if (size > buf.length)
			bufSize = buf.length;
	}

	public void reset() {
		readPtr = readCount;
	}

	public void mark() {
		readCount = readPtr;
	}

	public int getByteCount() {
		return readPtr - readCount;
	}

	public String toString() {
		StringBuilder tmp = new StringBuilder("["); //$NON-NLS-1$
		int b;
		for (int i = 0; i < bufSize; i++) {
			b = buf[i];
			if (b < 0)
				b += 256;
			if (b < 0x10)
				tmp.append("0"); //$NON-NLS-1$
			tmp.append(Integer.toHexString(b));
			if (i + 1 < bufSize)
				tmp.append(" "); //$NON-NLS-1$
		}
		tmp.append("]"); //$NON-NLS-1$
		return tmp.toString();
	}

	// static int copies = 0;
	public BitWise getCopy(int size) {
		return getCopy(readPtr, size);
	}

	public int getAbsolutePosition() {
		return readPtr;
	}

	public BitWise getCopy(int sourceIndx, int size) {
		if (sourceIndx > bufSize)
			sourceIndx = bufSize;
		if (sourceIndx + size > bufSize)
			size = bufSize - sourceIndx;
		byte[] ba = new byte[size];
		System.arraycopy(buf, sourceIndx, ba, 0, size);
		return new BitWise(ba);
	}

	public String getHexSequence(int size) {
		if (getAvailableSize() < size)
			size = getAvailableSize();
		if (size < 1)
			return "[]"; //$NON-NLS-1$
		StringBuffer sb = new StringBuffer(size);
		sb.append("["); //$NON-NLS-1$
		int b;
		for (int i = 0; i < size; i++) {
			b = pop();
			if (b < 0x10)
				sb.append("0"); //$NON-NLS-1$
			sb.append(Integer.toHexString(b));
			if (i + 1 < size)
				sb.append(" "); //$NON-NLS-1$
		}
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

	public int remainingBits = 0, lastByte = 0;

	public int consumeBits(int numOfBits) {
		if (numOfBits == 0)
			return 0;
		if (remainingBits < numOfBits) {
			lastByte = lastByte << 8;
			lastByte += pop();
			lastByte = lastByte & 0xFFFFFFF;
			remainingBits += 8;
		}
		int result = stripBits(lastByte, remainingBits, numOfBits);
		remainingBits -= numOfBits;
		return result;
	}
}
