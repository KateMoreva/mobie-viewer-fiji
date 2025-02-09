package de.embl.cba.mobie.display;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.mobie.bdv.ImageSliceView;
import de.embl.cba.mobie.color.opacity.AdjustableOpacityColorConverter;
import net.imglib2.display.ColorConverter;
import sc.fiji.bdvpg.bdv.projector.BlendingMode;
import sc.fiji.bdvpg.services.SourceAndConverterServices;

import java.util.ArrayList;
import java.util.List;

import static sc.fiji.bdvpg.bdv.projector.BlendingMode.BLENDING_MODE;

public class ImageSourceDisplay extends SourceDisplay
{
	// Serialization
	private String color;
	private double[] contrastLimits;
	private BlendingMode blendingMode;
	private boolean showImagesIn3d;

	// Runtime
	public transient ImageSliceView imageSliceView;

	public String getColor()
	{
		return color;
	}

	public double[] getContrastLimits()
	{
		return contrastLimits;
	}

	public BlendingMode getBlendingMode()
	{
		return blendingMode;
	}

	public ImageSourceDisplay( String name, double opacity, List< String > sources, String color,
							   double[] contrastLimits, BlendingMode blendingMode, boolean showImagesIn3d ) {
		this.name = name;
		this.opacity = opacity;
		this.sources = sources;
		this.color = color;
		this.contrastLimits = contrastLimits;
		this.blendingMode = blendingMode;
		this.showImagesIn3d = showImagesIn3d;
	}

	/**
	 * Create a serializable copy
	 *
	 * @param imageDisplay
	 */
	public ImageSourceDisplay( ImageSourceDisplay imageDisplay )
	{
		this.name = imageDisplay.name;
		this.sources = new ArrayList<>();
		for ( SourceAndConverter< ? > sourceAndConverter : imageDisplay.sourceAndConverters )
		{
			sources.add( sourceAndConverter.getSpimSource().getName() );
		}

		final SourceAndConverter< ? > sourceAndConverter = imageDisplay.sourceAndConverters.get( 0 );
		final ConverterSetup converterSetup = SourceAndConverterServices.getSourceAndConverterDisplayService().getConverterSetup( sourceAndConverter );

		if( sourceAndConverter.getConverter() instanceof AdjustableOpacityColorConverter )
		{
			this.opacity = ( ( AdjustableOpacityColorConverter ) sourceAndConverter.getConverter() ).getOpacity();
		}

		if ( sourceAndConverter.getConverter() instanceof ColorConverter)
		{
			// needs to be of form r=(\\d+),g=(\\d+),b=(\\d+),a=(\\d+)"
			String colorString = ( ( ColorConverter ) sourceAndConverter.getConverter() ).getColor().toString();
			colorString.replaceAll("[()]", "");
			this.color = colorString;
		}

		double[] contrastLimits = new double[2];
		contrastLimits[0] = converterSetup.getDisplayRangeMin();
		contrastLimits[1] = converterSetup.getDisplayRangeMax();
		this.contrastLimits = contrastLimits;

		this.blendingMode = (BlendingMode) SourceAndConverterServices.getSourceAndConverterService().getMetadata( sourceAndConverter, BLENDING_MODE );

		// TODO - show images in 3d (currently not supported in viewer)
	}
}
