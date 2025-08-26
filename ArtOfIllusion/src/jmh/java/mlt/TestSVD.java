package mlt;


import artofillusion.math.SVD;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1000, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
@State(Scope.Benchmark)
public class TestSVD {

    final double[][] input = {{-0.375, -0.010000000000000009, 0.0}, {-0.0, -0.9999999999999999, 0.0}, {0.0, 0.0, 1.0}};

    @Benchmark
    public void testJAMA() {
        double[] b = {0.37499999999999994, 0.010000000000000007, 0.0};
        SVD.solve(input, b);
    }

    @Benchmark
    public void testEJML() {
        double[] b = {0.37499999999999994, 0.010000000000000007, 0.0};
        SVD.solve2(input, b);
    }


}
