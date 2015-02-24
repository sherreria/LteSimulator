package es.uvigo.det.labredes.lte;

/**
 * This class simulates a node (eNB or UE) of the LTE network.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
abstract public class LteNode {
    /**
     * The traffic generator.
     */
    public TrafficGenerator trgen;
    /**
     * The transmission queue.
     */
    public EventList queue;
    /**
     * The amount of packets stored in the transmission queue.
     */
    public int qsize;
    /**
     * The maximum amount of packets that can be stored in the transmission queue.
     */
    public int max_qsize;

    // Statistics variables
    public int packets_received, packets_sent, packets_dropped;
    public double sum_packets_delay, maximum_packet_delay, current_packet_delay;
    public double prev_arrival_time, avg_arrival_rate;

    /**
     * Creates a new node.
     * The traffic is simulated with the specified traffic generator.
     *
     * @param tg the traffic generator
     */
    public LteNode (TrafficGenerator tg) {
	trgen = tg;
        queue = new EventList(LteSimulator.simul_length);
        qsize = max_qsize = 0;

	packets_received = packets_sent = packets_dropped = 0;
	sum_packets_delay = maximum_packet_delay = current_packet_delay = 0.0;
	prev_arrival_time = avg_arrival_rate = 0.0;

	LteSimulator.event_handler.addEvent(new PacketArrivalEvent (trgen.getNextArrival(), this, "handlePacketArrivalEvent"));
    }

    /**
     * Handles the specified packet arrival event.
     *
     * @param event the PacketArrivalEvent to be handled
     */
    public void handlePacketArrivalEvent (PacketArrivalEvent event) {
	packets_received++;
        if (max_qsize == 0 || qsize + 1 <= max_qsize) {
            qsize++;
	    double interarrival_time = event.time - prev_arrival_time;
	    if (avg_arrival_rate > 0) {
		avg_arrival_rate = 1.0 / interarrival_time + 
		    Math.exp(-0.5 * interarrival_time / LteSimulator.delay_threshold / LteSimulator.lte_psf) * 
		    (avg_arrival_rate - 1.0 / interarrival_time);
	    } else {
		avg_arrival_rate = 1.0 / interarrival_time;
	    }
	    prev_arrival_time = event.time;
            queue.addEvent(event);
            if (LteSimulator.simul_verbose) {
                event.print();
            }
        } else {
            LteSimulator.event_handler.addEvent(new PacketDropEvent (event.time, this, "handlePacketDropEvent"));
        }
        LteSimulator.event_handler.addEvent(new PacketArrivalEvent (trgen.getNextArrival(), this, "handlePacketArrivalEvent"));
    }

    /**
     * Handles the specified packet drop event.
     *
     * @param event the PacketDropEvent to be handled
     */
    public void handlePacketDropEvent (PacketDropEvent event) {
        packets_dropped++;
        if (LteSimulator.simul_verbose) {
            event.print();
        }
    }

    /**
     * Handles the specified packet transmission event.
     *
     * @param event the PacketTransmissionEvent to be handled
     */
    public void handlePacketTransmissionEvent (PacketTransmissionEvent event) {
        if (qsize == 0 || ((PacketArrivalEvent) (queue.getNextEvent(false))).packet_id != event.packet_id) {
	    event.print();
            LteSimulator.printError("Trying to handle an invalid packet transmission!");
        }
	qsize--;
        packets_sent++;
        current_packet_delay = event.time - queue.getNextEvent(true).time - LteSimulator.lte_psf;
        if (current_packet_delay > maximum_packet_delay) {
            maximum_packet_delay = current_packet_delay;
        }
        sum_packets_delay += current_packet_delay;
	if (LteSimulator.simul_verbose) {
            event.print();
        }
	if (qsize > 0) {
	    int pid = ((PacketArrivalEvent) (queue.getNextEvent(false))).packet_id;
            LteSimulator.event_handler.addEvent(new PacketTransmissionEvent (event.time + LteSimulator.lte_psf, this, "handlePacketTransmissionEvent", pid));
	}
    }

    /**
     * Prints on standard output some statistics.
     */
    public void printStatistics () {
	String class_node = this instanceof ENB ? "ENB" : "UE";
        System.out.format("%s packets: received %d sent %d dropped %d %n", class_node, packets_received, packets_sent, packets_dropped);
        if (packets_sent > 0) {
            System.out.format("%s packet delay: average %.9f max %.9f %n", class_node, sum_packets_delay / packets_sent, maximum_packet_delay);
        }
    }
}
