# Lesson 2

[TOC]

## KMP

KMP is used to judge if string A is a substring of string B. If B contains A, return the start index of A in B, else return -1. The length of A is m, B is n

### Traditional  way

From every index of B, start matching A. The time complexity is $$O(mn)$$

```java
int contains(String s, String m) {
  if (s == null || m == null || s.length() < 1 || s.length() < m.length()) return -1;
  char[] str1 = s.toCharArray(), str2 = m.toCharArray();
  int i = 0, j = 0;
  while (i < str1.length && j < str2.length) {
    if (str[i] == str2[j]) {
      i++;
      j++;
    } else {
      j = 0;
    }
  }
  return j == str2.length ? i - j : -1;
}
```

KMP can do the same thing in $$O(n)$$ time. j don't need to reset to 0

### Building next[]

next[i] is the max length of the max prefix length in the previous substring before i, which satisfying the prefix == the suffix. The prefix can't contain the last char while the suffix can't contain the first char

Set next[0] = -1, next[1] = 0, next[i] means if matching fails, set j to next[i] position and start matching again. "next[0] = -1" is only used to mark the start position. 

```java
int[] getNextArray(char[] ms) {
  if (ms.length == 1) return new int[]{-1};
  int[] next = new int[ms.length];
  next[0] = -1; //start position
  next[1] = 0;
  int pos = 2; //computing current position of next
  int cn = 0; //start with next[pos - 1], current next value
  while(pos < next.length) {
    if (ms[pos - 1] == ms[cn]) next[pos++] = ++cn; //ms[0...cn] == ms[pos-cn-1...pos-1]
    else if (cn > 0) cn = next[cn]; //shrink to ms[0..ms[cn]]
    else next[pos++] = 0; //return to start
  }
  return next;
}
```

> Why shrink to ms[0...ms[cn]]: To find the max prefix-suffix-same length, we need to judge if ms[pos - 1] == ms[cn]. If ms[pos - 1] == ms[cn], the max prefix-suffix-same is ms[0...(cn+1)] with ms[(pos-1-cn)...(pos-1)]. If not, we need shrink cn to next[cn]. Otherwise if we shrink cn to k and next[cn] < k < cn, ms[k...cn] should be the same as the last part of ms[pos-1-cn...pos-1], searching from k will get [0...cn] and [(pos-2-cn)...(pos-2)] again, then go back to the previous cases

### Using next[]

For example

```
Start matching. When we meet 'k' in B, we need to change the start index of A.
B: ...1234abc1234|k|...
A:    1234abc1234|a|...
So this case becomes:
B: ...1234abc1234|k|...
A:           1234|a|bc1234a...
```

>The reason to slide A like this way:
>
>If B[i...(x - 1)] == A[0...(y - 1)] and B[x] != A[y], change start index of A to next[y].
>
>If there is B[k...] == A[0...n] and i < k < x, there will be a longer (prefix == suffix) length in A because B[i...(x - 1)] == A[0...(y - 1)]. The next[y] will be larger, which is contradictory to the definition of next[y].

```java
int contains(String s, String m) {
  if (s == null || m == null || s.length() < 1 || s.length() < m.length()) return -1;
  char[] str1 = s.toCharArray(), str2 = m.toCharArray();
  int i = 0, j = 0;
  int[] next = getNextArray(str2);
  while (i < str1.length && j < str2.length) {
    if (str[i] == str2[j]) {
      // slide A and B at the same time
      i++;
      j++;
    } else if (next[j] == -1) {
      // same with j == 0, which means A has been slided to head
      i++;
    } else j = next[j]; // slide A to next[j]
  }
  return j == str2.length ? i - j : -1;
}
```

## Manacher

Find the longest plalindrome substring of a string A.

### Traditional way

From every index, extend the string to find the longest plalindrome substring.

Add special chars to find the odd & even plalindrome substring in the same way

For example,

```
abc12321abc -> #a#b#c#1#2#3#2#1#a#b#c:
find #1#2#3#2#1#, result is 11 / 2 = 5
adb1221abc -> #a#b#c#1#2#2#1#a#b#c:
find #1#2#2#1#, result is 9 / 2 = 5
```

Time complexity is $$O(n^2)$$

### The rightest plalindrome border

This is the rightest border of all plalindrome substrings. Mark the rightest plalindrome border as *R*, the first position when R updated as *C*, the max extended length as *r = R - C* and the leftest plalindrome border as *L = 2 * C - R*

For example,

```
0 1 2 3 4 5 6 7
# 1 # 2 # 1 #
C: 1("#a#")    , R: 2, r:1
C: 3("#1#2#1#"), R: 7, r:3
```

