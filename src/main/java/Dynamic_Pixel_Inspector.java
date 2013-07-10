import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

import ij.gui.ImageCanvas;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * <p>
 * Dynamic Pixel Inspector ImageJ plugin.
 * </p>
 * 
 * <p>
 * This plugin allows the user to watch the intensity curve of each
 * pixel through the different frames by just moving the mouse pointer over
 * the image or, in the "Disabled" mode, by clicking over the image. 
 * The plugin works  properly with 4D (3D + time) or 3D (2D + time) images but 
 * the user must be careful to open the image in a way that the time 
 * information has been correctly placed (HyperStacks).
 * </p>
 * 
 * @author First version by 
 * <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Macías
 *         Gordaliza</a>, with later improvements by 
 *         <a href="mailto:jmmateos@hggm.es">José María Mateos</a>.
 * @version 1.0
 * 
 */

public class Dynamic_Pixel_Inspector implements PlugInFilter, ActionListener,
        WindowListener, MouseListener, MouseMotionListener,
        KeyListener {

    private ImagePlus imp;
    private ImageCanvas canvas;
    private ImageStack is;
    private Calibration cal;
    private PlotWindow pw;
    private JFrame jf;
    private JLabel enabled;
    private int dim[];
    private boolean moveFlag = true;
    private boolean invert = false;

    // run method from PlugInFilter
    public void run(ImageProcessor arg0) {

        enabled = new JLabel();
        enabled.setBounds(10, 11, 89, 23);      
        changeEnabledLabel();
        jf = createFrame();     
        jf.addWindowListener(this);
        jf.setVisible(true);
        turnOn();

    }

    // setup method from PlugInFilter
    public int setup(String arg0, ImagePlus imp) {
        
        if(!imp.isHyperStack()) {
            IJ.error("This plugin only works on HyperStacks.");
            return DONE;
        } else {
            this.imp = imp;     
            dim = imp.getDimensions();   
            cal = imp.getCalibration();
            is = imp.getStack();
            canvas = imp.getCanvas();           
            return DOES_ALL;
        }
    }

    /**
     * Changes the value of the {@code invert} parameter when the appropriate
     * GUI element is modified.
     */
    @Override
    public void actionPerformed(ActionEvent e) {        
        String type = e.getSource().getClass().getName();
        if (type.equals("javax.swing.JCheckBox")) {
            JCheckBox jcb = (JCheckBox)e.getSource();
            invert = jcb.isSelected();          
        }
                
    }
    
    /**
     * Modifies the value of the {@code enabled} label.
     */
    private void changeEnabledLabel() {     
        if (moveFlag) {
            enabled.setText("Enabled");         
            enabled.setForeground(Color.green);
        } else {
            enabled.setText("Disabled");            
            enabled.setForeground(Color.red);
        }       
    }

    /**
     * Creates the main JFrame used in this plugin. 
     */
    private JFrame createFrame() {
        JFrame frame = new JFrame();
        
        frame.setTitle("Dynamic Pixel Inspector");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setBounds(100, 100, 288, 94);
        
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        frame.setContentPane(contentPane);
        contentPane.setLayout(null);        
        
        contentPane.add(enabled);
        
        JCheckBox chckboxInvertValues = new JCheckBox("Invert values");
        chckboxInvertValues.setBounds(139, 11, 97, 23);
        chckboxInvertValues.addActionListener(this);
        contentPane.add(chckboxInvertValues);
        
        JLabel lblPressCtrl = new JLabel("Press 'q' for enabling or disbabling" +
                                         " continous plotting");
        lblPressCtrl.setFont(new Font("Tahoma", Font.PLAIN, 8));
        lblPressCtrl.setBounds(20, 42, 216, 14);
        contentPane.add(lblPressCtrl);
        
        return frame;
    }

    @Override
    public void windowClosed(WindowEvent e) {       
        turnOff();      
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        // Catch the event that enables or disables plot updating
        if (arg0.getKeyCode() == KeyEvent.VK_Q) {
            moveFlag = !moveFlag;
            changeEnabledLabel();
        }
    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
        if (moveFlag)
            mouseClicked(arg0);        
    }

    /**
     * Does the actual plotting.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        
        int offscreenX = canvas.offScreenX(e.getX());
        int offscreenY = canvas.offScreenY(e.getY());
        double[] y = getTAC(offscreenX, offscreenY, imp.getSlice());

        if (y != null) {
            
            // Fill in X axis (frame number)
            double[] x = new double[dim[4]];
            for (int i = 1; i <= x.length; i++)
                x[i - 1] = i;
            
            // Fill in Y axis (image intensity)
            if (invert)
                for (int i = 0; i < y.length; i++)
                    y[i] = -y[i];

            // Prepare plot window            
            Plot chart = new Plot("Slice = " + imp.getSlice() + ", x = "
                    + offscreenX + ", y = " + offscreenY, 
                    "Frame number", "Intensity (calibrated)", x, y);
            if (pw == null) {
                pw = chart.show();
                pw.addWindowListener(this);
            } else
                pw.setTitle("Slice = " + imp.getSlice() + ", x = " + offscreenX
                        + ", y = " + offscreenY);
            
            // Actual plotting            
            chart.addPoints(x, y, PlotWindow.LINE);
            pw.drawPlot(chart);
        } 
    }
    
    /**
     * Enable the events
     */
    private void turnOn() {
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addKeyListener(this);
    }

    /**
     * Disable the events
     */
    private void turnOff() {
        canvas.removeMouseMotionListener(this);
        canvas.removeMouseListener(this);
        canvas.removeKeyListener(this);
    }
    
    /**
     * Returns the TAC (time-activity curve) for the given x, y and slice
     * coordinates.
     */
    private double[] getTAC(int x, int y, int slice) {
        
        // Dimension check
        if (x >= dim[0] || x < 0 || y >= dim[1] || y < 0 || slice > dim[3]
                || slice < 1) {
            return null;
        }

        // Alloc space for the result
        double[] result = new double[dim[4]];

        // Set the desired slice and iterate through the frames
        for (int frame = 1; frame <= dim[4]; frame++) {
            int stack_number = imp.getStackIndex(dim[2], slice, frame);            
            // Use calibration to return true value
            result[frame - 1] = cal.getCValue(
                                    is.getVoxel(x, y, stack_number - 1));
        }

        return result;

    }

    /*
     * UNUSED METHODS
     */

    @Override
    public void windowOpened(WindowEvent e) {}
    @Override
    public void windowClosing(WindowEvent e) {}
    @Override
    public void windowIconified(WindowEvent e) {}
    @Override
    public void windowDeiconified(WindowEvent e) {}
    @Override
    public void windowActivated(WindowEvent e) {}
    @Override
    public void windowDeactivated(WindowEvent e) {}
    @Override
    public void keyReleased(KeyEvent arg0) {}
    @Override
    public void keyTyped(KeyEvent arg0) {}
    @Override
    public void mouseDragged(MouseEvent arg0) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}

}
