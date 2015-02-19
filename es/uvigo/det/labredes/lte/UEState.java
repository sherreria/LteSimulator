package es.uvigo.det.labredes.lte;

/**
 * This type enums all the possible states of energy-aware UEs.
 */
public enum UEState {
    /**
     * The UE is disconnected (reception disabled).
     */
    DISCONNECTED,
	/**
	 * The UE is in the RRC_IDLE state (reception disabled).
	 */
	IDLE_DRX,
	/**
	 * The UE is in the RRC_IDLE state (reception enabled).
	 */
	IDLE_LISTENING,
	/**
	 * The UE is transitioning to the RRC_CONNECTED state.
	 */
	CONNECTING,
	/**
	 * The UE is in the RRC_CONNECTED state (not transmitting nor receiving data).
	 */
	CONNECTED,
	/**
	 * The UE is in the RRC_CONNECTED state (transmitting data).
	 */
	CONNECTED_TX,
	/**
	 * The UE is in the RRC_CONNECTED state (receiving data).
	 */
	CONNECTED_RX,
	/**
	 * The UE is in the RRC_CONNECTED state (transmitting and receiving data).
	 */
	CONNECTED_TX_RX,
	/**
	 * The UE is in the RRC_CONNECTED state (reception disabled).
	 */
	CONNECTED_DRX;    
}
