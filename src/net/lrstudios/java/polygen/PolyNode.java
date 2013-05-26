package net.lrstudios.java.polygen;

import java.util.ArrayList;



/**
 * Repr�sente un noeud servant au calcul du nombre de polygones internes composant un polygone.
 */
public class PolyNode
{
	private static int nextId = 1;
	
	public int id;
	public ArrayList<ConnectedNodeData> connectedNodes = new ArrayList<ConnectedNodeData>();
	double x;
	double y;
	
	
	// Constructeurs
	public PolyNode(double x, double y)
	{
		this.id = nextId++;
		this.x = x;
		this.y = y;
	}
	public PolyNode()
		{ this(0, 0); }
	
	@Override
	public String toString()
	{
		return "PolyNode ID=" + id + " : [x=" + x + ", y=" + y + "]";
	}
}
