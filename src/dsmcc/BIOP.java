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
import java.io.FileInputStream;

import sys.BitWise;
import sys.Messages;

//Broadcast Inter ORB (CORBA) Protocol 
public class BIOP {
	/*
	 * DSM::Directory Access, Directory DSM::File Base, Access, File DSM::Stream
	 * Base, Access, Stream DSM::ServiceGateway Access, ServiceGateway - Root
	 * Dir BIOP::StreamEvent Base, Access, Stream, Event
	 */
	public static int carouselPID = 0;
	int msgNumb = 0;
	FileList fl;

	public BIOP(FileList fl) {
		this.fl = fl;
	}

	public void parseModule(BitWise bw, int moduleLvl) {
		int biopLvl = MainPanel.addTreeItem("BIOP", moduleLvl); //$NON-NLS-1$
		bw.mark();
		while (bw.getAvailableSize() > 0)
			if (!parseMessage(bw, biopLvl)) {
				// TODO: i18n
				MainPanel.addTreeItem("Failed to parse module", biopLvl);
				break;
			}
		if (bw.getAvailableSize() > 0) {
			bw.reset();
			int i = bw.getAvailableSize();
			if (i > 120)
				i = 120;
			MainPanel.addTreeItem(bw.getHexSequence(i), MainPanel.addTreeItem(Messages.getString("BIOP.undefDataMsg"), //$NON-NLS-1$
					biopLvl));
		}
	}

	public boolean parseMessage(BitWise bw, int biopLvl) {
		int msglvl = MainPanel.addTreeItem(Messages.getString("BIOP.message") + msgNumb++, biopLvl); //$NON-NLS-1$
		// 0x42494F50 == BIOP
		if (bw.pop() != 0x42 || bw.pop() != 0x49 || bw.pop() != 0x4F || bw.pop() != 0x50)
			return false;
		// biop_version
		if (bw.pop16() != 0x0100)
			return false;
		// byte_order
		if (bw.pop() != 0x00) // big endian byte ordering
			return false;
		/* int message_type = */bw.pop();
		int message_size = bw.pop32();
		int mark = bw.getAbsolutePosition();
		Long objKey = parseObjKey(bw, msglvl);
		// objectKind_length == 4
		bw.pop(4);
		int objKind = bw.pop32();
		MainPanel.addTreeItem("objKind: " + (char) (BitWise.stripBits(objKind, 32, 8)) //$NON-NLS-1$
				+ (char) (BitWise.stripBits(objKind, 24, 8)) + (char) (BitWise.stripBits(objKind, 16, 8)), msglvl);
		int objectInfo_length;

		switch (objKind) {
		case 0x73726700: // "srg" == ServiceGateway
			fl.setSvcGatewayObjKey(objKey);
		case 0x64697200: // "dir" == Directory
			// fl.setToDir(objKey); //for empty directories
			objectInfo_length = bw.pop16();
			MainPanel.addTreeItem("objInfo: " + bw.getHexSequence(objectInfo_length), msglvl); //$NON-NLS-1$
			bw.pop(bw.pop() * 7); // ServiceContextList
			// int serviceCtxLstCount = bw.pop();
			// for (int i = 0; i < serviceCtxLstCount; i++) {
			// bw.pop32();// ctxId
			// bw.pop(bw.pop16()); //ctx datalengh
			// }
			/* int messageBody_length = */
			bw.pop32();
			int bindings = bw.pop16();
			int bindLvl = MainPanel.addTreeItem("bindings: " + bindings, msglvl); //$NON-NLS-1$
			for (int i = 0; i < bindings; i++) {
				String name = parseName(bw);// path
				int nameLvl = MainPanel.addTreeItem("name: " + name, bindLvl); //$NON-NLS-1$
				/* int bindingType = */bw.pop();
				// 0x01 == ncontext > bound to a Directory or ServiceGateway
				// TODO: use to create a dir.
				DSMCCObject f = fl.setName(parseIOR(bw, nameLvl), name);
				fl.addChildren(objKey, f);
				objectInfo_length = bw.pop16();
				MainPanel.addTreeItem("objInfo: " + bw.getHexSequence(objectInfo_length), nameLvl); //$NON-NLS-1$
			}

			break;
		case 0x66696C00: // "fil" == file
			objectInfo_length = bw.pop16();
			bw.pop32();
			bw.pop32(); // should be pop64(), but no file will be larger than a
			// few megs

			// serviceContextList_count + serviceContextList_data_byte
			bw.pop(bw.pop());
			// messageBody_length
			bw.pop32();
			int content_length = bw.pop32();
			MainPanel.addTreeItem(Messages.getString("BIOP.fileSize") + BitWise.toHex(content_length), msglvl); //$NON-NLS-1$
			fl.setContent(objKey, bw.buf, bw.getAbsolutePosition(), content_length);
			break;
		case 0x73747200: // "str" == Stream
			System.out.println("Stream"); //$NON-NLS-1$
			// TODO: show these events
			break;
		case 0x73746500: // "ste" == StreamEvent
			System.out.println("StreamEvent"); //$NON-NLS-1$
			// TODO: show these events
			break;
		default:
			break;
		}
		bw.pop(message_size - (bw.getAbsolutePosition() - mark));
		return true;
	}

