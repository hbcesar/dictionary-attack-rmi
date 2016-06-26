package br.inf.ufes.pp2016_01;

import java.rmi.RemoteException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MasterImpl implements Master {

    private Map<Integer, ThreadMestreEscravo> threads = new HashMap<>();
    private MasterCheckpoint masterCheckpoint;

    private static List<String> dictionary = new ArrayList<>();
    private Map<Integer, SlaveData> slaves = new HashMap<>();
    private Map<Integer, SlaveData> failed = new HashMap<>();
    private Map<String, Integer> failedNames = new HashMap<>();
    private Map<String, Integer> slaveID = new HashMap<>();
    private List<Guess> guessList = new ArrayList<Guess>();

    private int nSlaves = 0;
    private int nFailed = 0;
    private int completed = 0;
    private boolean done = true;

    public MasterImpl() {
        masterCheckpoint = new MasterCheckpoint(this, slaves);
    }

    public boolean isDone() {
        return this.done;
    }

    //metodo de Attacker
    @Override
    public Guess[] attack(byte[] ciphertext, byte[] knowntext) {
        this.readDictionary();
        done = false;

        long tamVetor = dictionary.size();
        long tamVetorEscravos = tamVetor / slaves.size();
        long resto = tamVetor % slaves.size();
        long inicio = 0;
        long fim = tamVetorEscravos - 1;

        for (Map.Entry<Integer, SlaveData> e : slaves.entrySet()) {
            //se a divisao de vetores nao for exata, vai atribuindo +1 campo do vetor a cada escravo
            if (resto > 0) {
                fim++;
                resto--;
            }

            SlaveData slaveData = e.getValue();
            Slave slave = slaveData.getSlaveReference();

            slaveData.setBeginIndex(inicio);
            slaveData.setEndIndex(fim);
            slaveData.setLastCheckedIndex(0);

            ThreadMestreEscravo thread = new ThreadMestreEscravo(slave, ciphertext, knowntext, inicio, fim, this);
            threads.put((int) slaveData.getId(), thread);
            slaveData.setTime(System.nanoTime());
            thread.start();

            inicio = fim + 1;
            fim += tamVetorEscravos;
        }

        
//        masterCheckpoint.start();

//        List<Integer> keysList = new ArrayList<Integer>();
//        synchronized (this) {
        Map<Integer, ThreadMestreEscravo> threads_cpy = new HashMap<>(threads);
        for (Map.Entry<Integer, ThreadMestreEscravo> e : threads_cpy.entrySet()) {
            try {
                e.getValue().join();
//                threads.remove(e.getKey());
            } catch (InterruptedException err) {
//                err.printStackTrace();
                System.out.println("Erro ao fazer join de threads no mestre.");
            }
        }
//        }

//        for (int key : keysList) {
//            threads.remove(key);
//        }
        threads.clear();
//        masterCheckpoint.interrupt();

        //se chegou até aqui, significa que os escravos terminaram de alguma forma (compeltaram ou falharam)
        //entao, verifica se existe trabalho a ser redistribuido
        if (!failed.isEmpty()) {
            rearrangeAttack(ciphertext, knowntext);
        } else {
            System.out.println("Lista de falhas vazia!");
        }

        done = true;

        Guess[] guesses = new Guess[guessList.size()];
        guesses = guessList.toArray(guesses);

        return guesses;
    }

    private void rearrangeAttack(byte[] ciphertext, byte[] knowntext) {
        System.out.println("\n\nHouve falha em algum escravo, realizando redistribuição de trabalho... \n\n");
        Iterator it = slaves.entrySet().iterator();

        for (Map.Entry<Integer, SlaveData> e : failed.entrySet()) {
            if (it.hasNext()) {
                SlaveData slaveData = e.getValue();
                SlaveData worker;
                Map.Entry pair = (Map.Entry) it.next();
                worker = (SlaveData) pair.getValue();
                it.remove();
                ThreadMestreEscravo thread;
                thread = new ThreadMestreEscravo(worker.getSlaveReference(), ciphertext, knowntext, slaveData.getBeginIndex(), slaveData.getEndIndex(), this);
                System.out.println("Refazendo tarefa, indices: " + slaveData.getBeginIndex() + " e " + slaveData.getEndIndex());
                threads.put((int) slaveData.getId(), thread);
                slaveData.setTime(System.nanoTime());
                thread.start();
                failed.remove((int) slaveData.getId());
            } else {
                System.out.println("Nao há nenhum escravo disponível para realizar o serviço.");
                System.out.println("Tentando novamente em 30s...");
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException ex) {
//                    Logger.getLogger(MasterImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        for (Map.Entry<Integer, ThreadMestreEscravo> e : threads.entrySet()) {
            try {
                e.getValue().join();
//                threads.remove(e.getKey());
            } catch (InterruptedException err) {
//                err.printStackTrace();
                System.out.println("Erro aguardando threads apos redistribuição");
            }
        }

        threads.clear();

        if (failed.size() > 0) {
            rearrangeAttack(ciphertext, knowntext);
        }
    }

    public void readDictionary() {
        try {
            if (dictionary.isEmpty()) {
                BufferedReader br;
                br = new BufferedReader(new FileReader("dictionary.txt"));

                //palavra lida
                String word;

                while ((word = br.readLine()) != null) {
                    dictionary.add(word);
                }

                br.close();
            }
        } catch (IOException ex) {
            System.out.println("Mestre: arquivo de dicionário não encontrado.");
        }
    }

    //metodos de SlaveManager
    @Override
    public synchronized int addSlave(Slave s, String slavename) throws RemoteException {

        if (slaveID.containsKey(slavename)) {
            System.out.println("Registro do escravo " + slavename + " atualizado.");

            return slaveID.get(slavename);
        } else if (failedNames.containsKey(slavename)) {
            SlaveData slaveData = new SlaveData(s, slavename, nSlaves);
            slaves.put(nSlaves, slaveData);
            slaveID.put(slavename, nSlaves);
            
            int failedId = failedNames.get(slavename);
            failedNames.remove(slavename);
            failed.remove(failedId);

            System.out.println("Escravo " + slavename + " readicionado.");

            return nSlaves++;
        } else {
            SlaveData slaveData = new SlaveData(s, slavename, nSlaves);
            slaves.put(nSlaves, slaveData);
            slaveID.put(slavename, nSlaves);

            System.out.println("Escravo " + slavename + " adicionado.");

            return nSlaves++;
        }
    }

    //TODO melhorar aqui
    @Override
    public synchronized void removeSlave(int slaveKey) throws RemoteException {
        if (slaves.containsKey(slaveKey)) {
            SlaveData s = slaves.get(slaveKey);

            if (!s.hasFinished()) {
                SlaveData remaining = new SlaveData();
                if (s.getLastCheckedIndex() != 0) {
                    remaining.setBeginIndex(s.getLastCheckedIndex());
                } else {
                    remaining.setBeginIndex(s.getBeginIndex());
                }
                remaining.setEndIndex(s.getEndIndex());
                failed.put(nFailed, remaining);
                failedNames.put(s.getName(), slaveKey);
            }

            slaves.remove(slaveKey);
            slaveID.remove(s.getName());

            //falta interromper a thread dele, caso haja alguma executando
            if (threads.containsKey(slaveKey)) {
                threads.get((int) s.getId()).interrupt();
                threads.remove((int) s.getId());
            }

            System.out.println("Escravo " + s.getName() + " removido.");
        } else {
            System.out.println("Escravo " + slaveKey + " não encontrado!");
        }
    }

    @Override
    public void foundGuess(long currentindex, Guess currentguess) throws RemoteException {
        System.out.println("Nova possível chave encontrada: " + currentguess.getKey());
        guessList.add(currentguess);
    }

    @Override
    public void checkpoint(long currentindex) throws RemoteException {

        //Procura em qual escravo esse checkpoint corresponde
        for (Map.Entry<Integer, SlaveData> entry : slaves.entrySet()) {
            if (currentindex >= entry.getValue().getBeginIndex() && currentindex <= entry.getValue().getEndIndex()) {
                System.out.println("Checkpoint Escravo: " + entry.getValue().getName() + " currentIndex: " + currentindex);
                entry.getValue().setTime(System.nanoTime());
                entry.getValue().setLastCheckedIndex(currentindex);
            }
        }

    }

    // Captura o CTRL+C
    //http://stackoverflow.com/questions/1611931/catching-ctrlc-in-java
    public synchronized void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                // Remove todos escravos da lista caso o mestre caia
//                for (Iterator<Map.Entry<Integer, SlaveData>> it = slaves
//                        .entrySet().iterator(); it.hasNext();) {
//                    Map.Entry<Integer, SlaveData> slave = it.next();
//                    slaves.remove(slave.getKey());
//                }
                System.out.println(" Mestre Caiu! :(");
            }
        });
    }

    public static void main(String[] args) throws Exception {

        try {
            String masterName = "mestre";

            if (args.length > 0) {
                System.setProperty("java.rmi.server.hostname", args[0]);
            }

            MasterImpl master = new MasterImpl();

            Master stubRef = (Master) UnicastRemoteObject.exportObject(master, 2001);

            final Registry registry = LocateRegistry.getRegistry();
            registry.rebind(masterName, stubRef);

            System.out.println("Mestre está pronto...");

            //TODO
            master.attachShutDownHook();
        } catch (Exception e) {
            System.err.println("Mestre gerou exceção.");
        }
    }
}
