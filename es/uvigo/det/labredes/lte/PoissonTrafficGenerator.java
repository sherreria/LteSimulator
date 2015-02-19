package es.uvigo.det.labredes.lte;

/**
 * This class extends TrafficGenerator class to simulate Poisson traffic.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class PoissonTrafficGenerator extends TrafficGenerator {
    /**
     * Creates a new Poisson traffic generator.
     *
     * @param prate packet rate (in packets per psf)
     */
    public PoissonTrafficGenerator (double prate) {
	super(prate);
    }

    /**
     * Returns the instant at which the next packet arrives.
     *
     * @return instant at which the next packet arrives (in seconds)
     */
    public double getNextArrival () {
	double rand = rng.nextDouble();
	arrival_time += -1.0 * Math.log(rand) / packet_rate;
	return arrival_time;
    }    

}