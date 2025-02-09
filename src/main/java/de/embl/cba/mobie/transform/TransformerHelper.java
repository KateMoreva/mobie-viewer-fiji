package de.embl.cba.mobie.transform;

import bdv.viewer.SourceAndConverter;

import java.util.List;

public class TransformerHelper
{
	public static List< SourceAndConverter< ? > > transformSourceAndConverters( List< SourceAndConverter< ? > > sourceAndConverters, List< SourceTransformer > sourceTransformers )
	{
		if ( sourceTransformers != null )
		{
			for ( SourceTransformer sourceTransformer : sourceTransformers )
			{
				sourceAndConverters = sourceTransformer.transform( sourceAndConverters );
			}
		}

		return sourceAndConverters;
	}
}
