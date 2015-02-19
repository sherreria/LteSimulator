package es.uvigo.det.labredes.lte;

import java.io.*;

/**
 * LteSimulator: Java program that simulates communications between the eNB and a connected UE in a LTE network.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public final class LteSimulator {

    /* Simulation parameters */
    /**
     * Length of the simulation (in seconds). Default = 10s.
     */
    public static double simul_length = 10;
    /**
     * Seed for the simulation. Default = 1.
     */
    public static long simul_seed = 1;
    /**
     * If true a message for each simulated event is printed on standard output. Default = false.
     */
    public static boolean simul_verbose = false;

    /* LTE parameters */
    /**
     * Physical subframe length (in seconds). Default = 1ms.
     */
    public static double lte_psf = 0.001;    
    /**
     * Delay required to put the UE in the RRC_CONNECTED state (in psf). Default = 260.
     */
    public static int connection_delay = 260;
    /**
     * If true the UE will disconnect the LTE radio instead of moving into the RRC_IDLE state. Default = false.
     */
    public static boolean disconnect_radio = false;

    /* RRC_CONNECTED DRX parameters */
    public static int short_drx_cycle = 32;
    public static int long_drx_cycle = 64;
    public static int short_drx_cycle_timer = 2;
    public static int on_duration = 2;
    public static int inactivity_timer = 10;
    public static int queue_threshold = 1;
    public static int delay_threshold = 1000;
    public static int target_avg_delay = 64;

    /* RRC_IDLE DRX parameters */
    public static int idle_drx_cycle = 1280;
    public static int idle_on_duration = 43;
    public static int idle_inactivity_timer = 10000;

    /**
     * Event handler.
     */
    public static EventList event_handler;

    private LteSimulator () {}

    /**
     * Prints on standard error the specified message and exits.
     */
    public static void printError (String s) {
	System.err.println("ERROR: " + s);
	System.exit(1);
    }

    /**
     * Main method.
     * Usage: java LteSimulator [-l simulation_length] [-s simulation_seed] [-f config_file] [-v]
     */
    public static void main (String[] args) {
	BufferedReader simul_file = null;

	// Traffic parameters
	String ul_traffic_distribution = "deterministic";
        double ul_packet_rate = 0.1; // in packets per psf
	double ul_alpha = 2.5; // if pareto distribution
	String ul_trace_file = ""; // if trace simulation
        String dl_traffic_distribution = "deterministic";
        double dl_packet_rate = 0.1; // in packets per psf
	double dl_alpha = 2.5; // if pareto distribution
	String dl_trace_file = ""; // if trace simulation

	// Arguments parsing
	for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-l")) {
		try {
		    simul_length = Double.parseDouble(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid simulation length!");
		}
		i++;
	    } else if (args[i].equals("-s")) {
		try {
		    simul_seed = Integer.parseInt(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid simulation seed!");
		}
		i++;
	    } else if (args[i].equals("-f")) {
		try {
		    simul_file = new BufferedReader(new FileReader(args[i+1]));
		} catch (FileNotFoundException e) {
		    printError("Config file not found!");
		}
		i++;
	    } else if (args[i].equals("-v")) {
                simul_verbose = true;
	    } else {
		printError("Unknown argument: " + args[i] + "\nUsage: java LteSimulator [-l simulation_length] [-s simulation_seed] [-f config_file] [-v]");
	    }
	}

	// Config file parsing
	if (simul_file != null) {
	    try {
		for (String line; (line = simul_file.readLine()) != null;) {
		    if (line.startsWith(";")) {
			// Just a comment
			continue;
		    } else {
			String[] line_fields = line.split("\\s+");
			if (line_fields[0].equals("PSF")) {
			    try {
				lte_psf = Double.parseDouble(line_fields[1]);
			    } catch (NumberFormatException e) {
				printError("Config file: invalid physical subframe duration!");
			    }			
			} else if (line_fields[0].equals("UL_TRAFFIC")) {
			    if (line_fields[1].equals("deterministic") || line_fields[1].equals("poisson") || line_fields[1].equals("pareto") || line_fields[1].equals("trace")) {
				ul_traffic_distribution = line_fields[1];
			    } else {
				printError("Config file: invalid uplink traffic distribution!");
			    }
			    if (line_fields[1].equals("trace")) {
				ul_trace_file = line_fields[2];
			    } else {
				try {
				    ul_packet_rate = Double.parseDouble(line_fields[2]);
				} catch (NumberFormatException e) {
				    printError("Config file: invalid uplink packet rate!");
				}
				if (line_fields[1].equals("pareto")) {
				    try {
					ul_alpha = Double.parseDouble(line_fields[3]);
				    } catch (NumberFormatException e) {
					printError("Config file: invalid uplink alpha parameter!");
				    }
				}
			    }
			} else if (line_fields[0].equals("DL_TRAFFIC")) {
			    if (line_fields[1].equals("deterministic") || line_fields[1].equals("poisson") || line_fields[1].equals("pareto") || line_fields[1].equals("trace")) {
				dl_traffic_distribution = line_fields[1];
			    } else {
				printError("Config file: invalid downlink traffic distribution!");
			    }
			    if (line_fields[1].equals("trace")) {
				dl_trace_file = line_fields[2];
			    } else {
				try {
				    dl_packet_rate = Double.parseDouble(line_fields[2]);
				} catch (NumberFormatException e) {
				    printError("Config file: invalid downlink packet rate!");
				}
				if (line_fields[1].equals("pareto")) {
				    try {
					dl_alpha = Double.parseDouble(line_fields[3]);
				    } catch (NumberFormatException e) {
					printError("Config file: invalid downlink alpha parameter!");
				    }
				}
			    }
			} else if (line_fields[0].equals("RRC_CONNECTED_DRX")) {
			    try {
				short_drx_cycle = Integer.parseInt(line_fields[1]);
				long_drx_cycle = Integer.parseInt(line_fields[2]);
				short_drx_cycle_timer = Integer.parseInt(line_fields[3]);
				on_duration = Integer.parseInt(line_fields[4]);
			        inactivity_timer = Integer.parseInt(line_fields[5]);
				queue_threshold = Integer.parseInt(line_fields[6]);
			    } catch (NumberFormatException e) {
				printError("Config file: invalid DRX RRC_CONNECTED configuration!");
			    }
			    if (queue_threshold != 1) {
				try {
				    delay_threshold = Integer.parseInt(line_fields[7]);
				    if (queue_threshold == 0) {
					target_avg_delay = Integer.parseInt(line_fields[8]);
				    }
				} catch (NumberFormatException e) {
				    printError("Config file: invalid DRX RRC_CONNECTED configuration!");
				}
			    }
			} else if (line_fields[0].equals("RRC_IDLE_DRX")) {
			    try {
				idle_drx_cycle = Integer.parseInt(line_fields[1]);
				idle_on_duration = Integer.parseInt(line_fields[2]);
			        idle_inactivity_timer = Integer.parseInt(line_fields[3]);
				connection_delay = Integer.parseInt(line_fields[4]);
			    } catch (NumberFormatException e) {
				printError("Config file: invalid DRX RRC_IDLE configuration!");
			    }
			}
		    }
		}
		simul_file.close();
	    } catch (IOException e) {
		printError("Error while reading config file!");
	    }
	}
	
	// Event handler initialization
	event_handler = new EventList(simul_length);

	// UE and eNB initialization
	TrafficGenerator tg = null;
	if (dl_traffic_distribution.equals("deterministic")) {
	    tg = new DeterministicTrafficGenerator(dl_packet_rate);
	} else if (dl_traffic_distribution.equals("poisson")) {
	    tg = new PoissonTrafficGenerator(dl_packet_rate);
	} else if (dl_traffic_distribution.equals("pareto")) {
	    tg = new ParetoTrafficGenerator(dl_packet_rate, dl_alpha);
	} else if (dl_traffic_distribution.equals("trace")) {
	    tg = new TraceTrafficGenerator(dl_trace_file);
	}
	tg.setSeed(simul_seed);
	ENB enb = new ENB(tg);
	if (ul_traffic_distribution.equals("deterministic")) {
	    tg = new DeterministicTrafficGenerator(ul_packet_rate);
	} else if (ul_traffic_distribution.equals("poisson")) {
	    tg = new PoissonTrafficGenerator(ul_packet_rate);
	} else if (ul_traffic_distribution.equals("pareto")) {
	    tg = new ParetoTrafficGenerator(ul_packet_rate, ul_alpha);
	} else if (ul_traffic_distribution.equals("trace")) {
	    tg = new TraceTrafficGenerator(ul_trace_file);
	}
	tg.setSeed(simul_seed);
	UE ue = new UE(tg, enb);	

	// Events processing
	Event event;
        while ((event = event_handler.getNextEvent(true)) != null) {
	    event_handler.handleEvent(event);
	}

	// Print statistics
	enb.printStatistics();
	ue.printStatistics();
    }

}