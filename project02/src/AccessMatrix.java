import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Arbitration extends Thread{
    private int threadID;
    private String[][] Matrix;
    private int domainID;

    ReentrantLock semLock = new ReentrantLock();


    public Arbitration(int threadID, String[][] Matrix, int domainID){
        this.threadID = threadID;
        this.Matrix = Matrix;
        this.domainID = domainID;

    }

    public void arbFunction(int threadID, int randomColumnNum, String randomColumn, int domainID){
        String[] attemptArray = {"read", "write"};
        int[] yieldTimesArray = {3, 4, 5, 6, 7 };
        String[] characterBufferArray = new String[1];
        characterBufferArray[0] = " ";
        String[] randomWordArray = {"Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet"};




        //Checks to see if it's a domain or object by looking at the first character of the string
        if(randomColumn.charAt(0) == 'F'){
            //Randomly picks to read or write
            Random random = new Random();
            int randomAttemptNum = random.nextInt(2);
            String randomAttempt = attemptArray[randomAttemptNum];

            System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Attempting to " + randomAttempt + " resource: " + randomColumn);

            if(randomAttempt == "read"){
                int objectID = Integer.parseInt(String.valueOf(randomColumn.charAt(1)));
                if(Matrix[domainID][objectID] == "R" || Matrix[domainID][objectID] == "R/W"){
                    //READS
                    try{
                        semLock.lock();
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Resource " + randomColumn + " contains: " + Arrays.toString(characterBufferArray));
                        semLock.unlock();
                    }catch(Exception e){
                        System.out.println(e);
                    }
                    //Yields for a random number of times [3-7]
                    Random rand = new Random();
                    int randomYieldNum = rand.nextInt(4);
                    int randomYieldTime = yieldTimesArray[randomYieldNum];
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Yielding " + randomYieldTime + " times");
                    for(int i = 1; i<=randomYieldTime; i++){
                        Thread.yield();
                    }
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation Complete");
                }else{
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation failed, permission denied");
                }




            }else{
                int objectID = Integer.parseInt(String.valueOf(randomColumn.charAt(1)));
                //Checks to see if that domain has write permissions on that object
                if(Matrix[domainID][objectID] == "W" || Matrix[domainID][objectID] == "R/W"){
                    //WRITES
                    try{
                        semLock.lock();
                        //Pick a random word to write to the buffer array
                        Random randomWordWrite = new Random();
                        int randomWordNum = randomWordWrite.nextInt(10);
                        String randomWord = randomWordArray[randomWordNum];
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Writing '" + randomWord + "' to " + randomColumn);
                        characterBufferArray[0] = randomWord;
                        semLock.unlock();
                    }catch(Exception e){
                        System.out.println(e);
                    }
                    Random randW = new Random();
                    int randomYieldNum = randW.nextInt(4);
                    int randomYieldTime = yieldTimesArray[randomYieldNum];
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Yielding " + randomYieldTime + " times");
                    for(int i = 1; i<=randomYieldTime; i++){
                        Thread.yield();
                    }
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation Complete");
                }else{
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation failed, permission denied");
                }
            }




        }else{
            System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Attempting to switch from D" + domainID + " to " + randomColumn);
            if(Matrix[domainID][randomColumnNum] == "allow"){
                domainID = Integer.parseInt(String.valueOf(randomColumn.charAt(1)));
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Switched to " + randomColumn);
                Random random = new Random();
                int randomYieldNum = random.nextInt(4);
                int randomYieldTime = yieldTimesArray[randomYieldNum];
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Yielding " + randomYieldTime + " times");
                for(int i = 1; i<=randomYieldTime; i++){
                    Thread.yield();
                }
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation Complete");
            }else{
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation failed, permission denied");
            }
        }
    }







    public void run(){
        for(int i = 0; i < 5; i++) {
            Random rand = new Random();
            // Depending on what number you get will determine if it's an object or domain
            int randomColumnNum = rand.nextInt(Matrix[0].length - 1) + 1;
            String randomColumn = Matrix[0][randomColumnNum];
            arbFunction(threadID, randomColumnNum, randomColumn, domainID);
        }
    }
}

