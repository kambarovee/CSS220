import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

public class Ukkonens {
    //Variables
    private Node node;  // It contains the index from which we should pass the edge, from which the following begins
    private Node root;  // Root of the suffix tree.
    private Node nodeInternal; // Contains an internal node whose suffix link remains for completion.
    private String word;  // Given string
    private int edge; // Contains the index from which we should pass the edge, from which the following begins.
    private int length;  // Variable for how many characters/symbols we need to go through
    private int remainingSuffixCount; // The number of explicit extensions that need to be done for this step
    private int leafEnd; // Variable to maintain a constant expansion of suffixes

    // The method responsible for the process calls other methods. And shares step by step.
    public Ukkonens() {
        root = new Node(-1);
        leafEnd = -1;
        node = root;
        edge = -1;
        length = 0;
        remainingSuffixCount = 0;
    }

    //Process for steps
    public void process(String str) {
        word = str;
        for (int i = 0; i < word.length(); i++) {
            System.out.println(" ");
            System.out.println("****************************** Step " + (i + 1) + " **********************************");
            System.out.println(" ");
            processIndex(i);
            printTree();
        }
        printTree();
    }

    private void processIndex(int index) {
        // if leaf, append latest character
        // Update end value of all leaf edges by updating the global end variable
        System.out.println("Adding last character and update value of all edges of leaf ");
        leafEnd = index;
        remainingSuffixCount++;
        nodeInternal = null;
        while (remainingSuffixCount > 0) {
            //at the start of extension, when Length is zero,
            //edge is set to the current character being processed
            if (length == 0) {
                System.out.println("length is zero");
                edge = index;
                printPoint();
            }
            // Create a new leaf edge, if not before
            if (!node.edgeMap.containsKey(word.charAt(edge))) {
                System.out.println("Creating a new leaf");
                node.edgeMap.put(word.charAt(edge), new Node(index));
                if (nodeInternal != null) {
                    System.out.println("Set up suffixLink from last internal node to node");
                    nodeInternal.suffixLink = node;
                    nodeInternal = null;
                }

            } else {

                Node next = node.edgeMap.get(word.charAt(edge));
                //using point to save time on the walk down
                System.out.println("Using point to save time on the walk down");
                if (walkedDown(next)) {
                    continue;
                }
                //ends the current phase
                if (word.charAt(next.start + length) == word.charAt(index)) {
                    System.out.println("ends the current phase");
                    if (nodeInternal != null && node != root) {
                        nodeInternal.suffixLink = node;
                        nodeInternal = null;
                    }
                    length++;
                    printPoint();
                    break;
                }
                System.out.println("Split Node");
                int splitEnd = next.start + length - 1;
                Node split = new Node(next.start, splitEnd); //The suffix only partially matches the edge. Inner edge must be created.
                node.edgeMap.put(word.charAt(edge), split); // Replace the existing pointer with a new internal node.
                split.edgeMap.put(word.charAt(index), new Node(index)); // Add new leaf edge
                next.start += length; // move the old leaf node below the new inner node.
                split.edgeMap.put(word.charAt(next.start), next); // add a leaf node as a child of the created internal node.

                if (nodeInternal != null) {

                    System.out.println("Set the suffix Link from the last internal node to this newly created");
                    nodeInternal.suffixLink = split;
                }
                nodeInternal = split;
            }

            remainingSuffixCount--;
            //If node is root length is greater than 0, decrease the value of length,
            // edge = phase-remaining SuffixCount + 1 length gets closer to the root after each extension,
            // so reducing it helps point to the same index. Edge is modified to indicate the next character to be added.
            if (node == root && length > 0) {

                System.out.println("Node is root and length is greater than 0, then reduce length");
                length--;
                edge = index - remainingSuffixCount + 1;
                printPoint();
            } else if (node != root) {
                System.out.println("Node is not root, make node the previous node’s suffix link.");
                node = node.suffixLink;
                printPoint();
            }
        }
    }

    // If length <edge length, no transition is required.
    private boolean walkedDown(Node n) {
        // WD: change point to go
        // Convert point variables to synonymous with point from
        // shorter length when walking on a tree. For example, from the root node
        // to the internal node.
        if (length >= edgeLength(n)) {
            System.out.println("Point Change For Walk Down");
            node = n;
            edge += edgeLength(n);
            length -= edgeLength(n);
            printPoint();
            return true;
        }

        return false;
    }

    private int edgeLength(Node n) {
        return n.end != -1 ? n.end - n.start + 1 : leafEnd - n.start + 1;
    }

    // Labels the edges with -1 if it's an inner node or
    // 0 ... phrase.length () - 1 for the leaves and prints them in DFS order.
    public void dfsSetAndPrint(Node n, int labelHeight) {
        if (n == null) return;
        if (n.start != -1) {
            System.out.print(n);
        }
        boolean isLeaf = true;
        for (Entry<Character, Node> entry : n.edgeMap.entrySet()) {
            Node node = entry.getValue();
            if (isLeaf && n.start != -1) {
                System.out.println(" " + n.suffixIndex);
            }
            isLeaf = false;
            dfsSetAndPrint(node, labelHeight + edgeLength(node));
        }
        if (isLeaf) {
            n.suffixIndex = word.length() - labelHeight;
            System.out.println(" " + n.suffixIndex);
        }
    }

    class Node {
        int start; // start indexing the substring edge from parent to this.
        int end; // end of the substring index. -1 for the edges of the leaf, since they follow end.
        Map<Character, Node> edgeMap; // display from the first character visible in the edge to the node that contains the edge.
        Node suffixLink; // speeds up the node traversal by contacting the node with the same edge content.
        int suffixIndex; // used when printing a tree. -1 for internal nodes, 0 ... m for end nodes.

        Node(int start) {
            this.start = start;
            this.end = -1;
            edgeMap = new HashMap<>();
            suffixLink = root;
            suffixIndex = -1;
        }

        Node(int start, int end) {
            this.start = start;
            this.end = end;
            edgeMap = new HashMap<>();
            suffixLink = root;
            suffixIndex = -1;
        }

        @Override
        public String toString() {
            return end == -1 ? word.substring(start, leafEnd + 1) : word.substring(start, end + 1);
        }

    }

    private void printPoint() {
        StringBuilder sb = new StringBuilder();
        sb.append("Point (");
        if (node.start == -1) {
            sb.append("root, ");
        } else {
            sb.append(word.substring(node.start, node.start + 1));
            sb.append(", ");
        }
        sb.append(word.substring(edge, edge + 1));
        sb.append(", ");
        sb.append(length).append(")");
        System.out.println(sb.toString());
    }

    public void printTree() {
        dfsSetAndPrint(root, 0);
        if (true) {
            printPoint();
        }
    }

    //Running algorithm
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        Ukkonens suffixTree = new Ukkonens();
        System.out.println("Enter the string:");
        suffixTree.process(scan.next() + "$");
    }
}
