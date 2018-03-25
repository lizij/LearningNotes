# Tree

[TOC]

## Heap

minheap and maxheap is already discussed in QuickSort part.

See HeapSort in [Sorting.md](Sorting.md)

More about [Heap](https://en.wikipedia.org/wiki/Heap_(data_structure)) and [PriorityQueue](https://en.wikipedia.org/wiki/Priority_queue)

## Binary Tree

Pre-order(head, left, right)

```java
void preOrder(Node head) {
    if (head == null) return;
    // print head.value
    preOrder(head.left);
    preOrder(head.right);
}

void preOrder(Node head) {
    if (head == null) return;
    Stack<Node> stack = new Stack<>();
    stack.add(head);
    while (!stack.isEmpty()) {
        head = stack.pop();
        // print head.value
        if (head.right != null) stack.push(head.right); // push head.right, 2nd to pop
        if (head.left != null) stack.push(head.left); // push head.left, 1st to pop
    }
}
```

In-order(left, head, right)

```java
void inOrder(Node head) {
    if (head == null) return;
    inOrder(head.left);
    // print head.value
    inOrder(head.right);
}

void inOrder(Node head) {
    Stack<Node> stack = new Stack<>();
    while (!stack.isEmpty() || head != null) {
        if (head != null) {
            // move to the leftmost node
            stack.push(head);
            head = head.left; 
        } else {
            // no left node
            head = stack.pop();
            // print head.value
            // move to head.right
            head = head.right;
        }
    }
}
```

Post-order(left, right, head)

```java
void postOrder(Node head) {
    if (head == null) return;
    postOrder(head.left);
    postOrder(head.right);
    // print head.value
}

void postOrder(Node head) {
    if (head == null) return;
    Stack<Node> s1 = new Stack<>();
    Stack<Node> s2 = new Stack<>();
    s1.push(head);
    while (!s1.isEmpty()) {
        // use s1 to generate "head-right-left", push in s2 to generate "left-right-head"
        head = s1.pop();
        s2.push(head);
        if (head.left != null) {
            s1.push(head.left);
        }
        if (head.right != null) {
            s1.push(head.right);
        }
    }
    while (!s2.isEmpty()) {
        head = s2.pop();
        // print head.value
    }
}

void postOrder(Node head) {
    if (head == null) return;
    Stack<Node> stack = new Stack<>();
    stack.push(head);
    Node current = null;
    while (!stack.isEmpty()) {
        current = stack.peek();
        if (current.left != null && head != current.left && head != current.right) {
            stack.push(current.left);
        } else if (current.right != null && head != current.right) {
            stack.push(current.right);
        } else {
            // print current.value
            head = current;
        }
    }
}
```

More about [Binary Tree](https://en.wikipedia.org/wiki/Binary_tree)

## TrieTree

See TrieTree in [String.md](String.md)

## Morris

do pre-order, in-order, post-order traversal in $$O(n)\&O(1)$$

Procedure: cur (current node)

1. If cur.left == null, cur = cur.right
2. If cur.left != null, find the rightest node (mostright) in the tree from cur.left
   1. If mostright.right == null, mostright = cur, cur = cur.left
   2. If mostright.right == cur, mostright = null, cur = cur.right

eg. Every node which node.left != null is visited twice

```
tree: [1, 2, 3, 4, 5, 6, 7]
cur == 1, mostright == 5, 5.right = 1, cur = 1.left = 2
cur == 2, mostright == 4, 4.right = 2, cur = 2.left = 4
cur == 4, 4.left == null, cur = 4.right = 2
cur == 2, mostright == 4 && 4.right == 2, 4.right = null, cur = 2.right = 5
cur == 5, 5.left == null, cur = 5.right = 1
cur == 1, mostright == 5 && 5.right == 1, 5.right = null, cur = 1.right = 3
cur == 3, mostright == 6, 6.right = 3, cur = 3.left = 6
cur == 6, 6.left == null, cur = 6.right = 3
cur == 3, mostright == 6 && 6.right == 3, 6.right = null, cur = 3.right = 7
cur == 7, 7.left == null, cur = 7.right = null, stop
```

This is the same with:

```java
void traverse(Node node) {
    if (node == null) return;
    // visit first time, in Morris if node.left == null, go to node.right
    traverse(node.left);
    // visit second time, in Morris, use mostright.right to return to node, then reset mostright.right and go to node.right
    traverse(node.right);
}
```

plain morris

```java
void morris(Node head) {
    if (head == null) return;
    Node cur = head;
    Node mostright = null;
    while (cur != null) {
        if (cur.left != null) {
            // cur.left != null, find mostright in cur.left tree
            mostright = cur.left;
            while (mostright.right != null && mostright.right != cur) mostright = mostright.right;
            if (mostright.right == null) {
                // set link to cur and move to cur.left
                mostright.right = cur;
                cur = cur.left;
            } else {
                // reset mostright.right to null
                mostright.right = null;
                cur = cur.right;
            }
        } else {
            // cur.left == null, move to cur.right
            cur = cur.right;
        }
    }
}
```

pre-order morris

```java
void morris(Node head) {
    if (head == null) return;
    Node cur = head;
    Node mostright = null;
    while (cur != null) {
        if (cur.left != null) {
            // cur.left != null, find mostright in cur.left tree
            mostright = cur.left;
            while (mostright.right != null && mostright.right != cur) mostright = mostright.right;
            if (mostright.right == null) {
                // set link to cur and move to cur.left
                mostright.right = cur;
                // print cur.value before go to cur.left
                cur = cur.left;
            } else {
                // reset mostright.right to null
                mostright.right = null;
                cur = cur.right;
            }
        } else {
            // cur.left == null, move to cur.right
            // print cur.value before go to cur.right since cur.left == null
            cur = cur.right;
        }
    }
}
```

in-order morris

```java
void morris(Node head) {
    if (head == null) return;
    Node cur = head;
    Node mostright = null;
    while (cur != null) {
        if (cur.left != null) {
            // cur.left != null, find mostright in cur.left tree
            mostright = cur.left;
            while (mostright.right != null && mostright.right != cur) mostright = mostright.right;
            if (mostright.right == null) {
                // set link to cur and move to cur.left
                mostright.right = cur;
                cur = cur.left;
            } else {
                // reset mostright.right to null
                mostright.right = null;
                // print cur.value before go to cur.right
                cur = cur.right;
            }
        } else {
            // cur.left == null, move to cur.right
            // print cur.value before go to cur.right
            cur = cur.right;
        }
    }
}
```

post-order morris

```java
void morris(Node head) {
  if (head == null) return;
  Node cur = head;
  Node mostright = null;
  while (cur != null) {
    if (cur.left != null) {
      // cur.left != null, find mostright in cur.left tree
      mostright = cur.left;
      while (mostright.right != null && mostright.right != cur) mostright = mostright.right;
      if (mostright.right == null) {
        // set link to cur and move to cur.left
        mostright.right = cur;
        cur = cur.left;
      } else {
        // reset mostright.right to null
        mostright.right = null;
        cur = cur.right;
        // print cur.right and cur after print cur.left
        printEdge(cur.right);
      }
    } else {
      // print cur.right and cur since cur.left == null, then move to cur.right
      printEdge(cur.right);
      cur = cur.right;
    }
  }
  printEdge(head);
}

void printEdge(Node head) {
  // print the right border from head reversely
  Node tail = reverseEdge(head);
  Node cur = tail;
  while (cur != null) {
    // print cur.value
    cur = cur.right;
  }
  reverseEdge(tail);
}

public static Node reverseEdge(Node from) {
  Node pre = null;
  Node next = null;
  while (from != null) {
    next = from.right;
    from.right = pre;
    pre = from;
    from = next;
  }
  return pre;
}
```

## Binary Search Tree

### Search Tree

> Search Tree: For every node in the tree, all descendants' values are smaller than node.value while all descendants' values are larger

a search tree which each node of it only have 2 children.

eg. [5, 3, 7, 2, 4, 6, 8] 

### Balanced Binary Search Tree

Use left rotation or right rotation to maintain a balanced binary tree. Insert, delete, search, update will all do in $$O(logn)$$

1. size(right) > size(left) => rotateLeft(), size(left) > size(right) => rotateRight()
2. LL: rotateRight, RR: rotateLeft, LR: rotateLeft&rotateRight, RL: rotateRight&rotateLeft

```
LL:             RR:             LR:           RL:
    N           N                 N        N  N      N
   /       N     \         N     /        /    \      \
  N   =>  / \     N   =>  / \   N   =>   N      N =>   N
 /       N   N     \     N   N   \      /      /        \
N                   N             N    N      N          N
```

#### AVL Tree

For every node,  abs(heightOfTree(node.left) - heightOfTree(node.right)) <= 1

#### Red Black Tree

Definition

1. Every node is red or black
2. Root node and leave nodes are black
3. Children of Every red node are black
4. The number of black nodes in left and right of a node is the same

In java, TreeMap is implemented by Red Black Tree

#### Size Balanced Tree

A size of a tree, won't be smaller than the size of its neighbored subtree.

Compared to Red Black Tree, the implementation is simpler.

[More](http://wcipeg.com/wiki/Size_Balanced_Tree)

### Segment Tree

Segment Tree is a kind of binary search tree which looks like this:

![](https://images2015.cnblogs.com/blog/1129509/201703/1129509-20170320173634080-768299163.png)

In Computer science, a segment tree is a tree data structure used for storing information about intervals, or segments. It allows querying which of the stored segments contain a given point. Every node in a segment tree has 3 values: left, right and weight. left and right decides an interval [left, right]. If a parent's interval is [x, y], the left child's is [x, (x + y) / 2], the right child's is [((x + y) / 2 + 1), y]

## SkipList

Use probability to decide the location of an element	

```java
class SkipListNode {
    Integer value;
    ArrayList<SkipListNode> nextNodes;

    SkipListNode(Integer value) {
        this.value = value;
        nextNodes = new ArrayList<SkipListNode>();
    }
}

class SkipList {
    SkipListNode head;
    int maxLevel;
    int size;
    double PROBABILITY = 0.5;

    SkipList() {
        size = 0;
        maxLevel = 0;
        head = new SkipListNode(null);
        head.nextNodes.add(null);
    }

    void add(Integer newValue) {
        if (!contains(newValue)) {
            size++;
            int level = 0;
            while (Math.random() < PROBABILITY) {
                level++;
            }
            while (level > maxLevel) {
                head.nextNodes.add(null);
                maxLevel++;
            }
            SkipListNode newNode = new SkipListNode(newValue);
            SkipListNode current = head;
            do {
                current = findNext(newValue, current, level);
                newNode.nextNodes.add(0, current.nextNodes.get(level));
                current.nextNodes.set(level, newNode);
            } while (level-- > 0);
        }
    }

    void delete(Integer deleteValue) {
        if (contains(deleteValue)) {
            SkipListNode deleteNode = find(deleteValue);
            size--;
            int level = maxLevel;
            SkipListNode current = head;
            do {
                current = findNext(deleteNode.value, current, level);
                if (deleteNode.nextNodes.size() > level) {
                    current.nextNodes.set(level, deleteNode.nextNodes.get(level));
                }
            } while (level-- > 0);
        }
    }

    // Returns the skiplist node with greatest value <= e
    SkipListNode find(Integer e) {
        return find(e, head, maxLevel);
    }

    // Returns the skiplist node with greatest value <= e
    // Starts at node start and level
    SkipListNode find(Integer e, SkipListNode current, int level) {
        do {
            current = findNext(e, current, level);
        } while (level-- > 0);
        return current;
    }

    // Returns the node at a given level with highest value less than e
    SkipListNode findNext(Integer e, SkipListNode current, int level) {
        SkipListNode next = current.nextNodes.get(level);
        while (next != null) {
            Integer value = next.value;
            if (lessThan(e, value)) { // e < value
                break;
            }
            current = next;
            next = current.nextNodes.get(level);
        }
        return current;
    }

    boolean contains(Integer value) {
        SkipListNode node = find(value);
        return node != null && node.value != null && equalTo(node.value, value);
    }

    boolean lessThan(Integer a, Integer b) {
        return a.compareTo(b) < 0;
    }

    boolean equalTo(Integer a, Integer b) {
        return a.compareTo(b) == 0;
    }
}
```

## Tips

### Ordered map in Java

In Java, TreeMap is implemented by Red Black Tree with ordered keys.

```values()```will return values ordered by keys.

```floorKey()```and ```ceilingKey()```will return a key which is floor to or ceiling to a given key, if it existed. Otherwise return null. 

## Practice

### MadianQuick

> Assuming that an int generator is here and you have enough space to store the output. Please design a structure named MedianHolder which can get the median number any time.
>
> Add a number in MedianHolder should be done in $$O(logn)$$ time
>
> Get the median should be done in $$O(1)$$ time

Build a max heap(max) and a min heap(min). Assuming we have a sorted array, the first n/2 part is in max while the last n/2 part is in min. The median is one or the average of the 2 heaps' roots.

Now add numbers in 2 heaps separately. 

1. maxHeap.isEmpty() == true || maxHeap.peek() >= num || minHeap.peek() > num: add to maxHeap
2. else: add to minHeap

To balance the size of 2 heaps, we need to adjust 2 heaps by poll one's root and add it in the other if their size's difference is larger than 1.

```java
class MedianHolder{
    PriorityQueue<Integer> maxHeap; // the first n/2 part
    PriorityQueue<Integer> minHeap; // the last n/2 part
    void modifyTwoHeapSize() {
        if (maxHeap.size() == minHeap.size() + 2) {
            minHeap.add(maxHeap.poll());
        } else if (minHeap.size() == maxHeap.size() + 2) {
            maxHeap.add(minHeap.poll());
        }
    }

    void addNumber(int num) {
        /*
    if (maxHeap.isEmpty()) {
      // maxHeap is empty -> no num added before
      maxHeap.add(num);
      return;
    }
    // in the first n/2 part
    if (maxHeap.peek() >= num) {
      maxHeap.add(num);
    } else {
      // in the last n/2 part probably
      if (minHeap.isEmpty()) {
        // minHeap is empty -> only 1 num added before
        minHeap.add(num);
        return;
      }
      // maxHeap.peek() < num < minHeap.peek(), give num to maxHeap
      if (minHeap.peek() > num) {
        maxHeap.add(num);
      }
      else {
        minHeap.add(num);
      }
    }
    */

        if (maxHeap.isEmpty() || maxHeap.peek() >= num || !minHeap.isEmpty() && minHeap.peek() > num) {
            maxHeap.add(num);
        } else {
            minHeap.add(num);
        }
        modifyTwoHeapSize();
    }

    Integer getMedian() {
        int maxHeapSize = maxHeap.size();
        int minHeapSize = minHeap.size();
        if (maxHeapSize + minHeapSize == 0) {
            return null;
        }
        Integer maxHeapHead = maxHeap.peek();
        Integer minHeapHead = minHeap.peek();
        if (((maxHeapSize + minHeapSize) & 1) == 0) {
            return (maxHeapHead + minHeapHead) / 2;
        }
        return maxHeapSize > minHeapSize ? maxHeapHead : minHeapHead;
    }
}
```

### LessMoney

> Cutting a gold bar into 2 parts need copper plates which the quantity is the same as the length of the gold bar. For example, if the length of the gold bar is 20, it will consume 20 copper plates no matter how the gold bar is separated. Compute the least plates if some people want to separate the bar.
>
> For example, if 3 people want to separate the bar into [10, 20, 30]
>
> If separate it into 10 and 50, then 50 to 20 and 30, it will need 60 + 50 = 110
>
> If separate it into 30 and 30, then 30 to 10 and 20, it will need 60 + 30 = 90 < 110

Build a [Huffman](https://en.wikipedia.org/wiki/Huffman_coding) tree

```java
int lessMoney(int[] arr) {
    PriorityQueue<Integer> queue = new PriorityQueue<>();
    for (int i = 0; i < arr.length; i++) {
        queue.add(arr[i]);
    }
    int sum = 0;
    int cur = 0;
    while (queue.size > 1) {
        cur = queue.poll() + queue.poll();
        sum += cur;
        queue.add(cur);
    }
    return sum;
}
```

### IPO

> Give some jobs(contains their cost and profit , the current money W. Compute the max money after k step.
>
> For example,
>
> cost: 	[10, 50, 100, 200]
>
> profit: 	[30, 100, 90, 100]
>
> Now have 50 money. If we invest 50, we will get 100 and the total money become 200 now. Then we can invest 200. The profit of 10 is too small and doesn't worth our investment.

Greedy solution($$O(klogn)$$): In every step, collect all feasible jobs and do the max-profit one. 

Firstly, define the node class

```java
class Node {
    int p;// profit
    int c;// cost

    Node(int p, int c) {
        this.p = p; 
        this.c = c;
    }
}
```

Now compute the max profit

```java
int findMaximizedCapital(int k, int W, int[] Profits, int[] Capital) {
    Node[] nodes = new Node[Profits.length];
    for (int i = 0; i < Profits.length; i++) {
        nodes[i] = new Node(Profits[i], Capital[i]);
    }

    PriorityQueue<Node> minCostQ = new PriorityQueue<>((o1, o2) -> o1.c - o2.c);
    PriorityQueue<Node> maxProfitQ = new PriorityQueue<>((o1, o2) -> o2.p - o1.p);

    for (int i = 0; i < nodes.length; i++) {
        minCostQ.add(nodes[i]);
    }

    for (int i = 0; i < k; i++) {
        // unlock affordable jobs
        while (!minCostQ.isEmpty() && minCostQ.peek().c <= W) {
            maxProfitQ.add(minCostQ.poll());
        }

        // no affordable jobs
        if (maxProfitQ.isEmpty()) {
            return W;
        }

        // do the most profitable job
        W += maxProfitQ.poll().p;
    }
    return W;
}
```

### PaperFolding

> Fold a paper n times, print all folds's direction

For example, fold 4 times, `d` for down, `u` for up, it will generate a tree

Every time of folding will generate a pair of `d` and `u` in both sides of a fold

`d` -> `ddu`, `u` -> `dud`

```
1.                     d
2.         d           d           u
3.   d     d     u     d     d     u     u
4. d d u d d u d u u d d u d d u d u u d u u
```

```java
void printAllFolds(int N) {
    printProcess(1, N, true);
}

void printProcess(int i, int N, boolean down) {
    if (i > N) return;
    printProcess(i + 1, N, true);
    System.out.println(down ? "down " : "up ");
    printProcess(i + 1, N, false);
}
```

### DescendantNode

```java
class Node {
    int value;
    Node left;
    Node right;
    Node parent; // head.parent = null

    Node(int data) {
        this.value = data;
    }
}
```

> Find the descendant node of a given node in a binary tree
>
> The descent node of a given node is the right neighbored one in in-order traversal. 

The descent of the node is:

* If the node.right != null: the leftmost node of node.right
* If the node.right == null:
  * If the node.parent.left == node.parent: return node.parent
  * If the node.parent.right == node: find the node.parent.parent until meet the last case or null (root node).

```java
Node getNextNode(Node node) {
    if (node == null) return node;

    if (node.right != null) return getLeftMost(node.right);

    Node parent = node.parent;
    while (parent != null && parent.left != node) {
        node = parent;
        parent = node.parent;
    }
    return parent;
}

Node getLeftMost(Node node) {
    if (node == null) return node;
    while (node.left != null) {
        node = node.left;
    }
    return node;
}
```

### ASubtreeEqualsB

See ASubtreeEqualsB in [String.md](String.md)

### MyCalendar

>[MyCalendar I](https://leetcode.com/problems/my-calendar-i/description/)
>
>[MyCalendar II](https://leetcode.com/problems/my-calendar-ii/description/)
>
>[MyCalendar III](https://leetcode.com/problems/my-calendar-iii/description/)

MyCalendar series are fit for practicing with TreeMap, which is an ordered map in Java.

The core problem is to detect if a new interval (*start*, *end*) has conflicts with existed intervals and how many conflicts will be caused if add this interval. In a word, the key is detecting conflicts number. However, *start* or *end* is a number from 1 to 10^9 which means we cannot use arrays to mark every time point. 

So we use TreeMap as a timeline to record start and end as different events. Every key is a time point and every value is the active events at the corresponding time. *start* adds 1 active events while *end* reduces 1 active events.

```java
// start and end are given
TreeMap<Integer, Integer> timeline;
timeline.put(start, timeline.getOrDefault(start, 0) + 1);
timeline.put(end, timeline.getOrDefault(end, 0) - 1);
```

Now we start to traverse all values in **timeline**. We can know the active events at every time point. For this series of problems, double-booking means 2 active events at a time while tripple-booking means 3 active events at a time.

```java
int active = 0; // representing the active events at the current time, also the number of conflicts
for (Integer i: timeline.values()) {
    active += i;
}
```

Simliar problem:[Employee Free Time](https://leetcode.com/problems/employee-free-time/)

### [Count Complete Tree Nodes](https://leetcode.com/problems/count-complete-tree-nodes/description/)

> Given a complete binary tree, count the number of nodes.
> In a complete binary tree every level, except possibly the last, is completely filled, and all nodes in the last level are as far left as possible. It can have between 1 and 2h nodes inclusive at the last level h.

Two key points:

1. Full binary tree: This means the leftmost node is possibly further than the rightmost node, we need to find which sub tree is unbalanced
2. Use "<<" instead of Math.pow() to speed up

```java
public int countNodes(TreeNode root) {
    if (root == null) return 0;
    int left = 0, right = 0;
    TreeNode leftmost = root, rightmost = root;
    while (leftmost != null) {
        left++;
        leftmost = leftmost.left;
    }

    while (rightmost != null) {
        right++;
        rightmost = rightmost.right;
    }

    if (left == right) return (1 << left) - 1;
    return countNodes(root.left) + countNodes(root.right) + 1;
}
```

### [Symmetric Tree](https://leetcode.com/problems/symmetric-tree/description/)

> Given a binary tree, check whether it is a mirror of itself (ie, symmetric around its center).
>
> For example, this binary tree `[1,2,2,3,4,4,3]` is symmetric
>
> But the following `[1,2,2,null,3,null,3]` is not

```java
boolean isSymmetric(TreeNode root) {
    if (root == null) return true;
    return isSymmetric(root.left, root.right);
}

boolean isSymmetric(TreeNode left, TreeNode right){
    return left == null && right == null 
        || left != null && right != null && left.val == right.val 
        && isSymmetric(left.left, right.right) && isSymmetric(left.right, right.left);
}
```

