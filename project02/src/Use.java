import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Use {

    /**
     * Returns a randomly generated integer between min and max (inclusive)
     *
     * @param min [int] Minimum/floor
     * @param max [int] Maximum/Ceiling
     * @return [int] Random integer
     */
    public static int randNum(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Simulates a coin flip
     *
     * @return [boolean] True/False at random
     */
    public static boolean coinFlip() {
        return randNum(0, 1) == 1;
    }

    public static void cycle(){
        Random rand = new Random();
        try{
            int time = rand.nextInt(7);
            while (time < 3) time = rand.nextInt(7);
            for (int i = 0; i<time; i++){
                Thread.yield();
            }
        }
        catch (Exception e){
            e.printStackTrace(System.out);
        }
    }
}
