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

public class MasterImpl implements Master {

    //lista com threads que estao handling trabalhos de escravos
    private Map<Integer, ThreadMestreEscravo> threads = new HashMap<>();

    //Referencia a classe que realiza verificacao de checkpoints
    //private MasterCheckpoint masterCheckpoint;

    //Palavras do dicionario
    private static List<String> dictionary = new ArrayList<>();

    //Lista com escravos ativos
    private Map<Integer, SlaveData> slaves = new HashMap<>();
    //mapeia nomes de escravos aos seus IDs na lista de escravos
    private Map<String, Integer> slaveID = new HashMap<>();

    //Lista de trabalhos de escravos que falharam
    private Map<Integer, SlaveData> failed = new HashMap<>();
    //Mapeia nomes de escravos falhos a suas IDs na lista de falhos
    private Map<String, Integer> failedNames = new HashMap<>();

    //Lista de guesses
    private List<Guess> guessList = new ArrayList<>();

    //Usada para retornar IDs aos escravos
    private int nSlaves = 0;
    //Usada para retornar IDs de escravos falhos
    private int nFailed = 0;

    //Indica se mestre esta trabalhando ou nao
    private boolean done = true;

    public MasterImpl() {
    }

    public boolean isDone() {
        return this.done;
    }

    //metodo de Attacker
    @Override
    public Guess[] attack(byte[] ciphertext, byte[] knowntext) {
        done = false;
        this.readDictionary();  //le dicionario

        //divide o trabalho igualmente entre escravos
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

            //Obtem escravo para realizar trabalho
            SlaveData slaveData = e.getValue();
            Slave slave = slaveData.getSlaveReference();

            //Seta indices com o qual escravo vai trabalhar
            slaveData.setBeginIndex(inicio);
            slaveData.setEndIndex(fim);
            slaveData.setLastCheckedIndex(0);

            //Cria uma thread para executar trabalho do escravo e manda executar
            ThreadMestreEscravo thread = new ThreadMestreEscravo(slave, ciphertext, knowntext, inicio, fim, this);
            threads.put((int) slaveData.getId(), thread);
            slaveData.setTime(System.nanoTime());
            thread.start();

            // System.out.println("Passou aqui...");

            inicio = fim + 1;
            fim += tamVetorEscravos;
        }

        //divididos os trabalhos, executa thread para verificar checkpoints
        MasterCheckpoint masterCheckpoint = new MasterCheckpoint(this, slaves);
        masterCheckpoint.start();

        Map<Integer, ThreadMestreEscravo> threads_cpy = new HashMap<>(threads);
        for (Map.Entry<Integer, ThreadMestreEscravo> e : threads_cpy.entrySet()) {
            try {
                e.getValue().join();
            } catch (InterruptedException err) {
                System.out.println("Erro ao fazer join de threads no mestre.");
            }
        }

        //com trabalho finalizado, limpa lista de threads (lista sera reusada para redividir trabalhos)
        threads.clear();

        //se chegou até aqui, significa que os escravos terminaram de alguma forma (compeltaram ou falharam)
        //entao, verifica se existe trabalho a ser redistribuido
        if (!failed.isEmpty()) {
            rearrangeAttack(ciphertext, knowntext);
        } else {
            System.out.println("Lista de falhas vazia!");
        }

        done = true;
        //interrompe thread checadora de checkpoints (pois trabalho terminou)
        masterCheckpoint.interrupt();

        //Copia lista de guesses em vetor e retorna
        Guess[] guesses = new Guess[guessList.size()];
        guesses = guessList.toArray(guesses);

        return guesses;
    }

    private void rearrangeAttack(byte[] ciphertext, byte[] knowntext) {
        System.out.println("Houve falha em algum escravo, realizando redistribuição de trabalho...");

        //Iterador sobre escravos disponiveis para retrabalho
        Iterator it = slaves.entrySet().iterator();

        //Iterador sobre trabalhos a serem refeitos
        for (Map.Entry<Integer, SlaveData> e : failed.entrySet()) {
            if (it.hasNext()) {
                SlaveData slaveData = e.getValue();

                //cria nova classe para enviar trabalho
                SlaveData worker;
                Map.Entry pair = (Map.Entry) it.next();
                worker = (SlaveData) pair.getValue();
                it.remove();

                //Cria thread para novo trabalho
                ThreadMestreEscravo thread;
                thread = new ThreadMestreEscravo(worker.getSlaveReference(), ciphertext, knowntext, slaveData.getBeginIndex(), slaveData.getEndIndex(), this);
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
                }
            }
        }

        for (Map.Entry<Integer, ThreadMestreEscravo> e : threads.entrySet()) {
            try {
                e.getValue().join();
            } catch (InterruptedException err) {
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
                br = new BufferedReader(new FileReader("/tmp/dictionary.txt"));

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
        //verifica se escravo ja nao foi adicionado e esta apenas se re-registrando
        if (slaveID.containsKey(slavename)) {
            System.out.println("Registro do escravo " + slavename + " atualizado.");

            return slaveID.get(slavename);
        } else {
            //adiciona escravo
            SlaveData slaveData = new SlaveData(s, slavename, nSlaves);
            slaves.put(nSlaves, slaveData);
            slaveID.put(slavename, nSlaves);

            //verifica se esravo ja foi removido anteriormente e retorna-o para lista de escravos ativos
            if (failedNames.containsKey(slavename)) {
                int failedId = failedNames.get(slavename);
                failedNames.remove(slavename);
                failed.remove(failedId);
            }

            System.out.println("Escravo " + slavename + " adicionado (ID: " + slaveData.getId() + ")");

            return nSlaves++;
        }
    }

    @Override
    public synchronized void removeSlave(int slaveKey) throws RemoteException {
        //confere se escravo deve mesmo ser removido
        if (slaves.containsKey(slaveKey)) {
            SlaveData s = slaves.get(slaveKey);

            //se escravo ainda nao terminou, deve ser registrado trabalho restante
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

            //se foi criado thread para trabalho, interrompa-a
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

        //Procura em qual escravo esse checkpoint corresponde e registra
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

            master.attachShutDownHook();
        } catch (Exception e) {
            System.err.println("Mestre gerou exceção.");
        }
    }
}
