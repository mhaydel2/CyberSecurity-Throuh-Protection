import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) {

        int domains = Use.randNum(3,7);
        int objects = Use.randNum(3,7);

        System.out.println("Domain count: " + domains);
        System.out.println("Object count: " + objects);
        CL list = new CL(domains, objects);

        // there are domain # of threads
        for(int i = 0; i < list.domains; i++){
            DomainCl cList = new DomainCl(String.valueOf(i), list);
            cList.start();                                                   // Start user thread
        }
    }
}


