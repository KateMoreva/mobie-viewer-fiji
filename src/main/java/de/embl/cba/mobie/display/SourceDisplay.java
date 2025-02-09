package de.embl.cba.mobie.display;

import bdv.viewer.SourceAndConverter;
import de.embl.cba.mobie.transform.SourceTransformer;
import de.embl.cba.mobie.bdv.SliceViewer;

import java.util.Collections;
import java.util.List;

public class SourceDisplay
{
	// Serialization
	protected String name;
	protected double opacity = 1.0;
	protected List< String > sources;

	// Runtime
	public transient List< SourceAndConverter< ? > > sourceAndConverters;
	public transient List< SourceTransformer > sourceTransformers;
	public transient SliceViewer sliceViewer;

	public String getName()
	{
		return name;
	}

	public List< String > getSources()
	{
		return Collections.unmodifiableList( sources );
	}
	public double getOpacity()
	{
		return opacity;
	}

	public List< SourceAndConverter< ? > > getSourceAndConverters()
	{
		return sourceAndConverters;
	}

	public List< SourceTransformer > getSourceTransformers()
	{
		return sourceTransformers;
	}

	public SliceViewer getSliceViewer()
	{
		return sliceViewer;
	}
}
