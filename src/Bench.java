package bench;

import org.openjdk.jmh.annotations.*;

import java.util.stream.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A simple JMH benchmark of mutation of an atomic variable, reduction using
 * reduce() with a lambda, and the sum() convenience reduction method, in
 * sequential and parallel versions.
 * 
 * See: http://openjdk.java.net/projects/code-tools/jmh/
 * for information about JMH.
 *
 * @author smarks
 */
public class Bench {

    public final static long LIMIT = 100_000_000L;

    @GenerateMicroBenchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public long atomicSeq() {
        AtomicLong sum = new AtomicLong(0);
        LongStream.rangeClosed(1, LIMIT)
                  .forEach(i -> sum.addAndGet(i));
        return sum.get();
    }
    
    @GenerateMicroBenchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public long atomicPar() {
        AtomicLong sum = new AtomicLong(0);
        LongStream.rangeClosed(1, LIMIT)
                  .parallel()
                  .forEach(i -> sum.addAndGet(i));
        return sum.get();
    }
    
    @GenerateMicroBenchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public long reduceSeq() {
        return
            LongStream.rangeClosed(1, LIMIT)
                      .reduce((i, j) -> i + j)
                      .getAsLong();
    }
    
    @GenerateMicroBenchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public long reducePar() {
        return
            LongStream.rangeClosed(1, LIMIT)
                      .parallel()
                      .reduce((i, j) -> i + j)
                      .getAsLong();
    }

    @GenerateMicroBenchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public long sumSeq() {
        return
            LongStream.rangeClosed(1, LIMIT)
                      .sum();
    }

    @GenerateMicroBenchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public long sumPar() {
        return
            LongStream.rangeClosed(1, LIMIT)
                      .parallel()
                      .sum();
    }
}
