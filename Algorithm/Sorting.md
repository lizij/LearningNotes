# Sorting Alogrithms

[TOC]


## Time complexity

### Definition

$$O(n)$$：In an algorithm procedure,  the metrics of constant operations

For example, BubbleSort：

From 0 to N - 1, the bigger one will move forward. The time complexity of BubbleSort is

$$O(n-1+n-2+...+1)=O(n^2)$$

### Master Theorem

$$T(n) = aT(\frac{n}{b}) + O(n^d)$$

if $$log^a_b < d$$, $$T(n) = O(n^d)$$

if $$log^a_b = d$$, $$T(n) = O(n^{d}log^n)$$

if $$log^a_b > d$$, $$T(n) = O(n^{log_b^a})$$

More about [Master Theorem](http://www.gocalf.com/blog/algorithm-complexity-and-master-theorem.html)

## Sorting Algorithms

| Name          | Time Complexity | Space Complexiy | Stable      |
| ------------- | --------------- | --------------- | ----------- |
| BubbleSort    | $$O(n^2)$$      | $$O(1)$$        | can be true |
| InsertionSort | $$O(n^2)$$      | $$O(1)$$        | can be true |
| SelectionSort | $$O(n^2)$$      | $$O(1)$$        | false       |
| QuickSort     | $$O(nlogn)$$    | $$O(log^n)$$    | false       |
| MergeSort     | $$O(nlogn)$$    | $$O(n)$$        | true        |
| HeapSort      | $$O(nlogn)$$    | $$O(1)$$        | false       |
| BucketSort    | $$O(n)$$        | $$O(n)$$+       | false       |
| RadixSort     | $$O(n)$$        | $$O(n)$$+       | false       |
| HillSort      | $$O(nlogn)$$    | $$O(1)$$        | false       |

### BubbleSort

In every loop, the last part is sorted. Move the larger ones backwards

```java
void bubbleSort(int[] arr) {
  if (arr == null) return;
  for (int e = arr.length - 1; e > 0; e--) {
    for (int i = 0; i < e; i++) {
      if (arr[i] > arr[i + 1]) swap(arr, i, i + 1);//stable
      // if (arr[i] >= arr[i + 1]) swap(arr, i, i + 1); //not stable
    }
  }
}
```

### InsertionSort

Similiar with BubbleSort, in every loop, the first part is sorted. Move the smaller ones forward

```java
void insertionSort(int[] arr) {
  if (arr == null) return;
  for (int i = 1; i < arr.length; i++) {
    for (int j = i; j >= 0 && arr[j] < arr[j - 1]; j--) {//stable
      //for (int j = i - 1; j >= 0 && arr[j] >= arr[j + 1]; j--) {//not stable
      swap(arr, j, j - 1);
    }
  }
}
```

### SelectionSort

In every loop, find the min one and put it in the right place

```java
void selectionSort(int[] arr) {
  if (arr == null) return;
  for (int i = 0; i < arr.length; i++) {
    int minIndex = i;
    for (int j = i + 1; j < arr.length; j++) {
      minIndex = arr[i] < arr[minIndex] ? i : minIndex;//not stable
    }
    swap(arr, i, minIndex);
  }
}
```
### QuickSort

```java
void quickSort(int[] arr) {
  if (arr == null) return;
  quickSort(arr, 0, arr.length - 1);
}

void quickSort(int[] arr, int lo, int hi) {
  if (lo >= hi) return;
  int[] p = partition(arr, lo, hi);
  //a[lo...p[0] - 1] < a[p[0]] = a[p[0]...p[1]] < a[p[1] + 1 ... hi]
  quicksort(arr, lo, p[0] - 1);
  quicksort(arr, p[1] + 1, hi);
}

int[] partition(int[] arr, int lo, int hi) {
  //swap pivot twice
  int pivot = lo + (int)(Math.random() * (hi - lo + 1));
  swap(arr, pivot, hi);
  int less = lo - 1; // the less ones
  int more = hi; // the bigger ones
  while(lo < more) {
    if (arr[lo] < arr[hi]) swap(arr, ++less, lo++);
    else if (arr[lo] > arr[hi]) swap(arr, --more, lo);
    else lo++;
  }
  swap(arr, more, hi);

  return new int[]{less + 1, more};
}

int[] partition(int[] arr, int lo, int hi) {
  //not swap pivot, only for numbers
  int pivot = arr[lo + (int)(Math.random() * (hi - lo + 1))];
  int small = lo - 1;
  int cur = lo;
  int big = hi + 1;
  while (cur < big) {
    if (arr[cur] < pivot) swap(arr, ++small, cur++);
    else if (arr[cur] > pivot) swap(arr, cur, --big);
    else cur++;
  }
  return new int[]{small + 1, big - 1};
}
```

> Explanation of Quicksort's extra space : In the process of recursion, pivots needs to be stored. Therefore, the least extra space is $$O(logn)$$. And there is no way to make quicksort stable if the extra space is limited in $$O(logn)$$ level.

### MergeSort

```java
void mergeSort(int[] arr) {
  if (arr == null) return;
  mergeSort(arr, 0, arr.length - 1);
}

void mergetSort(int[] arr, int lo, int hi) {
  if (lo == hi) return;
  int mid = lo + ((hi - lo) >> 1);
  mergeSort(arr, lo, mid);
  mergeSort(arr, mid + 1, hi);
  merge(arr, lo, mid, hi);
}

void merge(int[] arr, int lo, int mid, int hi) {
  int[] help = new int[hi - lo + 1];
  int i = 0;
  int p1 = lo;
  int p2 = mid + 1;
  while (p1 <= mid && p2 <= hi) {
    help[i++] = arr[p1] < arr[p2] ? arr[p1++] : arr[p2++];
  }
  while (p1 <= mid) {
    help[i++] = arr[p1++];
  }
  while (p2 <= hi) {
    help[i++] = arr[p2++];
  }
  for (int i = 0; i < help.length; i++) {
    arr[lo + i] = help[i];
  }
}
```

> $$O(1)$$ space complexity :mergesort internal cache

### HeapSort

#### Basic concepts

##### Complete binary tree

A binary tree satisfying one of the conditions:

* Every level is full
* The last level is generated from left to right one by one. Other levels are full

##### Binary tree to array

In a array which represents a binary tree. For the index i:

* Left node: 2 * i + 1
* Right node: 2 * i + 2
* Parent node: (i - 1) / 2

##### Min heap & max heap

The *min heap or max heap* property is: *each node at a level in the tree is less or greater than all of its descendants*

```java
void heapSort(int[] arr) {
  if (arr == null) return;

  //buiding a max heap
  for (int i = 0; i < arr.length; i++) swim(arr, i);

  int size = arr.length;

  //swap the root node with the last one
  //make sure the largest one is in the right border
  swap(arr, 0, --size);
  while(size > 0) {
    //adjust heap
    sink(arr, 0, size);
    swap(arr, 0, --size);
  }
}

void swim(int[] arr, int index) {
  while(arr[index] > arr[(index - 1) / 2]) {
    swap(arr, index, (index - 1) / 2);
    index = (index - 1) / 2;
  }
}

void sink(int[] arr, int index, int size) {
  int left = 2 * index + 1;
  while(left < size) {
    // get the bigger one between arr[left] and arr[left + 1]
    int largest = left + 1 < size && arr[left + 1] > arr[left] ? left + 1 : left;
    // get the bigger one between arr[biggest] and arr[index]
    largest = arr[largest] > arr[index] ? largest : index;
    if (largest == index) break; // don't need to adjust, arr[index] is bigger than or equal to its children
    swap(arr, index, largest);
    index = largest;
    left = index * 2 + 1;
  }
}
```

### BucketSort

BucketSort : *Put every element in its right position while these basic positions already have a order*

```java
void buckertSort(int[] arr) {
  if (arr == null) return;
  int max = Integer.MIN_VALUE;
  for (int n: arr) max = Math.max(max, n);
  int[] bucket = new int[max + 1];
  for (int n: arr) bucket[n]++;
  int i = 0;
  for (int j = 0; j < bucket.length; j++) {
    while(bucket[j]-- > 0) arr[i++] = j;
  }
}
```

### RadixSort

Sort by every digit of a number, from low to high.

RadixSort : An implement of BuckertSort based on counting integer 

```java
// only for no-negative value
void radixSort(int[] arr) {
  if (arr == null || arr.length < 2) {
    return;
  }
  radixSort(arr, 0, arr.length - 1, maxbits(arr));
}

int maxbits(int[] arr) {
  int max = Integer.MIN_VALUE;
  //get the max of an array
  for (int i = 0; i < arr.length; i++) max = Math.max(max, arr[i]);
  int res = 0;
  //count digits
  while (max > 0) {
    res++;
    max /= 10;
  }
  return res;
}

void radixSort(int[] arr, int begin, int end, int digit) {
  final int radix = 10;
  int i = 0, j = 0;
  int[] count = new int[radix];
  int[] bucket = new int[end - begin + 1];
  for (int d = 1; d <= digit; d++) {
    for (i = 0; i < radix; i++) {
      count[i] = 0;
    }
    for (i = begin; i <= end; i++) {
      j = getDigit(arr[i], d);
      count[j]++;
    }
    for (i = 1; i < radix; i++) {
      count[i] = count[i] + count[i - 1];
    }
    for (i = end; i >= begin; i--) {
      j = getDigit(arr[i], d);
      bucket[count[j] - 1] = arr[i];
      count[j]--;
    }
    for (i = begin, j = 0; i <= end; i++, j++) {
      arr[i] = bucket[j];
    }
  }
}

public static int getDigit(int x, int d) {
  return ((x / ((int) Math.pow(10, d - 1))) % 10);
}
```

### HillSort

InsertionSort is one case of HillSort which *step* is 1.

In other words, use InsertionSort *step* by some number larger than 1. However, *step* == 1 is always the final part.

## Tips

### Swap without extra space

regular code

```java
int swap(int[] arr, int a, int b) {
  int c = arr[a];
  arr[a] = arr[b];
  arr[b] = c;
}
```

code with xor(only for different int a and b. If a == b, arr[a] will be 0)

```java
int swap(int[] arr, int a, int b) {
  arr[a] = arr[a] ^ arr[b]; // a = a ^ b
  arr[b] = arr[a] ^ arr[b]; // b = a ^ b ^ b = a
  arr[a] = arr[a] ^ arr[b]; // a = a ^ b ^ a = b
}
```
### Get mid without probability of overflow

regular code (lo + hi may overflow)

```java
int mid = (lo + hi) / 2;
```

special way (hi - lo won't overflow)

```java
int mid = lo + ((hi - lo) >> 1);
```

### A random number from lo to hi

```java
//Math.random(): return a double satisfing [0.0, 1.0)
int pivot = lo + (int)(hi - lo + 1) * Math.random()
```

### Arrays.sort()

In java, a combination of different sorting algorithms is used in Arrays.sort()

For example, if n < 60, InsertionSort is quicker than QuickSort. Because the constant in Master Fomula of Insertion is less than that of QuickSort. In addtion, use InsertionSort is a good way to reduce the stack depth of recursion to prevent stack overflow.

If the input is basic value type such as int and double, QuickSort is used for saving time. However, if the input is the instances of one class, MergeSort is used instead because it's stable.

### Recursion vs non-recursion

Recursion is a simple way to write code. Compared to recursion, non-recursion has its advantages:

* Calling a function itself will use a lot of resources like computing time and memory storage. In most of time, the coder don't need to store so many values. So recursion will waste those resources while non-recursion don't have this problem.
* The system has its recursion depth limit. 

Therefore, in production, using non-recursion is recommended.

## Practice

### Partition odd & even
> Without extra space, is it possbile to partition odd numbers and even numbers, while the relative order of odd numbers or even numbers doesn't change?

This problem is like this: is there a way to make quicksort stable?

If there is a way to partition odd & even and keep them stable, there must be a way to stable quicksort.

There is truly a way to stable quicksort. However, it's very difficult to implement and it needs extra space (in paper:0 & 1 stable sort)

Therefore, the answer to this problem is impossble.

### Recusion to non-recursion

> Are all recursion ways can be changed to non-recursion ways?

Yes. Recursion is only a way to use stack. If the coder manipulates the stack manually, all recursions ways can be changed to no-recursion ways.

### Small sum

> In arr[], for every i in [0, arr.length - 1), the small sum of arr[i] is the sum of all numbers less than arr[i] before i. The small sum of arr[] is the sum of all small sums
>
> Compute the small sum of arr[]

Traditional way: traverse all arr[i] and find all smaller numbers before i. $$O(n^2)$$

MergeSort-like way:

In the process of merge(), for every number in the left part, find how many numbers larger than it. Then sum them all.

For example:

```
[4, 3, 6, 5]
merge [4], [3]: 
	4:no number larger than 4 in [3]
merge [6], [5]: 
	6:no number larger than 6 in [5]
merge [3, 4], [5, 6]: 
	3:2 numbers larger than 3 in [5, 6]
	4:2 numbers larger than 4 in [5, 6]
The sum of less sum is 2 * 3 + 2 * 4 = 14
```

```java
int smallSum(int[] arr) {
  if (arr == null) return 0;
  return mergeSort(arr, 0, arr.length - 1);
}

int mergeSort(int[] arr, int lo, int hi) {
  if (lo >= hi) return 0;
  int mid = lo + ((hi - lo) >> 1);
  return mergeSort(arr, lo, mid) + mergeSort(mid + 1, hi) + merge(arr, lo, mid, hi);
}

int merge(int[] arr, int lo, int mid, int hi) {
  int[] help = new int[hi - lo + 1];
  int i = 0, res = 0;
  int p1 = lo;
  int p2 = mid + 1;
  while (p1 <= mid && p2 <= hi) {
    // [p2...hi] is sorted, so if arr[p1] < arr[p2] there will be hi - p2 + 1 of numbers larger than arr[p1]
    res += arr[p1] < arr[p2] ? (hi - p2 + 1) * arr[p1] : 0;
    help[i++] = arr[p1] < arr[p2] ? arr[p1++] : arr[p2++];
  }
  while (p1 <= mid) {
    help[i++] = arr[p1++];
  }
  while (p2 <= hi) {
    help[i++] = arr[p2++];
  }
  for (int i = 0; i < help.length; i++) {
    arr[lo + i] = help[i];
  }
  return res;
}
```

### Reverse order pair

> find the amount of all pairs which satisfying:
>
> for i and j, if i < j and arr[i] > arr[j], (i, j) is a reverse order pair

Similar with small sum.

### Max gap 

> In an inordered integer array, compute the max gap of two numbers in a sorted version of this array.
>
> Time complexiy: $$\le O(n)$$ Space complexiy: $$\le O(n)$$
>
> eg. [8, 1, 7, 4, 2], the sorted version is [1, 2, 4, 7, 8], the max gap is 3 (between 4 and 7)

If the length of the array is *n*, prepare *n+1* buckets (double)

Find the *max* and the *min*, then seperate the array into *n+1* fields with $$\frac{max-min}{n}$$

Put every number in their buckets. Then the greatest difference is the maximum of every non-empty neighbored buckets' difference. This difference is equal to this bucket's min minus the last bucket's max.

```java
int maxGap(int[] nums) {
  if (nums == null) return;
  int n = nums.length;
  int min = Integer.MAX_VALUE;
  int max = Integer.MIN_VALUE;
  for (int num: nums) {
    min = Math.min(min, num);
    max = Math.max(max, num);
  }
  if (min == max) return 0;
  boolean hasNum = new boolean[n + 1];
  int[] maxs = new int[n + 1];
  int[] mins = new int[n + 1];
  int bid = 0;
  for (int num: nums) {
    bid = bucket(num, n, min, max);
    mins[bid] = hasNum[bid] ? Math.min(mins[bid], num) : num;
    maxs[bid] = hasNum[bid] ? Math.max(maxs[bid], num) : num;
    hasNum[bid] = true;
  }
  int res = 0;
  int lastMax = maxs[0];
  for (int i = 1; i <= n; i++) {
    if (hasNum[i]) {
      res = Math.max(res, mins[i] - lastMax);
      lastMax = maxs[i];
    }
  }
  return res;
}
int bucket(int num, int len, int min, int max) {
  return (int)(num - min) * len / (max - min);    
}
```

### BestArrange

```java
class Program {
  int start;
  int end;

  Program(int start, int end) {
    this.start = start;
    this.end = end;
  }
}
```

> Every program has its start time and end time
>
> Arrange programs from a given start time as many as possible 

Greedy solution: sort the array by the end time of a program

```java
int bestArrange(Program[] programs, int start) {
  Arrays.sort(programs, o -> o.end);
  int result = 0;
  for (int i = 0; i < programs.length; i++) {
    if (start <= programs[i].start) {
      result++;
      start = programs[i].end;
    }
  }
  return result;
}
```

