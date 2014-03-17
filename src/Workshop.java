package workshop;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

/**
 * Parallel Streams Workshop. Virtual JUG presentation, Stuart Marks,
 * 13 March 2014.
 * 
 * http://www.meetup.com/virtualJUG/events/169958212/
 * http://www.youtube.com/watch?v=LRh0nDuYCyY
 * 
 * @author smarks
 */
public class Workshop {
    // Define a few variables for use in examples.
    static List<String> strings = Arrays.asList("alfa", "bravo", "charlie",
        "delta", "echo", "foxtrot", "golf", "hotel", "india", "juliet");
    public static final long LIMIT = 100_000_000L;
    
    public static void main(String[] args) {
        // Fill in code here from ex## routines below.
    }

    /**
     * Sleep for 'millis' ignoring InterruptedException.
     * @param millis number of milliseconds to sleep
     */
    public static void sleepUninterrupted(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) { }
    }
    
    /** Does something for each element of a list. */
    public static void ex00() {
        strings.forEach(s -> System.out.println(s));
    }

    /** Replaces each element of a list with a replacement value. */
    public static void ex01() {
        strings.replaceAll(s -> s.toUpperCase());
        System.out.println(strings);
    }
    
    /**
     * Attempts to remove elements from the list. This fails
     * UnsupportedOperationException! Can't change the size of
     * strings because it's backed by an array.
     */
    public static void ex02() {
        strings.removeIf(s -> s.length() < 6); // fails
        System.out.println(strings);
    }
    
    /**
     * Copies the strings list before modifying it. This works but not very
     * well, as we have to copy all the elements, even the ones that we're
     * going to remove, and we have to make a second pass to modify them.
     */
    public static void ex03() {
        List<String> list2 = new ArrayList<>(strings);
        list2.removeIf(s -> s.length() < 6);
        list2.replaceAll(s -> s.toUpperCase());
        System.out.println(list2);
    }
    
    /**
     * Removes and modifies elements in a single pass, using streams.
     * This demonstrates a stream's source, intermediate operations, and
     * a terminal operation.
     */
    public static void ex04() {
        strings.stream()
               .filter(s -> s.length() >= 6)
               .map(s -> s.toUpperCase())
               .forEach(s -> System.out.println(s));
    }
    
    /** Same as above, but collects the results into a destination list. */
    public static void ex05() {
        System.out.println(
            strings.stream()
                   .filter(s -> s.length() >= 6)
                   .map(s -> s.toUpperCase())
                   .collect(toList())
        );
    }
    
    /** Demonstrates the "peek" intermediate operation. */
    public static void ex06() {
        System.out.println(
            strings.stream()
                   .filter(s -> s.length() >= 6)
                   .peek(s -> System.out.printf("**%s**%n", s))
                   .map(s -> s.toUpperCase())
                   .collect(toList())
        );
    }
    
    /**
     * Sorts the elements of the list in reverse of the natural order,
     * which is alphabetical order, since the elements are strings.
     * Note the static import of Comparator.* above.
     */
    public static void ex07() {
        System.out.println(
            strings.stream()
                   .sorted(reverseOrder())
                   .collect(toList())
        );
    }
    
    /**
     * Sorts the list by string length. The comparingInt() method creates
     * a Comparator for us, which compares the lengths of each string.
     */
    public static void ex08() {
        System.out.println(
            strings.stream()
                   .sorted(comparingInt(s -> s.length()))
                   .collect(toList())
        );
    }
    
    /** Sorts the string list by the last letter in the word. */
    public static void ex09() {
        System.out.println(
            strings.stream()
                   .sorted(comparing(s -> s.substring(s.length() - 1)))
                   .collect(toList())
        );
    }
    
    /**
     * Sorts the string list by length, and within length, by
     * reverse alphabetical order. Note that type inference doesn't
     * work for the lambda passed to comparingInt() so I've declared
     * the type of the lambda argument explicitly.
     */
    public static void ex10() {
        System.out.println(
            strings.stream()
                   .sorted(comparingInt((String s) -> s.length())
                           .thenComparing(reverseOrder()))
                   .collect(toList())
        );
    }
    
    /**
     * Simulate a long-running operation by sleeping in the midst
     * of a pipeline.
     */
    public static void ex11() {
        System.out.println(
            strings.stream()
                   .peek(s -> sleepUninterrupted(500L))
                   .map(s -> s.toUpperCase())
                   .collect(toList())
        );
    }
    
    /**
     * Run long-running operations in parallel. On multi-core machines,
     * this should see a speedup compared to the above method.
     */
    public static void ex12() {
        System.out.println(
            strings.parallelStream()
                   .peek(s -> sleepUninterrupted(500L))
                   .map(s -> s.toUpperCase())
                   .collect(toList())
        );
    }
    
    /**
     * Example illustrating different ordering in a parallel stream. The
     * ordering or positioning of elements in the stream source, a list,
     * is called the "encounter order". This may differ from the order in which
     * elements are processed in a pipeline. This example uses 'peek' to
     * add elements to a thread-safe list in order to get visibility into
     * the order of processing. For a sequential stream, the processing order
     * is generally the same as encounter order, so list1 and list2 are equal. 
     */
    public static void ex13() {
        List<Integer> list1 = Collections.synchronizedList(new ArrayList<>());
        List<Integer> list2 =
            IntStream.range(0, 20)
                     .peek(i -> list1.add(i))
                     .boxed()
                     .collect(toList());
        System.out.println(list1);
        System.out.println(list2);
    }

    /**
     * Demonstrates the difference between encounter order and processing order
     * in a parallel stream. No defined order of processing exists among the
     * threads of a parallel stream, and indeed on a multi-core system some
     * processing may actually occur simultaneously. For a parallel stream,
     * processing order may differ from encounter order, so list1 and list2
     * will probably end up with the same elements in a different order.
     * 
     * Put another way, even though processing is unordered among multiple
     * threads, the stream machinery knows how to reassemble the results
     * so that encounter order is preserved through to the output.
     */
    public static void ex14() {
        List<Integer> list1 = Collections.synchronizedList(new ArrayList<>());
        List<Integer> list2 =
            IntStream.range(0, 20)
                     .parallel()
                     .peek(i -> list1.add(i))
                     .boxed()
                     .collect(toList());
        System.out.println(list1);
        System.out.println(list2);
    }

    /** Shows the conventional technique for summing a range of integers. */
    public static void ex15() {
        long sum = 0L;
        for (long i = 1; i <= LIMIT; i++) {
            sum += i;
        }
        System.out.println(sum);
    }
    
    /**
     * Transliterate the above code to use streams. This fails, because it
     * attempts to mutate a local variable from within a lambda, which is
     * prohibited. Captured locals must be final or effectively final.
     */
    public static void ex16() {
        long sum = 0L;
        LongStream.rangeClosed(1, LIMIT)
                  // .forEach(i -> sum += i) // error
                  ;
        System.out.println(sum);
    }
    
    /**
     * Shows the typical hacky workaround for this limitation, which is to
     * mutate the element of a one-element array.
     */
    public static void ex17() {
        // )-: DON'T DO THIS! THIS IS A BAD IDEA! :-(
        long[] sum = { 0 };
        LongStream.rangeClosed(1, LIMIT)
                  .forEach(i -> sum[0] += i);
        System.out.println(sum[0]);
    }
    
    /**
     * Shows why this is a bad idea: this has a race condition when run in
     * parallel. This will often give the incorrect answer.
     */
    public static void ex18() {
        // )-: THIS CODE SHOWS WHY THIS IS A BAD IDEA! :-(
        long[] sum = { 0 };
        LongStream.rangeClosed(1, LIMIT)
                  .parallel()
                  .forEach(i -> sum[0] += i);
        System.out.println(sum[0]);
    }
    
    /**
     * Mitigates the race condition by using an AtomicInteger for accumulating
     * the sum. This works and gives the correct answer, but it's slow.
     */
    public static void ex19() {
        // THIS IS A LESS BAD IDEA:
        AtomicLong sum = new AtomicLong(0L);
        LongStream.rangeClosed(1, LIMIT)
                  .parallel()
                  .forEach(i -> sum.addAndGet(i));
        System.out.println(sum.get());
    }
    
    /**
     * Use reduction instead of accumulation. See slides 7-8 for diagrams
     * illustrating the differences between reduction and accumulation.
     */
    public static void ex20() {
        System.out.println(
            LongStream.rangeClosed(1, LIMIT)
                      .parallel()
                      .reduce(0, (long i, long j) -> i + j)
        );
    }

    /**
     * Use built-in summation (a form of reduction). This is equivalent to
     * the reduce(lambda) form above, but is faster, probably because it avoids
     * a method call to the lambda.
     * 
     * See Bench.java for a JMH benchmark of these last three methods,
     * in sequential mode and in parallel mode. See BenchResults.txt for
     * JMH results. Note that the atomic version is quite slow, and in
     * parallel the atomic version suffers a slowdown because of contention.
     * The other techniques get a parallel speedup.
     */
    public static void ex21() {
        System.out.println(
            LongStream.rangeClosed(1, LIMIT)
                      .parallel()
                      .sum()
        );
    }
}