package net.lrstudios.java.polygen;

import java.awt.*;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;


public class LRPolygon {
    public ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();

    private int mRefNodeId = -1;
    private int mRefJumpCount = -1;
    private int mRefSideCount = -1;
    private ArrayList<ArrayList<NodePathElem>> mValidPaths = new ArrayList<ArrayList<NodePathElem>>();


    public void add(double x, double y) {
        points.add(new Point2D.Double(x, y));
    }

    /**
     * Counts the total number of internal triangles in this polygon using a very fast algorithm.
     */
    public BigInteger countTriangles_fast() {
        final int BD_SCALE = 12;
        BigDecimal n = BigDecimal.valueOf(points.size());

        if (n.compareTo(BigDecimal.valueOf(4)) == 0)
            return BigInteger.valueOf(8);

        BigDecimal pn =
                n.multiply(n.subtract(BigDecimal.ONE)).multiply(
                        (n.subtract(BigDecimal.valueOf(2)))).multiply(
                        n.pow(3).add(BigDecimal.valueOf(18).multiply(n.pow(2))).subtract(
                                BigDecimal.valueOf(43).multiply(n)).add(BigDecimal.valueOf(60))).divide(
                        BigDecimal.valueOf(720));

        BigDecimal[] tn_table = {
                MathUtil.mod_int(n, 2).multiply(n.subtract(BigDecimal.valueOf(2))).multiply(n.subtract(BigDecimal.valueOf(7))).multiply(n.divide(BigDecimal.valueOf(8.0), BD_SCALE, RoundingMode.HALF_UP)),
                MathUtil.mod_int(n, 4).multiply(BigDecimal.valueOf(3).multiply(n).divide(BigDecimal.valueOf(4.0), BD_SCALE, RoundingMode.HALF_UP)),
                MathUtil.mod_int(n, 6).multiply(BigDecimal.valueOf(18).multiply(n).subtract(BigDecimal.valueOf(106))).multiply(n.divide(BigDecimal.valueOf(3.0), BD_SCALE, RoundingMode.HALF_UP)),
                MathUtil.mod_int(n, 12).multiply(BigDecimal.valueOf(33).multiply(n)),
                MathUtil.mod_int(n, 18).multiply(BigDecimal.valueOf(36).multiply(n)),
                MathUtil.mod_int(n, 24).multiply(BigDecimal.valueOf(24).multiply(n)),
                MathUtil.mod_int(n, 30).multiply(BigDecimal.valueOf(96).multiply(n)),
                MathUtil.mod_int(n, 42).multiply(BigDecimal.valueOf(72).multiply(n)),
                MathUtil.mod_int(n, 60).multiply(BigDecimal.valueOf(264).multiply(n)),
                MathUtil.mod_int(n, 84).multiply(BigDecimal.valueOf(96).multiply(n)),
                MathUtil.mod_int(n, 90).multiply(BigDecimal.valueOf(48).multiply(n)),
                MathUtil.mod_int(n, 120).multiply(BigDecimal.valueOf(96).multiply(n)),
                MathUtil.mod_int(n, 210).multiply(BigDecimal.valueOf(48).multiply(n))
        };

        int highest_index = -1;
        for (int i = tn_table.length - 1; i >= 0; i--) {
            if (tn_table[i].compareTo(BigDecimal.ZERO) > 0) {
                highest_index = i;
                break;
            }
        }

        BigDecimal Tn = BigDecimal.ZERO;
        if (highest_index >= 0) {
            for (int i = 0; i < highest_index; i++) {
                if (tn_table[i + 1].compareTo(BigDecimal.ZERO) == 0)
                    Tn = Tn.subtract(tn_table[i]);
                else
                    Tn = Tn.add(tn_table[i]);
            }
            Tn = Tn.subtract(tn_table[highest_index]);
            Tn = Tn.abs();
        }

		/*System.out.println("P(n) = " + Pn);
        System.out.println("T(n) = " + Tn);
		System.out.println();
		System.out.println("n = " + n + " :   P(n) - T(n) = " + (Pn - Tn));*/

        return pn.subtract(Tn).toBigInteger();
    }

