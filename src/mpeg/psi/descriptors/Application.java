package mpeg.psi.descriptors;


public class Application extends AITDescriptor {

	public static int tag = 0x00;

	static String name = "Application Descriptor";

	private int level;

	public void printDescription() {

		level = addSubItem(name, tableIndx);

		// normalmente o valor deste campo será 5
		final int application_profiles_lenght = bw.pop();

		if (application_profiles_lenght != 5) addSubItem(
				"Erro: Valor do campo 'application_profiles_lenght' inesperado ", level);
		else {
			final int app_profile_number = bw.pop16();
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

			addSubItem("Application Profile: " + application_profile, level);

			final int[] version = new int[] { bw.pop(), bw.pop(), bw.pop() };

			addSubItem("Version: " + version[0] + "." + version[1] + "." + version[2], level);

			final int service_bound_flag = bw.consumeBits(1);

			addSubItem("Service Bound Flag: " + service_bound_flag, level);

			final int visibility = bw.consumeBits(2);
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

			addSubItem("Visibility: " + visiString, level);

			// Reserved for future use according to 15606-3
			bw.consumeBits(5);

			final int app_priority = bw.pop();

			addSubItem("Application Priority: " + app_priority, level);

			// We read 8 bytes before this point
			int remaininBytes = descriptor_length - 8;
			while (remaininBytes > 0) {
				addSubItem("Transport Protocol Label: " + bw.getHexSequence(1), level);
				remaininBytes--;
			}

		}

	}

}
