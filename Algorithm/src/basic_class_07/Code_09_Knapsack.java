package basic_class_07;

public class Code_09_Knapsack {

	public static int maxValue1(int[] c, int[] p, int bag) {
		return process1(c, p, 0, 0, bag);
	}

	public static int process1(int[] c, int[] p, int i, int cost, int bag) {
		if (cost > bag) {
			return Integer.MIN_VALUE;
		}
		if (i == c.length) {
			return 0;
		}
		return Math.max(process1(c, p, i + 1, cost, bag), p[i] + process1(c, p, i + 1, cost + c[i], bag));
	}

	public static int maxValue2(int[] c, int[] p, int bag) {
		int[][] dp = new int[c.length + 1][bag + 1];
		for (int i = c.length - 1; i >= 0; i--) {
			for (int j = bag; j >= 0; j--) {
				dp[i][j] = dp[i + 1][j];
				if (j + c[i] <= bag) {
					dp[i][j] = Math.max(dp[i][j], p[i] + dp[i + 1][j + c[i]]);
				}
			}
		}
		return dp[0][0];
	}

	public static void main(String[] args) {
		int[] c = { 3, 2, 4, 7 };
		int[] p = { 5, 6, 3, 19 };
		int bag = 11;
		System.out.println(maxValue1(c, p, bag));
		System.out.println(maxValue2(c, p, bag));
	}

}