public class AccessMatrix extends Thread{
    private final int n; // number of domains
    private final int m; // number of file objects
    //private final int[][] matrix; // access matrix
    //private final String [][] matrix; // access matrix
    final String [][] matrix; // access matrix
    //private final Semaphore[] locks; // file object locks
    //ReentrantLock semLock = new ReentrantLock();

    public AccessMatrix(int domains, int objects) {
        // assign random values to n and m
        //Random rand = new Random();
        //n = rand.nextInt(5) + 3; // range [3,7]
        //m = rand.nextInt(5) + 3; // range [3,7]
        //n = Use.randNum(3, 7); // range [3,7]
        //m = Use.randNum(3, 7); // range [3,7]
        n = domains;
        m = objects;
        matrix = new String[n][m + n]; // create access matrix
         /*
        locks = new Semaphore[m]; // create file object locks
        for (int i = 0; i < m; i++) {
            locks[i] = new Semaphore(1); // initialize locks with permits
        }
          */

        // populate access matrix randomly
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m + n; j++) {
                if (j < m) {
                    // allow or prohibit read/write access randomly
                    //matrix[i][j] = rand.nextInt(3) - 1; // range [-1,1]
                    matrix[i][j] = givePermit();
                } else if (Use.coinFlip()){ // flips to see if it will give this Domain (j) permission to switch
                    matrix[i][j] = "allow";
                /*
                if the matrix has finished filling out the permissions for objects,
                then it will start giving permissions for the Domain switch permissions
                "allow" or ""
                 */

                } else {
                    matrix[i][j] = null;
                }
                    /*
                } else if (j != i + m) {

                    // allow or prohibit domain switch randomly
                    matrix[i][j] = rand.nextInt(2); // range [0,1]
                }
                */
            }
        }

        printAccessMatrix(); // print access matrix
    }




    private String givePermit(){
        if (Use.coinFlip()){ // flip for Read
            if (Use.coinFlip()) { // flip for Read and Write
                return "R/W";
            }
            return "R";
        } else if (Use.coinFlip()) { // flips for Write ; will not have Read
            return "W";
        }
        return null;
    }

    public boolean checkPermit(String op, int dom, int obj) {
        if (this.matrix[dom][obj] == null) {
            return false;
        }
        switch (op) {
            case "R":
                if (this.matrix[dom][obj].contains("R")) {
                    return true;
                }
            case "W":
                if (this.matrix[dom][obj].contains("W")) {
                    return true;
                }
            case "S":
                if (this.matrix[dom][obj].equals("allow")) {
                    return true;
                }
            default:
                return false;
        }
    }
/*
    private int pickTarget(){
        int targ = Use.randNum(0, m + n - 1);
        if(targ != domain)
        {
            return targ;
        }
        return pickTarget();
    }

    private String operation(int targ){         // determines what operation will be executed
        if (targ < m){
            switch (Use.randNum(0,1)){
                case 0: return "R";
                case 1: return "W";
            }
        }
        return "S";
    }
*/

