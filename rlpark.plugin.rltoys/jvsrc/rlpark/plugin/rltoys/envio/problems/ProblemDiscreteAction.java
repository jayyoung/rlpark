package rlpark.plugin.rltoys.envio.problems;

import rlpark.plugin.rltoys.envio.actions.Action;

public interface ProblemDiscreteAction extends RLProblem {
  Action[] actions();
}
