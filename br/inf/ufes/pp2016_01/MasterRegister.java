package br.inf.ufes.pp2016_01;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.TimerTask;

//Class que realiza o registro do mestre a cada 30s
public class MasterRegister extends TimerTask {

    private final Slave stub;
    private final SlaveImpl escravo;
    private final Master master;
    private final Registry registry;
    private final String name;

    public MasterRegister(Slave s, Master m, Registry registry, SlaveImpl escravo, String name) {
        this.stub = s;
        this.master = m;
        this.registry = registry;
        this.escravo = escravo;
        this.name = name;
    }

    //Procura pelo mestre
    private Master searchMaster() {
        Master mestre = new MasterImpl();

        try {
            mestre = (Master) registry.lookup("mestre");
        } catch (RemoteException | NotBoundException ex) {
            System.out.println("Escravo: " + this.escravo.getName() + " não consegui encontrar um mestre.");
        }
        return mestre;
    }

    @Override
    public void run() {
        try {
            master.addSlave(stub, name);
        } catch (RemoteException ex) {
            try {
                Master m = searchMaster();
                m.addSlave(stub, name);
            } catch (RemoteException ex1) {
                System.out.println("Escravo " + this.escravo.getName() + ": não consigo achar um mestre no host especificado ;(");
            }
        }
    }
}
