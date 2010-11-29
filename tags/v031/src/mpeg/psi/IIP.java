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
//ISDB-T Information Packet;
package mpeg.psi;

import sys.BitWise;

public class IIP extends Table {

	public IIP(int pid) {
		name = "IIP";
		this.pid = pid;
	}

	public boolean printDescription(byte[] ba) {
		bw = new BitWise(ba);
		bw.setBufferSize(22);
		// 7F DD 3D 25 8B 4B 3F FF 25 8B 4B 3F FF FF FF FF 73 3A DA 83
		treeIndx = addSubItem("IIP (pid " + BitWise.toHex(pid) + ")");
		// IIP_packet_pointer 16 - mas perdi algum byte no caminho
		bw.pop(); // sem uso no momento

		// modulation_control_configuration_information( ) 160
		modulationCCI();
		// IIP_branch_number 8
		// last_IIP_branch_number 8
		// network_synchronization_information_length 8
		// network_synchronization_information( )
		// for(i=0;i<(159- network_synchronization_information
		// _length);i++){
		// stuffing_byte(0xFF) 8
		return true;
	}

	int currentMode = 0;

	private void modulationCCI() {
		// modulation_control_configuration_information(){
		// TMCC_synchronization_word 1
		// AC_data_effective_position 1
		// reserved 2
		// mode_GI_information{
		// initialization_timing_indicator 4
		bw.pop();
		// current_mode 2
		currentMode = BitWise.stripBits(bw.pop(0), 8, 2);
		addSubItem("current_mode: " + currentMode);
		// current_guard_interval 2
		addSubItem("current_guard_interval: 1/" + (32 >> BitWise.stripBits(bw.pop(), 6, 2)));
		// next_mode 2
		// next_guard_interval 2
		// }
		// TMCC_information{
		// system_identifier 2
		// count_down_Index 4
		// switch-on_control_flag_used_for_alert_broadcasting 1
		addSubItem("alert_broadcasting_flag: " + BitWise.stripBits(bw.pop(), 2, 1));
		// current_configuration_information{
		// partial_reception_flag 1
		addSubItem("partial_reception_flag: " + BitWise.stripBits(bw.pop(0), 1, 1));
		// transmission_parameters_for_layer_A{
		int tmp = addSubItem("tx_parameters_for_layer_A");
		txParametersForLayer(tmp);
		// }
		// transmission_parameters_for_layer_B{
		tmp = addSubItem("tx_parameters_for_layer_B");
		txParametersForLayer(tmp);
		// }
		// transmission_parameters_for_layer_C{
		tmp = addSubItem("tx_parameters_for_layer_C");
		txParametersForLayer(tmp);
		// }
		// }
		// next_configuration_information{
		// partial_reception_flag 1
		// transmission_parameters_for_layer_A{
		// }
		// transmission_parameters_for_layer_B{
		// }
		// transmission_parameters_for_layer_C{
		// }
		// }
		// phase_correctiton_of_CP_in_connected_transmission 3
		// TMCC_reserved_future_use 12
		// reserved_future_use 10
		// }
		// CRC_32 32
		// }

	}

	private String[] modulation = { "DQPSK", "QPSK", "16QAM", "64QAM" };

	private String[] coding = { "1/2", "2/3", "3/4", "5/6", "7/8" };

	private void txParametersForLayer(int treeLevel) {
		// modulation_scheme 3
		int tmp = bw.consumeBits(3);
		if (tmp == 7) {
			addSubItem("Layer not in use", treeLevel);
			bw.consumeBits(10);
			return;
		} else if (tmp < 4)
			addSubItem("modulation_scheme: " + modulation[tmp], treeLevel);
		// coding_rate_of_inner_code 3
		tmp = bw.consumeBits(3);
		if (tmp < 5)
			addSubItem("coding_rate: " + coding[tmp], treeLevel);
		// length_of_time_interleaving 3
		tmp = bw.consumeBits(3);
		if (tmp == 0)
			addSubItem("length_of_time_interleaving: 0", treeLevel);
		else if (tmp < 4)
			addSubItem("length_of_time_interleaving: " + ((32 >> (3 - tmp)) >> currentMode), treeLevel);
		// number_of_segments 4
		tmp = bw.consumeBits(4);
		if (tmp > 0 && tmp < 14)
			addSubItem("number_of_segments: " + tmp, treeLevel);

	}
}
