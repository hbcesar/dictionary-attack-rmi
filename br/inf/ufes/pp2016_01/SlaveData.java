package br.inf.ufes.pp2016_01;

public class SlaveData {
    private double time;
    private long id;
    private long beginIndex;
    private long endIndex;
    private long lastCheckedIndex;
    private String name;
    private Slave slaveReference;
    private boolean working;
    
    public SlaveData(Slave slaveReference, String name, long id){
        this.slaveReference = slaveReference;
        this.name = name;
        this.id = id;
    }
    
    public SlaveData(){
        
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time / 1000;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBeginIndex() {
        return beginIndex;
    }

    public void setBeginIndex(long beginIndex) {
        this.beginIndex = beginIndex;
    }

    public long getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(long endIndex) {
        this.endIndex = endIndex;
    }

    public long getLastCheckedIndex() {
        return lastCheckedIndex;
    }

    public void setLastCheckedIndex(long lastCheckedIndex) {
        this.lastCheckedIndex = lastCheckedIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Slave getSlaveReference() {
        return slaveReference;
    }

    public void setSlaveReference(Slave slaveReference) {
        this.slaveReference = slaveReference;
    }
    
    public boolean isWorking(){
        return this.working;
    }
}
