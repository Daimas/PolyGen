package net.lrstudios.java.polygen;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;


public class MainFrame extends JFrame
        implements ActionListener, KeyListener, ItemListener, ChangeListener
{
    final String APP_TITLE = "PolyGen - v0.2";

    private static final String
            ACTION_GENERATE = "generate",
            ACTION_EXPORT = "export";

    private JSpinner jsp_numEdges = new JSpinner(new SpinnerNumberModel(5, 1, 9999999, 1));
    private JLabel jl_numEdges = new JLabel("Number of sides :");
    private JButton jb_generate = new JButton("Generate");
    private JButton jb_export = new JButton("Export");
    private JPanel jp_numEdges = new JPanel();

    private JCheckBox jcb_showLines = new JCheckBox("Diagonals");
    private JCheckBox jcb_showInfo = new JCheckBox("Infos");
    private JCheckBox jcb_antialias = new JCheckBox("Antialias");
    private JCheckBox jcb_x2 = new JCheckBox("By 2");
    private JPanel jp_options = new JPanel();

    private JPanel jp_top = new JPanel();
    private PolyPanel polyPane = new PolyPanel();


    public MainFrame() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle(APP_TITLE);

        this.setSize(800, 870);

        ((JSpinner.DefaultEditor) jsp_numEdges.getEditor()).getTextField().setColumns(4);

        jsp_numEdges.getEditor().addKeyListener(this);
        jsp_numEdges.addChangeListener(this);
        jb_generate.setActionCommand(ACTION_GENERATE);
        jb_generate.addActionListener(this);
        jb_export.setActionCommand(ACTION_EXPORT);
        jb_export.addActionListener(this);

        jp_numEdges.add(jl_numEdges);
        jp_numEdges.add(jsp_numEdges);
        jp_numEdges.add(jb_generate);
        jp_numEdges.add(jb_export);

        jcb_showLines.addItemListener(this);
        jcb_showLines.setSelected(true);
        jcb_showInfo.addItemListener(this);
        jcb_showInfo.setSelected(true);
        jcb_antialias.addItemListener(this);
        jcb_antialias.setSelected(true);
        jcb_x2.addItemListener(this);
        jp_options.add(jcb_showLines);
        jp_options.add(jcb_showInfo);
        jp_options.add(jcb_antialias);
        jp_options.add(jcb_x2);

        jp_top.setLayout(new GridLayout());
        jp_top.add(jp_numEdges);
        jp_top.add(jp_options);

        this.setLayout(new BorderLayout());
        this.add(jp_top, BorderLayout.SOUTH);
        this.add(polyPane, BorderLayout.CENTER);

        generate();
    }

    private void generate() {
        try {
            polyPane.setEdgeCount((Integer) jsp_numEdges.getValue());
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "The number of sides is invalid.", "Error", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Exports the polygon to a PNG file.
     */
    private void export() {
        JFileChooser chooser = new JFileChooser();
        chooser.setApproveButtonText("Export");
        chooser.setSelectedFile(new File("poly.png"));

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            BufferedImage saveBuf = new BufferedImage(polyPane.getSize().width, polyPane.getSize().height, BufferedImage.TYPE_3BYTE_BGR);
            polyPane.print(saveBuf.getGraphics());

            try {
                ImageIO.write(saveBuf, "PNG", new FileImageOutputStream(chooser.getSelectedFile()));
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(this, "An error occurred while writing the image : " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Affiche les lignes reliant les sommets du polygone
    private void showLines(boolean show) {
        polyPane.drawLines(show);
    }

    private void showInfo(boolean show) {
        polyPane.mShowInfo = show;
        polyPane.repaint();
    }

    private void antialias(boolean show) {
        polyPane.setAntialias(show);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals(ACTION_GENERATE))
            generate();
        else if (action.equals(ACTION_EXPORT))
            export();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            generate();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        boolean checked = e.getStateChange() == ItemEvent.SELECTED;

        if (source == jcb_showLines)
            showLines(checked);
        else if (source == jcb_showInfo)
            showInfo(checked);
        else if (source == jcb_antialias)
            antialias(checked);
        else if (source == jcb_x2)
            jsp_numEdges.setModel(new SpinnerNumberModel(5, 1, 9999999, checked ? 2 : 1));
    }


    /**
     * A spinner value changed
     */
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == jsp_numEdges)
            generate();
    }
}
