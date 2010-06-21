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

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import sys.BitWise;

public class Service extends Descriptor {

	public static int tag = 0x48;

	static String name = "Service Descriptor";

	/*
	 * service_descriptor(){ service_type 8 uimsbf service_provider_name_length
	 * 8 uimsbf for(i=0;i<N;i++){ char 8 uimsbf } service_name_length 8 uimsbf
	 * for(i=0;i<N;i++){ Char 8 uimsbf } }
	 */
	int[] typeTags = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x0A, 0x0B,
			0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x16, 0x17, 0x18, 0x19, 0x1A,
			0x1B, 0xA1, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xAA,
			0xAB, 0xAC, 0xC0 };

	final String[] typeNames = { "Reservado para uso futuro",
			"Televisão digital", "Áudio digital", "Teletexto",
			"Referência NVOD", "Serv. time-shifted NVOD", "Mosaico",
			"Cod. avançada de rádio digital", "Cod. avançada de mosaico",
			"Transmissão de dados",
			"Reservado para interface de uso comum (ver EN 50221)",
			"RCS Map (ver EN 301790)", "RCS FLS (ver EN 301790)",
			"Serv. DVB MHP", "Televisão digital MPEG-2",
			"Cod. avançada de televisão digital SD",
			"Cod. avançada de NVOD SD time-shifted",
			"Cod. avançada de referência NVOD SD",
			"Cod. avançada de televisão digital HD",
			"Cod. avançada de NVOD HD time-shifted",
			"Cod. avançada de referência NVOD HD", "Serv. especial de vídeo",
			"Serv. especial de áudio", "Serv. especial de dados",
			"Engenharia (atualização de software)",
			"Serv. promocional de vídeo", "Serv. promocional de áudio",
			"Serv. promocional de dados",
			"Dados para armazenamento antecipado",
			"Dados exclusivo para armazenamento",
			"Lista de Serv.s de bookmark", "Serv. simultâneo do tipo servidor",
			"Serv. independente de arquivos", "Dados" };

	public void printDescription() {
		int level = addSubItem(name, tableIndx);
		String serviceType = "Não definido";
		int typeTag = bw.pop();
		for (int i = 0; i < typeTags.length; i++)
			if (typeTags[i] == typeTag)
				serviceType = typeNames[i];
		addSubItem("service_type: " + serviceType, level);
		int textLenght = bw.pop();
		addSubItem("service_provider: [" + getText(textLenght, bw) + "]", level);
		textLenght = bw.pop();
		addSubItem("service_name: [" + getText(textLenght, bw) + "]", level);
	}

	public String getText(int lenght, BitWise bw) {
		String text = null;
		byte[] ba = new byte[lenght];
		for (int i = 0; i < ba.length; i++)
			ba[i] = (byte) bw.pop();
		try {
			InputStreamReader isr = new InputStreamReader(
					new ByteArrayInputStream(ba), "ISO8859_15_FDIS");
			char[] ca = new char[lenght];
			isr.read(ca);
			text = new String(ca);
		} catch (Exception e) {
			text = "codificaçao desconhecida";
		}
		return text;
	}
}
