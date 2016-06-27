package br.inf.ufes.pp2016_01;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

//Classe auxiliar que realiza verificação de checkpoints enviados pelos escravos
public class MasterCheckpoint extends Thread {

    private MasterImpl master;
    private Map<Integer, SlaveData> slaves;

    public MasterCheckpoint(MasterImpl master, Map<Integer, SlaveData> slaves) {
        this.master = master;
        this.slaves = slaves;
    }

    @Override
    public void run() {
        //laço verifica se mestre está realizando trabalho para o cliente
        while (!master.isDone()) {
            try {
                //Intervalo de 10s entre verificação e outra
                Thread.sleep(1000);
                
                //Itera sobre escravos verificando intervalos de checkpoint
                Map<Integer, SlaveData> slavex = new HashMap<>(slaves);
                
                for (Map.Entry<Integer, SlaveData> e : slavex.entrySet()) {
                    SlaveData s = e.getValue();
                    
                    long lastCheckedTime = (long) s.getTime(); //pega tempo do ultimo checkin
                    long currentTime = System.nanoTime()/1000000000; //pega tempo atual
                    long TimeBetweenCheckpoints = currentTime - lastCheckedTime;
                    
                    //verifica se slave nao terminou trabalho
                    boolean slaveWorking = !e.getValue().hasFinished();
                    
                    if (TimeBetweenCheckpoints > 20.0 && slaveWorking) {
                        System.out.println("Escravo vai ser removido por atraso em checkpoint: " + s.getName());
                        master.removeSlave((int) s.getId());
                    }
                }
            } catch (RemoteException e) {
                System.out.println("Erro ao criar thread verificadora de checkpoints no mestre.");
            } catch (InterruptedException ex){
                
            }
        }
    }
}
