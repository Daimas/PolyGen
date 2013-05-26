package net.lrstudios.java.polygen;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;


// Contient le dessin d'un polygone. Pour le dessiner, il faut appeler les fonctions d�finies
// dans cette classe qui permettent de choisir les diff�rents param�tres.
public class PolyPanel extends JPanel
{
	private static final long serialVersionUID = 2016047410252182934L;
	
	private boolean antialias = false;
	private boolean showLines = false;
	public boolean showInfos = false;
	private int aretes = 0;
	private LRPolygon poly;
	
	
	// Constructeur
	public PolyPanel()
	{
	}
	
	
	// [Set] Le nombre d'ar�tes du polygone
	public void setAretes(int aretes)
	{
		if (aretes > 0)
		{
			this.aretes = aretes;
			createPoly();
			repaint();
		}
	}
	
	// [Get] Le nombre d'ar�tes du polygone
	public int getAretes()
		{ return this.aretes; }
	
	public void setAntialias(boolean state)
	{
		this.antialias = state;
		repaint();
	}
	
	
	// Dessine les lignes reliant les sommets du polygone si le param�tre vaut vrai.
	public void drawLines(boolean show)
	{
		this.showLines = show;
		repaint();
	}
	
	
	// Recr�e le polygone � partir des donn�es actuelles
	private void createPoly()
	{
		// Les coordonn�es dans le polygone sont d'abord calcul�es pour des valeurs entre 0 et 1
		poly = new LRPolygon();
		
		double n = Math.PI / 2 - (Math.PI / aretes);
		for (int i = 0; i < aretes; i++)
		{
			poly.add((Math.cos(n) + 1.0) / 2.0, (Math.sin(n) + 1.0) / 2.0);
			
			n += (Math.PI * 2.0) / aretes;
		}
	}
	
	// Transforme les coordonn�es du point depuis des coordonn�es relatives (entre 0 et 1)
	// vers des coordonn�es relatives au rectangle fourni en param�tre.
	private static Point translateCoords(double x, double y, Rectangle rect)
	{
		return new Point(
				rect.x + (int)Math.round(x * rect.width),
				rect.y + (int)Math.round(y * rect.height));
	}
	
	
	//////////////////////////////
	// 
	// Paint()
	//
	public void paintComponent(Graphics gr)
	{
		long debut = System.currentTimeMillis();
		
		Graphics2D g = (Graphics2D)gr;
		super.paintComponent(g);
		
		// remplir le fond
		Rectangle rect = g.getClipBounds();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, rect.width, rect.height);
		g.setColor(Color.BLACK);
		
		if (aretes > 0)
		{
			final int MARGIN = 30;
			Rectangle thisRect = this.getBounds();
			Rectangle drawingRect = new Rectangle(thisRect);
			drawingRect.x = MARGIN;
			drawingRect.y = MARGIN;
			drawingRect.width -= MARGIN * 2;
			drawingRect.height -= MARGIN * 2;
			
			// La zone de dessin du polygone doit �tre carr�e pour ne pas le d�former
			if (drawingRect.width < drawingRect.height)
				drawingRect.height = drawingRect.width;
			else
				drawingRect.width = drawingRect.height;
			
			if (thisRect.width - MARGIN * 2 > drawingRect.width)
				drawingRect.x += (thisRect.width - MARGIN * 2 - drawingRect.width) / 2;
			if (thisRect.height - MARGIN * 2 > drawingRect.height)
				drawingRect.y += (thisRect.height - MARGIN * 2 - drawingRect.height) / 2;
			
			
			// Cr�ation du polygone � dessiner
			Polygon drawingPoly = new Polygon();
			
			// Ne pas d�passer 1000 points
			int nbPoints = poly.points.size();
			int increment = 1 + (int)(nbPoints / 1000);
			
			for (int i = 0; i < poly.points.size(); i += increment)
			{
				Point pt = translateCoords(poly.points.get(i).x, poly.points.get(i).y, drawingRect);
				drawingPoly.addPoint(pt.x, pt.y);
			}
			
			// Infos
			if (showInfos)
			{
				NumberFormat nf = NumberFormat.getInstance(Locale.FRENCH);
				g.drawString("Nombre d'ar�tes : " + nf.format(aretes), 5, 15);
				g.drawString("Nombre d'intersections : " + nf.format(poly.compteIntersections()), 5, 32);
				g.drawString("Nombre de triangles : " + nf.format(poly.compteTriangles_fast()), 5, 49);
			}
			
			// Dessin
			if (this.antialias)
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			else
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			
			g.drawPolygon(drawingPoly);
			
			
			// Dessiner les lignes reliant les sommets si drawLines vaut vrai
			if (showLines)
			{
				g.setColor(Color.BLUE);
				
				for (int i = 0; i < drawingPoly.npoints - 2; i++)
					for (int k = i + 2; k < (i + 2) + (drawingPoly.npoints - 3) && k < drawingPoly.npoints; k++)
						g.drawLine(drawingPoly.xpoints[i], drawingPoly.ypoints[i], drawingPoly.xpoints[k], drawingPoly.ypoints[k]);
			}
		}
		
		System.out.println("Temps d'ex�cution : " + (System.currentTimeMillis() - debut) + " ms");
	}
}
