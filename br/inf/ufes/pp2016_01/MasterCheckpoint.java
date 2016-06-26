package br.inf.ufes.pp2016_01;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MasterCheckpoint extends Thread {

    private MasterImpl master;
    private Map<Integer, SlaveData> slaves;

    public MasterCheckpoint(MasterImpl master, Map<Integer, SlaveData> slaves) {
        this.master = master;
        this.slaves = slaves;
    }

    @Override
    public void run() {
        while (!master.isDone()) {
            try {
                Thread.sleep(1000);
                Map<Integer, SlaveData> slavex = new HashMap<>(slaves);
                for (Map.Entry<Integer, SlaveData> e : slavex.entrySet()) {
                    SlaveData s = e.getValue();
                    long currentTime = System.nanoTime()/1000000000;
                    long lastCheckedTime = (long) s.getTime();
                    long TimeBetweenCheckpoints = currentTime - lastCheckedTime;
                    boolean slaveWorking = !e.getValue().hasFinished();
                    if (TimeBetweenCheckpoints > 20.0 && slaveWorking) {
                        System.out.println("Escravo vai ser removido por atraso em checkpoint: " + s.getName());
                        master.removeSlave((int) s.getId());
                    }
                }
            } catch (RemoteException e) {
                System.out.println("Erro ao criar thread verificadora de checkpoints no mestre.");
                //Logger.getLogger(MasterCheckpoint.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex){
                
            }
        }
    }
}
