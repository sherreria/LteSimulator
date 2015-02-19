# LteSimulator
Java program that simulates communications between the eNB and a connected UE in a LTE network.

# Invocation
java LteSimulator [-l simulation_length] [-s simulation_seed] [-f config_file] [-v]

# Output
The simulator outputs a summary of eNB and UE statistics:

    - Number of packets received, sent and dropped

    - Average and maximum packet delay

    - Time in each energy state (UE)

    - Average DRX queue threshold (eNB)

With option -v, the simulator outputs a line for every simulated event:

    `event_time ENB/UE event_type event_info`

# Legal
Copyright ⓒ Sergio Herrería Alonso <sha@det.uvigo.es> 2015

This simulator is licensed under the GNU General Public License, version 3 (GPL-3.0). For more information see LICENSE.txt
