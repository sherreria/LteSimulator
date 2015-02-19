package es.uvigo.det.labredes.lte;

/**
 * This class extends Event class to represent the drop of a new arriving packet.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class PacketDropEvent extends Event<LteNode> {

    /**
     * Creates a new event representing the drop of a new arriving packet.
     *
     * @param t      instant at which the new arriving packet is discarded
     * @param node   node that discards the new packet
     * @param method name of the method that handles the packet drop
     */
    public PacketDropEvent (double t, LteNode node, String method) {
	super(t, node, method);
    }

    /**
     * Compares two packet drop events.
     *
     * @param event the Event to be compared
     * @return true if the specified event is equal to this event
     */
    public boolean equals (Object event) {
	if (event instanceof PacketDropEvent && 
	    time == ((Event) event).time && handler.equals(((Event) event).handler) && handler_method_name.equals(((Event) event).handler_method_name)) {
	    return true;
	}
	return false;
    }

    /**
     * Prints on standard output a message describing the packet drop event.
     */
    public void print () {
	String handler_class = handler instanceof ENB ? "ENB" : "UE";
	System.out.format("%.9f %s PacketDropEvent %d %n", time, handler_class, handler.qsize);
    }
}
