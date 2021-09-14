package projects;

import org.embl.mobie.viewer.MoBIE;
import org.embl.mobie.viewer.MoBIESettings;
import org.embl.mobie.viewer.source.ImageDataFormat;
import net.imagej.ImageJ;

import java.io.IOException;

public class OpenLocalJulianNoTables
{
	public static void main( String[] args ) throws IOException
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		final MoBIE moBIE2 = new MoBIE("/Volumes/emcf/hennies/for_constantin/mobie_no_table_test/", MoBIESettings.settings().imageDataFormat( ImageDataFormat.BdvN5 ));
	}
}