    /**
     * Counts the total number of internal polygons with the specified number of sides in this polygon.
     * It includes the internal polygons composed themselves of other internal polygons.
     */
    public int countPolygons(int sides) {
        this.mRefSideCount = sides;

        final int pointCount = this.points.size();
        ArrayList<Point> lines = new ArrayList<Point>();
        for (int i = 0; i < pointCount - 1; i++)
            for (int k = i + 1; k < pointCount; k++)
                lines.add(new Point(i, k));

        ArrayList<Intersection> intersections = new ArrayList<Intersection>();
        final int lineCount = lines.size();
        for (int i = 0; i < lineCount; i++) {
            for (int k = i + 1; k < lineCount; k++) {
                Point num_d1 = lines.get(i);
                Point num_d2 = lines.get(k);

                Point2D.Double d11 = new Point2D.Double(points.get(num_d1.x).x, points.get(num_d1.x).y);
                Point2D.Double d12 = new Point2D.Double(points.get(num_d1.y).x, points.get(num_d1.y).y);
                Point2D.Double d21 = new Point2D.Double(points.get(num_d2.x).x, points.get(num_d2.x).y);
                Point2D.Double d22 = new Point2D.Double(points.get(num_d2.y).x, points.get(num_d2.y).y);

                double[] eq1 = MathUtil.lineEquation(d11.x, d11.y, d12.x, d12.y);
                double[] eq2 = MathUtil.lineEquation(d21.x, d21.y, d22.x, d22.y);

                Point2D.Double pt = MathUtil.intersect(eq1, eq2);
                if (pt != null) {
                    // Only add intersections located in the circle
                    double dist = Math.sqrt(Math.pow(pt.x - 0.5, 2) + Math.pow(pt.y - 0.5, 2));
                    if (dist <= 0.50001)
                        intersections.add(new Intersection(pt.x, pt.y, i, k));
                }
            }
        }

        // Remove duplicates (or points very close to each other)
        for (int i = 0; i < intersections.size() - 1; i++) {
            Intersection int1 = intersections.get(i);

            for (int k = i + 1; k < intersections.size(); k++) {
                Intersection int2 = intersections.get(k);
                double shiftX = Math.abs(int1.x - int2.x);
                double shiftY = Math.abs(int1.y - int2.y);

                if (shiftX < 0.0000001 && shiftY < 0.0000001) {
                    int1.addLines(int2.mLineList);
                    intersections.remove(k--);
                }
            }
        }

        // Round some coordinates, because floating point divisions can result in very small differences
        // although they should give the exact same result (0.49999... should equal 0.500...001)
        // TODO test performance if done before searching for duplicates
        final int numIntersections = intersections.size();
        for (int i = 0; i < numIntersections; i++) {
            Intersection int1 = intersections.get(i);
            for (int k = i + 1; k < numIntersections; k++) {
                Intersection int2 = intersections.get(k);
                double shiftX = Math.abs(int1.x - int2.x);
                double shiftY = Math.abs(int1.y - int2.y);

                if (shiftX < 0.0000001)
                    int2.x = int1.x;
                if (shiftY < 0.0000001)
                    int2.y = int1.y;
            }
        }

        ArrayList<PolyNode> nodeList = new ArrayList<PolyNode>();
        ArrayList<ArrayList<PolyNode>> nodesPerLine = new ArrayList<ArrayList<PolyNode>>();
        for (int i = 0; i < lineCount; i++)
            nodesPerLine.add(new ArrayList<PolyNode>());

        for (int i = 0; i < numIntersections; i++) {
            Intersection inter = intersections.get(i);
            PolyNode node = new PolyNode(inter.x, inter.y);
            nodeList.add(node);

            for (Integer line : inter.mLineList)
                nodesPerLine.get(line).add(node);
        }

        // Link nodes between themselves
        for (int i = 0; i < numIntersections; i++) {
            Intersection inter = intersections.get(i);

            // Find direct intersections for each line passing by the current intersection
            for (Integer line : inter.mLineList) {
                // For each intersection of the line, check if there is another one which could
                // be between this intersection the the current intersection
                ArrayList<PolyNode> lineNodeList = nodesPerLine.get(line);
                for (int k = 0; k < lineNodeList.size(); k++) {
                    PolyNode int1 = lineNodeList.get(k);
                    if (inter.x == int1.x && inter.y == int1.y) {
                        continue;
                    }

                    PolyNode currentNode = null;
                    boolean ok = true;

                    for (int n = 0; n < lineNodeList.size(); n++) {
                        PolyNode int2 = lineNodeList.get(n);
                        if (inter.x == int2.x && inter.y == int2.y) {
                            currentNode = int2;
                            continue;
                        }
                        else if (k == n) {
                            continue;
                        }

                        // Is int2 between int1 and inter?
                        boolean horizontalAlign = (int1.x <= int2.x && int2.x <= inter.x) || (int1.x >= int2.x && int2.x >= inter.x);
                        boolean verticalAlign = (int1.y <= int2.y && int2.y <= inter.y) || (int1.y >= int2.y && int2.y >= inter.y);

                        if (horizontalAlign && verticalAlign) {
                            ok = false;
                            break;
                        }
                    }

                    if (ok) {
                        currentNode.connectedNodes.add(new ConnectedNodeData(int1, line));
                    }
                }
            }
        }

        int count = 0;
        for (int i = sides; i < sides + 12; i++) {
            int jpCount = jumpTest(nodeList, i);
            System.out.printf("Polygons with %d sides, and %d jumps : %d%n", sides, i, jpCount);
            count += jpCount;
        }

        return count;
    }


    /**
     * Calls a recursive function to determine how many different paths exist to go back
     * to the first node by jumping "numJumps" times.
     */
    private int jumpTest(ArrayList<PolyNode> nodeList, int numJumps) {
        mValidPaths.clear();

        for (PolyNode node : nodeList) {
            this.mRefNodeId = node.id;
            this.mRefJumpCount = numJumps;
            _rec_jumpTest(new ConnectedNodeData(node, -1), numJumps, new ArrayList<NodePathElem>());
        }

        System.out.println(mValidPaths.size());
        return mValidPaths.size();
    }

