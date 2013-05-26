package net.lrstudios.java.polygen;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;


// Frame principale du programme
public class MainFrame extends JFrame implements ActionListener, KeyListener, ItemListener, ChangeListener
{
	// Constantes
	final String APP_TITLE = "PGen - version 0.2";
	
	
	private static final long serialVersionUID = 2065194524855026548L;
	
	private JSpinner jsp_nbAretes = new JSpinner(new SpinnerNumberModel(5, 1, 9999999, 1));
	private JLabel jl_nbAretes = new JLabel("Nombre d'ar�tes :");
	private JButton jb_generer = new JButton("G�n�rer");
	private JButton jb_exporter = new JButton("Exporter");
	private JPanel jp_nbAretes = new JPanel();
	
	private JCheckBox jcb_afficheLignes = new JCheckBox("Diagonales");
	private JCheckBox jcb_afficheInfos = new JCheckBox("Infos");
	private JCheckBox jcb_antialias = new JCheckBox("Antialias");
	private JCheckBox jcb_x2 = new JCheckBox("Par 2");
	private JPanel jp_options = new JPanel();
	
	private JPanel jp_top = new JPanel();
	private PolyPanel polyPane = new PolyPanel();
	
	
	// Constructeur
	public MainFrame()
	{
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setTitle(APP_TITLE);
		
		this.setSize(800, 870);
        
		// Panel haut
		((JSpinner.DefaultEditor)jsp_nbAretes.getEditor()).getTextField().setColumns(4);
		
		jsp_nbAretes.getEditor().addKeyListener(this);
		jsp_nbAretes.addChangeListener(this);
        jb_generer.setActionCommand("generer");
        jb_generer.addActionListener(this);
        jb_exporter.setActionCommand("exporter");
        jb_exporter.addActionListener(this);
		
		jp_nbAretes.add(jl_nbAretes);
        jp_nbAretes.add(jsp_nbAretes);
        jp_nbAretes.add(jb_generer);
        jp_nbAretes.add(jb_exporter);
        
        jcb_afficheLignes.addItemListener(this);
        jcb_afficheLignes.setSelected(true);
        jcb_afficheInfos.addItemListener(this);
        jcb_afficheInfos.setSelected(true);
        jcb_antialias.addItemListener(this);
        jcb_antialias.setSelected(true);
        jcb_x2.addItemListener(this);
        jp_options.add(jcb_afficheLignes);
        jp_options.add(jcb_afficheInfos);
        jp_options.add(jcb_antialias);
        jp_options.add(jcb_x2);
        
        jp_top.setLayout(new GridLayout());
        jp_top.add(jp_nbAretes);
        jp_top.add(jp_options);
        
        // Panel principal
        this.setLayout(new BorderLayout());
        this.add(jp_top, BorderLayout.SOUTH);
        this.add(polyPane, BorderLayout.CENTER);
        
        generer();
	}


	// G�n�re le polygone
	private void generer()
	{
        try {
        	polyPane.setAretes((Integer)jsp_nbAretes.getValue());
        } catch (Exception ex) {
        	JOptionPane.showMessageDialog(this, "Le nombre d'ar�tes est incorrect", "Erreur", JOptionPane.INFORMATION_MESSAGE);
        }
	}
	
	// Exporte le polygone sous forme d'image
	private void exporter()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setApproveButtonText("Exporter"); 
		chooser.setSelectedFile(new File("poly.png"));
		
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			BufferedImage saveBuf = new BufferedImage(polyPane.getSize().width, polyPane.getSize().height, BufferedImage.TYPE_3BYTE_BGR);
			polyPane.print(saveBuf.getGraphics()); 
			
			try {
				ImageIO.write(saveBuf, "PNG", new FileImageOutputStream(chooser.getSelectedFile()));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Une erreur est survenue lors de l'�criture du fichier : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	// Affiche les lignes reliant les sommets du polygone
	private void afficheLignes(boolean affiche)
		{ polyPane.drawLines(affiche); }
	private void afficheInfos(boolean affiche)
		{ polyPane.showInfos = affiche; polyPane.repaint(); }
	private void antialias(boolean affiche)
		{ polyPane.setAntialias(affiche); }
	
	
//---- Ev�nements -----------------------------------------
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if ("generer".equals(e.getActionCommand()))
			generer();
		else if ("exporter".equals(e.getActionCommand()))
			exporter();
	}

	// Appui sur entr�e
	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
			generer();
	}
	
	@Override
	public void keyReleased(KeyEvent e) { }
	@Override
	public void keyTyped(KeyEvent e) { }


	// Changement d'�tat des checkBox
	@Override
	public void itemStateChanged(ItemEvent e)
	{
		Object source = e.getItemSelectable();
		boolean checked = e.getStateChange() == ItemEvent.SELECTED;
		
		if (source == jcb_afficheLignes)
			afficheLignes(checked);
		else if (source == jcb_afficheInfos)
			afficheInfos(checked);
		else if (source == jcb_antialias)
			antialias(checked);
		else if (source == jcb_x2)
			jsp_nbAretes.setModel(new SpinnerNumberModel(5, 1, 9999999, checked ? 2 : 1));
	}
	
	
	// Changement de la valeur des spinners
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == jsp_nbAretes)
			generer();
    }
}
