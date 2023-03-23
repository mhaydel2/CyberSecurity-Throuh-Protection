import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


//File written by Patrick Leleux c00449233

public class ACLProgram extends Thread{

    String status;
    int threadID, domID;
    ACL ObjList;

    public ACLProgram(String ID, ACL ObjList){
        super(ID);
        threadID = Integer.parseInt(ID);
        domID = threadID + 1;
        // ID that is passed is 'i' from int i = 0 for loop, so the current Object will be i + 1
        // ** for print statements ** when calling the actual object, it will be objID - 1
        this.ObjList = ObjList;
        status = "[Thread: " + threadID + "(D" + domID + ")] ";
    }
    public void run(){
        int runs=Use.randNum(5, 10);   // # of times to attempt operation
        while (runs>0){
            int target = pickTarget();//picks a random object or domain
            switch (operation(target)){
                case "R":
                    Read(target);
                    Use.cycle();
                    break;
                case "W":
                    Write(target);
                    Use.cycle();
                    break;
                case "S":
                    Switch(target);
                    Use.cycle();
                    break;
                default:
                    System.out.println("Error");
                    break;
            }
            runs--;
        }
    }

    private void Switch(int target) {
        int domSwitch = target - ObjList.objects+1;
        System.out.print(status + "Attempting to switch from D" + domID + " to D" + domSwitch + "\n");
        if (ObjList.checkPermit("S", target, domID-1)){
            this.domID = domSwitch;
            this.status = "[Thread: " + this.threadID + "(D" + this.domID + ")] ";
            System.out.print(status + "Switched to D" + domID + "\n");
            System.out.print(status + "Operation complete\n");
        }
        else {
            System.out.print(status + "Operation failed: Permission denied\n");
        }
    }

    private void Read(int target) {
        System.out.println(status + "Attempting to read resource: F" + (target + 1));
        if (ObjList.checkPermit("R", target, domID-1)){
            if (ObjList.readCount[target].incrementAndGet() == 1){
                ObjList.getSem(target);
            }
            this.ObjList.lockFile[target].lock();
            Object message;
            if (ObjList.files[target].size() > 0){
                message = ObjList.files[target].remove(0);
                System.out.println(status + "F" + (target + 1) + " contains '" +
                        message + "'");
            }
            else {
                message = "nothing";
                System.out.println(status + "F" + (target + 1) + " contains " +
                        message);
            }
            Use.cycle();
            this.ObjList.lockFile[target].unlock();
            if (ObjList.readCount[target].decrementAndGet() == 0){
                ObjList.objWriteSem[target].release();
            }
            System.out.print(status + "Operation complete\n");
        }
        else{
            System.out.print(status + "Operation failed: Permission denied\n");
        }
    }


    private void Write(int target) {
        System.out.println(status + "Attempting to write resource: F" + (target + 1));
        if (ObjList.checkPermit("W", target, domID-1)){
            ObjList.getSem(target);
            this.ObjList.lockFile[target].lock();
            String message = randMsg();
            System.out.print(status + "Writing '" + message + "' to resource F" +
                    target + "\n");
            this.ObjList.files[target].add(message);
            this.ObjList.lockFile[target].unlock();
            ObjList.objWriteSem[target].release();
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

    private int pickTarget(){
        int targ = Use.randNum(0,ObjList.domains+ObjList.objects-1); //picks a random object from the ACL
        if((domID-1) != (targ- ObjList.objects)){
            return targ;
        }
        return pickTarget();
    }

    private String operation(int targ){         // determines what operation will be executed
        if (targ < ObjList.objects){
            switch (Use.randNum(0,1)){
                case 0: return "R";
                case 1: return "W";
            }
        }
        return "S";
    }

}

class ACL {
    int domains, objects;
    String[][] ObjectList;
    ArrayList<String>[] files;
    Lock[] lockFile;
    Semaphore[] objWriteSem;
    AtomicInteger[] readCount;
    //the constructor goes by Objects and makes it the length of objects+domains and fills it
    public ACL(int domains, int objects){
        this.domains = domains;
        this.objects = objects;
        this.ObjectList = new String[objects+domains][domains];
        fill2();

        //as far as i know this is what protects the objects from being accessed at the same time
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

    private void fill2(){
        int DandF = domains + objects;

        for(int i = 0; i < DandF; i++){
            //for everything in the list, where the objects have WR and the domains have allow or not
            for (int j = 0; j < domains; j++){
                if(i<this.objects){
                    ObjectList[i][j] = givePermit();
                }
                else if (Use.coinFlip()){ // flips to see if it will give this Domain (j) permission to switch
                    ObjectList[i][j] = "allow";
                }
                else {
                    ObjectList[i][j] = null;
                }
            }

            //this is what prints out the privileges
            if(i<this.objects){
                //if its an object and not a domain
                System.out.print("\nF" + (i+1) + " --> ");
            }
            else{
                System.out.print("\nD" + (i - objects + 1) + " --> ");
            }
            int countForPrint = 0;
            for (int j = 0; j < domains; j++){
                //for everything on the list
                //if (ObjectList[i][j] != null){
                //if its there
                if (countForPrint != 0){System.out.print(", ");} //write a comma if its the second
                System.out.print("D" + (j + 1) + ":" + ObjectList[i][j]);

                countForPrint++;


                // }
            }




        }
        System.out.println();
    }
    //this is fine to remain unchanged
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
    public boolean checkPermit(String op, int obj, int dom){
        if (this.ObjectList[obj][dom] == null){return false;}
        switch(op){
            case "R": if(this.ObjectList[obj][dom].contains("R")){return true;}
            case "W": if(this.ObjectList[obj][dom].contains("W")){return true;}
            case "S": if(this.ObjectList[obj][dom].equals("allow")){return true;}
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