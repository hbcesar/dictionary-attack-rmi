//Codigo baseado em: http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ScheduledExecutorService.html
class Checkpointer extends Thread {
  private SlaveManager slaveManager;
  private AttackThread attack;
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final ScheduledFuture<?> checkpointerHandler;

  public SlaveCheckpoint(SlaveManager slaveManager, AttackThread attack){
    this.attack = attack;
    this.slaveManager = slaveManager;
  }

  public void startCheckPointer(){
    final Runnable checkpointer = new Runnable() {
      public void run() {
        slaveManager.checkpoint(attack.getCurrentIndex());
      }
    };

    checkpointerHandler = scheduler.scheduleAtFixedRate(checkpointer, 20, 20, SECONDS);

  }

  public void endCheckPointer(){
    if(checkpointHandler != null){
      checkpointHandler.cancel(true);
    }
  }

  @Override
    public void run() {

        try {
            startCheckPointer();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
