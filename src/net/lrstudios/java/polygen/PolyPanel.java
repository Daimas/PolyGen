package net.lrstudios.java.polygen;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;


public class PolyPanel extends JPanel {

    /**
     * The maximum number of points to draw.
     */
    private static final int MAX_POLY_POINTS = 1000;

    /**
     * The margin around the polygon, in pixels.
     */
    private static final int MARGIN = 30;

    private boolean mAntialias = false;
    private boolean mShowLines = false;
    public boolean mShowInfo = false;
    private int mEdgeCount = 0;
    private LRPolygon poly;


    public PolyPanel() {
    }


    public void setEdgeCount(int edges) {
        if (edges > 0) {
            this.mEdgeCount = edges;
            createPoly();
            repaint();
        }
    }

    public int getEdgeCount() {
        return this.mEdgeCount;
    }

    public void setAntialias(boolean state) {
        this.mAntialias = state;
        repaint();
    }


    /**
     * Set whether or not the lines connecting the vertices should be drawn.
     */
    public void drawLines(boolean show) {
        this.mShowLines = show;
        repaint();
    }


    /**
     * Recreate the polygon using the current data.
     */
    private void createPoly() {
        poly = new LRPolygon();
        double n = Math.PI / 2 - (Math.PI / mEdgeCount);
        for (int i = 0; i < mEdgeCount; i++) {
            poly.add((Math.cos(n) + 1.0) / 2.0, (Math.sin(n) + 1.0) / 2.0);

            n += (Math.PI * 2.0) / mEdgeCount;
        }
    }

    private static Point translateCoords(double x, double y, Rectangle rect) {
        return new Point(
                rect.x + (int) Math.round(x * rect.width),
                rect.y + (int) Math.round(y * rect.height));
    }


    public void paintComponent(Graphics gr) {
        long debut = System.currentTimeMillis();

        Graphics2D g = (Graphics2D) gr;
        super.paintComponent(g);

        // Background
        Rectangle rect = g.getClipBounds();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rect.width, rect.height);
        g.setColor(Color.BLACK);

        if (mEdgeCount > 0) {
            Rectangle thisRect = this.getBounds();
            Rectangle drawingRect = new Rectangle(thisRect);
            drawingRect.x = MARGIN;
            drawingRect.y = MARGIN;
            drawingRect.width -= MARGIN * 2;
            drawingRect.height -= MARGIN * 2;

            // The drawing area must be a square
            if (drawingRect.width < drawingRect.height)
                drawingRect.height = drawingRect.width;
            else
                drawingRect.width = drawingRect.height;

            if (thisRect.width - MARGIN * 2 > drawingRect.width)
                drawingRect.x += (thisRect.width - MARGIN * 2 - drawingRect.width) / 2;
            if (thisRect.height - MARGIN * 2 > drawingRect.height)
                drawingRect.y += (thisRect.height - MARGIN * 2 - drawingRect.height) / 2;

            Polygon drawingPoly = new Polygon();
            int nbPoints = poly.points.size();
            int increment = 1 + (nbPoints / MAX_POLY_POINTS);
            for (int i = 0; i < poly.points.size(); i += increment) {
                Point pt = translateCoords(poly.points.get(i).x, poly.points.get(i).y, drawingRect);
                drawingPoly.addPoint(pt.x, pt.y);
            }

            if (mShowInfo) {
                NumberFormat nf = NumberFormat.getInstance(Locale.FRENCH);
                g.drawString("Total edges : " + nf.format(mEdgeCount), 5, 15);
                g.drawString("Total intersections : " + nf.format(poly.countIntersections()), 5, 32);
                g.drawString("Triangle count : " + nf.format(poly.countTriangles_fast()), 5, 49);
            }

            if (this.mAntialias)
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            else
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.drawPolygon(drawingPoly);

            if (mShowLines) {
                g.setColor(Color.BLUE);

                for (int i = 0; i < drawingPoly.npoints - 2; i++)
                    for (int k = i + 2; k < (i + 2) + (drawingPoly.npoints - 3) && k < drawingPoly.npoints; k++)
                        g.drawLine(drawingPoly.xpoints[i], drawingPoly.ypoints[i], drawingPoly.xpoints[k], drawingPoly.ypoints[k]);
            }
        }

        System.out.println("Execution time : " + (System.currentTimeMillis() - debut) + " ms");
    }
}
