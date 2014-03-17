package prime;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.stream.Collectors.*;

/**
 * A simplistic prime number finding program.
 * 
 * @author smarks
 */
public class Finder {
    static boolean verbose;
    static boolean par;
    static LongConsumer vprint;
    
    static boolean isPrime(long num) {
        if (num <= 1)
            return false;
        
        if (num == 2)
            return true;
        
        if ((num & 1) == 0)
            return false;
        
        long limit = (long)Math.sqrt(num);
        for (long i = 3L; i <= limit; i += 2L) {
            if (num % i == 0)
                return false;
        }
        return true;
    }
    
    static List<Long> find(long start, long count) {
        LongStream lstream = LongStream.range(start, start + count);
        if (par) {
            lstream = lstream.parallel();
        }
        return lstream.filter(n -> isPrime(n))
                      .peek(vprint)
                      .boxed()
                      .collect(toList());
    }
    
    static void doFind(long start, long count) {
        long startTime = System.currentTimeMillis();
        List<Long> list = find(start, count);
        long endTime = System.currentTimeMillis();

        if (verbose)
            System.out.println(list);
        
        System.out.printf("Found %d primes in %dms.%n", list.size(), endTime - startTime);
    }
    
    static void usage() {
        System.out.println("usage: prime.Finder [-v] {seq|par} start count");
        System.out.println("To vary the level of parallelism, use:");
        System.out.println("    -Djava.util.concurrent.ForkJoinPool.common.parallelism=#");
    }
    
    static long parse(String s) {
        try {
            return Long.parseLong(s.replace("_", ""));
        } catch (NumberFormatException nfe) {
            System.err.println("Illegal number: " + s);
            throw nfe;
        }
    }

    public static void main(String[] args) {
        int a = 0;
        
        if (args.length > 0 && "-v".equals(args[a])) {
            verbose = true;
            a++;
        }
        
        if (args.length - a != 3) {
            usage();
            return;
        }
        
        switch (args[a]) {
            case "seq":
                par = false;
                break;
            case "par":
                par = true;
                break;
            default:
                usage();
                return;
        }
        
        vprint = verbose ? n -> System.out.println(n)
                         : n -> { };
        
        long start = parse(args[a+1]);
        long count = parse(args[a+2]);
        
        // warm-up
        find(1_000_000_000_000L, 1000L);
        
        for (int i = 0; i < 5; i++) {
            doFind(start, count);
        }
    }
}