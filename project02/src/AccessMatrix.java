import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

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

    public void arbitration(int threadID, int randomColumnNum, String randColumn, int domainID){
        String operation;
        int objID;
        int[] yieldTimesArray = {3, 4, 5, 6, 7 };
        String[] charBufferArray = new String[1];
        charBufferArray[0] = " ";
        String[] randWordArray = {"Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet"};




        //Checks to see if it's a domain or object by looking at the first character of the string
        if(randColumn.charAt(0) == 'F'){
        //if(randomColumnNum <= m){
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
                        int randomYieldTime = yieldTimesArray[Use.randNum(0,4)]; // yields [3,7] times
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Yielding " + randomYieldTime + " times");
                        for(int i = 1; i <= randomYieldTime; i++){
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
                        int randomYieldTime = yieldTimesArray[Use.randNum(0,4)];
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Yielding " + randomYieldTime + " times");
                        for(int i = 1; i <= randomYieldTime; i++){
                            Thread.yield();
                        }
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation Complete");
                    }else{
                        System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Operation failed, permission denied");
                    }

            }


        } else {
            System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Attempting to switch from D" + domainID + " to " + randColumn);
            //System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Attempting to switch from D" + domainID + " to " + (randomColumnNum - m) );
            if(Matrix[domainID][randomColumnNum] == "allow"){
                domainID = Integer.parseInt(String.valueOf(randColumn.charAt(1)));
                //domainID = (randomColumnNum - m);
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Switched to " + randColumn);
                int randomYieldTime = yieldTimesArray[Use.randNum(0,4)];
                System.out.println("[Thread " + threadID + ": (D" + domainID + ")] Yielding " + randomYieldTime + " times");
                for(int i = 1; i <= randomYieldTime; i++){
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
            // Depending on what number you get will determine if it's an object or domain
            int randomColumnNum = rand.nextInt(Matrix[0].length - 1) + 1;
            String randomColumn = Matrix[0][randomColumnNum];
            arbitration(threadID, randomColumnNum, randomColumn, domainID);
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
        Matrix = new String[n + 1][(n+m) + 1]; // create access matrix with labels
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
                        Matrix[0][0] = "D/O";
                        System.out.printf("%-5s", Matrix[0][0] + " ");
                    } else if (columnNum <= m) {
                        // assigns column for number of objects
                        Matrix[0][columnNum] = "F" + columnNum;
                        System.out.printf("%-7s", Matrix[0][columnNum]);
                    } else {
                        // assigns column for number of domains
                        Matrix[0][columnNum] = "D" + (columnNum - m);
                        System.out.printf("%-7s", Matrix[0][columnNum]);
                    }
                }
            } else {
                Matrix[row][0] = "D" + row;
                System.out.printf("%-5s","D" + row);

                // Populates/loops the columns with access rights
                for(int columFill = 1; columFill < Matrix[0].length; columFill++){
                    if(columFill <= m){
                        if(matrix [row-1][columFill-1] == null){
                            Matrix[row][columFill] = "-";
                            System.out.printf("%-7s", Matrix[row][columFill] + "  ");
                        } else {
                            Matrix[row][columFill] = matrix[row - 1][columFill - 1];
                            System.out.printf("%-7s", Matrix[row][columFill] + " ");
                        }
                    } else {
                        if(matrix [row-1][columFill-1] == null){
                            Matrix[row][columFill] = "N/A";
                            System.out.printf("%-7s", Matrix[row][columFill] + "  ");
                        } else{
                            Matrix[row][columFill] = matrix [row - 1][columFill - 1];
                            System.out.printf("%-7s", Matrix[row][columFill] + "  ");
                        }
                    }
                }



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


}