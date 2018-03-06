package basic_class_07;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Code_08_Money_Problem {

	public static boolean money1(int[] arr, int aim) {
		return process1(arr, 0, 0, aim);
	}

	public static boolean process1(int[] arr, int i, int sum, int aim) {
		if (sum == aim) {
			return true;
		}
		if (i == arr.length) {
			return false;
		}
		return process1(arr, i + 1, sum, aim) || process1(arr, i + 1, sum + arr[i], aim);
	}

	public static boolean money2(int[] arr, int aim) {
//		boolean[][] dp = new boolean[arr.length + 1][aim + 1];
//
//		for (int i = 0; i < dp.length; i++) {
//			dp[i][aim] = true;
//		}
//
//		for (int i = arr.length - 1; i >= 0; i--) {
//			for (int j = aim - 1; j >= 0; j--) {
//				dp[i][j] = dp[i + 1][j];
//				if (j + arr[i] <= aim) {
//					dp[i][j] = dp[i][j] || dp[i + 1][j + arr[i]];
//				}
//			}
//		}
//		return dp[0][0];
        HashSet<Integer> res = new HashSet<>();
        res.add(0);
        for (int i = 0; i < arr.length; i++) {
            List<Integer> tmp = new ArrayList<>();
            for (Integer n: res) {
                tmp.add(n + arr[i]);
            }
            res.addAll(tmp);
            if (res.contains(aim)) return true;
        }
        return false;
    }

	public static void main(String[] args) {
		int[] arr = {1,2,3,100,-90};
		int aim = 16;
		System.out.println(money1(arr, aim));
		System.out.println(money2(arr, aim));
	}

}