/*
    public void arbFunction(int threadID, int randomColumnNum, String randomColumn, int domainID){
        String[] attemptArray = {"read", "write"};
        int[] yieldTimesArray = {3, 4, 5, 6, 7 };
        String[] characterBufferArray = new String[1];
        characterBufferArray[0] = " ";
        String[] randomWordArray = {"Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet"};




        //Checks to see if it's a domain or object by looking at the first character of the string
        if(randomColumn.charAt(0) == 'F'){
            //Randomly picks to read or write
            Random random = new Random();
            int randomAttemptNum = random.nextInt(2);
            String randomAttempt = attemptArray[randomAttemptNum];

            System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Attempting to " + randomAttempt + " resource: " + randomColumn);

            if(randomAttempt == "read"){
                int objectID = Integer.parseInt(String.valueOf(randomColumn.charAt(1)));
                if(accessMatrix[domainID][objectID] == "R" || accessMatrix[domainID][objectID] == "R/W"){
                    //READS
                    try{
                        semLock.lock();
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Resource " + randomColumn + " contains: " + Arrays.toString(characterBufferArray));
                        semLock.unlock();
                    }catch(Exception e){
                        System.out.println(e);
                    }
                    //Yields for a random number of times [3-7]
                    Random rand = new Random();
                    int randomYieldNum = rand.nextInt(4);
                    int randomYieldTime = yieldTimesArray[randomYieldNum];
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Yielding " + randomYieldTime + " times");
                    for(int i = 1; i<=randomYieldTime; i++){
                        Thread.yield();
                    }
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation Complete");
                }else{
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation failed, permission denied");
                }




            }else{
                int objectID = Integer.parseInt(String.valueOf(randomColumn.charAt(1)));
                //Checks to see if that domain has write permissions on that object
                if(accessMatrix[domainID][objectID] == "W" || accessMatrix[domainID][objectID] == "R/W"){
                    //WRITES
                    try{
                        semLock.lock();
                        //Pick a random word to write to the buffer array
                        Random randomWordWrite = new Random();
                        int randomWordNum = randomWordWrite.nextInt(10);
                        String randomWord = randomWordArray[randomWordNum];
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Writing '" + randomWord + "' to " + randomColumn);
                        characterBufferArray[0] = randomWord;
                        semLock.unlock();
                    }catch(Exception e){
                        System.out.println(e);
                    }
                    Random randW = new Random();
                    int randomYieldNum = randW.nextInt(4);
                    int randomYieldTime = yieldTimesArray[randomYieldNum];
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Yielding " + randomYieldTime + " times");
                    for(int i = 1; i<=randomYieldTime; i++){
                        Thread.yield();
                    }
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation Complete");
                }else{
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation failed, permission denied");
                }
            }




        }else{
            System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Attempting to switch from D" + domainID + " to " + randomColumn);
            if(accessMatrix[domainID][randomColumnNum] == "allow"){
                domainID = Integer.parseInt(String.valueOf(randomColumn.charAt(1)));
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Switched to " + randomColumn);
                Random random = new Random();
                int randomYieldNum = random.nextInt(4);
                int randomYieldTime = yieldTimesArray[randomYieldNum];
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Yielding " + randomYieldTime + " times");
                for(int i = 1; i<=randomYieldTime; i++){
                    Thread.yield();
                }
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation Complete");
            }else{
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation failed, permission denied");
            }
        }
    }
*/





    private void printAccessMatrix() {
        String[][] accessMatrix = new String[n + 1][(n+m) + 1];

        for(int row = 0; row < accessMatrix.length; row++){
            //Populates the first row with column names
            if(row == 0){
                for(int colNum = 0; colNum < accessMatrix[0].length; colNum++){
                    if(colNum == 0){
                        accessMatrix[0][0] = "D/O";
                        System.out.printf("%-5s", accessMatrix[0][0] + " ");
                    } else if (colNum <= m) {
                        //Assigns the column for the number of objects
                        accessMatrix[0][colNum] = "F" + colNum;
                        System.out.printf("%-7s", accessMatrix[0][colNum]);
                    } else {
                        //Assigns the column for the number of domains
                        accessMatrix[0][colNum] = "D" + (colNum - n);
                        System.out.printf("%-7s", accessMatrix[0][colNum]);
                    }
                }
            } else {
                accessMatrix[row][0] = "D" + row;
                System.out.printf("%-5s","D" + row);

                // Populates/loops the columns with access rights
                for(int colFill = 1; colFill < accessMatrix[0].length; colFill++){
                    if(colFill <= m){
                        if(matrix [row-1][colFill-1] == null){
                            accessMatrix[row][colFill] = " - ";
                            System.out.printf("%-7s", accessMatrix[row][colFill] + "  ");
                        } else {
                            //1 - 3 Fills the objects access rights
                            //int objRan = rand.nextInt(4);
                            //String randomOperation = objectArray[objRan];
                            //matrix[row][colFill] = randomOperation;
                            accessMatrix[row][colFill] = matrix[row - 1][colFill - 1];
                            System.out.printf("%-7s", accessMatrix[row][colFill] + " ");
                        }
                    } else {
                        if(matrix [row-1][colFill-1] == null){
                            accessMatrix[row][colFill] = " N/A ";
                            System.out.printf("%-7s", accessMatrix[row][colFill] + "  ");
                        }
                        /*
                        //Checks if the domain is the same or not, if so fill it with -
                        if(matrix[row][0].equals(matrix[0][colFill])){
                            //matrix[row][colFill] = " - ";
                            accessMatrix[row][colFill] = " - ";
                            System.out.printf("%-7s", accessMatrix[row][colFill] + "  ");
                        }*/
                        else{
                            //int domainRandom = rand.nextInt(2);
                            //String randomRight = domainArray[domainRandom];
                            //matrix[row][colFill] = randomRight;
                            accessMatrix[row][colFill] = matrix [row-1][colFill-1];
                            System.out.printf("%-7s", accessMatrix[row][colFill] + "  ");
                        }
                    }
                }



            }
            System.out.println();//Starts a new row on another line
        }


        /*
        System.out.println("Access Matrix:");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m + n; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
         */
    }
