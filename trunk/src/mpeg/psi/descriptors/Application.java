package mpeg.psi.descriptors;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import sys.Log;

public class Application extends AITDescriptor {

	public static int tag = 0x00;

	static String name = "Application Descriptor";

	private int level;

	public void printDescription() {

		this.level = addSubItem(name, tableIndx);

		// normalmente o valor deste campo será 5
		int application_profiles_lenght = bw.pop();

		if (application_profiles_lenght != 5) {
			this.printSubItem("Erro: ",
					"Valor do campo 'application_profiles_lenght' inesperado "
							.getBytes());
		} else {
			int app_profile_number = bw.pop16();
			String application_profile;

			switch (app_profile_number) {
			case 1:
				application_profile = "Perfil FSA_09 ou OSA_09 - Sem canal de interatividade";
				break;
			case 2:
				application_profile = "Perfil FSB_09 ou OSB_09 - Sem canal de interatividade";
				break;
			case 32769:
				application_profile = "Perfil FSA_09 ou OSA_09 - Com canal de interatividade";
				break;
			case 32770:
				application_profile = "Perfil FSB_09 ou OSB_09 - Com canal de interatividade";
				break;
			default:
				application_profile = "";
			}

			this.printSubItem("Application Profile: ", application_profile
					.getBytes());

			int[] version = new int[] { bw.pop(), bw.pop(), bw.pop() };
			String versionS = version[0] + "." + version[1] + "." + version[2];

			this.printSubItem("Version: ", versionS.getBytes());

			int service_bound_flag = bw.consumeBits(1);
			String serviceS = "" + service_bound_flag;

			this.printSubItem("Service Bound Flag: ", serviceS.getBytes());

			int visibility = bw.consumeBits(2);
			String visiString;
			
			switch (visibility) {
			case 0:
				visiString = "Not visible";
				break;
			case 1:
				visiString = "Visible only to another apps";
				break;
			case 2:
				visiString = "Reserved for future use";
				break;
			case 3:
				visiString = "Visible";
				break;
			default:
				visiString = "Unexpected value";
				break;
			}

			this.printSubItem("Visibility: ", visiString.getBytes());

			
			
			// Reserved for future use according to 15606-3
			bw.consumeBits(5);
			
			int app_priority = bw.pop();
			String app_priority_S = ""+app_priority;
			
			this.printSubItem("Application Priority: ", app_priority_S.getBytes());
			
			
			// We read 8 bytes before this point
			int remaininBytes = descriptor_length - 8;
			while(remaininBytes > 0){
				byte[] transport_protocol_label = new byte[]{(byte)bw.pop()};
				this.printSubItem("Transport Protocol Label: ", transport_protocol_label);
				remaininBytes--;
			}
			
			
		
			
			
		}

	}

	private void printSubItem(String label, byte[] value) {
		try {
			InputStreamReader isr = new InputStreamReader(
					new ByteArrayInputStream(value), "ISO8859_15_FDIS");
			char[] ca = new char[value.length];
			isr.read(ca);
			String valueS = new String(ca);
			addSubItem(label.concat(valueS), level);
		} catch (Exception e) {
			Log.printStackTrace(e);
		}
	}

}
