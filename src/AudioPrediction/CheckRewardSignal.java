package AudioPrediction;

import java.io.IOException;
import java.util.Random;

import nao.NaoAction;
import nao.NaoRobot;
import nao.RewardFromCardGenerator;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class CheckRewardSignal {

  /**
   * @param args
   * @throws IOException
   * @throws IllegalArgumentException
 * @throws InterruptedException 
   */
  public static void main(String[] args) throws IllegalArgumentException, IOException, InterruptedException {
    // TODO Auto-generated method stub
    // seconds, filename as parameters
    NaoRobot robot = new NaoRobot();
    double[] obsArray;
    double reward = 0;
    double[] possibleActions = {.3, .5, .7};
    double[] truePositions = {-0.8284019827842712,0.004560038447380066,0.8298520445823669};
    BufferVector pastActions = new BufferVector(3,1);
    double formerJoints = .5;
    int[] fS = new int[4]; //Motion 01 / action1ago / action2ago / reward +-
    double[][][][] V = new double[3][3][3][2];
    
    RewardFromCardGenerator rewardGen = new RewardFromCardGenerator();
    double[] stiffness = new double[14];
    stiffness[0] = 0.8;
    double[] joints = new double[14];
    
    NaoAction action = new NaoAction();
    double[] leds = null;

    Random generator = new Random();
    int nextNum = 0;
    // Wait for the desired number of Steps and record the sound Data:
    while (true) {
      //Save OLD state for updates:
      fS[0] = pastActions.get(0);
      fS[1] = pastActions.get(1);
      fS[2] = pastActions.get(2);
      fS[3] = (int) (reward < .5 ? 0 : 1);
      
      // Wait for new sound Data:	
      obsArray = robot.waitNewObs();
      
      //Get Reward
      reward = rewardGen.getReward(robot.getImage());
      reward = reward > 0.99 ? 1 : -1;
      
      //Calculate Motion:
      if ((Math.abs(obsArray[12]-truePositions[pastActions.get(0)]) < 0.03 && formerJoints == joints[0])) {
    	  if (reward == 1) {
        	  pastActions.push(pastActions.get(0));
          }
          else {
        	  if (pastActions.get(0) == pastActions.get(1)) {
        		  System.out.println("pastActions(0) == pastActions(1)!");
        		  nextNum=generator.nextInt(2) + 1;
        		  pastActions.push((pastActions.get(0)+nextNum) % 3);
        	  }
        	  else {
        		  System.out.println("pastActions != pastAction(t-1)!");
        		  pastActions.push((-(pastActions.get(0) + pastActions.get(1))+3) % 3);
        	  }
          }
      }
      
      //Adapt the Real Reward according to whether there was motion or not:
      
      // POLICY:

      
      // POLICY END
      
      //Value Function update:
      // V(s_t) = V(s_t) + alpha*(r_tp1 + gamma*V(s_tp1) - V(s_t))
      V[fS[0]][fS[1]][fS[2]][fS[3]] = V[fS[0]][fS[1]][fS[2]][fS[3]] + .1*(reward + .9*V[pastActions.get(0)][pastActions.get(1)][pastActions.get(2)][(int) reward == 1 ? 1 : 0] -V[fS[0]][fS[1]][fS[2]][fS[3]]);
      formerJoints = joints[0];
      joints[0] = possibleActions[pastActions.get(0)];
      action.set(joints, 0.6, stiffness, leds, null);
      robot.sendAction(action);
      
      for(int a = 0; a < 3; a++) {
    	  for (int b = 0; b < 3; b++) {
    		  for (int c = 0; c < 3; c++) {
    			  for (int d = 0; d < 2; d++) {
    				  System.out.print(a + " " + " " + b + " " + c + " " + d + "        " + V[a][b][c][d] + "\n");
    			  }
    				  
    		  }
    	  }
      }
      
    	  
    }

  }
}