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
package mpeg.psi.descriptors;

import sys.BitWise;

public class ConditionalAccess extends Descriptor {

	public static int tag = 0x09;

	static String name = "Conditional Access Descriptor";

	public void printDescription() {
		final int level = addSubItem(name, tableIndx);
		addSubItem("CA_system_id: " + BitWise.toHex(bw.pop16()), level);
		final int caPID = BitWise.stripBits(bw.pop16(), 3, 13);
		addSubItem("CA PID: " + BitWise.toHex(caPID), level);
		addSubItem("private data: " + bw.getHexSequence(bw.getAvailableSize()), level);
	}
}
/*
0x4AEB 	Abel Quintic
0x4AF0 	ABV
0x4800 	Accessgate
0x4A20 	AlphaCrypt
0x1702, 0x1722, 0x1762 	BetaCrypt 1
0x1710 	BetaCrypt 2
0x2600 	BISS
0x4900 	China Crypt
0x22F0 	Codicrypt
0x4AEA 	Cryptoguard
0x0B01, 0x0B02, 0x0B03, 0x0B04, 0x0B05, 0x0B06, 0x0B07, 0x0BAA 	Conax
0x4AE4 	CoreCrypt
0x4347 	CryptOn
0x0D00, 0x0D02, 0x0D03, 0x0D05, 0x0D07, 0x0D20 	Cryptoworks
0x4ABF 	CTI-CAS
0x0700 	DigiCipher 2
0x4A70 	DreamCrypt
0x4A10 	EasyCas
0xEAD0 	VanyaCas
0x0464 	EuroDec
0x5501 	Griffin
0x5581 	Bulcrypt
0x0606 	Irdeto 1
0x0602, 0x0604, 0x0608, 0x0622, 0x0626, 0x0664, 0x0614 	Irdeto 2
0x0692 	Irdeto 3
0x4AA1 	KeyFly
0x0100 	Seca Mediaguard 3
0x1800, 0x1801, 0x1810, 0x1830 	Nagravision
0x1702, 0x1722, 0x1762, 0x1801 	Nagravision Aladin
0x4A02 	Tongfang
0x4AD4 	OmniCrypt
0x0E00 	PowerVu
0x1000 	RAS (Remote Authorisation System)
0x4AC1 	Latens Systems
0xA101 	RosCrypt-M
0x4A60, 0x4A61, 0x4A63 	SkyCrypt/Neotioncrypt
0x4A80 	ThalesCrypt
0x0500 	Viaccess
0x0911, 0x0919, 0x0960, 0x0961, 0x093b, 0x0963, 0x09AC 	NDS Videoguard 3
0x0927, 0x09AC 	NDS Videoguard 4
0x4AD0, 0x4AD1 	X-Crypt
0x5500, 0x4AE0, 0x4AE1 	Z-Crypt/DRE-Crypt
0x4AE5 	PRO-Crypt
*/