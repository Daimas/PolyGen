package net.lrstudios.java.polygen;

import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * Contains useful math functions.
 */
public class MathUtil {
    /**
     * Returns the value of C(N, K).
     */
    public static long binomial(int N, int K) {
        long[][] binomial = new long[N + 1][K + 1];

        // base cases
        for (int k = 1; k <= K; k++) binomial[0][k] = 0;
        for (int n = 0; n <= N; n++) binomial[n][0] = 1;

        // bottom-up dynamic programming
        for (int n = 1; n <= N; n++)
            for (int k = 1; k <= K; k++)
                binomial[n][k] = binomial[n - 1][k - 1] + binomial[n - 1][k];

        return binomial[N][K];
    }

    public static BigDecimal binomial(BigDecimal N, int K) {
        int N_int = N.toBigInteger().intValue();
        BigDecimal[][] binomial = new BigDecimal[N_int + 1][K + 1];

        // base cases
        for (int k = 1; k <= K; k++) binomial[0][k] = BigDecimal.ZERO;
        for (int n = 0; n <= N_int; n++) binomial[n][0] = BigDecimal.ONE;

        // bottom-up dynamic programming
        for (int n = 1; n <= N_int; n++)
            for (int k = 1; k <= K; k++)
                binomial[n][k] = binomial[n - 1][k - 1].add(binomial[n - 1][k]);

        return binomial[N_int][K];
    }


    /**
     * rounds a number to the specified number of decimals.
     */
    public static double round(double n, int decimals) {
        if (decimals <= 0)
            return Math.round(n);

        double factor = 10.0 * decimals;
        return Math.round(n * factor) / factor;
    }


    /**
     * Gets the intersection point of two lines of the form ax + by + c = 0.
     * Returns null if they don't intersect.
     */
    public static Point2D.Double intersect(double a1, double b1, double c1, double a2, double b2, double c2) {
        double det = (a1 * b2 - a2 * b1);

        if (det != 0)
            return new Point2D.Double((b1 * c2 - b2 * c1) / det, (c1 * a2 - c2 * a1) / det);
        else
            return null;
    }

    /**
     * Gets the intersection point of two lines of the form ax + by + c = 0.
     * Returns null if they don't intersect.
     */
    public static Point2D.Double intersect(double[] line1, double[] line2) {
        return intersect(line1[0], line1[1], line1[2], line2[0], line2[1], line2[2]);
    }

    /**
     * Gets the intersection point of two lines of the form ax + by + c = 0.
     * Returns null if they don't intersect.
     */
    public static Point2D.Double intersect(double a1, double b1, double a2, double b2) {
        return intersect(a1, -1, b1, a2, -1, b2);
    }

    /**
     * Returns the a, b, c values of the equation ax + by + c = 0 of the line joining the two specified points.
     */
    public static double[] lineEquation(double x1, double y1, double x2, double y2) {
        if (x2 == x1) {
            return new double[]{-1, 0, x1};
        }
        else if (y2 == y1) {
            return new double[]{0, -1, y1};
        }
        else {
            double a = (y2 - y1) / (x2 - x1);
            return new double[]{a, -1, -a * x1 + y1};
        }
    }

    /**
     * Returns 1 if the specified divisor is a divisor of n, else 0.
     */
    public static int mod_int(int n, int divisor) {
        return (n % divisor == 0) ? 1 : 0;
    }

    /**
     * Returns 1 if the specified divisor is a divisor of n, else 0.
     */
    public static BigInteger mod_int(BigInteger n, int divisor) {
        return (n.mod(BigInteger.valueOf(divisor)).compareTo(BigInteger.ZERO) == 0) ? BigInteger.ONE : BigInteger.ZERO;
    }

    /**
     * Returns 1 if the specified divisor is a divisor of n, else 0.
     */
    public static BigDecimal mod_int(BigDecimal n, int divisor) {
        return (n.divideAndRemainder(BigDecimal.valueOf(divisor))[1].compareTo(BigDecimal.ZERO) == 0) ? BigDecimal.ONE : BigDecimal.ZERO;
    }
}
