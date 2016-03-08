import java.util.Random;

/**
 * use this class to generate a random number between lower and upper
 * RandomNumGenerator.getRandomNum(lower,upper)
 * @author Tao
 *
 */
public class RandomNumGenerator {
	/**
	 * The following variables are defined to generate a random number 
	 * according to the article "Random Number Generators: Good Ones
	 *  Are Hard to Find", Communications of the ACM, Vol. 31 No. 10,
	 *  October 1988, pp. 1192-1201.
	 */
	private static final double a = 16807.0;
	private static final double m = 2147483647.0;
	private static final double q = 127773.0;
	private static final double r = 2836.0;
	private static final double lambda = 2.38907;
	
	//used as default seed when user didn't give one
	private static final double DEFAULT_SEED = 0.009;

	/**
	 * Default constructor
	 */
	public RandomNumGenerator() {
	}

	/**
	 * generated a random number in the range between
	 * lower and upper without giving a seed
	 */
	public static int getRandomNum(int lower, int upper) {
		return getRandomNum(DEFAULT_SEED, lower, upper);
	}

	public static int getRandomNum(double seed, int lower, int upper) {
		Random random = new Random();
		return random.nextInt(upper)%(upper-lower+1) + lower;
	}

	/**
	 * Generate a random number between 0 and 1
	 * @param double seed
	 * @return double random number
	 */
	private static double random(double seed) {
		double rand, lo, hi, test;
		int tmp_int;

		/* generate a random number */
		tmp_int =  (int) (seed / q);
		hi = tmp_int * 1.0;
		lo = seed - q * hi;
		test = a * lo - r * hi;

		if (test > 0.0)
			seed = test;
		else
			seed = test + m;

		rand = seed / m;
		return rand;
	}

	/**
	 * Find a number between "lower" and "upper"
	 * @param seed
	 * @param lower
	 * @param upper
	 * @return
	 */
	private static int normalize(double seed, int lower, int upper) {
		int i;
		long N, temp, norm;
		double temp2;

		random(seed); /* make one idle call to random */

		norm = 0;
		/* don't want a number less than lower */
		while (norm < lower) {
			temp2 = -Math.log(random(seed)) / lambda;
			/**
			 * Pascal: 
			 * trunc(exp(random(seed)*maxint))/2.38907 mod upper
			 */
			while (temp2 > 1)
				temp2 = temp2 - 1.0;

			temp = (long) (m * temp2);
			N = temp % upper;

			if (N == 0)
				norm = upper;
			else
				norm = N;

		}
		return (int) norm;
	}

}
