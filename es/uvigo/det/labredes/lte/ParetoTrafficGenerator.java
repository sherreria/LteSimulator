package es.uvigo.det.labredes.lte;

/**
 * This class extends TrafficGenerator class to simulate Pareto traffic.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class ParetoTrafficGenerator extends TrafficGenerator {
    private double alpha;

    /**
     * Creates a new Pareto traffic generator.
     *
     * @param prate packet rate (in packets per psf)
     * @param a shape parameter (alpha)
     */
    public ParetoTrafficGenerator (double prate, double a) {
	super(prate);
	alpha = a;
    }

    /**
     * Sets the shape parameter (alpha) for the Pareto traffic generator.
     *
     * @param a shape parameter (alpha)
     */
    public void setAlpha (double a) {
	alpha = a;
    }

    /**
     * Returns the instant at which the next packet arrives.
     *
     * @return instant at which the next packet arrives (in seconds)
     */
    public double getNextArrival () {
	double xm = (alpha - 1) / alpha / packet_rate;
	double rand = rng.nextDouble();
	arrival_time += xm / Math.pow(rand, 1 / alpha);	
	return arrival_time;
    }    

}