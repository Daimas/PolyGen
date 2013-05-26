package net.lrstudios.java.polygen;


/**
 * Represents a node connected to a reference node.
 */
public class ConnectedNodeData {
    /**
     * The node connected to the reference node.
     */
    public PolyNode polyNode;

    /**
     * The node is connected to the reference node by this line.
     */
    public int lineId;


    public ConnectedNodeData(PolyNode node, int lineId) {
        this.polyNode = node;
        this.lineId = lineId;
    }
}
