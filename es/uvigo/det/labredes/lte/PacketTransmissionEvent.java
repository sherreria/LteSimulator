package es.uvigo.det.labredes.lte;

/**
 * This class extends Event class to represent the transmission of a packet.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class PacketTransmissionEvent extends Event<LteNode> {
    /**
     * The unique identifier of the packet transmitted.
     */
    public int packet_id;

    /**
     * Creates a new event representing the transmission of one packet.
     *
     * @param t      instant at which the node ends packet transmission
     * @param node   node that transmits the packet
     * @param method name of the method that handles the packet transmission
     */
    public PacketTransmissionEvent (double t, LteNode node, String method, int pid) {
	super(t, node, method);
	packet_id = pid;
    }

    /**
     * Compares two packet transmission events.
     *
     * @param event the Event to be compared
     * @return true if the specified event is equal to this event
     */
    public boolean equals (Object event) {
	if (event instanceof PacketTransmissionEvent && 
	    time == ((Event) event).time && handler.equals(((Event) event).handler) && handler_method_name.equals(((Event) event).handler_method_name) &&
	    packet_id == ((PacketTransmissionEvent) event).packet_id) {
	    return true;
	}
	return false;
    }

    /**
     * Prints on standard output a message describing the packet transmission event.
     */
    public void print () {
	String handler_class = handler instanceof ENB ? "ENB" : "UE";
	System.out.format("%.9f %s PacketTransmissionEvent %d %d %n", time, handler_class, packet_id, handler.qsize);
    }
}