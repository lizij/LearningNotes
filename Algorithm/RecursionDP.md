# Recursion and Dynamic Programming

[TOC]

## Recursion

Recursion is the process a procedure goes through when one of the steps of the procedure involves invoking the procedure itself. A procedure that goes through recursion is said to be 'recursive'.

Disassemble a big problem into several smaller-scale same type of the problem

> Recursion may contains a lot of replicate computing. Using memorization to solve this

[More](https://en.wikipedia.org/wiki/Recursion)

## Dynamic Programming

**Dynamic programming** (also known as **dynamic optimization**) is a method for solving a complex problem by breaking it down into a collection of simpler subproblems, solving each of those subproblems just once, and storing their solutions. The next time the same subproblem occurs, instead of recomputing its solution, one simply looks up the previously computed solution, thereby saving computation time at the expense of a (hopefully) modest expenditure in storage space. (Each of the subproblem solutions is indexed in some way, typically based on the values of its input parameters, so as to facilitate its lookup.) The technique of storing solutions to subproblems instead of recomputing them is called "[memoization](https://en.wikipedia.org/wiki/Memoization)"

> Dynamic programming can be seen as the optimized result of recursion. Use recursion to analyze the problem and get the origin code. Then use dynamic programming to optimize computing progresses.

[More](https://en.wikipedia.org/wiki/Dynamic_programming)

## Practice

### Factorial

> Compute n!

```java
long getFactorial(int n) {
  if (n == 1) return 1;
  return (long) n * getFactorial(n - 1);
}
```

## Hanoi

> Compute the number of moves n plates of Hanoi Towers

```java
void hanoi(int n) {
  if (n <= 0) return;
  return hanoi(n, "left", "mid", "right");
}

void hanoi(int n, String from, String to, String to) {
  if (n == 1) System.out.println("move from " + from " to " + to);
  else {
    hanoi(n - 1, from, to, mid); //move n - 1 ones
    hanoi(1, from, mid, to); //move the bottom
    hanoi(n - 1, mid, from, to);//move n - 1 back 
  }
}
```

### PrintAllSubsequence

> Print all subsequences of a string including ""

```java
void printAllSubsequence(String str) {
  char[] chs = str.toCharArray();
  printAllSubsequence(chs, 0);
}

void printAllSubsequence(char[] chs, int index) {
  if (start == chs.length) {
    System.out.println(String.valueOf(chs)); // print all chars not equals to 0
    return;
  }
  printAllSubsequence(chs, i + 1);
  char tmp = chs[index];
  chs[index] = 0;
  printAllSubsequence(chs, i + 1);
  chs[index] = tmp;
}
```

### PrintAllPermutations

> Print all permutations of a given string without replicates
>
> eg. input "abc", print ["abc", "acb", "bac", "bca", "cab", "cba"]

```java
void printAllPermutations(String str) {
  char[] chs = str.toCharArray();
  process1(chs, 0);
}

void process1(char[] chs, int i) {
  if (i == chs.length) {
    System.out.println(String.valueOf(chs));
  }
  for (int j = i; j < chs.length; j++) {
    // from every pos j from i, use j as the start(i)
    swap(chs, i, j);
    process1(chs, i + 1);
    swap(chs, i, j);
  }
}

void process2(char[] chs, int i) {
  // this can deal with the case that "a(1)a(2)b" and "a(2)a(1)b" is the same
  if (i == chs.length) {
    System.out.println(String.valueOf(chs));
  }
  HashSet<Character> set = new HashSet<>();
  for (int j = i; j < chs.length; j++) {
    if (!set.contains(chs[j])) {
      set.add(chs[j]);
      swap(chs, i, j);
      process2(chs, i + 1);
      swap(chs, i, j);
    }
  }
}
```

### CowNumber

> One cow give birth to a child. A child will be a cow after 3 year. Assuming cows won't die. Compute the number of cows at the nth year.

$$F(n) = F(n- 1) + F(n-3)$$

```java
int cowNumber1(int n) {
  if (n < 1) return 0;
  if (n == 1 || n == 2 || n == 3) return n;
  return cowNumber1(n - 1) + cowNumber1(n - 3);
}
```

### ReverseStackUsingRecursion

> Reverse a stack without extra space. Only recursion is allowed.

```java
void reverse(Stack<Integer> stack) {
  if (stack.isEmpty()) return;
  int last = getAndRemoveLastElement(stack);
  reverse(stack);
  stack.push(last);
}

int getAndRemoveLastElement(Stack<Integer> stack) {
  // remove the last element and return its value
  int res = stack.pop();
  if (stack.isEmpty()) return res;
  else {
    int last = getAndRemoveLastElement(stack);
    stack.push(res);
    return last;
  }
}
```

### NumberToChars(Facebook)

> If 1->a, 2->b, ..., 26->z, give a number string, give all possible results after transformation.
>
> eg. input 1111, return 5 ("aaaa", "aak", "aka", "kaa", "kk")
>
> input 0123, return [], for there is no char for 0

If chs[i] == '0', $$F(i)=0$$

If chs[i] == '1' or (chs[i] == '2' and chs[i + 1] < '7'), $$F(i)=F(i+1) + F(i+2)$$

If (chs[i] == '2' and chs[i + 1] >= '7') or chs[i] >= '3', $$F(i) = F(i+1)$$

```java
int numberToChars(char[] chs, int i) {
  if (i == chs.length) {
    return 1;
  }
  if (chs[i] == '0' || i > chs.length) {
    return 0;
  }
  if (chs[i] == '1' || chs[i] == '2' && i + 1 < chs.length && chs[i + 1] < '7') {
    return numberToChars(chs, i + 1) + numberToChars(chs, i + 2);
  }
  return numberToChars(chs, i);
}
```

### MinPath

> A matrix with all positive numbers is given. Every number is a cost when move to the point. Compute the min cost of moving from (0, 0) to the right bottom, you can move right or down
>
> eg. 
>
> [[1, 0, 2, 0], 
>
>  [1, 0, 3, 0]], the min path is 3, [1, 0, 2, 0, 0]

Recursion with cache

```java
int minPath(int[][] matrix) {
  if (matrix == null || matrix.length == 0 || matrix[0].length == 0) return 0;
  int m = matrix.length, n = matrix[0].length;
  int[][] cache = new int[m][n];
  for (int i = 0; i < m; i++) {
    for (int j = 0; j < n; j++) {
      cache[i][j] = Integer.MAX_VALUE;
    }
  }
  return move(matrix, m, n, cache);
}

int move(int[][] matrix, int i, int j, int[][] cache) {
  if (cache[i][j] != Integer.MAX_VALUE) {
    return cache[i][j];
  }
  int res = matrix[i][j];
  if (i == 0 && j == 0) {
    cache[i][j] = res;
  }
  else if (i == 0 && j > 0) {
    cache[i][j] = res + move(matrix, i, j - 1);
  }
  else if (i > 0 && j == 0) {
    cache[i][j] = res + move(matrix, i - 1, j);
  }
  else {
    cache[i][j] = res + Math.min(move(matrix, i, j - 1), move(matrix, i - 1, j));
  }
  return cache[i][j];
}
```

DP

```java
int minPath(int[][] matrix) {
  if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
    return 0;
  }
  int m = matrix.length, n = matrix[0].length;
  int[][] dp = new int[m][n];
  dp[0][0] = matrix[0][0];
  // set the first column
  for (int i = 1; i < m; i++) {
    dp[i][0] = dp[i - 1][0] + matrix[i][0];
  }
  // set the first row
  for (int i = 1; i < m; i++) {
    dp[0][j] = dp[0][j - 1] + matrix[0][j];
  }
  // from left to right, top to bottom, compute others
  for (int i = 1; i < m; i++) {
    for (int j = 1; j < n; j++) {
      dp[i][j] = Math.min(dp[i - 1][j], dp[i][j - 1]) + matrix[i][j];
    }
  }
  return dp[m - 1][n - 1];
}
```

### TargetSubsequenceSum

> Return true if there is the sum of a subsequence of the given array equals to the given target, otherwise false.

Recursion without cache

```java
boolean targetSubsequenceSum(int[] arr, int target) {
  return helper(arr, 0, 0, target);
}

boolean helper(int[] arr, int i, int sum, int target) {
  if (sum == target) {
    return true;
  }
  if (i == arr.length) {
    return false;
  }
  return helper(arr, i + 1, sum + arr[i], target) || helper(arr, i + 1, sum, target);
} 
```

DP(only works for positive data, counter-example:[1,2,3,100,-90], 16)

```java
boolean targetSubsequenceSum(int[] arr, int target) {
  boolean[][] dp = new boolean[arr.length + 1][target + 1];
  for (int i = 0; i < dp.length; i++) {
    dp[i][target] = true;
  }
  for (int i = arr.length - 1; i >= 0; i--) {
    for (int j = target - 1; j >= 0; j--) {
      dp[i][j] = dp[i + 1][j];
      if (j + arr[i] <= target) {
        dp[i][j] = dp[i][j] || dp[i + 1][j + arr[i]];
      }
    }
  }
  return dp[0][0];
}
```

BFS

```java
boolean targetSubsequenceSum(int[] arr, int target) {
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
```

### Knapsack

> Compute the max value of Knapsack
>
> In Knapsack problem, we have a limited-size bag, some things with their weight and value. We need to make use of this bag to hold as more valuable things as possible

Recursion

```java
int maxValue(int[] weight, int[] values, int i, int curWeight, int bag) {
  if (cur > bag) return Integer.MIN_VALUE;
  if (i == weight.length) return 0;
  return Math.max(
    maxValue(weight, value, i + 1, curWeight, bag), 
    values[i] + maxValue(weight, values, i + 1, curWeight + weight[i], bag)
  );
}
```

Recursion with cache

```java
int maxValue(int[] weight, int[] values, int bag) {
  int[][] cache = new [weight.length + 1][bag + 1];
  return maxValue(weight, values, 0, 0, bag, cache);
}

int maxValue(int[] weight, int[] values, int i, int curWeight, int bag, int[][] cache) {
  if (curWeight > bag) {
    return Integer.MIN_VALUE;
  }
  if (i == weight.length) {
    return 0;
  }
  if (cache[i][curWeight] > 0) {
    return cache[i][curWeight];
  }
  cache[i][curWeight] = Math.max(
    maxValue(weight, value, i + 1, curWeight, bag), 
    values[i] + maxValue(weight, values, i + 1, curWeight + weight[i], bag)
  );
  return cache[i][curWeight];
}
```

DP

```java
int maxValue(int[] weight, int[] values, int bag) {
  int[][] dp = new int[weight.length + 1][bag + 1];
  for (int i = weight.length - 1; i >= 0; i--) {
    for (int j = bag; j >= 0; j--) {
      // not use i
      dp[i][j] = dp[i + 1][j];
      // if i is usable, compare using it with no-using it
      if (j + weight[i] <= bag) {
        dp[i][j] = Math.max(
          dp[i][j], 
          values[i] + dp[i + 1][j + weight[i]]
        );
      }
    }
  }
  return dp[0][0];
}
```

### Maximum Length of Repeated Subarray

See Maximum Length of Repeated Subarray in [Linear.md](Linear.md)

### Maximum Length of Repeated Subsequence

See Maximum Length of Repeated Subsequence in [Linear.md](Linear.md)