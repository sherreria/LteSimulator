package es.uvigo.det.labredes.lte;

/**
 * This class extends Event class to represent the arrival of a new packet.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class PacketArrivalEvent extends Event<LteNode> {
    /**
     * Event counter used to assign an unique identifier to each arriving packet.
     */
    static public int packet_counter = 0;
    /**
     * The unique identifier of the arriving packet.
     */
    public int packet_id;

    /**
     * Creates a new event representing the arrival of a new packet.
     *
     * @param t      instant at which the new packet arrives
     * @param node   node that receives the new packet
     * @param method name of the method that handles the packet arrival
     */
    public PacketArrivalEvent (double t, LteNode node, String method) {
	super(t, node, method);
	packet_id = packet_counter;
	packet_counter++;
    }

    /**
     * Compares two packet arrival events.
     *
     * @param event the Event to be compared
     * @return true if the specified event is equal to this event
     */
    public boolean equals (Object event) {
	if (event instanceof PacketArrivalEvent && 
	    time == ((Event) event).time && handler.equals(((Event) event).handler) && handler_method_name.equals(((Event) event).handler_method_name) &&
	    packet_id == ((PacketArrivalEvent) event).packet_id) {
	    return true;
	}
	return false;
    }

    /**
     * Prints on standard output a message describing the packet arrival event.
     */
    public void print () {
	String handler_class = handler instanceof ENB ? "ENB" : "UE";
	System.out.format("%.9f %s PacketArrivalEvent %d %d %f %n", time, handler_class, packet_id, handler.qsize, handler.avg_arrival_rate);
    }
}
