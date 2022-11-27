import java.util.*;
import java.util.concurrent.*;

public class Main {

    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String[] routes = new String[1000];
        for (int i = 0; i < routes.length; i++) {
            routes[i] = generateRoute("RLRFR", 100);
        }

        final ExecutorService threadPool = Executors.newFixedThreadPool(4);

        List<Future> tasks = new ArrayList<>();


        for (String route : routes) {

            Callable<Integer> myCallable = () -> {
                int rNumber = 0;
                for (int i = 0; i < route.length(); i++) {
                    if (route.charAt(i) == 'R') {
                        rNumber++;
                    }
                }
//                System.out.println(route + " -> " + rNumber);
                return rNumber;
            };

            Future<Integer> task = threadPool.submit(myCallable);
            tasks.add(task);
        }

        threadPool.shutdown();

        new Thread(() -> {
            int counter = 1;
            for (Future<Integer> task : tasks) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.put(task.get(), counter);
                        for (Map.Entry<Integer, Integer> kv : sizeToFreq.entrySet()) {
                            if (kv.getKey().equals(task.get())) {
                                counter++;
                                sizeToFreq.put(task.get(), counter);
                            }
                        }
                        sizeToFreq.notify();
                    } catch (InterruptedException e) {
                        return;
                    } catch (ExecutionException e) {
                        return;
                    }
                }
            }
        }).start();

        new Thread(() -> {
            int max = 0;
            int freq = 0;
            for (Map.Entry<Integer, Integer> kv : sizeToFreq.entrySet()) {
                synchronized (sizeToFreq) {
                    if (sizeToFreq.isEmpty()) {
                        try {
                            sizeToFreq.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (max == 0 || max < kv.getKey()) {
                        max = kv.getKey();
                        freq = kv.getValue();
                    }
                }
            }
            System.out.println("Самое частое количество повторений " + max + " (встретилось " + freq + " раз)");
//            System.out.println("Другие размеры:");
        }).start();


        new Thread(() -> {

            for (Map.Entry<Integer, Integer> kv : sizeToFreq.entrySet()) {
                synchronized (sizeToFreq) {
                    if (sizeToFreq.isEmpty()) {
                        try {
                            sizeToFreq.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println(" - " + kv.getKey() + " (" + kv.getValue() + " раз)");

                }
            }
        }).start();


    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}
