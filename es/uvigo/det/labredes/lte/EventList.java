package es.uvigo.det.labredes.lte;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.*;

/**
 * This class implements a discrete sequence of events sorted by event time.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class EventList {
    private List<Event> list;
    private double simul_time;
    private double end_time;

    /**
     * Creates a new list of events.
     *
     * @param t the end of time
     */
    public EventList(double t) {
	list = new ArrayList<Event>();
	simul_time = 0.0;
	end_time = t;
    }

    /**
     * Adds the specified event to the event list at the right position.
     *
     * @param event the Event to be added
     * @return true if the specified event is correctly added to the event list
     */
    public boolean addEvent (Event event) {
	event.time = (double) Math.round(event.time * 1000000000) / 1000000000;
	if (event.time < simul_time) {
	    LteSimulator.printError("Trying to add an event with an invalid event time!");
	}
	if (event.time > end_time || list.contains(event)) {
	    return false;
	}

	int i = 0;
	int list_size = list.size();
	while (i < list_size && event.compareTo(list.get(i)) <= 0) {
	    i++;
	}
	if (i == list_size) {
	    return list.add(event);
	}
	list.add(i, event);
	return true;
    }

    /**
     * Returns the time of the last event handled.
     *
     * @return the time of the last event handled
     */
    public double getSimulTime () {
	return simul_time;
    }

    /**
     * Returns the next event in the event list.
     *
     * @param remove if true the event is removed from the list
     * @return the next event in this event list or null if the list is empty
     */
    public Event getNextEvent (boolean remove) {
	Event event;
	try {
	    event = remove ? list.remove(0) : list.get(0);
	} catch (Exception e) {
	    event = null;
	}
	return event;
    }

    /**
     * Invokes the method that handles the specified event on the corresponding object.
     *
     * @param event the Event to be handled
     */
    public void handleEvent (Event event) {
	simul_time = event.time;
	try {
	    Method handler_method = event.handler.getClass().getMethod(event.handler_method_name, event.getClass());
	    try {
		handler_method.invoke(event.handler, event);
	    } catch (Exception e) {
		LteSimulator.printError("Handler method invoke exception: " + event.handler_method_name + ": " + e.getMessage());
	    }
	} catch (Exception e) {
	    LteSimulator.printError("Handler method exception: " + e.getMessage());
	}
    }

    /**
     * Prints on standard output a message for each event contained in the event list.
     */
    public void print () {
	Event event;
	for (int i = 0; i < list.size(); i++) {
	    list.get(i).print();
	}
    }

    /**
     * Removes the specified event from the event list. If the list does not contain the event, it is unchanged.
     *
     * @param event the Event to be removed
     * @return true if the event list contained the specified event 
     */
    public boolean removeEvent (Event event) {
	return list.remove(event);
    }
}
