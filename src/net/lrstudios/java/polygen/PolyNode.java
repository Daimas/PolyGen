package net.lrstudios.java.polygen;

import java.util.ArrayList;


/**
 * Represents a node, used to compute the total number of internal polygons contained in a regular polygon.
 */
public class PolyNode {
    private static int nextId = 1;

    public int id;
    public ArrayList<ConnectedNodeData> connectedNodes = new ArrayList<ConnectedNodeData>();
    public double x;
    public double y;


    public PolyNode(double x, double y) {
        this.id = nextId++;
        this.x = x;
        this.y = y;
    }

    public PolyNode() {
        this(0, 0);
    }

    @Override
    public String toString() {
        return String.format("PolyNode ID=%d : [x=%s, y=%s]", id, x, y);
    }
}
