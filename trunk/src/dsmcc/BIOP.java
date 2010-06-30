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

//Broadcast Inter ORB (CORBA) Protocol 
public class BIOP {
/*
DSM::Directory Access, Directory
DSM::File Base, Access, File
DSM::Stream Base, Access, Stream
DSM::ServiceGateway Access, ServiceGateway - Root Dir
BIOP::StreamEvent Base, Access, Stream, Event
 */
	static byte[] svcGatewayObjKey = null;
	public static int carouselPID = 0;
	int msgNumb = 0;
	
	public void parseModule(BitWise bw, int moduleLvl) {
		int biopLvl = MainPanel.addTreeItem("BIOP", moduleLvl);
		bw.mark();
		while (bw.getAvailableSize() > 0)
			if (!parseMessage(bw, biopLvl)) {
				System.out.println("pm fail");
				break;
			}
		if (bw.getAvailableSize() > 0) {
			bw.reset();
			int i = bw.getAvailableSize();
			if (i > 120)
				i = 120;
			MainPanel.addTreeItem(bw.getHexSequence(i),
					MainPanel.addTreeItem("Module contains unidentified data!", biopLvl));
		}
	}
	
	public boolean parseMessage(BitWise bw, int biopLvl) {
		System.out.println("pm");
		int msglvl = MainPanel.addTreeItem("message "+msgNumb++, biopLvl);
		//0x42494F50 == BIOP
		if (bw.pop() != 0x42 || bw.pop() != 0x49 || bw.pop() != 0x4F || bw.pop() != 0x50)
			return false;
		//biop_version
		if (bw.pop16() != 0x0100)
			return false;
		//byte_order
		if (bw.pop() != 0x00) //big endian byte ordering
			return false;
		int message_type = bw.pop();
		int message_size = bw.pop32();
		int mark = bw.getAbsolutePosition();
		byte[] objKey = parseObjKey(bw, msglvl);
		//objectKind_length == 4
		bw.pop(4);
		int objKind = bw.pop32();
		MainPanel.addTreeItem("objKind: "+(char)(bw.stripBits(objKind, 32, 8))+(char)(bw.stripBits(objKind, 24, 8))
				+(char)(bw.stripBits(objKind, 16, 8)), msglvl);
		System.out.println(bw.toHex(objKind));
		int objectInfo_length;
		
		switch (objKind) {
		case 0x73726700: //"srg" == ServiceGateway
			System.out.println("ServiceGateway");
			svcGatewayObjKey = objKey;
//			break;
		case 0x64697200: //"dir" == Directory
			System.out.println("Directory");
			objectInfo_length = bw.pop16();
			System.out.println("objInfo: "+bw.getHexSequence(objectInfo_length));
			bw.pop(bw.pop()*7); //ServiceContextList
//			int serviceCtxLstCount = bw.pop();
//			System.out.println("serviceCtxLstCount: "+serviceCtxLstCount);
//			for (int i = 0; i < serviceCtxLstCount; i++) {
//				bw.pop32();// ctxId
//				bw.pop(bw.pop16()); //ctx datalengh
//			}
			int messageBody_length = bw.pop32();
			int bindings = bw.pop16();
//			System.out.println("messageBody_length: "+messageBody_length);
			int bindLvl = MainPanel.addTreeItem("bindings: "+bindings, msglvl);
			for (int i = 0; i < bindings; i++) {
				String name = parseName(bw);//path
				int nameLvl = MainPanel.addTreeItem("name: "+name, bindLvl);
				int bindingType = bw.pop(); //0x01 == ncontext > bound to a Directory or ServiceGateway
//				System.out.println("bindingType: "+bindingType);
//				bw.pop(0x49);
				DSMCCFile f = FileList.setName(parseIOR(bw, nameLvl), name);
				FileList.add(objKey, f);
				objectInfo_length = bw.pop16();
				MainPanel.addTreeItem("objInfo: "+bw.getHexSequence(objectInfo_length), nameLvl);
			}
			
			break;
		case 0x66696C00: //"fil" == file
			System.out.println("File");
			objectInfo_length = bw.pop16();
			bw.pop32();
			bw.pop32(); //should be pop64(), but no file will be larger than a few megs
//			System.out.println("objInfo: "+bw.getHexSequence(objectInfo_length-8));
			
			//serviceContextList_count + serviceContextList_data_byte
			bw.pop(bw.pop());
			//messageBody_length
			bw.pop32();
			int content_length = bw.pop32();
			MainPanel.addTreeItem("file size "+bw.toHex(content_length), msglvl);
			FileList.setContent(objKey, bw.buf, bw.getAbsolutePosition(), content_length);
			break;
		case 0x73747200: //"str" == Stream
			System.out.println("Stream");
			break;
		case 0x73746500: //"ste" == StreamEvent
			System.out.println("StreamEvent");
			break;
		default:
			break;
		}
		bw.pop(message_size - (bw.getAbsolutePosition() - mark));
		return true;
	}

