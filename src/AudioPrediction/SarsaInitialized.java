package AudioPrediction;

import rltoys.algorithms.learning.control.sarsa.Sarsa;
import rltoys.math.vector.RealVector;
import rltoys.math.vector.implementations.PVector;

public class SarsaInitialized extends Sarsa {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6904023260764878838L;

	public SarsaInitialized(double alpha, double gamma, double lambda,
			int nbFeatures) {
		super(alpha, gamma, lambda, nbFeatures);
		// TODO Auto-generated constructor stub
	}
	
	public SarsaInitialized(double alpha, double gamma, double lambda,
			int nbFeatures, PVector theta) {
		super(alpha, gamma, lambda, nbFeatures);
		System.out.println("initialThetaSize: " + theta.size + " thetaOfSarsa: " + this.theta.size);
		if (this.theta.size < theta.size) {
			System.out.println("ERROR! Input THETA IS LARGER!");
			return;
		}

		for (int n = 0; n < theta.size; n++) {
			this.theta.setEntry(n, theta.getEntry(n));
		}
	}

	public void updateSingleState(RealVector x, double z){
		double y = 0;
		double delta = 0;
		y = theta.dotProduct(x);
		delta = z - y;
		theta.addToSelf(alpha*delta,x);
	}
}
