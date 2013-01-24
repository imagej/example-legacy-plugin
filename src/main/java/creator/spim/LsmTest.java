 package creator.spim;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
                Object realReader = get( r, "reader" );
                realReader = get( realReader, "reader" );
                realReader = run( realReader, "getReader" );
                System.out.println( realReader.getClass() );
        }

        public static Object get( Object object, String fieldName ) {
		if ( object == null ) {
			return null;
		}
		for ( Class< ? > clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass() ) try {
			Field field = clazz.getDeclaredField( fieldName );
			field.setAccessible( true );
			return field.get( object );
		} catch ( Throwable t ) {
			// ignore
		}
		System.err.println( "Could not get field " + fieldName + " of object " + object );
		return null;
        }

        public static Object run( Object object, String methodName, Object... parameters ) {
		if ( object == null ) {
			return null;
		}
		for ( Class< ? > clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass() ) {
			for ( Method method : clazz.getDeclaredMethods() ) try {
				if ( method.getName().equals( methodName ) && isCompatible( method.getParameterTypes(), parameters ) ) {
					method.setAccessible( true );
					return method.invoke( object, parameters );
				}
			} catch ( Throwable t ) {
				t.printStackTrace();
			}
		}
            System.err.println( "Cannot find compatible method " + methodName + " in object " + object );
	    return null;
        }

        public static boolean isCompatible( Class<?>[] types, Object[] values ) {
		if ( types.length != values.length ) {
			return false;
		}
		for ( int i = 0; i < types.length; i++ ) {
			if ( values[i] == null ) {
				if ( types[i].isPrimitive() ) {
					return false;
				}
			} else if ( ! types[i].isAssignableFrom( values[i].getClass() ) ) {
				return false;
			}
		}
		return true;
        }
}
