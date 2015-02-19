package es.uvigo.det.labredes.lte;

/**
 * This class implements each of the individual events simulated.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
abstract public class Event<T> implements Comparable {
    /**
     * The instant at which the event occurs.
     */
    public double time;
    /**
     * The object responsible for handling the event.
     */
    public T handler;
    /**
     * The name of the method that handles the event.
     */
    public String handler_method_name;

    /**
     * Creates a new event ocurring at the specified time.
     *
     * @param t      instant at which the event occurs
     * @param obj    object responsible for handling the event
     * @param method name of the method that handles the event
     */
    public Event (double t, T obj, String method) {
	time = t;
	handler = obj;
	handler_method_name = method;
    }

    /**
     * Compares two events based on the instant at which each event occurs.
     *
     * @param event the Event to be compared
     * @return 0 if both the specified event and this event occur at the same instant; a value less than 0 if this event is later than the specified event; and a value greater than 0 if this event is earlier than the specified event
     */
    public int compareTo (Object event) {
        return (int) (10e9 * (((Event) event).time - this.time));
    }

    /**
     * Prints on standard output a message describing this event.
     */
    abstract public void print ();
}
