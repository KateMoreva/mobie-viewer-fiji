package org.embl.mobie.viewer.annotate;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.roi.RealMaskRealInterval;
import org.embl.mobie.viewer.MoBIEHelper;
import org.embl.mobie.viewer.TableColumnNames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnnotatedIntervalCreator
{
	private final Map< String, List< String > > columns;
	private final Map< String, List< String > > annotationIdToSources;
	private final Function< String, SourceAndConverter< ? > > sourceAndConverterSupplier;
	private List< AnnotatedMaskTableRow > annotatedIntervalTableRows;

	public AnnotatedIntervalCreator( Map< String, List< String > > columns, Map< String, List< String > > annotationIdToSources, Function< String, SourceAndConverter< ? > > sourceAndConverterSupplier)
	{
		this.columns = columns;
		this.annotationIdToSources = annotationIdToSources;
		this.sourceAndConverterSupplier = sourceAndConverterSupplier;
		createAnnotatedIntervals();
	}

	private void createAnnotatedIntervals()
	{
		annotatedIntervalTableRows = new ArrayList<>();
		final Set< String > annotationIds = annotationIdToSources.keySet();
		final List< String > annotationIdColumn = columns.get( TableColumnNames.ANNOTATION_ID );

		for ( String annotationId : annotationIds )
		{
			final List< ? extends Source< ? > > sources = annotationIdToSources.get( annotationId ).stream().map( name -> sourceAndConverterSupplier.apply( name ).getSpimSource() ).collect( Collectors.toList() );
			// TODO: if all the sources cover the same interval, could we simplify the below call?
			final RealMaskRealInterval mask = MoBIEHelper.unionRealMask( sources );
			System.out.println( annotationId );
			System.out.println( sources.size() );
			System.out.println( Arrays.toString( mask.minAsDoubleArray() ));

			// TODO: do we still need the row index?
			final int rowIndex = annotationIdColumn.indexOf( annotationId );

			annotatedIntervalTableRows.add(
					new DefaultAnnotatedMaskTableRow(
							annotationId,
							mask,
							columns,
							rowIndex )
			);
		}
	}

	public List< AnnotatedMaskTableRow > getAnnotatedIntervalTableRows()
	{
		return annotatedIntervalTableRows;
	}
}
