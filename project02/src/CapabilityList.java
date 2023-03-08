import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class DomainCl extends Thread{
    String status;
    int threadID, domID;
    CL cList;

    public DomainCl(String ID, CL cList){
        super(ID);
        threadID = Integer.parseInt(ID);
        domID = threadID + 1;
        // ID that is passed is 'i' from int i = 0 for loop, so the current Domain will be i + 1
        // ** for print statements ** when calling the actual domain, it will be domID - 1
        this.cList = cList;
        status = "[Thread: " + threadID + "(D" + domID + ")] ";
    }
    public void run(){
        int runs = Use.randNum(5, 10);   // # of times to attempt operation
        while (runs > 0){
            int target = pickTarget();              // 0 through cList.objects + cList.domains - 1

            switch (operation(target)){
                case "R":
                    Read(target);
                    break;
                case "W":
                    Write(target);
                    break;
                case "S":
                    Switch(target);
                    break;
                default:
                    System.out.println("Error");
                    break;
            }
            runs--;
        }
    }
    private int pickTarget(){
        int targ = Use.randNum(0, cList.objects + cList.domains - 1);
        if(targ != domID)
        {
            return targ;
        }
        return pickTarget();
    }
    private String operation(int targ){         // determines what operation will be executed
        if (targ < cList.objects){
            switch (Use.randNum(0,1)){
                case 0: return "R";
                case 1: return "W";
            }
        }
        return "S";
    }
    private void Read(int target){
        System.out.println(status + "Attempting to read resource: F"
                + (target + 1));
        if (cList.checkPermit("R", domID - 1, target)){
            if (cList.readCount[target].getAndIncrement() == 1){
                cList.getSem(target);
            }
            this.cList.lockFile[target].lock();
            Object message;
            if (cList.files[target].size() > 0){
                message = cList.files[target].remove(0);
                System.out.println(status + "F" + (target + 1) + " contains '" +
                        message + "'");
            }
            else {
                message = "nothing";
                System.out.println(status + "F" + (target + 1) + " contains " +
                        message);
            }
            this.cList.lockFile[target].unlock();
            if (cList.readCount[target].getAndDecrement() == 0){
                cList.objWriteSem[target].release();
            }
            System.out.print(status + "Operation complete\n");
        }
        else {
            System.out.print(status + "Operation failed: Permission denied\n");
        }
    }
    private void Write(int target){
        System.out.println(status + "Attempting to write resource: F"
                + (target + 1));
        if (cList.checkPermit("W", domID - 1, target)){
            cList.getSem(target);
            this.cList.lockFile[target].lock();
            String message = randMsg();
            System.out.print(status + "Writing '" + message + "' to resource F" +
                    target + "\n");
            this.cList.files[target].add(message);
            this.cList.lockFile[target].unlock();
            cList.objWriteSem[target].release();
            System.out.print(status + "Operation complete\n");
        }
        else {
            System.out.print(status + "Operation failed: Permission denied\n");
        }
    }
    private void Switch(int target){
        int domSwitch = target - cList.objects + 1;
        System.out.print(status + "Attempting to switch from D" +
                domID + " to D" + domSwitch + "\n");
        if (cList.checkPermit("S", domID - 1, target)){
            this.domID = domSwitch;
            this.status = "[Thread: " + this.threadID + "(D" + this.domID + ")] ";
            System.out.print(status + "Switched to D" + domID + "\n");
            System.out.print(status + "Operation complete\n");
        }
        else {
            System.out.print(status + "Operation failed: Permission denied\n");
        }
    }
    public String randMsg(){
        String[] msgs = {
                "Red", "Orange", "Yellow", "Green", "Blue",
                "Indigo", "Violet"
        };
        return msgs[Use.randNum(0, (msgs.length - 1))];
    }
}

class CL{
    int domains, objects;
    String[][] CL;
    ArrayList<String>[] files;
    Lock[] lockFile;
    Semaphore[] objWriteSem;
    AtomicInteger[] readCount;

    public CL(int domains, int objects){
        this.domains = domains;
        this.objects = objects;
        this.CL = new String[domains][domains+objects];
        fill();

        files = new ArrayList[objects];
        lockFile = new Lock[objects];
        objWriteSem = new Semaphore[objects];
        readCount = new AtomicInteger[objects];
        for (int i = 0; i < objects; i++){
            files[i] = new ArrayList<>();
            lockFile[i] = new ReentrantLock();
            objWriteSem[i] = new Semaphore(1);
            readCount[i] = new AtomicInteger(0);
        }
    }

    private void fill(){
        int DandF = domains + objects;
        for (int i = 0; i < domains; i++){
            for (int j = 0; j < DandF; j++){
                if (j < this.objects){
                    CL[i][j] = givePermit();
                }
                /*
                if the matrix has finished filling out the permissions for objects,
                then it will start giving permissions for the Domain switch permissions
                "allow" or ""
                 */
                else if (Use.coinFlip()){ // flips to see if it will give this Domain (j) permission to switch
                    CL[i][j] = "allow";
                }
                else {
                    CL[i][j] = null;
                }
            }
            System.out.print("\nD" + (i+1) + " --> ");
            int countForPrint = 0;
            for (int j = 0; j < DandF; j++){
                if (CL[i][j] != null){
                    if (countForPrint != 0){System.out.print(", ");}
                    if (j < this.objects){
                        System.out.print("F" + (j + 1) + ":" + CL[i][j]);
                    }
                    else{
                        System.out.print("D" + (j - objects + 1) + ":" + CL[i][j]);
                    }
                    countForPrint++;
                }
            }
        }
        System.out.println("\n");
    }

    private String givePermit(){
        if (Use.coinFlip()){ // flip for Read
            if (Use.coinFlip()) { // flip for Read and Write
                return "RW";
            }
            return "R";
        } else if (Use.coinFlip()) { // flips for Write ; will not have Read
            return "W";
        }
        return null;
    }
    public boolean checkPermit(String op, int dom, int obj){
        if (this.CL[dom][obj] == null){return false;}
        switch(op){
            case "R": if(this.CL[dom][obj].contains("R")){return true;}
            case "W": if(this.CL[dom][obj].contains("W")){return true;}
            case "S": if(this.CL[dom][obj].equals("allow")){return true;}
            default: return false;
        }
    }

    public void getSem(int i){
        try {
            objWriteSem[i].acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}