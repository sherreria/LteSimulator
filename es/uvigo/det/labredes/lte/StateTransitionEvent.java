package es.uvigo.det.labredes.lte;

/**
 * This class extends Event class to represent state transitions at the UE.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class StateTransitionEvent extends Event<UE> {
    /**
     * The new state of the UE.
     */
    public UEState new_state;

    /**
     * Creates a new event representing a state transition at the UE.
     *
     * @param t      instant at which the UE changes its state
     * @param ue     UE that changes its state
     * @param method name of the method that handles the state transition
     * @param state  new state of the UE
     */
    public StateTransitionEvent (double t, UE ue, String method, UEState state) {
	super(t, ue, method);
	new_state = state;
    }

    /**
     * Compares two state transition events.
     *
     * @param event the Event to be compared
     * @return true if the specified event is equal to this event
     */
    public boolean equals (Object event) {
	if (event instanceof StateTransitionEvent && 
	    time == ((Event) event).time && handler.equals(((Event) event).handler) && handler_method_name.equals(((Event) event).handler_method_name) &&
	    new_state == ((StateTransitionEvent) event).new_state) {
	    return true;
	}
	return false;
    }

    /**
     * Prints on standard output a message describing the state transition event.
     */
    public void print () {
	System.out.format("%.9f UE StateTransitionEvent %s %n", time, new_state);
    }
}