    private void _rec_jumpTest(ConnectedNodeData curNode, int numJumps, ArrayList<NodePathElem> currentPath) {
        ArrayList<NodePathElem> newPath = new ArrayList<NodePathElem>(currentPath);

        // Add curNode to the current path only if it's the first jump
        if (numJumps < mRefJumpCount)
            newPath.add(new NodePathElem(curNode.polyNode.id, curNode.lineId));

        if (numJumps > 0) {
            // Call this function again for each node connected to curNode, and by decrementing
            // the remaining amount of jumps to do
            for (ConnectedNodeData nextConnectedNode : curNode.polyNode.connectedNodes) {
                // Never use the same path (node) twice
                boolean exist = false;
                for (NodePathElem elem : currentPath) {
                    if (nextConnectedNode.polyNode.id == elem.nodeId) {
                        exist = true;
                        break;
                    }
                }

                if (!exist)
                    _rec_jumpTest(nextConnectedNode, numJumps - 1, newPath);
            }
        }
        else if (mRefNodeId == curNode.polyNode.id) // We are back to the first node
        {
            int[] nodeIDs = new int[newPath.size()];
            int[] lineIDs = new int[newPath.size()];

            for (int i = 0; i < newPath.size(); i++) {
                nodeIDs[i] = newPath.get(i).nodeId;
                lineIDs[i] = newPath.get(i).lineId;
            }

            // Sorting the array allow to remove duplicates faster
            Arrays.sort(nodeIDs);

            // Search if the path already exists
            boolean exist = false;
            for (ArrayList<NodePathElem> list : mValidPaths) {
                int n;
                for (n = 0; n < nodeIDs.length; n++)
                    if (nodeIDs[n] != list.get(n).nodeId)
                        break;

                if (n == nodeIDs.length) {
                    exist = true;
                    break;
                }
            }

            if (!exist) {
                // Only keep paths with the desired number of sides
                ArrayList<Integer> nDistincts = new ArrayList<Integer>();
                for (int n = 0; n < lineIDs.length; n++) {
                    exist = false;
                    for (Integer nDroite : nDistincts) {
                        if (lineIDs[n] == nDroite)
                            exist = true;
                    }

                    if (!exist)
                        nDistincts.add(lineIDs[n]);
                }

                if (nDistincts.size() == mRefSideCount) {
                    newPath.clear();
                    for (int i = 0; i < nodeIDs.length; i++)
                        newPath.add(new NodePathElem(nodeIDs[i], lineIDs[i]));

                    mValidPaths.add(newPath);
                }
            }
        }
    }


    /**
     * Returns the total number of intersections of the diagonals of the polygon.
     */
    public BigInteger countIntersections() {
        BigDecimal n = BigDecimal.valueOf(points.size());

        if (n.compareTo(BigDecimal.valueOf(3)) <= 0)
            return BigInteger.ZERO;

        BigDecimal res =
                MathUtil.binomial(n, 4).add(
                        MathUtil.mod_int(n, 2).multiply((BigDecimal.valueOf(-5).multiply(n.pow(3)).add(BigDecimal.valueOf(45).multiply(n.pow(2))).subtract(BigDecimal.valueOf(70).multiply(n)).add(BigDecimal.valueOf(24))).divide(BigDecimal.valueOf(24)))).subtract(
                        MathUtil.mod_int(n, 4).multiply(BigDecimal.valueOf(3).multiply(n).divide(BigDecimal.valueOf(2)))).add(
                        MathUtil.mod_int(n, 6).multiply(BigDecimal.valueOf(-45).multiply(n.pow(2)).add(BigDecimal.valueOf(262).multiply(n))).divide(BigDecimal.valueOf(6))).add(
                        MathUtil.mod_int(n, 12).multiply(BigDecimal.valueOf(42).multiply(n))).add(
                        MathUtil.mod_int(n, 18).multiply(BigDecimal.valueOf(60).multiply(n))).add(
                        MathUtil.mod_int(n, 24).multiply(BigDecimal.valueOf(35).multiply(n))).subtract(
                        MathUtil.mod_int(n, 30).multiply(BigDecimal.valueOf(38).multiply(n))).subtract(
                        MathUtil.mod_int(n, 42).multiply(BigDecimal.valueOf(82).multiply(n))).subtract(
                        MathUtil.mod_int(n, 60).multiply(BigDecimal.valueOf(330).multiply(n))).subtract(
                        MathUtil.mod_int(n, 84).multiply(BigDecimal.valueOf(144).multiply(n))).subtract(
                        MathUtil.mod_int(n, 90).multiply(BigDecimal.valueOf(96).multiply(n))).subtract(
                        MathUtil.mod_int(n, 120).multiply(BigDecimal.valueOf(144).multiply(n))).subtract(
                        MathUtil.mod_int(n, 210).multiply(BigDecimal.valueOf(96).multiply(n)));

        return res.toBigInteger();
    }


    /**
     * Returns the number of lines contained by a polygon with the specified number of sides.
     */
    public static int lineCount(int sides) {
        return (sides * (sides - 3)) / 2;
    }
}
