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
import sys.Log;

public class DescriptorList {

	static Class[] descList = { TSinformation.class, TerrestrialSystemDelivery.class, PartialReception.class,
			StreamIdentifier.class, DataComponent.class, Component.class, ApplicationSignaling.class,
			NetworkName.class, ParentalRating.class, ShortEvent.class, Service.class, DataContent.class,
			AudioComponent.class, CarouselID.class, AssociationTag.class, ExtendedEvent.class };

	static boolean exception;

	static int getTag(Class cl) {
		int tag = 0;
		try {
			tag = cl.getField("tag").getInt(cl);
		} catch (Exception e) {
			System.err.println("getTag(" + cl.getName() + ")");
			System.err.println(e.getLocalizedMessage());
			exception = true;
		}
		return tag;
	}

	static void invokeMethod(Class cl, Object o, String method) {
		try {
			(cl.cast(o)).getClass().getMethod(method, null).invoke(o, null);
		} catch (Exception e) {
			Log.printStackTrace(new Exception("invokeMethod(" + cl.getName() + ", " + o.getClass().getName() + ")"));
			Log.printStackTrace(e);
			exception = true;
		}
	}

	static Descriptor getDescriptor(Class cl, int treeIndex, BitWise bw) {
		Descriptor d = null;
		try {
			d = (Descriptor) (cl.getConstructors()[0]).newInstance(null);
		} catch (Exception e) {
			System.err.println("getDescriptor(" + cl.getName() + ", " + treeIndex + ")");
			System.err.println(e.getLocalizedMessage());
			exception = true;
		}
		return d;
	}

	public static void print(BitWise bw, int treeIndex) {
		int tag = Descriptor.preparse(bw);
		exception = false;
		for (int i = 0; i < descList.length; i++) {
			Class descClass = descList[i];
			if (getTag(descClass) == tag) {
				Descriptor d = getDescriptor(descClass, treeIndex, bw);
				d.setUp(treeIndex, bw);
				invokeMethod(descClass, d, "printDescription");
				if (!exception)
					return;
			}
		}
		Descriptor d = new Descriptor();
		d.setUp(treeIndex, bw);
		d.print();
	}
}
