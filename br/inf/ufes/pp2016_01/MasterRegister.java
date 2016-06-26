package br.inf.ufes.pp2016_01;

//Inner class que realiza o registro do mestre a cada 30s
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.TimerTask;

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
            // Registry registry = LocateRegistry.getRegistry(masterName);
            mestre = (Master) registry.lookup("mestre");
        } catch (RemoteException | NotBoundException ex) {
            System.out.println("Escravo: " + this.escravo.getName() + " não consegui encontrar um mestre.");
//                Logger.getLogger(SlaveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mestre;
    }

    @Override
    public void run() {
        try {
            // final Slave stub = (Slave) UnicastRemoteObject.exportObject(this.s, 0);
            master.addSlave(stub, name);
        } catch (RemoteException ex) {
            try {
                Master m = searchMaster();
                // final Slave stub = (Slave) UnicastRemoteObject.exportObject(this.s, 0);
                m.addSlave(stub, name);
            } catch (RemoteException ex1) {
                System.out.println("Escravo " + this.escravo.getName() + ": não consigo achar um mestre no host especificado ;(");
//                    Logger.getLogger(SlaveImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
}
