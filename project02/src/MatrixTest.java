/* import java.util.Random;
import java.util.concurrent.Semaphore;

public class AccessMatrix {

    private final int N, M;
    private final boolean[][] accessMatrix;
    private final Semaphore[] fileSemaphores;

    public AccessMatrix() {
        Random rand = new Random();
        N = rand.nextInt(5) + 3;
        M = rand.nextInt(5) + 3;
        accessMatrix = new boolean[N][M + N];
        fileSemaphores = new Semaphore[M];

        // Initialize access matrix
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M + N; j++) {
                accessMatrix[i][j] = rand.nextBoolean();
            }
        }

        // Initialize file semaphores
        for (int i = 0; i < M; i++) {
            fileSemaphores[i] = new Semaphore(1);
        }

        // Print access matrix
        System.out.println("Access matrix:");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M + N; j++) {
                System.out.print(accessMatrix[i][j] ? "1" : "0");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void run() {
        Thread[] threads = new Thread[N];
        for (int i = 0; i < N; i++) {
            threads[i] = new Thread(new User(i));
            threads[i].start();
        }
        for (int i = 0; i < N; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class User implements Runnable {
        private final int domain;

        public User(int domain) {
            this.domain = domain;
        }

        @Override
        public void run() {
            Random rand = new Random();
            int numRequests = 5;
            while (numRequests > 0) {
                int x = rand.nextInt(M + N);
                if (x < M) {
                    // Access file object
                    boolean read = rand.nextBoolean();
                    if (read) {
                        // Read from file object
                        System.out.println("Domain " + domain + " requesting read access to file " + x);
                        if (accessMatrix[domain][x]) {
                            try {
                                fileSemaphores[x].acquire();
                                System.out.println("Domain " + domain + " granted read access to file " + x);
                                Thread.sleep(rand.nextInt(5) + 3);
                                fileSemaphores[x].release();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("Domain " + domain + " denied read access to file " + x);
                        }
                    } else {
                        // Write to file object
                        System.out.println("Domain " + domain + " requesting write access to file " + x);
                        if (accessMatrix[domain][x]) {
                            try {
                                fileSemaphores[x].acquire();
                                System.out.println("Domain " + domain + " granted write access to file " + x);
                                Thread.sleep(rand.nextInt(5) + 3);
                                fileSemaphores[x].release();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else

*/