package de.embl.cba.mobie.utils;

import bdv.img.n5.N5ImageLoader;
import bdv.spimdata.SpimDataMinimal;
import de.embl.cba.mobie.n5.N5FSImageLoader;
import de.embl.cba.mobie.n5.N5S3ImageLoader;
import de.embl.cba.mobie.projects.projectsCreator.ProjectsCreator;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import mpicbg.spim.data.generic.sequence.BasicSetupImgLoader;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.ImgLoader;
import net.imglib2.FinalDimensions;
import net.imglib2.realtransform.AffineTransform3D;

import java.io.File;

public class ExportUtils {

    public static boolean isImageSuitable(ImagePlus imp) {
        // check the image type
        switch (imp.getType()) {
            case ImagePlus.GRAY8:
            case ImagePlus.GRAY16:
            case ImagePlus.GRAY32:
                break;
            default:
                IJ.showMessage("Only 8, 16, 32-bit images are supported currently!");
                return false;
        }

        // check the image dimensionality
        if (imp.getNDimensions() < 2) {
            IJ.showMessage("Image must be at least 2-dimensional!");
            return false;
        }

        return true;
    }

    public static boolean isValidAffine ( String affine ) {
        if ( !affine.matches("^[0-9. ]+$") ) {
            Utils.log( "Invalid affine transform - must contain only numbers and spaces");
            return false;
        }

        String[] splitAffine = affine.split(" ");
        if ( splitAffine.length != 12) {
            Utils.log( "Invalid affine transform - must be of length 12");
            return false;
        }

        return true;
    }

    public static AffineTransform3D parseAffineString( String affine ) {
        if ( isValidAffine( affine )) {
            AffineTransform3D sourceTransform = new AffineTransform3D();
            String[] splitAffineTransform = affine.split(" ");
            double[] doubleAffineTransform = new double[splitAffineTransform.length];
            for (int i = 0; i < splitAffineTransform.length; i++) {
                doubleAffineTransform[i] = Double.parseDouble(splitAffineTransform[i]);
            }
            sourceTransform.set(doubleAffineTransform);
            return sourceTransform;
        } else {
            return null;
        }
    }

    public static String generateDefaultAffine ( ImagePlus imp ) {
        final double pixelWidth = imp.getCalibration().pixelWidth;
        final double pixelHeight = imp.getCalibration().pixelHeight;
        final double pixelDepth = imp.getCalibration().pixelDepth;

        String defaultAffine = pixelWidth + " 0.0 0.0 0.0 0.0 " + pixelHeight + " 0.0 0.0 0.0 0.0 " + pixelDepth + " 0.0";
        return defaultAffine;
    }

    public static FinalVoxelDimensions getVoxelSize (ImagePlus imp ) {
        final double pw = imp.getCalibration().pixelWidth;
        final double ph = imp.getCalibration().pixelHeight;
        final double pd = imp.getCalibration().pixelDepth;
        String punit = imp.getCalibration().getUnit();
        if ( punit == null || punit.isEmpty() )
            punit = "px";
        final FinalVoxelDimensions voxelSize = new FinalVoxelDimensions( punit, pw, ph, pd );
        return voxelSize;
    }

    public static FinalDimensions getSize (ImagePlus imp ) {
        final int w = imp.getWidth();
        final int h = imp.getHeight();
        final int d = imp.getNSlices();
        final FinalDimensions size = new FinalDimensions( w, h, d );
        return size;
    }

    public static File getSeqFileFromPath ( String seqFilename ) {
        final File seqFile = new File( seqFilename );
        final File parent = seqFile.getParentFile();
        if ( parent == null || !parent.exists() || !parent.isDirectory() )
        {
            IJ.showMessage( "Invalid export filename " + seqFilename );
            return null;
        }
        return seqFile;
    }

    public static AffineTransform3D generateSourceTransform ( FinalVoxelDimensions  voxelSize ) {
        // create SourceTransform from the images calibration
        final AffineTransform3D sourceTransform = new AffineTransform3D();
        sourceTransform.set( voxelSize.dimension(0), 0, 0, 0, 0, voxelSize.dimension(1),
                0, 0, 0, 0, voxelSize.dimension(2), 0 );
        return sourceTransform;
    }

    public static File getN5FileFromXmlPath ( String xmlPath ) {
        final String n5Filename = xmlPath.substring( 0, xmlPath.length() - 4 ) + ".n5";
        return new File( n5Filename );
    }

    public static ProjectsCreator.BdvFormat getBdvFormatFromSpimDataMinimal( SpimDataMinimal spimDataMinimal ) {
        ProjectsCreator.BdvFormat bdvFormat = null;
        BasicImgLoader imgLoader = spimDataMinimal.getSequenceDescription().getImgLoader();
        if ( imgLoader instanceof N5ImageLoader | imgLoader instanceof N5FSImageLoader |
                imgLoader instanceof N5S3ImageLoader | imgLoader instanceof de.embl.cba.mobie.n5.N5ImageLoader )
        {
            bdvFormat = ProjectsCreator.BdvFormat.n5;
        }

        return bdvFormat;
    }

    public static File getImageLocationFromSpimDataMinimal( SpimDataMinimal spimDataMinimal, ProjectsCreator.BdvFormat bdvFormat) {
        File imageLocation = null;

        switch ( bdvFormat ) {
            case n5:
                // get image loader to find absolute image location
                BasicImgLoader imgLoader = spimDataMinimal.getSequenceDescription().getImgLoader();
                if ( imgLoader instanceof N5ImageLoader ) {
                    N5ImageLoader n5ImageLoader = (N5ImageLoader) imgLoader;
                    imageLocation = n5ImageLoader.getN5File();
                } else if ( imgLoader instanceof N5FSImageLoader ) {
                    N5FSImageLoader n5ImageLoader = (N5FSImageLoader) imgLoader;
                    imageLocation = n5ImageLoader.getN5File();
                }
                break;
        }

        return imageLocation;
    }
}
