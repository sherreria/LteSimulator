package es.uvigo.det.labredes.lte;

import java.io.*;

/**
 * This class extends TrafficGenerator class to simulate previously traced traffic.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class TraceTrafficGenerator extends TrafficGenerator {
    private BufferedReader tracefile;

    /**
     * Creates a new trace traffic generator.
     *
     * @param filename name of the trace file
     */
    public TraceTrafficGenerator (String filename) {
	super(0);
	try {
	    tracefile = new BufferedReader(new FileReader(filename));
	} catch (FileNotFoundException e) {
	    LteSimulator.printError("Trace file not found!");
	}
    }

    /**
     * Returns the instant at which the next packet arrives.
     *
     * @return instant at which the next packet arrives (in seconds)
     */
    public double getNextArrival () {
	try {
	    String line = tracefile.readLine();
	    if (line != null) {
		String[] line_fields = line.split("\\s+");
		try {
		    arrival_time += Double.parseDouble(line_fields[0]);
		} catch (NumberFormatException e) {
		    LteSimulator.printError("Trace file: invalid interarrival time!");
		}
	    } else {
		arrival_time = LteSimulator.simul_length + 1;
	    }
	} catch (IOException e) {
	    LteSimulator.printError("Error while reading trace file!");
	}
	return arrival_time;
    }

}