package SyntacticAnalysis;

public class AbstractSyntaxTree {
    private final Node root;
    private Node currentNode;

    public AbstractSyntaxTree(Node root) {
        this.root = root;
        currentNode = root;
    }

    public void addChild(Node node, boolean isNewLevel) {
        currentNode.addChild(node);
        if (isNewLevel) {
            currentNode = node;
        }
    }

    public void removeChild() {
        currentNode.getChildren().removeLast();
    }

    public void toParent() {
        if (currentNode.getParent() != null) {
            currentNode = currentNode.getParent();
        }
    }

    public Node getRoot() {
        return root;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }

    public void print() {
        print(root, 0);
    }

    private void print(Node node, int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        System.out.println(node.getClass() + " " + node.getId() + " " + node.getBegin() + " " + node.getEnd() + " " + node.getParent());
        if (!node.isBlockNode()) {
            return;
        }
        for (Node child : node.getChildren()) {
            print(child, level + 1);
        }
    }
}
