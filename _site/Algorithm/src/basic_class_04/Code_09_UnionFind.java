package basic_class_04;

import java.util.HashMap;
import java.util.List;

public class Code_09_UnionFind {

	public static class Node {
		// whatever you like
	}

	public static class DisjointSets {
		public HashMap<Node, Node> fatherMap;
		public HashMap<Node, Integer> rankMap;

		public DisjointSets() {
			fatherMap = new HashMap<Node, Node>();
			rankMap = new HashMap<Node, Integer>();
		}

		public void makeSets(List<Node> nodes) {
			fatherMap.clear();
			rankMap.clear();
			for (Node node : nodes) {
				fatherMap.put(node, node);
				rankMap.put(node, 1);
			}
		}

		public Node findFather(Node n) {
			Node father = fatherMap.get(n);
			if (father != n) {
				father = findFather(father);
			}
			fatherMap.put(n, father);
			return father;
		}

		public void union(Node a, Node b) {
			if (a == null || b == null) {
				return;
			}
			Node aFather = findFather(a);
			Node bFather = findFather(b);
			if (aFather != bFather) {
				int aFrank = rankMap.get(aFather);
				int bFrank = rankMap.get(bFather);
				if (aFrank <= bFrank) {
					fatherMap.put(aFather, bFather);
					rankMap.put(bFather, aFrank + bFrank);
				} else {
					fatherMap.put(bFather, aFather);
					rankMap.put(aFather, aFrank + bFrank);
				}
			}
		}

	}

	public static void main(String[] args) {

	}

}