### Building manacher string

```java
char[] manacherString(String str) {
  char charArr = str.toCharArray();
  char res = new char[str.length() * 2 + 1];
  int index = 0;
  for (int i = 0; i < res.length; i++) {
    res[i] = (i & 1) == 0 ? '#' : charArr[index++];
  }
  return res;
}
```

### Compute *r* and get the max one

From left to right, compute every $r_i$ of postion $$i$$

If $$i$$ > *R*, extend the substring from $$i$$ and get $$r_i$$, and update *R*

If $$i$$ <= *R*, there must be $$j = 2 * C - i$$,

 * If $$j - r_j$$ > *L*,  $$r_i=r_j$$. Because the substring extended from $$i$$ and $$j$$ are symmetrical

   ```
   For example,
   k{ab121cdc121ba}z
    L   j  C  i   R
   C = 7, R = 13, L = 1, i = 10, compute r10
   j = 4, r4 = 3, because "b121c" and "c121b" is symmetrical. So r10 = r4 = 3
   ```

 * If $$j - r_j$$ < *L*,  $$ri$$ = *R* - $$i$$

   ```
   Assume the values as follow, all chars from L to R are a plalindrome substring:
   (...X{...j...)Y...O...Z...i...}P
        L            C           R
   Obviously, X != P
   Because j - rj < L, X == Y (X and Y are both in the plalindrome substring extended from j).
   Y and Z are symmetrical. So Z == Y == X != P. ri = R - i

   For example
   (ab{c1cba)dab[c1c]}kz
      L j    C    i  R
   ```

* If $$j - r_j$$ == *L*,  $$ri$$ >= *R* - $$i$$. Try to extend more.

Because *R* increases strictly, the time complexity of Manacher is $$O(n)$$

```java
int maxLcpsLength(String str) {
  if (str == null || str.length() == 0) return 0;
  char[] charArr = manacherString(str);
  int[] r = new int[charArr.length];//r + 1
  int C = -1;//C
  int R = -1;//R
  int max = Integer.MIN_VALUE;
  for (int i = 0; i < charArr.length; i++) {
    r[i] = i < R ? Math.min(r[2 * C - i], R - i) : 1;//give the least possbile r
    while(i + r[i] < charArr.length && i - r[i] > -1) {//valid indexes
      //try to extend
      //if i <= R && rj != R, this will fail at once
      if (charArr[i + r[i]] == charArr[i - r[i]]) r[i]++;
      else break;//r[i] will be ri + 1
    }

    if (i + r[i] > R) {
      R = i + r[i];
      C = i;
    }
    max = Math.max(max, r[i]);
  }
  return max - 1;
}
```

## TrieTree

also called digital tree and sometimes radix tree or prefix tree. 

An ordered tree data structure that is used to store a dynamic set or associate array where the keys are usually strings.

For example, we have ["abdh", "abdi", "ade", "acf", "acgi"], the TrieTree is:

