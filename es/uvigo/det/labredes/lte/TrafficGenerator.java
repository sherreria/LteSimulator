package es.uvigo.det.labredes.lte;

import java.util.Random;

/**
 * This class simulates the arrival of a stream of packets.
 * It is assumed that each packet transmission exactly requires 1 PSF.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
abstract public class TrafficGenerator {
    /**
     * The packet rate (in packets per second).
     */
    public double packet_rate;
    /**
     * The instant at which the last packet arrived (in seconds).
     */
    public double arrival_time;
    /**
     * The random number generator.
     */
    public Random rng;

    /**
     * Creates a new random traffic generator.
     *
     * @param prate packet rate (in packets per psf)
     */
    public TrafficGenerator (double prate) {
	packet_rate = prate / LteSimulator.lte_psf;
	arrival_time = 0.0;
	rng = new Random();
    }

    /**
     * Sets the packet rate.
     *
     * @param prate packet rate (in packets per psf)
     */
    public void setPacketRate (double prate) {
	packet_rate = prate / LteSimulator.lte_psf;
    }

    /**
     * Sets the seed for the random number generator.
     *
     * @param seed initial seed
     */
    public void setSeed (long seed) {
	rng.setSeed(seed);
    }

    /**
     * Returns the instant at which the next packet arrives.
     *
     * @return instant at which the next packet arrives (in seconds)
     */
    abstract public double getNextArrival ();

}
