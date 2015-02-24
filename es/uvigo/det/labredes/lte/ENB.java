package es.uvigo.det.labredes.lte;

/**
 * This class simulates the eNB of the LTE network.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class ENB extends LteNode {
    /**
     * The UE connected to the eNB.
     */
    private UE lte_ue;

    // Adaptive DRX variables
    private int cycle_packets_sent;
    private double cycle_sum_packets_delay;
    private double hol_drx_packet_time;
    private double adaptive_queue_threshold, max_queue_threshold;
    private double sum_weighted_queue_threshold, update_queue_threshold_time;

    /**
     * Creates a new eNB.
     * The eNB downlink traffic is simulated with the specified traffic generator.
     *
     * @param tg the traffic generator
     */
    public ENB (TrafficGenerator tg) {
	super(tg);

	if (LteSimulator.queue_threshold == 0) {
	    cycle_packets_sent = 0;
	    cycle_sum_packets_delay = hol_drx_packet_time = 0.0;
	    adaptive_queue_threshold = 1.0;
	    max_queue_threshold = LteSimulator.delay_threshold;
	}
	sum_weighted_queue_threshold = update_queue_threshold_time = 0.0;
    }

    /**
     * Connects the specified UE with the eNB.
     *
     * @param ue the UE to be connected
     */
    public void connectUE (UE ue) {
        lte_ue = ue;
    }

    /**
     * Handles the specified packet arrival event.
     *
     * @param event the PacketArrivalEvent to be handled
     */
    public void handlePacketArrivalEvent (PacketArrivalEvent event) {
	super.handlePacketArrivalEvent(event);

	UEState state = lte_ue.getState();
	if (state == UEState.IDLE_LISTENING) {
	    LteSimulator.event_handler.addEvent(new StateTransitionEvent (event.time, lte_ue, "handleStateTransitionEvent", UEState.CONNECTING));
	}
	if (state == UEState.CONNECTED && (lte_ue.getPreviousState() != UEState.CONNECTED_DRX || qsize >= getQueueThreshold())) {
	    int pid = lte_ue.getPreviousState() != UEState.CONNECTED_DRX ? event.packet_id : ((PacketArrivalEvent) (queue.getNextEvent(false))).packet_id;
	    LteSimulator.event_handler.addEvent(new PacketTransmissionEvent (event.time + LteSimulator.lte_psf, this, "handlePacketTransmissionEvent", pid));
	    LteSimulator.event_handler.addEvent(new StateTransitionEvent (event.time, lte_ue, "handleStateTransitionEvent", UEState.CONNECTED_RX));
	}
	if (state == UEState.CONNECTED_TX) {
	    LteSimulator.event_handler.addEvent(new PacketTransmissionEvent (event.time + LteSimulator.lte_psf, this, "handlePacketTransmissionEvent", event.packet_id));
	    LteSimulator.event_handler.addEvent(new StateTransitionEvent (event.time, lte_ue, "handleStateTransitionEvent", UEState.CONNECTED_TX_RX));
	}
	if (hol_drx_packet_time == 0.0 && (state == UEState.CONNECTED_DRX || (state == UEState.CONNECTED && lte_ue.getPreviousState() == UEState.CONNECTED_DRX))) {
	    hol_drx_packet_time = event.time;
	}
    }

    /**
     * Handles the specified packet transmission event.
     *
     * @param event the PacketTransmissionEvent to be handled
     */
    public void handlePacketTransmissionEvent (PacketTransmissionEvent event) {
	super.handlePacketTransmissionEvent(event);

	if (LteSimulator.queue_threshold == 0) {
	    cycle_packets_sent++;
	    cycle_sum_packets_delay += current_packet_delay;
	}
	if (qsize == 0) {
	    if (lte_ue.getState() == UEState.CONNECTED_TX_RX && lte_ue.qsize > 0) {
		LteSimulator.event_handler.addEvent(new StateTransitionEvent (event.time, lte_ue, "handleStateTransitionEvent", UEState.CONNECTED_TX));
	    } else {
		LteSimulator.event_handler.addEvent(new StateTransitionEvent (event.time, lte_ue, "handleStateTransitionEvent", UEState.CONNECTED));
	    }
	}
    }

    /**
     * Returns current queue threshold value.
     *
     * @return current queue threshold value
     */
    public int getQueueThreshold () {
	if (LteSimulator.queue_threshold > 0) {
	    return LteSimulator.queue_threshold;
	}
	return (int) Math.ceil(adaptive_queue_threshold);
    }

    /**
     * Returns true if the UE must abandon the DRX mode.
     *
     * @param now current simulation time
     * @param drx_cycle length of the next DRX cycle (in psf)
     * @return true if the UE must abandon the DRX mode
     */
    public boolean exitDRX (double now, int drx_cycle) {
	if (lte_ue.getState() == UEState.CONNECTED_DRX) {
	    if (qsize >= getQueueThreshold()) {
		return true;
	    }
	    if (LteSimulator.delay_threshold > 0 &&
		hol_drx_packet_time > 0 && 
		(now - hol_drx_packet_time + drx_cycle * LteSimulator.lte_psf > LteSimulator.delay_threshold * LteSimulator.lte_psf ||
		 now - prev_arrival_time + (drx_cycle + qsize - 1) * LteSimulator.lte_psf > LteSimulator.delay_threshold * LteSimulator.lte_psf)) {	    
		return true;
	    }
	}
	return false;
    }

    /**
     * Updates DRX queue threshold value.
     *
     * @param now current simulation time
     */
    public void updateQueueTreshold (double now) {
	hol_drx_packet_time = 0.0;
	if (LteSimulator.queue_threshold == 0 && cycle_packets_sent > 0) {
	    sum_weighted_queue_threshold += getQueueThreshold() * (now - update_queue_threshold_time);
	    update_queue_threshold_time = now;
	    double cycle_avg_delay = cycle_sum_packets_delay / cycle_packets_sent;
	    adaptive_queue_threshold += 2.0 * avg_arrival_rate * (LteSimulator.target_avg_delay * LteSimulator.lte_psf - cycle_avg_delay);
	    if (adaptive_queue_threshold < 1) {
		adaptive_queue_threshold = 1.0;
	    } else if (adaptive_queue_threshold > max_queue_threshold) {
		adaptive_queue_threshold = max_queue_threshold;
	    }
	    cycle_packets_sent = 0;
	    cycle_sum_packets_delay = 0.0;
	}
    }

    /**
     * Prints on standard output some eNB statistics.
     */
    public void printStatistics () {
	super.printStatistics();

	sum_weighted_queue_threshold += getQueueThreshold() * (LteSimulator.simul_length - update_queue_threshold_time);
	System.out.format("ENB average DRX queue threshold: %.9f %n", sum_weighted_queue_threshold/LteSimulator.simul_length);
    }

}
