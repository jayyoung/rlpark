package rltoys.algorithms.learning.control;

import rltoys.algorithms.representations.acting.Policy;
import rltoys.algorithms.representations.actions.Action;
import rltoys.math.vector.RealVector;

public class ControlPolicyAdapter implements PolicyBasedControl {
  private static final long serialVersionUID = 7405967970830537947L;
  private final Policy policy;

  public ControlPolicyAdapter(Policy policy) {
    this.policy = policy;
  }

  @Override
  public Action step(RealVector x_t, Action a_t, RealVector x_tp1, double r_tp1) {
    return policy.decide(x_tp1);
  }

  @Override
  public Action proposeAction(RealVector x) {
    return policy.decide(x);
  }

  @Override
  public Policy policy() {
    return policy;
  }
}