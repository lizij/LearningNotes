

# Linear Data Structure

[TOC]

## Stack

Last in first out.

## Queue

First in first out.

## HashMap

### Hash()

1. One-to-one mapping: Try to make sure one-to-one mapping between the input and the output.
2. Conflict: If the number of input is gigantic while the output is limited, there will be conflicts in output.
3. Discrete: Try to make sure the output is evenly distributed

### Consistent hashing

If the output of `hash()` is evenly distributed, `hash() % m` is also evenly distributed.

However, if m becomes n (n > m), the previous output of `% m` is not valid anymore. We need to compute all again.

For example, we use `hash()` to distribute some data evenly in 3 computers. If we need to add 1 computer, to make sure the data distributed evenly we need to compute all again. It will cause a heavy computing burden on computers. Consistent hashing is a good way to solve this problem:

1. Divide the computer nodes into many virtual nodes.

2. Use the same `hash()` to map virtual nodes and data in a same hashing circle

  ![](http://img.blog.csdn.net/20140411001433375?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3l3b3Nw/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

More info about [Consistent hashing](http://blog.csdn.net/cywosp/article/details/23397179)

### Bloom filter

Give a list A which contains 10 billion samples(64B size), and a list B contains some test cases.

Return true if a given case in B is existed in A.

Because A is too large, loading all data of A (about 640GB space) and build a HashMap is infeasible.

Use Bloom filter can reduce the required space remarkably(about 20GB space). In a result, it may return true when the given case is not in A.

![](BloomFilter.png)

## Binary Search

Usage:

* find a value in a sorted array
* find the first or the last value in a sorted array
* find the first one larger or smaller than a value in a sorted array 

## BFPRT

Find the kth smallest number of an array

### Traditional way

Sort the array and get the kth number: $$O(nlogn)$$

### Classic quick-select way

Average time complexity: $$O(n)$$

```java
int quickselect(int[] arr, int lo, int hi, int k) {
  if (lo >= hi) return lo;
  int p = partition(arr, lo, hi, k);//random-pick partition
  if (p == k) return arr[p];
  else if (p > k) return quickselect(arr, lo, p - 1, k);
  else return quickselect(arr, p + 1, hi, k);
}

int quick3select(int[] arr, int lo, int hi, int k) {
  if (lo >= hi) return lo;
  int[] p = partition(arr, lo, hi, k);//random-pick partition
  if (k >= p[0] && k <= p[1]) return arr[k];
  else if (k < p[0]) return quick3select(arr, lo, p[0] - 1, k);
  else return quick3select(arr, p[1] + 1, hi, k);
}
```

Because pivot is randomly picked, partition can't promise to separate the array into 3 similar scale parts. One of the smaller ones' or the larger ones' recursion depth may be too large. Therefore, the time complexity can't be stable and will become very large.

### BFPRT pivot

Separate the array into 5 elements a group. Sort every groups and get medians. Then get the median *M* of medians. If the group is less than 5, get the frontier one (get 2th if 4 in a group).

Because *M* is the median of medians, for every *i* < *M* and *i* in medians, there should be at average two numbers smaller thant *i* in *i*'s 5 group. The amount of groups is *N/5*. Therefore there are *N/10* numbers smaller than M in medians and at least *3N/10* numbers smaller than *M* in the array.  Similarly, there are also *3N/10* numbers larger than M in the array. Using M as the partition's pivot, the array will be divided into a 3 similar scale part.

```java
int select(int[] arr, int lo, int hi, int k) {
  //same as quick3select()
  if (lo >= hi) return arr[lo];
  int[] p = partition(arr, lo, hi);
  if (k >= p[0] && k <= p[1]) return arr[k];
  else if (k < p[0]) return select(arr, lo, p[0] - 1, k);
  else return select(arr, p[1] + 1, hi, k)
}

int[] partition(int[] arr, int lo, int hi) {
  int pivot = medianOfMedians(arr, lo, hi);
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

// start of BFPRT part
int medianOfMedians(int[] arr, lo, hi) {
  int num = hi - lo + 1;
  int offset = num % 5 == 0 ? 0 : 1;
  int[] mArr = new int[num / 5 + offset];
  for (int i = 0; i < mArr.length; i++) {
    int begin = lo + i * 5;
    mArr[i] = getMedian(arr, begin, Math.end(hi, begin + 4));
  }
  return select(mArr, 0, mArr.length - 1, mArr.length / 2);
}

int getMedian(int[] arr, int lo, int hi) {
  insertionSort(arr, lo, hi);
  int sum = lo + hi;
  int mid = (sum / 2) + (sum % 2);
  return arr[mid];
}

void insertionSort(int[] arr, int lo, int hi) {
  for (int i = lo + 1; i < hi; i++) {
    for (int j = i; j > lo && arr[j - 1] > arr[j]; j--) {
      swap(arr, j - 1, j);
    }
  }
}
```

## Slidingwindow

A window from left to right (left <= right) containing some elements in an array. Most of time the size is fixed and left & right move simultaneously

## Tips

### Coordinate compression

In some cases, we need to get the sorted indexes of numbers distributed in a very large scale.

eg. We have 100 nubmers in [1, 10^8], which are unsorted and not necessarily distinct. And we need to map them as adjacent numbers so we can get the sorted indexes of them.

```java
// Coordinate Compression
Set<Integer> coords = new HashSet<>(); // coordinates without duplicates
for (int num: nums) {
  coords.add(num);
}

// sort coordinates
List<Integer> sortedCoords = new ArrayList<>(coords);
sortedCoords.sort((o1, o2) -> o1 - o2);

// map coordinates to adjacent integers
Map<Integer, Integer> index = new HashMap<>();
int t = 0;
for (int coord: sortedCoords) {
  index.put(coord, t++);
}
```

### Simple way to print an array

```java
// in Java
int[] arr = {1, 2, 3}
System.out.println(Arrays.toString(arr));
// output: [1, 2, 3]
```

### Integer.equals vs "=="

use Integer.equals() instead of "=="

```java
new Integer(1234567) == new Integer(1234567) //false
1234567 == 1234567 //false
new Integer(1234567).equals(new Integer(1234567)) //true
new Integer(123) == new Integer(123) //true
123 == 123 //true
new Integer(123).equals(new Integer(123)) //true
```

This is because in class Integer.IntegerCache

```java
public static Integer valueOf(int i) {
  if (i >= IntegerCache.low && i <= IntegerCache.high) //low = -128, hi = 128 by default
    return IntegerCache.cache[i + (-IntegerCache.low)];
  return new Integer(i);
}
```

### Remove entry in iterator

```java
for (Entry<> entry: map.entrySet()) {
  map.remove(entry.getKey());
}
```

This operation will result in that the iterator can't find next element and throw an exception.

The solution is to remember keys that need to be removed then delete them after iteration. 

### ArrayList vs LinkedList

ArrayList is based on a variable array. Its get() is fast while its add() and remove() is slow

LinkedList is based on a doubly-linked list. Its add() and remove() is fast while its get() is slow. 

## Practice

### Array to Stack and Queue

> Implement a stack or queue with an array

Stack

```java
class ArrayStack {
  Integer[] arr;
  Integer size;

  ArrayStack(int initSize) {
    if (initSize < 0) {
      throw new IllegalArgumentException("The init size is less than 0");
    }
    arr = new Integer[initSize];
    size = 0;
  }

  Integer peek() {
    if (size == 0) {
      return null;
    }
    return arr[size - 1];
  }

  void push(int obj) {
    if (size == arr.length) {
      throw new ArrayIndexOutOfBoundsException("The queue is full");
    }
    arr[size++] = obj;
  }

  Integer pop() {
    if (size == 0) {
      throw new ArrayIndexOutOfBoundsException("The queue is empty");
    }
    return arr[--size];
  }
}
```

Queue

```java
class ArrayQueue {
  Integer[] arr;
  Integer size;
  Integer first;
  Integer last;

  ArrayQueue(int initSize) {
    if (initSize < 0) {
      throw new IllegalArgumentException("The init size is less than 0");
    }
    arr = new Integer[initSize];
    size = 0;
    first = 0;
    last = 0;
  }

  Integer peek() {
    if (size == 0) {
      return null;
    }
    return arr[first];
  }

  void push(int obj) {
    if (size == arr.length) {
      throw new ArrayIndexOutOfBoundsException("The queue is full");
    }
    size++;
    arr[last] = obj;
    last = last == arr.length - 1 ? 0 : last + 1;
  }

  Integer poll() {
    if (size == 0) {
      throw new ArrayIndexOutOfBoundsException("The queue is empty");
    }
    size--;
    int tmp = first;
    first = first == arr.length - 1 ? 0 : first + 1;
    return arr[tmp];
  }
}
```

### GetMinStack

> Implement a stack with getMin()
>
> 1. pop(), push() and getMin() should be in $$O(1)$$
> 2. Stack<> that prebuilt in Java is available 

Use an extra min stack to remember all new mins in order. When in pop(), if the pop data is equal to the min stack's peek, pop it too. 

```java
class GetMinStack {
  Stack<Integer> data;
  Stack<Integer> min;
  GetMinStack() {
    data = new Stack<>();
    min = new Stack<>();
  }

  void push(int num) {
    if (min.isEmpty() || num <= getmin()) {
      min.push(num);
    }
    data.push(num);
  }

  void pop() {
    if (data.isEmpty()) throw new RuntimeException("Empty stack");
    int value = data.pop();
    if (value == getmin()) min.pop();
    return value;
  }

  int getmin() {
    // return min stack peek
    if (min.isEmpty()) throw new RuntimeException("Empty stack");
    return min.peek();
  }
}
```

Similiar with the last one, the difference is to remember every min for every pushed data

```java
class GetMinStack {
  Stack<Integer> data;
  Stack<Integer> min;
  GetMinStack() {
    data = new Stack<>();
    min = new Stack<>();
  }

  void push(int num) {
    if (min.isEmpty() || num <= getMin()) {
      min.push(num);
    } else {
      int newMin = min.peek();
      min.push(newMin);
    }
    data.push(num);
  }

  void pop() {
    if (data.isEmpty()) throw new RuntimeException("Empty stack");
    min.pop();
    return data.pop();
  }

  int getMin() {
    if (min.isEmpty()) throw new RuntimeException("Empty stack");
    return min.peek();
  }
}
```

### Stack and Queue convert

> Stack and Queue convert

Stack -> Queue

```java
class TwoStacksQueue {
  Stack<Integer> stackPush;
  Stack<Integer> stackPop;

  TwoStacksQueue() {
    stackPush = new Stack<Integer>();
    stackPop = new Stack<Integer>();
  }

  void push(int pushInt) {
    stackPush.push(pushInt);
  }

  int poll() {
    if (stackPop.empty() && stackPush.empty()) {
      throw new RuntimeException("Queue is empty!");
    } else if (stackPop.empty()) {
      while (!stackPush.empty()) {
        stackPop.push(stackPush.pop());
      }
    }
    return stackPop.pop();
  }

  int peek() {
    if (stackPop.empty() && stackPush.empty()) {
      throw new RuntimeException("Queue is empty!");
    } else if (stackPop.empty()) {
      while (!stackPush.empty()) {
        stackPop.push(stackPush.pop());
      }
    }
    return stackPop.peek();
  }
}
```

Queue -> Stack

```java
class TwoQueuesStack {
  Queue<Integer> queue;
  Queue<Integer> help;

  TwoQueuesStack() {
    queue = new LinkedList<Integer>();
    help = new LinkedList<Integer>();
  }

  void push(int pushInt) {
    queue.add(pushInt);
  }

  int peek() {
    if (queue.isEmpty()) {
      throw new RuntimeException("Stack is empty!");
    }
    while (queue.size() != 1) {
      help.add(queue.poll());
    }
    int res = queue.poll();
    help.add(res);
    swap();
    return res;
  }

  int pop() {
    if (queue.isEmpty()) {
      throw new RuntimeException("Stack is empty!");
    }
    while (queue.size() != 1) {
      help.add(queue.poll());
    }
    int res = queue.poll();
    swap();
    return res;
  }

  void swap() {
    Queue<Integer> tmp = help;
    help = queue;
    queue = tmp;
  }

}
```

### DogCatQueue

```java
class Pet {
  String type;

  Pet(String type) {
    this.type = type;
  }

  String getPetType() {
    return this.type;
  }
}

class Dog extends Pet {
  Dog() {
    super("dog");
  }
}

class Cat extends Pet {
  Cat() {
    super("cat");
  }
}
```

> Implement DogCatQueue:
>
> 1. add(): add an instance of Cat or Dog;
> 2. pollAll(): poll a Cat and Dog by FIFO;
> 3. pollCat(): poll a Cat by FIFO;
> 4. pollDog(): poll a Dog by FIFO;
> 5. isEmpty(): return true if there is any Cat or Dog, otherwise false;
> 6. isCatEmpty(): return true if there is any Cat, otherwise false;
> 7. isDogEmpty(): return true if there is any Dog, otherwise false;
>
> Do not modify class Pet, Cat and Dog

Define a class called PetEnterQueue, mark every Pet added in queue.


```java
class PetEnterQueue {
  Pet pet;
  long count;

  PetEnterQueue(Pet pet, long count) {
    this.pet = pet;
    this.count = count;
  }

  Pet getPet() {
    return this.pet;
  }

  long getCount() {
    return this.count;
  }

  String getEnterPetType() {
    return this.pet.getPetType();
  }
}
```

Because a new class is defined. Different PetEnterQueue's pet can point to the same Pet. If we use HashMap or something else to mark Pet, we won't have this feature.

eg. Assume that we need a queue to record the feeding order of cats and dogs. Feed dog1, cat1, dog2, dog3, cat2, then feed dog1 and dog3 again. PetEnterQueue class is suitable for this case.

Then define a queue for Dog and a queue for Cat.

```java
class DogCatQueue {
  Queue<PetEnterQueue> dogQ;
  Queue<PetEnterQueue> catQ;
  long count;

  DogCatQueue() {
    this.dogQ = new LinkedList<PetEnterQueue>();
    this.catQ = new LinkedList<PetEnterQueue>();
    this.count = 0;
  }

  void add(Pet pet) {
    if (pet.getPetType().equals("dog")) {
      this.dogQ.add(new PetEnterQueue(pet, this.count++));
    } else if (pet.getPetType().equals("cat")) {
      this.catQ.add(new PetEnterQueue(pet, this.count++));
    } else {
      throw new RuntimeException("err, not dog or cat");
    }
  }

  Pet pollAll() {
    if (!this.dogQ.isEmpty() && !this.catQ.isEmpty()) {
      if (this.dogQ.peek().getCount() < this.catQ.peek().getCount()) {
        return this.dogQ.poll().getPet();
      } else {
        return this.catQ.poll().getPet();
      }
    } else if (!this.dogQ.isEmpty()) {
      return this.dogQ.poll().getPet();
    } else if (!this.catQ.isEmpty()) {
      return this.catQ.poll().getPet();
    } else {
      throw new RuntimeException("err, queue is empty!");
    }
  }

  Dog pollDog() {
    if (!this.isDogQueueEmpty()) {
      return (Dog) this.dogQ.poll().getPet();
    } else {
      throw new RuntimeException("Dog queue is empty!");
    }
  }

  Cat pollCat() {
    if (!this.isCatQueueEmpty()) {
      return (Cat) this.catQ.poll().getPet();
    } else
      throw new RuntimeException("Cat queue is empty!");
  }

  boolean isEmpty() {
    return this.dogQ.isEmpty() && this.catQ.isEmpty();
  }

  boolean isDogQueueEmpty() {
    return this.dogQ.isEmpty();
  }

  boolean isCatQueueEmpty() {
    return this.catQ.isEmpty();
  }

}
```

### RandomPool

> Implement a data structure which support:
>
> insert(key): add an unexisted key
>
> delete(key): remove an existed key
>
> getRandom(): return an existed key randomly
>
> All operations should be done in $$O(1)$$ time

```java
class Pool<K> {
  HashMap<K, Integer> keyIndexMap;
  HashMap<Integer, K> indexKeyMap;
  int size;

  Pool() {
    this.keyIndexMap = new HashMap<K, Integer>();
    this.indexKeyMap = new HashMap<Integer, K>();
    this.size = 0;
  }

  void insert(K key) {
    if (!this.keyIndexMap.containsKey(key)) {
      this.keyIndexMap.put(key, this.size);
      this.indexKeyMap.put(this.size++, key);
    }
  }

  void delete(K key) {
    if (this.keyIndexMap.containsKey(key)) {
      int deleteIndex = this.keyIndexMap.get(key);
      int lastIndex = --this.size;
      K lastKey = this.indexKeyMap.get(lastIndex);
      this.keyIndexMap.put(lastKey, deleteIndex);
      this.indexKeyMap.put(deleteIndex, lastKey);
      this.keyIndexMap.remove(key);
      this.indexKeyMap.remove(lastIndex);
    }
  }

  K getRandom() {
    if (this.size == 0) {
      return null;
    }
    int randomIndex = (int) (Math.random() * this.size);
    return this.indexKeyMap.get(randomIndex);
  }

}
```

### PrintMatrixSpiralOrder

> Print a matrix by spiral order
>
>  1   2   3   4
>
>  5   6   7   8
>
>  9 10 11 12
>
> print 1 2 3 4 8 12 11 10 9 5 6 7 8

```java
void main(int[][] matrix) {
  int tR = 0;
  int tC = 0;
  int dR = matrix.length - 1;
  int dC = matrix[0].length - 1;
  while (tR <= dR && tC <= dC) {
    printEdge(matrix, tR++, tC++, dR--, dC--);
  }	
}

void print(int[][] matrix, int tR, int tC, int dR, int dC) {
  //print a square determined by (tr, tc) in the topleft and (dr, dc) in the bottomright
  if (tR == dR) { 
    // only one row
    for (int i = tC; i <= dC; i++) System.out.print(m[tR][i] + " ");
  } else if (tC == dC) { 
    // only one column
    for (int i = tR; i <= dR; i++) System.out.print(m[i][tC] + " ");
  } else {
    // other cases
    int curC = tC;
    int curR = tR;
    while (curC != dC) { // left to right in tr row
      System.out.print(m[tR][curC++] + " ");
    }
    while (curR != dR) { // up to down in dc column
      System.out.print(m[curR++][dC] + " ");
      curR++;
    }
    while (curC != tC) { // right to left in dr row
      System.out.print(m[dR][curC--] + " ");
    }
    while (curR != tR) { // down to up in tc column
      System.out.print(m[curR--][tC] + " ");
    }
  }
}
```

If the question is to rotate the matrix clockwisely. Use the same main function and the rotate-matrix function. 

### ZigZagPrintMatrix

> Print a matrix by spiral order
>
>  1   2   3   4
>
>  5   6   7   8
>
>  9 10 11 12
>
> print 1 2 5 9 6 3 4 7 10 11 8 12

Similiar with PrintMatrixSpiralOrder

From the table, for (tr, tc), tc increases in every loop until tc == 3, then tr increases. Similiar with (tc, tc), for (dr, dc), dr increases until dr == 2 , then dc increases

| Round | tr, tc | dr, dc |
| :---- | :----- | :----- |
| 1     | 0, 0   | 0, 0   |
| 2     | 0, 1   | 1, 0   |
| 3     | 0, 2   | 2, 0   |
| 4     | 0, 3   | 2, 1   |
| 5     | 1, 3   | 2, 2   |
| 6     | 2, 3   | 2, 3   |

```java
void main(int[][] matrix) {
  int tr = 0, tc = 0, dr = 0, dc = 0, endr = matrix.length - 1, endc = matrix[0].length - 1;
  boolean fromUp = false;
  while(tr <= endr) {
    print(matrix, tr, tc, dr, dc, fromUp);
    // increase tc or tr
    tr = tc == endc ? tr + 1 : tr;
    tc = tc == endc ? tc : tc + 1;

    // increase dr or dc
    dc = dr == endr ? dc + 1 : dc;
    dr = dr == endr ? dr : dr + 1;
    fromUp = !fromUp; // reverse direction
  }
}

void print(int[] matrix, int tr, int tc, int dr, int dc, boolean fromUp) {
  //print the line from (tr, tc) to (dr, dc) if fromUp, else print reversely
  if (fromUp) {
    while (tr != dr + 1) {
      System.out.print(m[tr++][tc--] + " ");
    }
  } else {
    while (dr != tr - 1) {
      System.out.print(m[dr--][dc++] + " ");
    }
  }
}
```

### FindNumInSortedMatrix

> Find K in a matrix in $$O(n + m)$$ time with $$O(1)$$ extra space which every row or column is sorted.
>
> For example,
>
> 0 1 2 5
>
> 2 3 4 7
>
> 4 4 4 8
>
> 5 7 7 9
>
> Return true if K == 7 or false if K == 6

For (i, j) in matrix

1. matrix(i - 1, j) < matrix(i, j) < matrix(i + 1, j)
2. matrix(i, j - 1) < matrix(i, j) < matrix(i, j + 1)

Therefore use one of these conclusions to search:

1. From the top right point, matrix(i - 1, j) < matrix(i, j) < matrix(i, j + 1) => i-- or j++
2. From the bottom left point, matrix(i, j - 1) < matrix(i, j) < matrix(i + 1, j) => j-- or i++

```java
boolean isContains(int[][] matrix, int K) {
  int row = 0, col = matrix[0].length - 1;
  while(row < matrix.length && col > -1) {
    if (matrix[row][col] == K) return true;
    // matrix[row - 1][col] < matrix[row][col] < matrix[row][col + 1];
    else if (matrix[row][col] > K) col--;
    else row++;
  }
}
```

### PrintCommonPart

> Print common part of two sorted linked list

```java
if (h1.value < h2.value) h1 = h1.next;
else if (h1.value > h2.value) h2 = h2.next;
else { // h1.value == h2.value
  System.out.println(h1.value);
  h1 = h1.next;
  h2 = h2.next;
}
```

### IsPalindromeList

> Determine if a linked list is palindrome

Classic stack solution: push the list in stack and return true if the list is equal to the reverse version in the stack. Need $$O(n)$$ extra space

```java
boolean isPalindrome(Node head) {
  Stack<Node> stack = new Stack<Node>();
  Node cur = head;
  while (cur != null) {
    stack.push(cur);
    cur = cur.next;
  }
  while (head != null) {
    if (head.value != stack.pop().value) return false;
    head = head.next;
  }
  return true;
}
```

Better solution with stack: use slow-fast pointers to find the center of the list. Then push the right part in stack and return true if left part is equal to the right part. Need $$O(n / 2)$$ extra space.

```java
boolean isPalindrome(Node head) {
  if (head == null || head.next == null) return true;
  Node slow = head.next;
  Node fast = head;
  while (fast.next != null && fast.next.next != null) {
    slow = slow.next;
    fast = fast.next.next;
  }
  // the length of the list is 2n or 2n + 1, slow will be n + 1, fast will be 2n
  Stack<Node> stack = new Stack<Node>();
  while (slow != null) {
    stack.push(slow);
    slow = slow.next;
  }
  while (!stack.isEmpty()) {
    if (head.value != stack.pop().value) return false;
    head = head.next;
  }
  return true;
}
```

$$O(1)$$ extra space solution: use slow-fast pointers to find the center of the list. Then reverse the right part and compare two parts. Finally, restore the right part.

```java
boolean isPalindrome(Node head) {
  if (head == null || head.next == null) return true;
  Node n1 = head;
  Node n2 = head;
  while (n2.next != null && n2.next.next != null) { // find mid node
    n1 = n1.next; // n1 -> mid
    n2 = n2.next.next; // n2 -> end
  }
  n2 = n1.next; // n2 -> right part first node
  n1.next = null; // mid.next -> null
  Node n3 = null;
  while (n2 != null) { // right part convert
    n3 = n2.next; // n3 -> save next node
    n2.next = n1; // next of right node convert
    n1 = n2; // n1 move
    n2 = n3; // n2 move
  }
  n3 = n1; // n3 -> save last node
  n2 = head;// n2 -> left first node
  boolean res = true;
  while (n1 != null && n2 != null) { // check palindrome
    if (n1.value != n2.value) {
      res = false;
      break;
    }
    n1 = n1.next; // left to mid
    n2 = n2.next; // right to mid
  }
  n1 = n3.next;
  n3.next = null;
  while (n1 != null) { // recover list
    n2 = n1.next;
    n1.next = n3;
    n3 = n1;
    n1 = n2;
  }
  return res;
}
```

### SmallerEqualBigger

> Partition the linked list by given pivot
>
> eg. 9-0-4-5-1, pivot = 3  ==> 1-0-4-9-5

Use 3 queue: traverse the list and enqueue the node in smaller, equal and bigger queue. Then  concatenate them all. If use Queue\<T\>, the extra space is $$O(n)$$. However if only mark the head and tail of the queue, the extra space can be reduced to $$O(1)$$

```java
Node listPartition2(Node head, int pivot) {
  Node sH = null; // small head
  Node sT = null; // small tail
  Node eH = null; // equal head
  Node eT = null; // equal tail
  Node bH = null; // big head
  Node bT = null; // big tail
  Node next = null; // save next node
  // every node distributed to three lists
  while (head != null) {
    next = head.next;
    head.next = null;
    if (head.value < pivot) {
      if (sH == null) {
        sH = head;
        sT = head;
      } else {
        sT.next = head;
        sT = head;
      }
    } else if (head.value == pivot) {
      if (eH == null) {
        eH = head;
        eT = head;
      } else {
        eT.next = head;
        eT = head;
      }
    } else {
      if (bH == null) {
        bH = head;
        bT = head;
      } else {
        bT.next = head;
        bT = head;
      }
    }
    head = next;
  }
  // small and equal reconnect
  if (sT != null) {
    sT.next = eH;
    eT = eT == null ? sT : eT;
  }
  // all reconnect
  if (eT != null) eT.next = bH;
  return sH != null ? sH : eH != null ? eH : bH;
}
```

Or converse the list to array then partition. 

### CopyListWithRandom

```java
class Node {
  public int value;
  public Node next;
  public Node rand;

  public Node(int data) {
    this.value = data;
  }
}
```

> Copy the node list with the structure like this.
>
> Node.rand will point to a random node in the list or null

1. Use HashMap: traverse and put(node, node.clone()). Then traverse the hashmap and replicate every key's rand.

```java
Node cur = head;
while(cur != null) {
  map.put(cur, new Node(cur.value));
  cur = cur.next;
}
cur = head;
while(cur != null) {
  map.get(cur).next = map.get(cur.next);
  map.get(cur).rand = map.get(cur.rand);
  cur = cur.next;
}
return map.get(head);
```

2. First, copy the list like this: 1->2->3 ==> 1->1'->2->2'->3->3'

Then traverse this list, evert time we can get i->i'. If i.rand == j, i'.rand = j' == j.next == i.rand.next

```java
Node copyListWithRand(Node head) {
  if (head == null) return null;
  Node cur = head;
  Node next = null;
  // copy node and link to every node
  while (cur != null) {
    // create i -> i'
    next = cur.next;
    cur.next = new Node(cur.value);
    cur.next.next = next;
    cur = next;
  }
  cur = head;
  Node curCopy = null;
  // set copy node rand
  while (cur != null) {
    next = cur.next.next;
    curCopy = cur.next;
    curCopy.rand = cur.rand != null ? cur.rand.next : null;
    cur = next;
  }
  Node res = head.next;
  cur = head;
  // split
  while (cur != null) {
    next = cur.next.next;
    curCopy = cur.next;
    cur.next = next;
    curCopy.next = next != null ? next.next : null;
    cur = next;
  }
  return res;
}
```

### FindFirstIntersectNode

> Give head nodes of 2 single lists(may have loop). The 2 lists may insertect.
>
> Find the first intersect node of the 2 lists if they intersects, otherwise return null

1. Determine whether the list has a circle. If there is a circle,  find the entry node.

   * Use hashmap to record every node to determine the loop and its entry point
   * Use slow-fast pointers, if there is a loop, the slow and the fast pointers will meet. The fast's distance is double of the slow's. After that the fast start from the head 1 step every time. Finally the slow and the fast will meet at the start of the loop

   > Proof: Assume that the slow passed x nodes and the fast passed 2x nodes when they first met. When they met first time, the fast has already passed the circle one time and the slow just came in. So 2x is consisted of 3 parts: a(distance from head to the entry), b(distance from entry to the meet point), p(the perimeter of the circle) while x = a + b. Then we get x = p, which means the slow's distance is the perimeter of the circle.
   >
   > Then the fast started from head again 1 step every time. When the fast passed a nodes, it just came on the entry point of the circle and the slow passed a nodes too. Because the slow has passed b nodes from the entry point since they first met and a + b == x == p. Therefore the slow would came on the entry point too and they met again.

   More about [Circle detection](https://en.wikipedia.org/wiki/Cycle_detection#Floyd.27s_Tortoise_and_Hare)

2. If 2 lists both don't have loops:

   * If they intersect, they will seem like "Y". So the last of their nodes is the same.
   * If they don't intersect, they will seem like "| |".

3. If one of 2 lists have a loops, they won't intersect. They seem like "6 |"

4. If 2 lists both have loops:

   * They will seem like:

```
 Y         \   /        |   |
 |    OR    \ /    OR   |   | 
 O           O          O   O
(1)         (2)          (3)
If the start of 2 loops is the same, the case is (1). This is like "Y" case
If not, from the start of loop1 to find loop2's start, if find it, this case is (2), otherwise (3)
```



```java
Node getIntersectNode(Node head1, Node head2) {
  if (head1 == null || head2 == null) {
    return null;
  }
  Node loop1 = getLoopNode(head1);
  Node loop2 = getLoopNode(head2);
  if (loop1 == null && loop2 == null) {
    return noLoop(head1, head2, null);
  }
  if (loop1 != null && loop2 != null) {
    return bothLoop(head1, loop1, head2, loop2);
  }
  return null;
}

public static Node getLoopNode(Node head) {
  // return the circle entry if there is one
  if (head == null || head.next == null || head.next.next == null) {
    return null;
  }
  Node n1 = head.next; // n1 -> slow
  Node n2 = head.next.next; // n2 -> fast
  while (n1 != n2) {
    if (n2.next == null || n2.next.next == null) {
      return null;
    }
    n2 = n2.next.next;
    n1 = n1.next;
  }
  n2 = head; // n2 -> walk again from head
  while (n1 != n2) {
    n1 = n1.next;
    n2 = n2.next;
  }
  return n1;
}

Node noLoop(Node head1, Node head2, Node end) {
  // from head1 and head2 to find the same end, if found return the first intersect node
  if (head1 == null || head2 == null) {
    return null;
  }
  Node cur1 = head1; // list1
  Node cur2 = head2; // list2
  int n = 0;
  while (cur1.next != end) {
    // find list1's end and count list1's length
    n++;
    cur1 = cur1.next;
  }
  while (cur2.next != end) {
    // find list2's end and count list2's length
    n--;
    cur2 = cur2.next;
  }
  if (cur1 != cur2) {
    // no same end -> no intersect node
    return null;
  }
  cur1 = n > 0 ? head1 : head2; // cur1 = the longer list head
  cur2 = cur1 == head1 ? head2 : head1; // cur2 = the other
  n = Math.abs(n); // get the diff
  // find the intersect
  while (n != 0) {
    n--;
    cur1 = cur1.next;
  }
  while (cur1 != cur2) {
    cur1 = cur1.next;
    cur2 = cur2.next;
  }
  return cur1;
}

Node bothLoop(Node head1, Node loop1, Node head2, Node loop2) {
  Node cur1 = null;
  Node cur2 = null;
  if (loop1 == loop2) {
    return noLoop(head1, head2, loop1);
  } else {
    cur1 = loop1.next;
    while (cur1 != loop1) {
      if (cur1 == loop2) {
        return loop1;
      }
      cur1 = cur1.next;
    }
    return null;
  }
}
```

### ReverseList

> Reverse a node list

```java
Node reverseList(Node head) {
  Node pre = null;
  Node next = null;
  while (head != null) {
    next = head.next;
    head.next = pre;
    pre = head;
    head = next;
  }
  return pre;
}
```

### FindOneLessValueIndex

> Find a index i of less-value in an array A[] satisfying A[i-1] > A[i] < A[i+1] (including A[0] < A[1] and A[N-2] > A[N-1]). Each number in A[] is different from each other.

Judge if A[0] or A[N-1] is less-value. If A[0] and A[N-1] are both not less-value, use BinarySearch to search from 1 to N-2

```java
int getLessIndex(int[] arr) {
  if (arr == null || arr.length == 0) return;
  int N = arr.length;
  if (N == 1 || arr[0] < arr[1]) return 0;
  if (arr[N - 1] < arr[N - 2]) return N - 1;
  int lo = 1, hi = N - 2;
  while (lo < hi) {
    int mid = lo + (hi - lo) / 2;
    if (arr[mid] > arr[mid - 1]) hi = mid - 1;
    else if (arr[mid] > arr[mid + 1]) lo = mid + 1;
    else return mid;
  }
  return lo;
}
```

### WaterProblem

> Give a array which represents a container. Each value is the height of a block. Compute the max water the container can hold.
>
> eg. 
>
> ![](http://www.leetcode.com/static/images/problemset/rainwatertrap.png)

For each pos i, the max water blocks in i is min( max(arr[0...(i-1)]) , max(arr[(i+1)...(n-1)]) ) - arr[i]

brute force solution $$O(n^2)$$&$$O(1)$$

```java
int getWater(int[] arr) {
  if (arr == null || arr.length < 3) {
    return 0;
  }
  int value = 0;
  for (int i = 1; i < arr.length - 1; i++) {
    int leftMax = 0;
    int rightMax = 0;
    for (int l = 0; l < i; l++) {
      leftMax = Math.max(arr[l], leftMax);
    }
    for (int r = i + 1; r < arr.length; r++) {
      rightMax = Math.max(arr[r], rightMax);
    }
    value += Math.max(0, Math.min(leftMax, rightMax) - arr[i]);
  }
  return value;
}
```

better solution. Compute leftMax and rightMax in advance $$O(n)$$&$$O(n)$$

```java
int getWater(int[] arr) {
  if (arr == null || arr.length < 3) {
    return 0;
  }
  int n = arr.length - 2;
  int[] leftMaxs = new int[n];
  leftMaxs[0] = arr[0];
  for (int i = 1; i < n; i++) {
    leftMaxs[i] = Math.max(leftMaxs[i - 1], arr[i]);
  }
  int[] rightMaxs = new int[n];
  rightMaxs[n - 1] = arr[n + 1];
  for (int i = n - 2; i >= 0; i--) {
    rightMaxs[i] = Math.max(rightMaxs[i + 1], arr[i + 2]);
  }
  int value = 0;
  for (int i = 1; i <= n; i++) {
    value += Math.max(0, Math.min(leftMaxs[i - 1], rightMaxs[i - 1]) - arr[i]);
  }
  return value;
}
```

best two-pointer. $$O(n)$$&$$O(1)$$: Use 2 pointers `left` and `right`. Record `leftMax` from 0 and `rightMax` from n - 1. `leftMax` is the max from 0 to left - 1 while `rightMax` is the max from right + 1 to n - 1.  If `leftMax` is smaller than `rightMax`, the max from left + 1 to n - 1 won't be smaller than `rightMax`. Then the water blocks in `left` can be computed. Otherwise, we can compute the water in `right`.

```java
int getWater(int[] arr) {
  if (arr == null || arr.length < 3) return 0;
  int res = 0, leftMax = arr[0], rightMax = arr[arr.length - 1];
  int left = 1, right = arr.length - 2;
  while (left <= right) {
    if (leftMax <= rightMax) {
      res += Math.max(0, leftMax - arr[left]);
      leftMax = Math.max(leftMax, arr[left++]);
    } else {
      res += Math.max(0, rightMax - arr[right]);
      rightMax = Math.max(rightMax, arr[right--]);
    }
  }
  return res;
}
```

### SubArrayMaxSum

> Compute the max subarray sum of a given array

brute force $$O(n^3)$$&$$O(1)$$: for every i, compute every subarray's sum from i.

better brute force $$O(n^2)$$&$$O(n)$$: utilize `sum[i...j]` = `sum[0...j] - sum[0...i]`

best $$O(n)$$&$$O(1)$$:  if `arr[i]` is positive, `sum[...i...]` will be larger, otherwise it will be smaller. So we remember `sum[0...i]` and record the max of `sum[0...i]` as `maxSum`. If `sum[0...i]` increases, record it if it's larger than the old max. If `sum[0...i]` decreases to be negative, reset it to 0 which means we abandon `arr[i]` and compute from `i + 1`. In a word, we only want the diff between the most increasing part's top and bottom. curSum guarantees that we won't miss the possible start, `maxSum` guarantees that we won't miss the possible end. In this way, if `sum[i...j]` is the result, k <= i and m >= j, `sum[k...i]` and `sum[j...m]` should be negative. Otherwise, `sum[k...j]` or `sum[i...m]` will be larger.

```java
int maxSum(int[] arr) {
  if (arr == null || arr.length == 0) return 0;
  int maxSum = Integer.MIN_VALUE;
  int curSum = 0;
  for (int i = 0; i < arr.length; i++) {
    curSum += arr[i];
    maxSum = Math.max(maxSum, curSum);
    curSum = Math.max(curSum, 0);
  }
  return maxSum;
}
```

### MaxABSBetweenLeftAndRight

> Divide an array into 2 non-empty parts. Compute the max absolute diff between the left subarray's max and the right subarray's max

brute force $$O(n^2)$$&$$O(1)$$: for every i, find the left max and the right max

```java
int maxABS(int[] arr) {
  int res = Integer.MIN_VALUE;
  int maxLeft = 0;
  int maxRight = 0;
  for (int i = 0; i != arr.length - 1; i++) {
    maxLeft = Integer.MIN_VALUE;
    for (int j = 0; j != i + 1; j++) {
      maxLeft = Math.max(arr[j], maxLeft);
    }
    maxRight = Integer.MIN_VALUE;
    for (int j = i + 1; j != arr.length; j++) {
      maxRight = Math.max(arr[j], maxRight);
    }
    res = Math.max(Math.abs(maxLeft - maxRight), res);
  }
  return res;
}
```

better brute force $$O(n)$$&$$O(n)$$: find the left max and the right max then for every i ahead of time

 ```java
int maxABS(int[] arr) {
  int[] lArr = new int[arr.length];
  int[] rArr = new int[arr.length];
  lArr[0] = arr[0];
  rArr[arr.length - 1] = arr[arr.length - 1];
  for (int i = 1; i < arr.length; i++) {
    lArr[i] = Math.max(lArr[i - 1], arr[i]);
  }
  for (int i = arr.length - 2; i > -1; i--) {
    rArr[i] = Math.max(rArr[i + 1], arr[i]);
  }
  int max = 0;
  for (int i = 0; i < arr.length - 1; i++) {
    max = Math.max(max, Math.abs(lArr[i] - rArr[i + 1]));
  }
  return max;
}
 ```

best (not understand) $$O(n)$$&$$O(1)$$: compute the max of the array, then use the max to minus arr[0] or arr[n - 1]. Because the max will be in the left subarray or the right subarray,  the abs max diff of the left and the right should be the diff between the max and some value in one side. To guarantee the abs diff to be the largest, we need to shrink the scale of one side and enlarge the other, so the max of one side will be smaller while the opposite's will be larger. Then the abs diff will be larger. 

```java
int maxABS(int[] arr) {
  int max = Integer.MIN_VALUE;
  for (int i = 0; i < arr.length; i++) {
    max = Math.max(arr[i], max);
  }
  return max - Math.min(arr[0], arr[arr.length - 1]);
}
```

### SlidingWindowMaxArray

> Get every max of a given size w sliding windows of a given array.
>
> eg. [4, 3, 5, 4, 3, 3, 6, 7], w = 3, return [5, 5, 5, 4, 6, 7]
>
> sliding windows: [4, 3, 5] -> [3, 5, 4] -> [5, 4, 3] -> [4, 3, 3] -> ...

Use doubly-linked list to store max values's indexes reversely sorted

```
i  [4, 3, 5, 4, 3, 3, 6, 7], w = 3
0: [0(4)]
1: [0(4), 1(3)]
2: [2(5)], poll 0(4), 1(3), record 5(0-2)
3: [2(5), 3(4)], record 5(1-3)
4: [2(5), 3(4), 4(3)], record 5(2-4) 
5: [3(4), 5(3)], poll 2(5), 4(3), record 4(3-5)
6: [6(6)], poll 3(4), 5(3), record 6(4-6)
7: [7(7)], poll 6(6), record 7(5-7)
```



```java
int[] getMaxWindow(int[] arr, int w) {
  if (arr == null || w < 1 || arr.length < w) return null;
  LinkedList<Integer> qmax = new LinkedList<Integer>();
  int[] res = new int[arr.length - w + 1];
  int index = 0;
  for (int i = 0; i < arr.length; i++) {
    while (!qmax.isEmpty() && arr[qmax.peekLast()] <= arr[i]) qmax.pollLast();
    qmax.addLast(i);
    if (qmax.peekFirst() == i - w) qmax.pollFirst();//remove out-date index
    if (i >= w - 1) res[index++] = arr[qmax.peekFirst()];//from w - 1, start to record every max of sliding windows
  }
  return res;
}
```

### [Maximum Length of Repeated Subarray](https://leetcode.com/problems/maximum-length-of-repeated-subarray/description/)

> Given two integer arrays A and B, return the maximum length of an subarray that appears in both arrays.
>

DP solution:

dp\[i\]\[j\]: the longest common prefix of A[i...] and B[j...]

* if A[i] == B[j], dp\[i\]\[j\] = dp\[i + 1\]\[j + 1\] + 1, else dp\[i\]\[j\] = 0
* The answer is max(dp\[i\]\[j\]) over all (i, j)

```java
int findLength(int[] A, int[] B) {
  if (A == null || A.length == 0 || B == null || B.length == 0) {
    return 0;
  }

  int[][] dp = new int[A.length + 1][B.length + 1];
  int res = 0;
  for (int i = A.length - 1; i >= 0; i--) {
    for (int j = B.length - 1; j >= 0; j--) {
      if (A[i] == B[j]) {
        dp[i][j] = dp[i + 1][j + 1] + 1;
        res = Math.max(res, dp[i][j]);
      }
    }
  }
  return res;
}
```

Similiar problem:Longest Common Substring

### Maximum Length of Repeated Subsequence

> Given two integer arrays A and B, return the maximum length of an subsequence that appears in both arrays.

Recursion with cache

```java
int lcs(int[] a, int alen, int[] b, int blen, int[][] mem){
  if (alen < 0 || blen < 0) {
    return 0;
  }
  if (mem[alen][blen] == 0) {
    if (a[alen] == b[blen]) {
      mem[alen][blen] = lcs(a, alen - 1, b, blen - 1, mem) + 1;
    }
    else {
      mem[alen][blen] = Math.max(lcs(a, alen - 1, b, blen, mem), lcs(a, alen, b, blen - 1, mem));
    }
  }
  return mem[alen][blen];
}
```

Similiar problem:Longest Common Subsequence

### More than Half Number

> Find a number in an array that appears more than half of the array's length
>
> eg. input [1, 3, 3, 2, 3, 2, 3, 3, 2], output 3 (appear 5 times in 9 numbers)

Sorting solution: sort the array and then find the number. $$O(logn)$$

Count times solution $$O(n)$$: record current number as res and count times as count. Then traverse this array. For arr[i]:

* If res == arr[i], count++
* If res != arr[i], count--
  * If count == 0, count = 1, res = arr[i]

```java
int moreThanHalfNum(int[] nums) {
  if (nums == null || nums.length == 0) {
    throws new IllegalArgumentException();
  }

  int res = nums[0];
  int count = 1;
  for (int i = 1; i < nums.length; i++) {
    if (res == nums[i]) {
      count++;
    } else {
      if (--count == 0) {
        count = 1;
        res = arr[i];
      }
    }
  }
  return res;
}
```

Similarly, we can count 2 numbers appears more than 1/3, 3 numbers appears more than 1/4, if we can remember them separately.