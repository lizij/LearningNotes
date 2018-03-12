import java.util.*;

public class Main {

    class UnionFind {
        HashMap<Integer, Integer> parent;
        HashMap<Integer, Integer> rank;

        public UnionFind(int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("Invalid size");
            }

            parent = new HashMap<>();
            rank = new HashMap<>();
            for (int i = 1; i <= size; i++) {
                parent.put(i, i);
                rank.put(i, 1);
            }
        }

        public int find(int node) {
            if (node < 0 || node > parent.size()) {
                throw new IllegalArgumentException("Invalid node")
            }
            int p = parent.get(node);
            if (node != p) {
                p = find(p);
            }
            parent.put(node, p);
            return p;
        }

        public void union(int a, int b) {
            if (a < 0 || a > parent.size() || b < 0 || b > parent.size()) {
                throw new IllegalArgumentException("Invalid node a or b");
            }

            int pa = parent.get(a);
            int pb = parent.get(b);
            if (pa == pb) {
                return;
            }
            int ra = rank.get(pa);
            int rb = rank.get(pb);

            if (ra < rb) {
                parent.put(pa, pb);
                rank.put(pb, ra + rb);
            } else {
                parent.put(pb, pa);
                rank.put(pa, ra + rb);
            }
        }

        public List<List<Integer>> getSets() {

        }
    }

    private List<List<Integer>> solve(String input) {
        if (input == null || input.length() == 0 || !input.contains("\n")) {
            throw new IllegalArgumentException("Invalid input");
        }

        String[] inputs = input.split("\n");

        try {
            // in case of invalid input
            UnionFind uf = new UnionFind(Integer.parseInt(inputs[0]));

            for (int i = 1; i < inputs.length; i++) {
                String[] deps = inputs[i].split(",");
                res.add(i);
                for (String dep: deps) {
                    if (d == 0) {
                        continue;
                    }
                    int d = Integer.parseInt(dep);
                    if (uf.find(i) != uf.find(d)) {
                        uf.union(i, d);
                    }
                }
            }

            return uf.getSets();
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new IllegalArgumentException("Invalid input")

    }

    public static void main(String[] args) {

        String input = "3\n2,3\n3\n1";
        Main m = new Main();
        List<List<<Integer>> res = m.solve(input);
        res.sort();
        if (res.size() == 0) {
            System.out.println(0);
        } else {
            for (List<Integer> list: res) {
                System.out.println(list);
            }
        }
    }
}

/**
 * 新入职的小王，一直在忙于做一个项目，埋头写了大量的代码，项目快结束时，他发现自己的项目里存在着一些循环依赖，
 * 比如file1依赖file2，file2又依赖了file1，那么它们就循环依赖，还有隐藏比较深的
 * file1依赖file2，file2依赖了file3，file3依赖了file1，那么这3个文件循环依赖。
 * 请你帮小王设计一款循环依赖的分析器，输出项目里面的循环依赖的文件集合
 *
 * 编译器版本: Java 1.8.0_66
        请使用标准输入输出(System.in, System.out)；已禁用图形、文件、网络、系统相关的操作，如java.lang.Process , javax.swing.JFrame , Runtime.getRuntime；不要自定义包名称，否则会报错，即不要添加package answer之类的语句；您可以写很多个类，但是必须有一个类名为Main，并且为public属性，并且Main为唯一的public class，Main类的里面必须包含一个名字为'main'的静态方法（函数），这个方法是程序的入口
        时间限制: 3S (C/C++以外的语言为: 5 S)   内存限制: 128M (C/C++以外的语言为: 640 M)
        输入:
        第一行表示文件数n 接下来的n行里面第m行表示第m个文件依赖的文件，多个用英文逗号隔开，如果没有则用0表示
        输出:
        输出循环依赖的文件列表，按顺序从小到大排序，用英文逗号隔开，多个循环列表则分多行展示。如果没有则输出0
        输入范例:
        3
        2,3
        3
        1
        输出范例:
        1,2,3
 */