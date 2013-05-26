package net.lrstudios.java.polygen;


/**
 * Repr�sente une �tape d'un chemin de noeuds (PolyNode).
 */
public class NodePathElem
{
	public int nodeId;
	
	/** Num�ro de la droite par laquelle on est arriv� au noeud actuel (d'ID 'nodeId'). */
	public int nDroite;
	
	
	public NodePathElem(int nodeId, int nDroite)
	{
		this.nodeId = nodeId;
		this.nDroite = nDroite;
	}
}