	private byte[] parseObjKey(BitWise bw, int msgLvl) {
		int objectKey_length = bw.pop();
		byte[] objKey = new byte[objectKey_length];
		for (int i = 0; i < objKey.length; i++)
			objKey[i] = (byte)bw.pop();
		MainPanel.addTreeItem("objKey: "+DSMCCFile.printObjKey(objKey), msgLvl);
		return objKey;
	}
	
	byte[] parseIOR(BitWise bw, int iorLvl) {
		iorLvl = MainPanel.addTreeItem("IOR", iorLvl);
		byte[] objKey = null;
		int type_id_length = bw.pop32();
		System.out.println("IOR:Type id: "+bw.getHexSequence(type_id_length));
		if (type_id_length % 4 != 0) //CDR alignment rule
			bw.pop(4-(type_id_length % 4));
		int taggedProfiles_count = bw.pop32();
		System.out.print("IOR:Tagged profiles: [");
		for (int j=0; j<taggedProfiles_count; j++) {
			int profileId_tag = bw.pop32();
			System.out.print("id: ");
			System.out.print(bw.toHex(profileId_tag));
			int profile_data_length = bw.pop32();
			bw.mark();
			//0x49534F06 == TAG_BIOP
			if (profileId_tag == 0x49534F06) {
				bw.pop(); //0x00 big endian byte order
				bw.pop(); //liteComponents_count == 2
				//0x49534F50 == TAG_ObjectLocation
				if (bw.pop32() == 0x49534F50) {
					bw.pop();//component_data_length
					MainPanel.addTreeItem("carouselId: "+bw.toHex(bw.pop32()), iorLvl);
					MainPanel.addTreeItem("moduleId: "+bw.toHex(bw.pop16()), iorLvl);
					bw.pop16(); //BIOP version
					objKey = parseObjKey(bw, iorLvl);
				}
					
			}
			bw.pop(profile_data_length-bw.getByteCount());
		}
		System.out.println("]");
		return objKey;
	}
	
	String parseName(BitWise bw) {
//		BIOP::Name(){
		int nameComponents_count = bw.pop();
		System.out.println("nameComponents_count: "+nameComponents_count);
		for (int i=0; i<nameComponents_count; i++) {
			int id_length = bw.pop();
			System.out.print("NameComponent id: [");
			StringBuffer sb = new StringBuffer();
			for (int j=0; j<id_length-1; j++)
				sb.append((char)bw.pop());
			bw.pop();//null terminated
			System.out.println(sb.toString()+"]");
			int kind_length = bw.pop();
			System.out.print("NameComponent kind: [");
			for (int j=0; j<kind_length-1; j++)
				System.out.print((char)bw.pop());
			bw.pop();//null terminated
			System.out.println("]");
			return sb.toString();
		}
		return null;
	}




	public static void main(String[] cmdArgs) {
		BitWise btw = new BitWise(new byte[] {});
		byte[] svcGatewayObjKey = null;
		for (int i = 1; i < 31; i++) {
			File f =  new File("K:\\modules\\Module00"+btw.toHex(i).substring(2));
			System.out.println("\n\nModule: "+f.getName());
			try {
				FileInputStream fis = new FileInputStream(f);
				byte[] ba = new byte[(int)f.length()];
				fis.read(ba);
				BitWise bw = new BitWise(ba);
				BIOP me =  new BIOP();
				me.parseModule(bw, 0);
				fis.close();
				if (me.svcGatewayObjKey != null) {
					svcGatewayObjKey = me.svcGatewayObjKey;
					System.out.println("gateway found");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		DSMCCDir rd = (DSMCCDir)FileList.getByObjKey(svcGatewayObjKey);
		rd.name = "root";
		File root =  new File("K:\\modules");
		root.mkdir();
		rd.saveIn(root);
//		File f =  new File("K:\\modules\\Module0002");
//		System.out.println("Module: "+f.getName());
//		try {
//			FileInputStream fis = new FileInputStream(f);
//			byte[] ba = new byte[(int)f.length()];
//			fis.read(ba);
//			BitWise bw = new BitWise(ba);
//			BIOP me =  new BIOP();
//			me.parseModule(bw);
//			fis.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}