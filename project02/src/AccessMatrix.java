import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

// Code in this section by Chris Walther, C00408978
class Arbitration extends Thread{
    int n, m; // n = domains and m = objects
    private int threadID, domainID;
    private String[][] Matrix;

    ReentrantLock semLock = new ReentrantLock();


    public Arbitration(int n, int m, int threadID, String[][] Matrix, int domainID){
        this.n = n;
        this.m = m;
        this.threadID = threadID;
        this.Matrix = Matrix;
        this.domainID = domainID;

    }

    public void arbitration(int threadID, int randColumnNum, String randColumn, int domainID){
        String operation;
        int objID;
        String[] charBufferArray = new String[1];
        charBufferArray[0] = " ";
        String[] randWordArray = {"Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet"};


        if(randColumn.charAt(0) == 'F'){ // determines whether selected column corresponds to a domain or file object
        //if(randColumnNum <= m){
            // randomly picks to read or write
            switch (Use.randNum(0,1)) {
                case 0:
                    operation = "read";
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Attempting to " + operation + " resource: " + randColumn);

                    objID = Integer.parseInt(String.valueOf(randColumn.charAt(1)));
                    if(Matrix[domainID][objID] == "R" || Matrix[domainID][objID] == "R/W"){
                        try{
                            semLock.lock();

                            System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Resource " + randColumn + " contains: " + Arrays.toString(charBufferArray));

                            semLock.unlock();
                        } catch(Exception e){
                            System.out.println(e);
                        }
                        int randYieldTime = Use.randNum(3,7); // yields [3,7] times
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Yielding " + randYieldTime + " times");
                        for(int i = 1; i <= randYieldTime; i++){
                            Thread.yield();
                        }
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation Complete");
                    }else{
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation failed, permission denied");
                    }
                case 1:
                    operation = "write";
                    System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Attempting to " + operation + " resource: " + randColumn);

                    objID = Integer.parseInt(String.valueOf(randColumn.charAt(1)));
                    if(Matrix[domainID][objID] == "W" || Matrix[domainID][objID] == "R/W"){ // check for write permissions
                        try{
                            semLock.lock();

                            String randWord = randWordArray[Use.randNum(0,6)];
                            System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Writing '" + randWord + "' to " + randColumn);
                            charBufferArray[0] = randWord;

                            semLock.unlock();
                        }catch(Exception e){
                            System.out.println(e);
                        }
                        int randYieldTime = Use.randNum(3,7);
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Yielding " + randYieldTime + " times");
                        for(int i = 1; i <= randYieldTime; i++){
                            Thread.yield();
                        }
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation Complete");
                    }else{
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation failed, permission denied");
                    }

            }


        } else {
            System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Attempting to switch from D" + domainID + " to " + randColumn);
            //System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Attempting to switch from D" + domainID + " to " + (randColumnNum - m) );
            if(Matrix[domainID][randColumnNum] == "allow"){
                domainID = Integer.parseInt(String.valueOf(randColumn.charAt(1)));
                //domainID = (randColumnNum - m);
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Switched to " + randColumn);
                int randYieldTime = Use.randNum(3,7);
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Yielding " + randYieldTime + " times");
                for(int i = 1; i <= randYieldTime; i++){
                    Thread.yield();
                }
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation Complete");
            } else {
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation failed, permission denied");
            }

        }
    }


    public void run(){
        for(int i = 0; i < 5; i++) {
            Random rand = new Random();
            int randColumnNum = rand.nextInt(Matrix[0].length - 1) + 1;
            String randColumn = Matrix[0][randColumnNum];
            arbitration(threadID, randColumnNum, randColumn, domainID);
        }
    }
}



public class AccessMatrix{
    private final int n; // number of domains
    private final int m; // number of file objects
    //private final int[][] matrix; // access matrix
    //private final String [][] matrix; // access matrix
    final String [][] matrix; // access matrix
    //private final Semaphore[] locks; // file object locks
    //ReentrantLock semLock = new ReentrantLock();
    final String[][] Matrix;

    public AccessMatrix(int domains, int objects) {
        n = domains;
        m = objects;
        matrix = new String[n][m + n]; // create access matrix
        Matrix = new String[n + 1][(n + m) + 1]; // create access matrix with labels
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
/*
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
 */

    private void printAccessMatrix() {
        //String[][] accessMatrix = new String[n + 1][(n+m) + 1];
        for(int row = 0; row < Matrix.length; row++){
            // populates first row with column names
            if(row == 0){
                for(int columnNum = 0; columnNum < Matrix[0].length; columnNum++){
                    if(columnNum == 0){
                        Matrix[0][0] = "Dom/Obj";
                        System.out.printf("%-8s", Matrix[0][0] + " ");
                    } else if (columnNum <= m) {
                        // assigns column number and name for file objects in first row
                        Matrix[0][columnNum] = "F" + columnNum;
                        System.out.printf("%-10s", Matrix[0][columnNum]);
                    } else {
                        // assigns column number and name for domains in first row
                        Matrix[0][columnNum] = "D" + (columnNum - m);
                        System.out.printf("%-10s", Matrix[0][columnNum]);
                    }
                }
            } else {
                Matrix[row][0] = "D" + row;
                System.out.printf("%-8s", "D" + row);

                // populates columns of Matrix with access rights from original matrix generated
                for(int fill = 1; fill < Matrix[0].length; fill++){
                    if(fill <= m){
                        if(matrix [row - 1][fill - 1] == null){
                            Matrix[row][fill] = "--";
                            System.out.printf("%-10s", Matrix[row][fill] + "  ");
                        } else {
                            Matrix[row][fill] = matrix[row - 1][fill - 1];
                            System.out.printf("%-10s", Matrix[row][fill] + " ");
                        }
                    } else {
                        if(matrix [row - 1][fill - 1] == null){
                            Matrix[row][fill] = "N/A";
                            System.out.printf("%-10s", Matrix[row][fill] + "  ");
                        } else{
                            Matrix[row][fill] = matrix [row - 1][fill - 1];
                            System.out.printf("%-10s", Matrix[row][fill] + "  ");
                        }
                    }
                }
                /* by end of this section, "Matrix" is populated with data from "matrix" with extra column
                and row to accommodate for labeling for purpose of printing and arbitration made easier  */


            }
            System.out.println();
            System.out.println(" ");
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

    // Code in this section by Chris Walther, C00408978
}