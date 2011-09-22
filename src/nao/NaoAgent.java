package nao;

import rltoys.environments.envio.Agent;

public interface NaoAgent extends Agent {

  @Override
  NaoAction getAtp1(double[] obs);

}