![](http://img.blog.csdn.net/20150405165903540)

An example TrieTree for lower-case strings

```java
class TrieNode {
  int path; // how many strings are using this node
  int end; // how many strings are ended with this node
  TrieNode[] map; // default size 26, point to chars
  TrieNode() {
    path = 0;
    end = 0;
    map = new TrieNode[26];
  }
}

class Trie {
  TrieNode root;

  Trie() {
    root = new TrieNode();
  }

  void insert(String word) {
    if (word == null) return;
    char[] chs = word.toCharArray();
    TrieNode node = root;
    int index = 0;
    for (int i = 0; i < chs.length; i++) {
      index = chs[i] - 'a';
      if (node.map[index] == null) node.map[index] = new TrieNode();
      node = node.map[index];
      node.path++;
    }
    node.end++;
  }

  void delete(String word) {
    if (!search(word)) return;
    char[] chs = word.toCharArray();
    TrieNode node = root;
    int index = 0;
    for (int i = 0; i < chs.length; i++) {
      index = chs[i] - 'a';
      if (--node.map[index].path == 0) {
        node.map[index] = null;
        return;
      }
      node = node.map[index];
    }
    node.end--;
  }

  boolean search(String word) {
    if (word == null) return false;
    char[] chs = word.toCharArray();
    TrieNode node = root;
    int index = 0;
    for (int i = 0; i < chs.length; i++) {
      index = chs[i] - 'a';
      if (node.map[index] == null) return false;
      node = node.map[index];
    }
    return node.end > 0;
  }

  int prefixNumber(String pre) {
    if (pre == null) return 0;
    char[] chs = pre.toCharArray();
    int index = 0;
    for (int i = 0; i < chs.length; i++) {
      index = chs[i] - 'a';
      if (node.map[index] == null) return 0;
      node = node.map[index];
    }
    return node.path;
  }
}
```

Another implementation using HashMap

```java
class Node {
  char c;
  HashMap<Character, Node> children;
  int end; // the index of the string ending with this

  public Node(char c) {
    this.c = c;
    children = new HashMap<>();
    end = -1;
  }
}

class Trie {

  Node root;
  List<String> words;

  public Trie() {
    // start with '\0' node
    root = new Node('\0');
    words = new ArrayList<>();
  }

  public void insert(String word) {
    if (contains(word)) {
      return;
    }
    // from root to set each child in their position
    Node cur = root;
    for (char c: word.toCharArray()) {
      cur.children.putIfAbsent(c, new Node(c));
      cur = cur.children.get(c);
    }
    words.add(word);
    cur.end = words.size() - 1;
  }

  public boolean contains(String word) {
    if (word == null || word.length() == 0) {
      return false;
    }
    Node cur = root;
    for (char c: word.toCharArray()) {
      if (!cur.children.containsKey(c)) {
        return false;
      }
      cur = cur.children.get(c);
    }
    return cur.end >= 0;
  }
}
```

Application：

* find every string in B[] that is one of strings' prefix in A[]
* find the closest number in A[] to a given value 

More about [TrieTree](https://en.wikipedia.org/wiki/Trie)

## Practice

### ShortestHaveTwice

 >For a string, add the least chars at the end to make sure that the new string contains the original string twice.
 >
 >For example, 
 >
 >aaa -> aaaa
 >
 >abc -> abcabc
 >
 >aba -> ababa

The core of this problem is to compute the length of the same prefix and suffix.

So building next[] is the right way to solve this.

```java
String shortestHaveTwice(String str) {
  if (str == null || str.length == 0) return null;
  char[] chs = str.toCharArray;
  if (chs.length == 1) return str + str;
  if (chs.length == 2) return chs[0] == chs[1] ? str + chs[0] : str + str;
  int[] next = getNextArray(chs);
  return str + str.substring(next[next.length - 1]);
}
```

### ASubtreeEqualsB

> Judge if tree A is a subtree of the other tree B

Traditional way: DFS traverse and match A from root in B

```java
boolean isSameTree(Node t1, Node t2) {
  return t1 == null && t2 == null
    || t1 != null && t2 != null && t1.value == t2.value && isSameTree(t1.left, t2.left) && isSameTree(t1.right, t2.right);
}

boolean isSubtree(Node t1, Node t2) { // t2 is a subtree of t1?
  return isSameTree(t1, t2) 
    || isSameTree(t1.left, t2) 
    || isSameTree(t1.right, t2);
}
```

KMP solultion: Serialize A and B to strings by pre-order, then find A's string in B's string

For example, 

A = [2,4,null], A's string is "2!4!#!"

B = [1,2,3,4,null,null,5], B's string is "1!2!4!#!3!#!5!".

Obviously, B's string cotains A's string

```java
boolean isSubtree(Node t1, Node t2) {// t2 is a subtree of t1?
  String t1Str = serialByPre(t1);
  String t2Str = serialByPre(t2);
  return contains(t1Str, t2Str) != -1;
}

String serialByPre(Node node) {
  if (node == null) return "#";
  return node.value + "!" + serialByPre(node.left) + serialByPre(node.right);
}
```

### ShortestEnd

> For a string, add the least chars to make it plalindrome.

Use Manacher to get R to the end and C. Copy the reverse order substring from 0 to L to the end.

```java
String shortestEnd(String str) {
  //same as maxLcpsLength()
  for (int i = 0; i < charArr.length; i++) {
    //same as maxLcpsLength()
    if (R == charArr.length) {
      maxContainsEnd = r[i];
      break;
    }
  }
  char[] res = new char[str.length() - maxContainsEnd + 1];
  for (int i = 0; i < res.length; i++) {
    res[res.length - 1 - i] = charArr[i * 2 + 1];
  }
  return String.valueOf(res);
}
```

### IsRotation

> Define the rotation of a string.
>
> eg. rotate "abc" 1 step -> "cab"
>
> rotate "abc" 2 step -> "bca"
>
> return true if string1 is the rotation result of string2

In rotation, the length won't change. So contact string b with itself. Any rotation result is in the contacted result.

eg. "abc" -> "abcabc": "cab", "bca", "abc" are all in. 

```java
boolean isRotation(String a, String b) {
  if (a == null || b == null || a.length() != b.length()) {
    return false;
  }
  String bb = b + b;
  return contains(b2, a) != -1;
}
```

### RotateString

> Define rotation as the rotation in last practice. Return the rotation result of k place without extra space

```
origin:   [0, 1, ..., n - 1]
rotate k: [n - k, n - k + 1, ..., n - 1, 0, 1, ..., n - k - 1]
separate: [n - k, n - k + 1, ..., n - 1], [0, 1, ..., n - k - 1]
reverse:  [n - 1, ..., n - k], [n - k - 1, ..., 0]
```

So reverse these operations

```
origin:   		[0, 1, ..., n - 1]
separate: 		[0, 1, ..., n - k - 1], [n - k, ..., n - 1]
reverse:  		[n - k - 1, ..., 1, 0], [n - 1, ..., n - k]
merge&reverse:	[n - k, n - k + 1, ..., n - 1, 0, 1, ..., n - k - 1]
```



```java
void rotate(char[] chs, int k) {
  if (chs == null || k <= 0 || k > chs.length) return;
  reverse(chs, 0, chs.length - k - 1);
  reverse(chs, chs.length - k, chs.length - 1);
  reverse(chs, 0, chs.length - 1);
}

void reverse(char[] chs, int start, int end) {
  char tmp = 0;
  while (start < end) {
    tmp = chs[start];
    chs[start] = chs[end];
    chs[end] = tmp;
    start++;
    end--;
  }
}
```

### LowestLexicography

> Contact all words in a string array in the lowest lexicography order
>
> eg. ["b", "ba"] return "bab". Because "bab" is smaller than "bba"

Greedy solution: sort the string array by (a, b) -> (a + b).compareTo(b + a)

eg. "ba", "b" -> "bab" < "bba", "ba" < "b"; "ab", "bc" -> "abbc" < "bcab", "ab" < "bc"

> Proof of comparation transitivity: a.b <= b.a, b.c <= c.b, prove a.c <= c.a
>
> Define move(): a.b = a * move(b) + b, eg. for int, (100, 15) -> 100 * 10^2 + 15 = 100 * move(15) + 15
>
> Therefore, a * move(b) + b <= b * move(a) + a, b * move(c) + c <= c * move(b) + b, now prove
>
> a * move(c) + c <= c * move(a) + a
>
> get a * (b * move(c) + c - b) <= c (b * move(a) + a - b) simplify it and get the result.

> Proof of comparation: If a < b, prove [a... b] is the smallest
>
> If no string between a and b, [ab] < [ba].
>
> If [a$$m_1...m_n$$b] exists, [a$$m_1...m_n$$b] <= [$$m_1$$a$$...m_n$$b] <= [$$m_1...m_n$$ab] <= [$$m_1...m_n$$ba] <= [$$m_1...m_{n-1}$$b$$m_n$$a] <= [b$$m_1...m_n$$a]

```java
lowestString(String[] strs) {
  if (strs == null || strs.length == 0) return "";
  Arrays.sort(strs, (a, b) -> (a + b).compareTo(b + a));
  StringBuilder builder = new StringBuilder();
  for (int i = 0; i < strs.length; i++) res.append(strs[i]);
  return res;
}
```

### PrintAllSubsequence

See PrintAllSubsequence in [RecursionDP.md](RecursionDP.md)

### PrintAllPermutations

See PrintAllPermutations in [RecursionDP.md](RecursionDP.md)

### Longest Common Substring

See Maximum Length of Repeated Subarray in [Linear.md](Linear.md)

### Longest Common Subsequence

See Longest Common Subsequence in [Linear.md](Linear.md)

### [Partition Labels](https://leetcode.com/problems/partition-labels/description/)

> A string S of lowercase letters is given. We want to partition this string into as many parts as possible so that each letter appears in at most one part, and return a list of integers representing the size of these parts.

Greedy solution: only consider the last occurrence fo a letter

```java
public List<Integer> partitionLabels(String S) {
  if (S == null || S.length() == 0) {
    return null;
  }

  List<Integer> res = new ArrayList<>();
  int[] last = new int[26];
  for (int i = 0; i < S.length(); i++) {
    last[S.charAt(i) - 'a'] = i;
  }

  int j = 0, anchor = 0;
  for (int i = 0; i < S.length(); i++) {
    j = Math.max(j, last[S.charAt(i) - 'a']);
    if (i == j) {
      res.add(i - anchor + 1);
      anchor = i + 1;
    }
  }

  return res;
}
```

