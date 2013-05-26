package net.lrstudios.java.polygen;

import java.util.ArrayList;


/**
 * Represents an intersection of two lines.
 */
public class Intersection {
    public double x;
    public double y;
    public ArrayList<Integer> mLineList = new ArrayList<Integer>();


    /**
     * (x, y) : the coordinates of the intersection<br>
     * d1, d2 : the line IDs.
     */
    public Intersection(double x, double y, int d1, int d2) {
        this.x = x;
        this.y = y;
        mLineList.add(d1);
        mLineList.add(d2);
    }

    public Intersection() {
        this(0, 0, 0, 0);
    }


    public void addLines(ArrayList<Integer> lines) {
        for (Integer line : lines) {
            if (!mLineList.contains(line))
                mLineList.add(line);
        }
    }

    /**
     * Returns true if the specified intersection have the same coordinates.
     */
    public boolean equals(Intersection inter) {
        return (inter.x == this.x && inter.y == this.y);
    }
}
