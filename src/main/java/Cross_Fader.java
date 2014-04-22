import ij.*;
import ij.process.*;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import java.awt.*;

/**Cross_Fader 1.0 Michael Schmid 2014-04-22
 * Takes a two-slice stack and copies part of the second slice into the current one.
 */

public class Cross_Fader implements ExtendedPlugInFilter, DialogListener {
    private static int FLAGS =      //bitwise or of the following flags:
            STACK_REQUIRED |
            DOES_ALL |              //this plugin processes 8-bit, 16-bit, 32-bit gray & 24-bit/pxl RGB
            KEEP_PREVIEW;           //When using preview, the preview image can be kept as a result

    private double percentage;      //how much of the other slice is shown
    private ImageProcessor otherSlice;  //Image data of the other slice

    /**
     * This method is called by ImageJ for initialization.
     * @param arg Unused here. For plugins in a .jar file this argument string can
     *            be specified in the plugins.config file of the .jar archive.
     * @param imp The ImagePlus containing the image (or stack) to process.
     * @return    The method returns flags (i.e., a bit mask) specifying the
     *            capabilities (supported formats, etc.) and needs of the filter.
     *            See PlugInFilter.java and ExtendedPlugInFilter in the ImageJ
     *            sources for details.
     */
    public int setup (String arg, ImagePlus imp) {
        return FLAGS;
    }

    /** Ask the user for the parameters. This method of an ExtendedPlugInFilter
     *  is called by ImageJ after setup.
     * @param imp       The ImagePlus containing the image (or stack) to process.
     * @param command   The ImageJ command (as it appears the menu) that has invoked this filter
     * @param pfr       A reference to the PlugInFilterRunner, needed for preview
     * @return          Flags, i.e. a code describing supported formats etc.
     */
    public int showDialog (ImagePlus imp, String command, PlugInFilterRunner pfr) {
        if (imp.getNSlices() != 2) {
            IJ.error("stack with two slices required");
            return DONE;
        }
        int currentSliceN = imp.getSlice();
        int otherSliceN = 3 - currentSliceN;  //exchanges 1 & 2
        otherSlice = imp.getStack().getProcessor(otherSliceN);
        GenericDialog gd = new GenericDialog(command+"...");
        gd.addSlider("Fraction to Fade In", 0.0, 100.0, 50.0);
        gd.addPreviewCheckbox(pfr);
        gd.addDialogListener(this);
        gd.showDialog();           // user input (or reading from macro) happens here
        if (gd.wasCanceled())      // dialog cancelled?
            return DONE;
        return FLAGS;              // makes the user process the slice
    }

    /** Listener to modifications of the input fields of the dialog.
     *  Here the parameters should be read from the input dialog.
     *  @param gd The GenericDialog that the input belongs to
     *  @param e  The input event
     *  @return whether the input is valid and the filter may be run with these parameters
     */
    public boolean dialogItemChanged (GenericDialog gd, AWTEvent e) {
        percentage = gd.getNextNumber();
        return !gd.invalidNumber() && percentage>=0 && percentage <=100;
    }

    /**
     * This method is called by ImageJ for processing
     * @param ip The image that should be processed
     */
    public void run (ImageProcessor ip) {
        crossFade(ip, otherSlice, percentage);
    }

    /** And here you do the actual cross-fade */
    private void crossFade(ImageProcessor ip, ImageProcessor otherSlice, double percentage) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        Object pixelsDest = ip.getPixels();
        Object pixelsSource = otherSlice.getPixels();
        int pixelsToCopyPerLine = (int)(width*(percentage/100.));
IJ.log("fade="+ip+"\n"+otherSlice);
        for (int y=0; y<height; y++) {
            System.arraycopy(pixelsSource, y*width, pixelsDest, y*width, pixelsToCopyPerLine);
        }
    }

    /** Set the number of calls of the run(ip) method. This information is
     *  needed for displaying a progress bar; unused here.
     */
    public void setNPasses (int nPasses) {}

}
