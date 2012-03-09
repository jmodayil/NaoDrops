package AudioPrediction;

import java.io.IOException;

import rltoys.algorithms.representations.actions.Action;
import rltoys.algorithms.representations.actions.TabularAction;
import rltoys.algorithms.representations.tilescoding.TileCoders;
import rltoys.environments.envio.actions.ActionArray;
import rltoys.math.vector.RealVector;
import rltoys.math.vector.implementations.PVector;

public class PrintActionValueTable {

	
	public static void printTable(SarsaInitialized learning, TabularAction toStateAction, TileCoders tileCoder, Action[] actions, RealVector currentObs) {
		PVector obs = new PVector(17);
		boolean sameObs = false;
//		System.out.printf("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		System.out.println("PASTSTATE: \t\t\t LEFT \t\t\t CENTER \t\t\t RIGHT");
		System.out.println("CURRENTSTATE: \t\t L \t C \t R \t\t L \t C \t R \t\t L \t C \t R \t");
		for (int reward = 0; reward <2; reward++) {
			for (int motion = 0; motion < 2; motion++) {
				System.out.print("Reward / Motion: " + reward + " " + motion + " | ");
				for (int pastAction = 0; pastAction < 3; pastAction++) {
					for (int action = 0; action < 3; action++) {
						obs.setEntry(0, motion);
						obs.setEntry(1, pastAction);
						obs.setEntry(2, reward);
						
						RealVector x_t = tileCoder.project(obs.accessData());
						x_t = (RealVector) x_t.ebeMultiply(AudioPredictionZephyr_RealEnv.getSuppressAudioVector());
						sameObs = true;
						for (int runVar = 0; runVar < currentObs.getDimension(); runVar++) {
							if (x_t.getEntry(runVar) != currentObs.getEntry(runVar)) {
								sameObs = false;
							}
						}
						if (sameObs) {
							System.out.print("#");
						}
						else {
							System.out.print(" ");
						}
						System.out.printf("%.2f\t", learning.predict(toStateAction.stateAction(x_t, actions[action])));
					}
					System.out.print("|\t");
				}
				System.out.println();
			}
		}
		System.out.print("\n\n");
	}

	
	public static void printTableForAction(SarsaInitialized learning, TabularAction toStateAction, ActionArray action) {
		
	}
}
