package net.lrstudios.java.polygen;

import java.util.ArrayList;


/**
 * Repr�sente une intersection de droites.
 * d1 et d2 sont facultatifs, ils peuvent repr�senter les num�ros des droites coup�es.
 */
public class Intersection
{
	public double x;
	public double y;
	public ArrayList<Integer> listeDroites = new ArrayList<Integer>();
	
	
	// Constructeurs
	public Intersection(double x, double y, int d1, int d2)
	{
		this.x = x;
		this.y = y;
		listeDroites.add(d1);
		listeDroites.add(d2);
	}
	
	public Intersection() { this(0, 0, 0, 0); }
	
	
	// Ajoute des droites � la liste de droites.
	public void ajouteDroites(ArrayList<Integer> droites)
	{
		for (Integer droite : droites)
		{
			if (!listeDroites.contains(droite))
				listeDroites.add(droite);
		}
	}
	
	/**
	 * Teste si deux intersections ont les m�mes coordonn�es.
	 */
	public boolean equals(Intersection inter)
	{
		return (inter.x == this.x && inter.y == this.y);
	}
}
