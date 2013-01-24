 package creator.spim;

import java.io.IOException;

import loci.formats.ChannelFiller;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;

public class LsmTest
{
        public static void main( final String[] args ) throws FormatException, IOException
        {
                // r = new ChannelSeparator();
                IFormatReader r = new ImageReader();
                r = new ChannelFiller( r );
                r = new ChannelSeparator( r );

                r.setId( "/home/gene099/fiji/spim_TL71_Angle150.lsm" );
                System.out.println( "z: " + r.getSizeZ() + ", t: " + r.getSizeT() );
        }
}
