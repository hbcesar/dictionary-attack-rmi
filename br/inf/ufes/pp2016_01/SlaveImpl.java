package br.inf.ufes.pp2016_01;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SlaveImpl implements Slave {

    private static List<String> dictionary = new ArrayList<>();
    private String name;
    private int id;
    private Master master;

    private Thread thread;

    private Timer scheduler;
    private TimerTask masterRegister;

    public SlaveImpl(String name) {
        this.name = name;
    }

    /**
     * **** Getters and Setters *******
     *
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Inicia o sub-ataque
     */
    @Override
    public void startSubAttack(byte[] ciphertext, byte[] knowntext, long initialwordindex, long finalwordindex, SlaveManager callbackinterface) throws RemoteException {
        System.out.println("Iniciando trabalho..");
        // SlaveAttacker exec = new SlaveAttacker(ciphertext, knowntext, initialwordindex, finalwordindex, callbackinterface);
        // exec.setInitialwordindex(initialwordindex);
        // exec.setFinalwordindex(finalwordindex);
        // exec.startSubAttack();
        System.out.println("Terminado.");
    }

    //Registre o escravo no mestre a cada 30s
    private void registerSlave(Slave stub, Master master, Registry registry, SlaveImpl escravo, String name) {
        this.scheduler = new Timer();
        this.masterRegister = new MasterRegister(stub, master, registry, escravo, name);
        scheduler.scheduleAtFixedRate(masterRegister, 10000, 30000);
    }

    private void unregisterSlave() {
        this.masterRegister.cancel();
        this.scheduler.cancel();
    }

    public static void main(String[] args) {
        /*
         Cria instância da interface do mestre,
         essa operação é necessária para que o escravo consiga se
         registrar na fila gerenciada pelo mestre.
         */
        Master mestre;

        if (args.length < 2) {
            System.out.println("Parâmetros inválidos, por favor, forneça referencia ao mestre e nome do escravo (nessa ordem)");
        }

        //Para execução distribuida: java SlaveImpl IPESCRAVO IPMESTRE
        //Aqui o escravo recebe sua propria referencia
        String host = null;
        if (args.length > 0) {
            host = args[0];
        }

        try {
            //Procura Mestre no Registry
            System.out.println(host);
            Registry registry = LocateRegistry.getRegistry(host);
            mestre = (Master) registry.lookup("mestre");
            SlaveImpl escravo = new SlaveImpl(args[1]);

            //cria stub do escravo
            Slave stub = (Slave) UnicastRemoteObject.exportObject(escravo, 0);

            //De acordo com especificação, escravo deve se registrar no menino mestre
            int id = mestre.addSlave(stub, escravo.getName());
            escravo.setId(id);

            //"Attach" o metodo que executa operacoes necessarias caso o escravo finalize
            // escravo.attachShutDownHook(mestre);
            escravo.registerSlave(stub, mestre, registry, escravo, args[1]); //slave ira se registrar a cada 30s

            //Captura ctrl+c e desregistra do escravo antes de terminar
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        Master master = (Master) registry.lookup("mestre");
                        master.removeSlave(escravo.getId());
                        escravo.unregisterSlave();
                    } catch (RemoteException | NotBoundException ex) {
                        System.out.println("Escravo " + escravo.getName() + " nao consegui me desregistrar no mestre");
                    }
                }
            });

        } catch (RemoteException | NotBoundException e) {
            System.out.println("Escravo " + args[1] + ": nao consegui achar mestre no host especificado :(");
            Logger.getLogger(SlaveImpl.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