/*
    public void run() {
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            threads[i] = new Thread(new User(i));
            threads[i].start(); // start user threads
        }
        for (int i = 0; i < n; i++) {
            try {
                threads[i].join(); // wait for user threads to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class User implements Runnable {
        private final int domain; // user domain

        public User(int domain) {
            this.domain = domain;
        }


        @Override
        public void run() {
            System.out.println("Thread " + domain + " started.");
            Random rand = new Random();
            for (int i = 0; i < 5; i++) {
                int x = rand.nextInt(m + n); // range [0,M+N)
                if (x < m) {
                    // attempt to read from or write to file object x
                    //int access = matrix[domain][x];
                    String access = matrix[domain][x];
                    if (access >= 0) {
                        // claim file object lock
                        try {
                            locks[x].acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // read from or write to file object
                        System.out.println("Thread " + domain + " accessed file object " + x + ".");
                        if (access == 1) {
                            // write to file object
                            System.out.println("Thread " + domain + " is writing to file object " + x + ".");
                        } else {
                            // read from file object
                            System.out.println("Thread " + domain + " is reading from file object " + x + ".");
                        }
                        // release file object lock
                        locks[x].release();
                    } else {
                        System.out.println("Thread " + domain + " is not authorized to access file object " + x + ".");
                    }
                } else if (x != domain + m) {
                    // attempt to switch to domain x
                    int access = matrix[domain][x];
                    if (access == 1) {
                        // claim domain switch lock
                        try {
                            locks[m + domain].acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // switch to domain x
                        System.out.println("Thread " + domain + " is switching to domain " + (x - m) + ".");
                        // release domain switch lock
                        locks[m + domain].release();
                    } else {
                        System.out.println("Thread " + domain + " is not authorized to switch to domain " + (x - m) + ".");
                    }
                }
            }
            System.out.println("Thread " + domain + " finished.");
        }

    }

    public void run(){
        for(int i = 0; i < 5; i++) {
            Random rand = new Random();
            // Depending on what number you get will determine if it's an object or domain
            int randomColumnNum = rand.nextInt(matrix[0].length - 1) + 1;
            String randomColumn = matrix[0][randomColumnNum];
            arbFunction(threadID, randomColumnNum, randomColumn, domainID);
        }
    }
*/
}