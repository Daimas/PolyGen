package net.lrstudios.java.polygen;

import java.awt.*;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;


public class LRPolygon
{
	public ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
	
	// Variables utilis�es pour compter les polygones internes
	private int _refNodeId = -1;
	private int _refNbSauts = -1;
	private int _refNbAretes = -1;
	private ArrayList<ArrayList<NodePathElem>> _cheminsValides = new ArrayList<ArrayList<NodePathElem>>();
	
	
	/**
	 * Ajoute un point de coordonn�es sp�cifi�es au polygone.
	 */
	public void add(double x, double y)
		{ points.add(new Point2D.Double(x, y)); }
	
	
	
	public BigInteger compteTriangles_fast()
	{
		int bd_scale = 12; // Pr�cision (nombre de d�cimales) pour les calculs
		BigDecimal n = BigDecimal.valueOf(points.size());
		
		if (n.compareTo(BigDecimal.valueOf(4)) == 0)
			return BigInteger.valueOf(8);
		
		BigDecimal Pn =
			n.multiply(n.subtract(BigDecimal.ONE)).multiply(
			(n.subtract(BigDecimal.valueOf(2)))).multiply(
			n.pow(3).add(BigDecimal.valueOf(18).multiply(n.pow(2))).subtract(BigDecimal.valueOf(43).multiply(n)).add(BigDecimal.valueOf(60))).divide(
					BigDecimal.valueOf(720));
		
		BigDecimal[] Tn_table = {
			LRMath.mod_int(n, 2).multiply(n.subtract(BigDecimal.valueOf(2))).multiply(n.subtract(BigDecimal.valueOf(7))).multiply(n.divide(BigDecimal.valueOf(8.0), bd_scale, RoundingMode.HALF_UP)),
			LRMath.mod_int(n, 4).multiply(BigDecimal.valueOf(3).multiply(n).divide(BigDecimal.valueOf(4.0), bd_scale, RoundingMode.HALF_UP)),
			LRMath.mod_int(n, 6).multiply(BigDecimal.valueOf(18).multiply(n).subtract(BigDecimal.valueOf(106))).multiply(n.divide(BigDecimal.valueOf(3.0), bd_scale, RoundingMode.HALF_UP)),
			LRMath.mod_int(n, 12).multiply(BigDecimal.valueOf(33).multiply(n)),
			LRMath.mod_int(n, 18).multiply(BigDecimal.valueOf(36).multiply(n)),
			LRMath.mod_int(n, 24).multiply(BigDecimal.valueOf(24).multiply(n)),
			LRMath.mod_int(n, 30).multiply(BigDecimal.valueOf(96).multiply(n)),
			LRMath.mod_int(n, 42).multiply(BigDecimal.valueOf(72).multiply(n)),
			LRMath.mod_int(n, 60).multiply(BigDecimal.valueOf(264).multiply(n)),
			LRMath.mod_int(n, 84).multiply(BigDecimal.valueOf(96).multiply(n)),
			LRMath.mod_int(n, 90).multiply(BigDecimal.valueOf(48).multiply(n)),
			LRMath.mod_int(n, 120).multiply(BigDecimal.valueOf(96).multiply(n)),
			LRMath.mod_int(n, 210).multiply(BigDecimal.valueOf(48).multiply(n))
		};
		
		int highest_index = -1;
		for (int i = Tn_table.length - 1; i >= 0; i--)
		{
			if (Tn_table[i].compareTo(BigDecimal.ZERO) > 0)
			{
				highest_index = i;
				break;
			}
		}
		
		BigDecimal Tn = BigDecimal.ZERO;
		if (highest_index >= 0)
		{
			for (int i = 0; i < highest_index; i++)
			{
				if (Tn_table[i + 1].compareTo(BigDecimal.ZERO) == 0)
					Tn = Tn.subtract(Tn_table[i]);
				else
					Tn = Tn.add(Tn_table[i]);
			}
			Tn = Tn.subtract(Tn_table[highest_index]);
			Tn = Tn.abs();
		}
		
		/*System.out.println("P(n) = " + Pn);
		System.out.println("T(n) = " + Tn);
		System.out.println();
		System.out.println("n = " + n + " :   P(n) - T(n) = " + (Pn - Tn));*/
		
		return Pn.subtract(Tn).toBigInteger();
	}
	
