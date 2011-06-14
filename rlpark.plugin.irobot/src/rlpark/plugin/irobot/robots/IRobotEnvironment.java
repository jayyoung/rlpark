package rlpark.plugin.irobot.robots;

import rlpark.plugin.irobot.data.CreateAction;
import rlpark.plugin.irobot.data.CreateLeds;
import rlpark.plugin.irobot.data.IRobotDrops;
import rlpark.plugin.irobot.data.IRobotObservationReceiver;
import rlpark.plugin.robot.RobotEnvironment;
import rlpark.plugin.robot.disco.datagroup.DropScalarGroup;
import rlpark.plugin.robot.disco.drops.Drop;
import rlpark.plugin.robot.sync.ObservationReceiver;
import rltoys.environments.envio.Agent;
import rltoys.environments.envio.observations.Legend;
import rltoys.environments.envio.observations.TStep;
import rltoys.math.ranges.Range;
import zephyr.plugin.core.api.monitoring.abstracts.DataMonitor;
import zephyr.plugin.core.api.monitoring.abstracts.MonitorContainer;
import zephyr.plugin.core.api.monitoring.abstracts.Monitored;
import zephyr.plugin.core.api.synchronization.Clock;

abstract public class IRobotEnvironment extends RobotEnvironment implements MonitorContainer, IRobotProblem {
  private final IRobotObservationReceiver connection;
  private CreateAction agentAction;
  private TStep currentStep;

  protected IRobotEnvironment(ObservationReceiver receiver, boolean persistent) {
    super(receiver, persistent);
    connection = (IRobotObservationReceiver) receiver();
  }

  @Override
  public Legend legend() {
    return connection.legend();
  }

  public Range[] ranges() {
    return connection.ranges();
  }

  public void sendMessage(byte[] bs) {
    connection.sendMessage(bs);
  }

  public void passiveMode() {
    sendMessage(new byte[] { (byte) 128 });
  }

  public void safeMode() {
    sendMessage(new byte[] { (byte) 131 });
  }

  public void fullMode() {
    sendMessage(new byte[] { (byte) 132 });
  }

  public void clean() {
    sendMessage(new byte[] { (byte) 135 });
  }

  public void dock() {
    sendMessage(new byte[] { (byte) 143 });
  }

  public void registerSong(int songNumber, int[] song) {
    byte songLength = (byte) Math.min(song.length / 2, 16);
    byte[] message = new byte[3 + songLength * 2];
    message[0] = (byte) 140;
    message[1] = (byte) songNumber;
    message[2] = songLength;
    for (int i = 0; i < songLength * 2; i++)
      message[3 + i] = (byte) song[i];
    sendMessage(message);
  }

  public void playSong(int songNumber) {
    sendMessage(new byte[] { (byte) 141, (byte) songNumber });
  }

  public void playSong(int[] song) {
    registerSong(0, song);
    playSong(0);
  }

  @Override
  public void addToMonitor(DataMonitor monitor) {
    addToMonitor(monitor, this);
  }

  static public void addToMonitor(DataMonitor monitor, final IRobotProblem problem) {
    for (String label : problem.legend().getLabels()) {
      final int obsIndex = problem.legend().indexOf(label);
      monitor.add(label, 0, new Monitored() {
        @Override
        public double monitoredValue() {
          double[] obs = problem.lastReceivedObs();
          if (obs == null)
            return -1;
          return obs[obsIndex];
        }
      });
    }
  }

  public void sendAction(CreateAction agentAction) {
    sendAction(agentAction.left(), agentAction.right());
  }

  abstract public void sendAction(double left, double right);

  protected short toActionValue(double maxAction, double value) {
    return (short) Math.min(maxAction, Math.max(-maxAction, value));
  }

  public void run(Agent agent) {
    String name = String.format("%s[%s]", getClass().getSimpleName(), agent.getClass().getSimpleName());
    run(new Clock(name), agent);
  }

  @Override
  public void run(Clock clock, Agent agent) {
    CreateAgent createAgent = (CreateAgent) agent;
    while (!isClosed() && clock.tick()) {
      double[] obsArray = waitNewObs();
      TStep lastStep = currentStep;
      currentStep = new TStep(lastStep, agentAction, obsArray);
      agentAction = createAgent.getAtp1(currentStep);
      if (agentAction != null && agentAction.actions != null)
        sendAction(agentAction);
    }
  }

  protected static Range[] getRanges(Drop drop) {
    DropScalarGroup sensors = new DropScalarGroup(drop);
    return IRobotDrops.rangeProvider(sensors).ranges(sensors.legend());
  }

  public void sendLeds(CreateLeds leds) {
    sendLeds((byte) leds.powerColor, (byte) leds.powerIntensity, leds.play, leds.advance);
  }

  abstract public void resetForCharging();

  abstract public void sendLeds(int powerColor, int powerIntensity, boolean play, boolean advance);
}
