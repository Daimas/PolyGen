package net.lrstudios.java.polygen;


/**
 * Repr�sente un noeud connect� � un noeud de r�f�rence. (d�f. de "noeud" : cf. PolyNode)
 */
public class ConnectedNodeData
{
	/** Le noeud connect� au noeud de r�f�rence. */
	public PolyNode polyNode;
	
	/** D�signe la droite par lequel le noeud est connect� au noeud de r�f�rence. */
	public int nDroite;
	
	
	public ConnectedNodeData(PolyNode node, int nDroite)
	{
		this.polyNode = node;
		this.nDroite = nDroite;
	}
}