	/**
	 * Compte le nombre de polygones internes composant le polygone, y compris les polygones
	 * internes compos�s eux m�mes de plusieurs polygones internes.
	 */
	public int comptePolygones(int aretes)
	{
		this._refNbAretes = aretes;
		
		/* Plusieurs �tapes :
		 * 
		 *  1. Lister les points composant les polygones internes, c'est � dire les sommets
		 *     ainsi que les intersections des droites et les organiser de fa�on � faire
		 *     un syst�me de noeuds reli�s entre eux :
		 *     chaque intersection est un noeud, qui peut �tre connect� � d'autres noeuds
		 *     (graphiquement, 2 noeuds connect�s sont 2 intersections reli�es directement l'une �
		 *     l'autre, c'est � dire sans qu'une autre intersection soit pr�sente entre les deux.)
		 * 
		 *  2. Compter les polygones internes. Pour cela, on utilise un syst�me de noeuds :
		 *     Pour d�tecter les triangles par exemple, on cherchera si en 3 "sauts" depuis un
		 *     noeud donn�, on peut revenir � ce noeud en passant par un autre chemin.
		 */
	    int nbPoints = this.points.size();
		
		// Lister les droites (d�finies par 2 num�ros de sommets)
		ArrayList<Point> droites = new ArrayList<Point>();
		
		for (int i = 0; i < nbPoints - 1; i++)
			for (int k = i + 1; k < nbPoints; k++)
				droites.add(new Point(i, k));
		
		
		// D�terminer les intersections des droites
		ArrayList<Intersection> intersections = new ArrayList<Intersection>();
		int nbDroites = droites.size();
		for (int i = 0; i < nbDroites; i++)
		{
			for (int k = i + 1; k < nbDroites; k++)
			{
				Point num_d1 = droites.get(i);
				Point num_d2 = droites.get(k);
				
				// Conversion de num�ros de sommets vers des coordonn�es r�elles
				Point2D.Double d11 = new Point2D.Double(points.get(num_d1.x).x, points.get(num_d1.x).y);
				Point2D.Double d12 = new Point2D.Double(points.get(num_d1.y).x, points.get(num_d1.y).y);
				Point2D.Double d21 = new Point2D.Double(points.get(num_d2.x).x, points.get(num_d2.x).y);
				Point2D.Double d22 = new Point2D.Double(points.get(num_d2.y).x, points.get(num_d2.y).y);
				
				double[] eq1 = LRMath.equationDroite(d11.x, d11.y, d12.x, d12.y);
				double[] eq2 = LRMath.equationDroite(d21.x, d21.y, d22.x, d22.y);
				
				Point2D.Double pt = LRMath.intersect(eq1, eq2);
				if (pt != null)
				{
					double distanceRayon = Math.sqrt(Math.pow(pt.x - 0.5, 2) + Math.pow(pt.y - 0.5, 2));
					if (distanceRayon <= 0.50001) // N'ajouter que les intersections qui se trouvent � l'int�rieur du cercle
						intersections.add(new Intersection(pt.x, pt.y, i, k));
				}
			}
		}
		
		// Eliminer les doublons
		for (int i = 0; i < intersections.size() - 1; i++)
		{
			Intersection int1 = intersections.get(i);
			
			for (int k = i + 1; k < intersections.size(); k++)
			{
				Intersection int2 = intersections.get(k);
				double ecartX = Math.abs(int1.x - int2.x);
				double ecartY = Math.abs(int1.y - int2.y);
				
				if (ecartX < 0.0000001 && ecartY < 0.0000001)
				{
					int1.ajouteDroites(int2.listeDroites);
					intersections.remove(k--);
				}
			}
		}
		
		int nbIntersections = intersections.size();
		
		// "Standardiser" les coordonn�es des intersections, car les divisions
		// de nombres flottants sont impr�cises (faire en sorte que 0.499999... == 0.500...001)
		//#######""TODO faire avant la rech. de doublons
		for (int i = 0; i < nbIntersections; i++)
		{
			Intersection int1 = intersections.get(i);
			for (int k = i + 1; k < nbIntersections; k++)
			{
				Intersection int2 = intersections.get(k);
				double ecartX = Math.abs(int1.x - int2.x);
				double ecartY = Math.abs(int1.y - int2.y);
				
				if (ecartX < 0.0000001)
					int2.x = int1.x;
				if (ecartY < 0.0000001)
					int2.y = int1.y;
			}
		}
		
		// R�organiser la liste des intersections : les lister pour chaque droite
		// et les stocker d�j� dans des noeuds
		ArrayList<PolyNode> listeNoeuds = new ArrayList<PolyNode>();
		ArrayList<ArrayList<PolyNode>> noeudsParDroites = new ArrayList<ArrayList<PolyNode>>();
		for (int i = 0; i < nbDroites; i++)
			noeudsParDroites.add(new ArrayList<PolyNode>());
		
		for (int i = 0; i < nbIntersections; i++)
		{
			Intersection inter = intersections.get(i);
			PolyNode node = new PolyNode(inter.x, inter.y);
			listeNoeuds.add(node);
			
			for (Integer droite : inter.listeDroites)
				noeudsParDroites.get(droite).add(node);
		}
		
		
		// Lier les noeuds entre eux
		for (int i = 0; i < nbIntersections; i++)
		{
			Intersection inter = intersections.get(i);
			
			// Chercher les intersections directes pour chaque droite passant par l'intersection actuelle
			for (Integer droite : inter.listeDroites)
			{
				// Pour chaque intersection de la droite, v�rifier qu'il n'y a pas une autre
				// intersection de cette droite qui serait entre cette intersection et
				// l'intersection actuelle
				ArrayList<PolyNode> nodeList = noeudsParDroites.get(droite);
				for (int k = 0; k < nodeList.size(); k++)
				{
					PolyNode int1 = nodeList.get(k);
					if (inter.x == int1.x && inter.y == int1.y)
						continue;
					
					PolyNode noeudActuel = null;
					boolean ok = true;
					
					for (int n = 0; n < nodeList.size(); n++)
					{
						PolyNode int2 = nodeList.get(n);
						if (inter.x == int2.x && inter.y == int2.y)
						{
							noeudActuel = int2;
							continue;
						}
						else if (k == n)
							continue;
						
						// Est-ce que int2 est situ�e entre int1 et inter?
						boolean aligne_horizontal = (int1.x <= int2.x && int2.x <= inter.x) || (int1.x >= int2.x && int2.x >= inter.x);
						boolean aligne_vertical = (int1.y <= int2.y && int2.y <= inter.y) || (int1.y >= int2.y && int2.y >= inter.y);
						
						if (aligne_horizontal && aligne_vertical)
						{
							ok = false;
							break;
						}
					}
					
					if (ok)
						noeudActuel.connectedNodes.add(new ConnectedNodeData(int1, droite));
				}
			}
		}
		
		/*System.out.println("-----------------------");
		for (PolyNode node : listeNoeuds)
		{
			System.out.println("N " + node.id + " [" + (LRMath.round(node.x, 2)) + ", " + (LRMath.round(node.y, 2)) + "] : ");
			for (ConnectedNodeData node2 : node.connectedNodes)
				System.out.println("  >> " + node2.polyNode.id + "  [" + (LRMath.round(node2.polyNode.x, 2)) + ", " + (LRMath.round(node2.polyNode.y, 2)) + "]");
		}*/
		
		
		/**************************************************
		 *  ETAPE 2
		 *  Compter les polygones internes.
		 */
		
		int count = 0;
		
		for (int i = aretes; i < aretes + 12; i++)
		{
			int jpCount = jumpTest(listeNoeuds, i);
			
			System.out.println("Polygones � " + aretes + " ar�tes, avec " + i + " sauts : " + jpCount);
			count += jpCount;
		}
		
		return count;
	}
	
	
	/** Appelle une fonction r�cursive permettant de d�terminer combien de fois il est possible de
	 * revenir au noeud de d�part en sautant "nbSauts" fois (de toutes les fa�ons possibles) en
	 * partant du noeud de d�part et en ne passant jamais 2 fois par le m�me chemin (cf. d�terminer
	 * le nombre de polygones internes).
	 * 
	 * Les r�sultats seront stock�s dans les ArrayList fournies en param�tre (ne doivent donc
	 * pas �tre null).
	 */
	private int jumpTest(ArrayList<PolyNode> nodeList, int nbSauts)
	{
		_cheminsValides.clear();
		
		for (PolyNode node : nodeList)
		{
			this._refNodeId = node.id;
			this._refNbSauts = nbSauts;
			_rec_jumpTest(new ConnectedNodeData(node, -1), nbSauts, new ArrayList<NodePathElem>());
		}
		
		System.out.println(_cheminsValides.size());
		return _cheminsValides.size();
	}