	private Long parseObjKey(BitWise bw, int msgLvl) {
		int objectKey_length = bw.pop();
		long objKey = 0;
		for (int i = 0; i < objectKey_length; i++)
			objKey = objKey << 8 | (byte) bw.pop();
		MainPanel.addTreeItem("objKey: 0x" + Long.toHexString(objKey), msgLvl); //$NON-NLS-1$
		return new Long(objKey);
	}

	Long parseIOR(BitWise bw, int iorLvl) {
		iorLvl = MainPanel.addTreeItem("IOR", iorLvl); //$NON-NLS-1$
		Long objKey = null;
		int type_id_length = bw.pop32();
		bw.pop(type_id_length); // IOR:Type id
		if (type_id_length % 4 != 0) // CDR alignment rule
			bw.pop(4 - (type_id_length % 4));
		int taggedProfiles_count = bw.pop32();
		for (int j = 0; j < taggedProfiles_count; j++) {
			int profileId_tag = bw.pop32();
			int profile_data_length = bw.pop32();
			bw.mark();
			// 0x49534F06 == TAG_BIOP
			if (profileId_tag == 0x49534F06) {
				bw.pop(); // 0x00 big endian byte order
				bw.pop(); // liteComponents_count == 2
				// 0x49534F50 == TAG_ObjectLocation
				if (bw.pop32() == 0x49534F50) {
					bw.pop();// component_data_length
					MainPanel.addTreeItem("carouselId: " + BitWise.toHex(bw.pop32()), iorLvl); //$NON-NLS-1$
					MainPanel.addTreeItem("moduleId: " + BitWise.toHex(bw.pop16()), iorLvl); //$NON-NLS-1$
					bw.pop16(); // BIOP version
					objKey = parseObjKey(bw, iorLvl);
				}

			}
			bw.pop(profile_data_length - bw.getByteCount());
		}
		return objKey;
	}

	String parseName(BitWise bw) {
		// BIOP::Name(){
		String name = null;
		int nameComponents_count = bw.pop();
		for (int i = 0; i < nameComponents_count; i++) {
			int id_length = bw.pop();
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < id_length - 1; j++)
				sb.append((char) bw.pop());
			bw.pop();// null terminated
			int kind_length = bw.pop();
			bw.pop(kind_length); // nameComponent kind: fil, dir... - null
			// terminated string
			if (name == null)
				name = sb.toString();
		}
		return name;
	}

	public static void main(String[] cmdArgs) {
		FileList fl = new FileList();
		File f = new File("K:\\TS\\ModuleListB"); //$NON-NLS-1$
		File[] modules = f.listFiles();
		for (int i = 0; i < modules.length; i++) {
			f = modules[i];
			if (f.isFile()) {
				System.out.println("\n\nModule: " + f.getName()); //$NON-NLS-1$
				try {
					FileInputStream fis = new FileInputStream(f);
					byte[] ba = new byte[(int) f.length()];
					fis.read(ba);
					BitWise bw = new BitWise(ba);
					BIOP me = new BIOP(fl);
					me.parseModule(bw, 0);
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		DSMCCObject rd = fl.getRoot();
		rd.name = "root"; //$NON-NLS-1$
		File root = new File("K:\\TS\\ModuleListB\\fs"); //$NON-NLS-1$
		root.mkdir();
		rd.saveIn(root);
	}
}