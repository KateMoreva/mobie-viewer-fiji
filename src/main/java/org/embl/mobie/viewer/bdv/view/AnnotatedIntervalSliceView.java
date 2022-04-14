package org.embl.mobie.viewer.bdv.view;

import bdv.util.BdvHandle;
import bdv.viewer.SourceAndConverter;
import org.embl.mobie.viewer.MoBIE;
import org.embl.mobie.viewer.annotate.AnnotatedMask;
import org.embl.mobie.viewer.annotate.AnnotatedMaskTableRow;
import org.embl.mobie.viewer.annotate.TableRowsIntervalImage;
import org.embl.mobie.viewer.color.ListItemsARGBConverter;
import org.embl.mobie.viewer.color.OpacityAdjuster;
import org.embl.mobie.viewer.display.AnnotatedIntervalDisplay;
import org.embl.mobie.viewer.transform.PositionViewerTransform;
import org.embl.mobie.viewer.transform.MoBIEViewerTransformChanger;
import de.embl.cba.tables.color.ColoringListener;
import de.embl.cba.tables.select.SelectionListener;
import net.imglib2.type.numeric.integer.IntType;
import sc.fiji.bdvpg.scijava.services.SourceAndConverterBdvDisplayService;
import sc.fiji.bdvpg.services.SourceAndConverterServices;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;


// TODO: code duplication with SegmentationSourceDisplay => derive from a parent class
public class AnnotatedIntervalSliceView< S extends AnnotatedMask > implements ColoringListener, SelectionListener< S >
{
	private final SourceAndConverterBdvDisplayService displayService;
	private final MoBIE moBIE;
	private final AnnotatedIntervalDisplay display;
	private BdvHandle bdvHandle;

	public AnnotatedIntervalSliceView( MoBIE moBIE, AnnotatedIntervalDisplay display, BdvHandle bdvHandle  )
	{
		this.moBIE = moBIE;
		this.display = display;
		this.bdvHandle = bdvHandle;
		this.displayService = SourceAndConverterServices.getBdvDisplayService();
		show();
	}

	private void show( )
	{
		display.selectionModel.listeners().add( this );
		display.coloringModel.listeners().add( this );

		// TODO: Make a SourceAnnotationSliceView with the listeners for the focussing.
		final TableRowsIntervalImage< AnnotatedMaskTableRow > intervalImage = new TableRowsIntervalImage<>( display.tableRows, display.coloringModel, display.getName() );
		SourceAndConverter< IntType > sourceAndConverter = intervalImage.getSourceAndConverter();
		display.sourceNameToSourceAndConverter = new HashMap<>();
		display.sourceNameToSourceAndConverter.put( intervalImage.getName(), sourceAndConverter );

		// set opacity
		OpacityAdjuster.adjustOpacity( sourceAndConverter, display.getOpacity() );

		// show
		final boolean visible = display.isVisible();
		displayService.show( bdvHandle, visible, sourceAndConverter );

		// add listener
		ListItemsARGBConverter converter = ( ListItemsARGBConverter ) sourceAndConverter.getConverter();
		bdvHandle.getViewerPanel().addTimePointListener(  converter );
	}

	public void close( boolean closeImgLoader )
	{
		for ( SourceAndConverter< ? > sourceAndConverter : display.sourceNameToSourceAndConverter.values() )
		{
			moBIE.closeSourceAndConverter( sourceAndConverter, closeImgLoader );
		}
		display.sourceNameToSourceAndConverter.clear();
	};

	@Override
	public synchronized void coloringChanged()
	{
		bdvHandle.getViewerPanel().requestRepaint();
	}

	@Override
	public synchronized void selectionChanged()
	{
		bdvHandle.getViewerPanel().requestRepaint();
	}

	@Override
	public synchronized void focusEvent( S selection )
	{
		if ( selection.getTimepoint() != getBdvHandle().getViewerPanel().state().getCurrentTimepoint() )
		{
			getBdvHandle().getViewerPanel().state().setCurrentTimepoint( selection.getTimepoint() );
		}

		final double[] max = selection.getMask().maxAsDoubleArray();
		final double[] min = selection.getMask().minAsDoubleArray();
		final double[] center = new double[ min.length ];
		for ( int d = 0; d < 3; d++ )
		{
			center[ d ] = ( max[ d ] + min[ d ] ) / 2;
		}

		MoBIEViewerTransformChanger.changeViewerTransform( bdvHandle, new PositionViewerTransform( center, bdvHandle.getViewerPanel().state().getCurrentTimepoint() ) );

	}

	public BdvHandle getBdvHandle()
	{
		return bdvHandle;
	}

	public Window getWindow()
	{
		return SwingUtilities.getWindowAncestor( bdvHandle.getViewerPanel() );
	}
}
