package es.uvigo.det.labredes.lte;

import java.util.Map;
import java.util.HashMap;

/**
 * This class simulates an UE of the LTE network.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class UE extends LteNode {
    /**
     * The UE state.
     */
    private UEState state;
    /**
     * The previous UE state.
     */
    private UEState prev_state;
    /**
     * The next idle drx event.
     */
    private Event next_idle_drx_event;
    /**
     * The next drx event.
     */
    private Event next_drx_event;
    /**
     * Current number of consecutive cycles in CONNECTED_DRX state.
     */
    private int consecutive_connected_drx_cycles;
    /**
     * The eNB that manages the UE.
     */
    private ENB lte_enb;

    // Statistic variables
    private double last_state_transition_time;
    private Map<UEState, Double> time_in_states;

    /**
     * Creates a new UE.
     * The UE uplink traffic is simulated with the specified traffic generator.
     *
     * @param tg the traffic generator
     * @param enb the eNB of the LTE network
     */
    public UE (TrafficGenerator tg, ENB enb) {
	super(tg);

	lte_enb = enb;
	lte_enb.connectUE(this);
	last_state_transition_time = 0.0;
        time_in_states = new HashMap<UEState, Double>();
        for (UEState st : UEState.values()) {
            time_in_states.put(st, 0.0);
        }
	state = prev_state = UEState.CONNECTED;
	LteSimulator.event_handler.addEvent(new StateTransitionEvent (0.0, this, "handleStateTransitionEvent", state));
	consecutive_connected_drx_cycles = 0;
    }

    /**
     * Returns the current state of the UE.
     *
     * @return current state of the UE
     */
    public UEState getState () {
        return state;
    }

    /**
     * Returns the previous state of the UE.
     *
     * @return previous state of the UE
     */
    public UEState getPreviousState () {
        return prev_state;
    }

    /**
     * Handles the specified packet arrival event.
     *
     * @param event the PacketArrivalEvent to be handled
     */
    public void handlePacketArrivalEvent (PacketArrivalEvent event) {
	super.handlePacketArrivalEvent(event);

	if (state == UEState.DISCONNECTED || state == UEState.IDLE_DRX || state == UEState.IDLE_LISTENING) {
	    LteSimulator.event_handler.addEvent(new StateTransitionEvent (event.time, this, "handleStateTransitionEvent", UEState.CONNECTING));
	}
	if (state == UEState.CONNECTED_DRX || state == UEState.CONNECTED || state == UEState.CONNECTED_RX) {
	    LteSimulator.event_handler.addEvent(new PacketTransmissionEvent (event.time + LteSimulator.lte_psf, this, "handlePacketTransmissionEvent", event.packet_id));
	    if (state == UEState.CONNECTED_DRX) {
		LteSimulator.event_handler.addEvent(new StateTransitionEvent (event.time, this, "handleStateTransitionEvent", UEState.CONNECTED));
	    } else if (state == UEState.CONNECTED) {
		LteSimulator.event_handler.addEvent(new StateTransitionEvent (event.time, this, "handleStateTransitionEvent", UEState.CONNECTED_TX));
	    } else if (state == UEState.CONNECTED_RX) {
		LteSimulator.event_handler.addEvent(new StateTransitionEvent (event.time, this, "handleStateTransitionEvent", UEState.CONNECTED_TX_RX));
	    }
	}
    }

    /**
     * Handles the specified packet transmission event.
     *
     * @param event the PacketTransmissionEvent to be handled
     */
    public void handlePacketTransmissionEvent (PacketTransmissionEvent event) {
	super.handlePacketTransmissionEvent(event);

	if (qsize == 0) {
	    if (state == UEState.CONNECTED_TX_RX && lte_enb.qsize > 0) {
		LteSimulator.event_handler.addEvent(new StateTransitionEvent (event.time, this, "handleStateTransitionEvent", UEState.CONNECTED_RX));
	    } else {
		LteSimulator.event_handler.addEvent(new StateTransitionEvent (event.time, this, "handleStateTransitionEvent", UEState.CONNECTED));
	    }
	}
    }

    /**
     * Handles the specified state transition event.
     *
     * @param event the StateTransitionEvent to be handled
     */
    public void handleStateTransitionEvent (StateTransitionEvent event) {
	double new_event_time;
	if (event.new_state == UEState.IDLE_DRX) {
	    new_event_time = event.time + (LteSimulator.idle_drx_cycle - LteSimulator.idle_on_duration) * LteSimulator.lte_psf;
	    next_idle_drx_event = new StateTransitionEvent (new_event_time, this, "handleStateTransitionEvent", UEState.IDLE_LISTENING);
	    LteSimulator.event_handler.addEvent(next_idle_drx_event);
	    LteSimulator.event_handler.removeEvent(next_drx_event);
	} else if (event.new_state == UEState.IDLE_LISTENING) {
	    if (lte_enb.qsize > 0) {
		LteSimulator.event_handler.addEvent(new StateTransitionEvent (event.time, this, "handleStateTransitionEvent", UEState.CONNECTING));
	    } else {
		new_event_time = event.time + LteSimulator.idle_on_duration * LteSimulator.lte_psf;
		next_idle_drx_event = new StateTransitionEvent (new_event_time, this, "handleStateTransitionEvent", UEState.IDLE_DRX); 
		LteSimulator.event_handler.addEvent(next_idle_drx_event);
	    }
	} else if (event.new_state == UEState.CONNECTING) {
	    LteSimulator.event_handler.removeEvent(next_idle_drx_event);
	    new_event_time = event.time + LteSimulator.connection_delay * LteSimulator.lte_psf;
	    LteSimulator.event_handler.addEvent(new StateTransitionEvent (new_event_time, this, "handleStateTransitionEvent", UEState.CONNECTED));
	} else if (event.new_state == UEState.CONNECTED) {
	    int drx_cycle = consecutive_connected_drx_cycles + 1 <= LteSimulator.short_drx_cycle_timer ? LteSimulator.short_drx_cycle : LteSimulator.long_drx_cycle;
	    if (qsize == 0 && !lte_enb.exitDRX(event.time, drx_cycle)) {
		int new_event_interval = LteSimulator.inactivity_timer;
		if (state == UEState.CONNECTED_DRX) {
		    new_event_interval = LteSimulator.on_duration;
		} else {
		    new_event_time = event.time + LteSimulator.idle_inactivity_timer * LteSimulator.lte_psf;
		    UEState next_idle_drx_event_state = LteSimulator.disconnect_radio ? UEState.DISCONNECTED : UEState.IDLE_DRX;
		    next_idle_drx_event = new StateTransitionEvent (new_event_time, this, "handleStateTransitionEvent", next_idle_drx_event_state); 
		    LteSimulator.event_handler.addEvent(next_idle_drx_event);
		}
		new_event_time = event.time + new_event_interval * LteSimulator.lte_psf;
		next_drx_event = new StateTransitionEvent (new_event_time, this, "handleStateTransitionEvent", UEState.CONNECTED_DRX); 
		LteSimulator.event_handler.addEvent(next_drx_event);
	    } else {
		UEState next_state = UEState.CONNECTED_TX_RX;
		if (qsize > 0) {
		    int pid = ((PacketArrivalEvent) (queue.getNextEvent(false))).packet_id;
		    LteSimulator.event_handler.addEvent(new PacketTransmissionEvent (event.time + LteSimulator.lte_psf, this, "handlePacketTransmissionEvent", pid));
		    if (lte_enb.qsize == 0) {
			next_state = UEState.CONNECTED_TX;
		    }
		}
		if (lte_enb.qsize > 0) {
		    int enb_pid = ((PacketArrivalEvent) (lte_enb.queue.getNextEvent(false))).packet_id;
		    LteSimulator.event_handler.addEvent(new PacketTransmissionEvent (event.time + LteSimulator.lte_psf, lte_enb, "handlePacketTransmissionEvent", enb_pid));
		    if (qsize == 0) {
			next_state = UEState.CONNECTED_RX;
		    }
		}
		LteSimulator.event_handler.addEvent(new StateTransitionEvent (event.time, this, "handleStateTransitionEvent", next_state));
	    }
	} else if (event.new_state == UEState.CONNECTED_TX || event.new_state == UEState.CONNECTED_RX || event.new_state == UEState.CONNECTED_TX_RX) {
	    consecutive_connected_drx_cycles = 0;
	    LteSimulator.event_handler.removeEvent(next_drx_event);
	    LteSimulator.event_handler.removeEvent(next_idle_drx_event);
	} else if (event.new_state == UEState.CONNECTED_DRX) {
	    if (consecutive_connected_drx_cycles == 0) {
		lte_enb.updateQueueTreshold(event.time);
	    }
	    consecutive_connected_drx_cycles++;
	    int drx_cycle_length = consecutive_connected_drx_cycles <= LteSimulator.short_drx_cycle_timer ? LteSimulator.short_drx_cycle : LteSimulator.long_drx_cycle;
	    new_event_time = event.time + (drx_cycle_length - LteSimulator.on_duration) * LteSimulator.lte_psf;
	    next_drx_event = new StateTransitionEvent (new_event_time, this, "handleStateTransitionEvent", UEState.CONNECTED); 
	    LteSimulator.event_handler.addEvent(next_drx_event);
	}

        time_in_states.put(state, time_in_states.get(state) + event.time - last_state_transition_time);
	prev_state = state;
        state = event.new_state;
        last_state_transition_time = event.time;
        if (LteSimulator.simul_verbose) {
            event.print();
        }
    }

    /**
     * Prints on standard output some UE statistics.
     */
    public void printStatistics () {
	super.printStatistics();

        time_in_states.put(state, time_in_states.get(state) + LteSimulator.simul_length - last_state_transition_time);
        for (UEState st : UEState.values()) {
            System.out.format("UE time in state %s: %.9f %.2f %% %n", st, time_in_states.get(st), 100.0 * time_in_states.get(st) / LteSimulator.simul_length);
        }
    }

}