	// Fonction r�cursive utilis�e par la fonction jumpTest()
	private void _rec_jumpTest(ConnectedNodeData currNode, int nbSauts, ArrayList<NodePathElem> cheminActuel)
	{
		ArrayList<NodePathElem> nouveauChemin = new ArrayList<NodePathElem>(cheminActuel);
		
		// Ajouter currNode au chemin actuel, sauf s'il s'agit du premier saut
		if (nbSauts < _refNbSauts)
			nouveauChemin.add(new NodePathElem(currNode.polyNode.id, currNode.nDroite));
		
		if (nbSauts > 0)
		{
			// Rappeler cette fonction pour chaque noeud connect� � currNode,
			// en d�cr�mentant le nombre de sauts � effectuer (nbSauts)
			for (ConnectedNodeData nextConnectedNode : currNode.polyNode.connectedNodes)
			{
				// Ne pas repasser 2 fois par le m�me chemin (noeud)
				boolean existe = false;
				for (NodePathElem elem : cheminActuel)
				{
					if (nextConnectedNode.polyNode.id == elem.nodeId)
					{
						existe = true;
						break;
					}
				}
				
				if (!existe)
					_rec_jumpTest(nextConnectedNode, nbSauts - 1, nouveauChemin);
			}
		}
		else if (_refNodeId == currNode.polyNode.id) // On est revenu au point de d�part
		{
			// Chercher si ce chemin existe d�j�
			// Pour cela on trie d'abord l'ArrayList "nouveauChemin"
			int[] nodeIDsArr = new int[nouveauChemin.size()];
			int[] nDroitesArr = new int[nouveauChemin.size()];
			
			for (int i = 0; i < nouveauChemin.size(); i++)
			{
				nodeIDsArr[i] = nouveauChemin.get(i).nodeId;
				nDroitesArr[i] = nouveauChemin.get(i).nDroite;
			}
			
			// Trier permet d'�liminer les doublons plus rapidement
			Arrays.sort(nodeIDsArr);
			
			// Chercher maintenant si le chemin existe d�j�
			boolean existe = false;
			for (ArrayList<NodePathElem> list : _cheminsValides)
			{
				int n;
				for (n = 0; n < nodeIDsArr.length; n++)
					if (nodeIDsArr[n] != list.get(n).nodeId)
						break;
				
				if (n == nodeIDsArr.length)
				{
					existe = true;
					break;
				}
			}
			
			if (!existe)
			{
				// Eliminer ce qui n'est pas du type de polygone recherch�
				// (par exemple ne garder que les triangles)
				// Pour cela, rechercher les chemins qui passent par plus de "aretes" droites
				ArrayList<Integer> nDistincts = new ArrayList<Integer>();
				
				for (int n = 0; n < nDroitesArr.length; n++)
				{
					existe = false;
					for (Integer nDroite : nDistincts)
					{
						if (nDroitesArr[n] == nDroite)
							existe = true;
					}
					
					if (!existe)
						nDistincts.add(nDroitesArr[n]);
				}
				
				if (nDistincts.size() == _refNbAretes)
				{
					nouveauChemin.clear();
					for (int i = 0; i < nodeIDsArr.length; i++)
						nouveauChemin.add(new NodePathElem(nodeIDsArr[i], nDroitesArr[i]));
					
					_cheminsValides.add(nouveauChemin);
				}
			}
		}
	}
	
	
	/**
	 * Renvoie le nombre d'intersections des diagonales du polygone.
	 */
	public BigInteger compteIntersections()
	{
		BigDecimal n = BigDecimal.valueOf(points.size());
		
		if (n.compareTo(BigDecimal.valueOf(3)) <= 0)
			return BigInteger.ZERO;
		
		BigDecimal res =
			LRMath.binomial(n, 4).add(
			LRMath.mod_int(n, 2).multiply((BigDecimal.valueOf(-5).multiply(n.pow(3)).add(BigDecimal.valueOf(45).multiply(n.pow(2))).subtract(BigDecimal.valueOf(70).multiply(n)).add(BigDecimal.valueOf(24))).divide(BigDecimal.valueOf(24)))).subtract(
			LRMath.mod_int(n, 4).multiply(BigDecimal.valueOf(3).multiply(n).divide(BigDecimal.valueOf(2)))).add(
			LRMath.mod_int(n, 6).multiply(BigDecimal.valueOf(-45).multiply(n.pow(2)).add(BigDecimal.valueOf(262).multiply(n))).divide(BigDecimal.valueOf(6))).add(
			LRMath.mod_int(n, 12).multiply(BigDecimal.valueOf(42).multiply(n))).add(
			LRMath.mod_int(n, 18).multiply(BigDecimal.valueOf(60).multiply(n))).add(
			LRMath.mod_int(n, 24).multiply(BigDecimal.valueOf(35).multiply(n))).subtract(
			LRMath.mod_int(n, 30).multiply(BigDecimal.valueOf(38).multiply(n))).subtract(
			LRMath.mod_int(n, 42).multiply(BigDecimal.valueOf(82).multiply(n))).subtract(
			LRMath.mod_int(n, 60).multiply(BigDecimal.valueOf(330).multiply(n))).subtract(
			LRMath.mod_int(n, 84).multiply(BigDecimal.valueOf(144).multiply(n))).subtract(
			LRMath.mod_int(n, 90).multiply(BigDecimal.valueOf(96).multiply(n))).subtract(
			LRMath.mod_int(n, 120).multiply(BigDecimal.valueOf(144).multiply(n))).subtract(
			LRMath.mod_int(n, 210).multiply(BigDecimal.valueOf(96).multiply(n)));
		
		return res.toBigInteger();
	}
	
	
	
//---- Fontions STATIC ---------------------------------------
	
	/**
	 * Renvoie le nombre de droites internes que contient un polygone d'un nombre d'ar�tes donn�.
	 */
	public static int nombreDroites(int aretes)
	{
		return (aretes * (aretes - 3)) / 2;
	}
}
