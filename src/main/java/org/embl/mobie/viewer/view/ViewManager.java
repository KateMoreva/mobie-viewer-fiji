package org.embl.mobie.viewer.view;

import bdv.util.BdvHandle;
import bdv.viewer.SourceAndConverter;
import org.embl.mobie.viewer.MoBIE;
import org.embl.mobie.viewer.annotate.AnnotatedIntervalAdapter;
import org.embl.mobie.viewer.annotate.AnnotatedIntervalTableRow;
import org.embl.mobie.viewer.bdv.view.AnnotatedIntervalSliceView;
import org.embl.mobie.viewer.color.MoBIEColoringModel;
import org.embl.mobie.viewer.display.AbstractSourceDisplay;
import org.embl.mobie.viewer.display.AnnotatedRegionDisplay;
import org.embl.mobie.viewer.display.AnnotatedIntervalDisplay;
import org.embl.mobie.viewer.display.SourceDisplay;
import org.embl.mobie.viewer.playground.PlaygroundUtils;
import org.embl.mobie.viewer.Utils;
import org.embl.mobie.viewer.bdv.view.ImageSliceView;
import org.embl.mobie.viewer.bdv.view.SegmentationSliceView;
import org.embl.mobie.viewer.bdv.view.SliceViewer;
import org.embl.mobie.viewer.plot.ScatterPlotViewer;
import org.embl.mobie.viewer.segment.SegmentAdapter;
import org.embl.mobie.viewer.display.ImageSourceDisplay;
import org.embl.mobie.viewer.display.SegmentationSourceDisplay;
import org.embl.mobie.viewer.source.SegmentationSource;
import org.embl.mobie.viewer.table.TableDataFormat;
import org.embl.mobie.viewer.table.TableViewer;
import org.embl.mobie.viewer.ui.UserInterface;
import org.embl.mobie.viewer.ui.WindowArrangementHelper;
import org.embl.mobie.viewer.view.additionalviews.AdditionalViewsLoader;
import org.embl.mobie.viewer.view.saving.ViewsSaver;
import org.embl.mobie.viewer.volume.SegmentsVolumeViewer;
import org.embl.mobie.viewer.volume.UniverseManager;
import de.embl.cba.tables.color.ColoringModel;
import de.embl.cba.tables.color.ColumnColoringModelCreator;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.tablerow.TableRowImageSegment;
import ij.IJ;
import net.imglib2.realtransform.AffineTransform3D;
import org.apache.commons.lang.ArrayUtils;
import org.embl.mobie.viewer.transform.MoBIEViewerTransformChanger;
import org.embl.mobie.viewer.transform.NormalizedAffineViewerTransform;
import org.embl.mobie.viewer.transform.SourceTransformer;
import sc.fiji.bdvpg.bdv.navigate.ViewerTransformAdjuster;
import sc.fiji.bdvpg.scijava.services.SourceAndConverterService;
import sc.fiji.bdvpg.services.SourceAndConverterServices;


import javax.swing.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.embl.mobie.viewer.ui.UserInterfaceHelper.*;

public class ViewManager
{
	private final MoBIE moBIE;
	private final UserInterface userInterface;
	private final SliceViewer sliceViewer;
	private final SourceAndConverterService sacService;
	private List<SourceDisplay> currentSourceDisplays;
	private List<SourceTransformer> currentSourceTransformers;
	private final BdvHandle bdvHandle;
	private final UniverseManager universeManager;
	private final AdditionalViewsLoader additionalViewsLoader;
	private final ViewsSaver viewsSaver;

	private View currentView;

    public List<SourceTransformer> getCurrentSourceTransformers()
    {
        return currentSourceTransformers;
    }

    public View getCurrentView()
    {
        return currentView;
    }

    public UserInterface getUserInterface()
    {
        return userInterface;
    }

    public ViewManager( MoBIE moBIE, UserInterface userInterface, boolean is2D, int timepoints )
	{
		this.moBIE = moBIE;
		this.userInterface = userInterface;
		currentSourceDisplays = new ArrayList<>();
		currentSourceTransformers = new ArrayList<>();
		sliceViewer = new SliceViewer( is2D, this, timepoints );
		universeManager = new UniverseManager();
		bdvHandle = sliceViewer.get();
		additionalViewsLoader = new AdditionalViewsLoader( moBIE );
		viewsSaver = new ViewsSaver( moBIE );
		sacService = ( SourceAndConverterService ) SourceAndConverterServices.getSourceAndConverterService();
	}

	public static void initScatterPlotViewer( AnnotatedRegionDisplay< ? > display )
	{
		if ( display.tableRows.size() == 0 ) return;

		String[] scatterPlotAxes = display.getScatterPlotAxes();
		display.scatterPlotViewer = new ScatterPlotViewer( display.tableRows, display.selectionModel, display.coloringModel, scatterPlotAxes, new double[]{1.0, 1.0}, 0.5 );
		display.selectionModel.listeners().add( display.scatterPlotViewer );
		display.coloringModel.listeners().add( display.scatterPlotViewer );
		display.sliceViewer.getBdvHandle().getViewerPanel().addTimePointListener( display.scatterPlotViewer );

		if ( display.showScatterPlot() )
		{
			display.scatterPlotViewer.setShowColumnSelectionUI( false );
			display.scatterPlotViewer.show();
		}
	}

	private static void configureMoBIEColoringModel( AnnotatedRegionDisplay< ? > display )
	{
		if ( display.getColorByColumn() != null )
		{
			final ColumnColoringModelCreator< TableRowImageSegment > modelCreator = new ColumnColoringModelCreator( display.tableRows );
			final ColoringModel< TableRowImageSegment > coloringModel;
			String coloringLut = display.getLut();

			if ( display.getValueLimits() != null )
			{
				coloringModel = modelCreator.createColoringModel(display.getColorByColumn(), coloringLut, display.getValueLimits()[0], display.getValueLimits()[1]);
			}
			else
			{
				coloringModel = modelCreator.createColoringModel(display.getColorByColumn(), coloringLut, null, null );
			}

			display.coloringModel = new MoBIEColoringModel( coloringModel );
		}
		else
		{
			display.coloringModel = new MoBIEColoringModel<>( display.getLut() );
		}
	}

	public void showInTableViewer( SegmentationSourceDisplay display  )
	{
		Map<String, String> sourceNameToTableDir = new HashMap<>();
		for ( String source: display.getSources() )
		{
			sourceNameToTableDir.put( source, moBIE.getTablesDirectoryPath( (SegmentationSource) moBIE.getSource( source ) )
			);
		}
		display.tableViewer = new TableViewer<>( moBIE, display.tableRows, display.selectionModel, display.coloringModel, display.getName(), sourceNameToTableDir, false ).show();
		display.selectionModel.listeners().add( display.tableViewer );
		display.coloringModel.listeners().add( display.tableViewer );
	}

	public List< SourceDisplay > getCurrentSourceDisplays()
	{
		return currentSourceDisplays;
	}

	public SliceViewer getSliceViewer()
	{
		return sliceViewer;
	}

	public AdditionalViewsLoader getAdditionalViewsLoader() { return additionalViewsLoader; }

	public ViewsSaver getViewsSaver() { return viewsSaver; }

	public View getCurrentView( String uiSelectionGroup, boolean isExclusive, boolean includeViewerTransform ) {

		List< SourceDisplay > viewSourceDisplays = new ArrayList<>();
		List< SourceTransformer > viewSourceTransforms = new ArrayList<>();

		for ( SourceDisplay sourceDisplay : currentSourceDisplays )
		{
			SourceDisplay currentDisplay = null;

			if ( sourceDisplay instanceof ImageSourceDisplay)
			{
				currentDisplay = new ImageSourceDisplay( ( ImageSourceDisplay ) sourceDisplay );
			} else if ( sourceDisplay instanceof SegmentationSourceDisplay )
			{
				SegmentationSourceDisplay segmentationSourceDisplay = ( SegmentationSourceDisplay ) sourceDisplay;
				if ( segmentationSourceDisplay.tableViewer.hasColumnsFromTablesOutsideProject() )
				{
					IJ.log( "Cannot make a view with tables that have columns loaded from the filesystem (not within the project)." );
					return null;
				}
				currentDisplay = new SegmentationSourceDisplay( segmentationSourceDisplay );
			}

			if ( currentDisplay != null )
			{
				viewSourceDisplays.add( currentDisplay );
			}
		}

		// TODO - would be good to pick up any manual transforms here too. This would allow e.g. manual placement
		// of differing sized sources into a grid
		for ( SourceTransformer sourceTransformer : currentSourceTransformers )
			if ( ! viewSourceTransforms.contains( sourceTransformer ) )
				viewSourceTransforms.add( sourceTransformer );

		if ( includeViewerTransform )
		{
			AffineTransform3D normalisedViewTransform = Utils.createNormalisedViewerTransform( bdvHandle,
					PlaygroundUtils.getWindowCentreInPixelUnits( bdvHandle ) );

			final NormalizedAffineViewerTransform transform = new NormalizedAffineViewerTransform( normalisedViewTransform.getRowPackedCopy(), bdvHandle.getViewerPanel().state().getCurrentTimepoint() );
			return new View(uiSelectionGroup, viewSourceDisplays, viewSourceTransforms, transform, isExclusive);
		} else {
			return new View(uiSelectionGroup, viewSourceDisplays, viewSourceTransforms, isExclusive);
		}
	}

	public synchronized void show( View view )
	{
		if ( view.isExclusive() )
		{
			removeAllSourceDisplays();
		}
		currentView = view;

		// fetch the names of all sources that are either shown or to be transformed
		final Set< String > sources = fetchSources( view );
		final Set< String > datasetSources = sources.stream().filter( s -> moBIE.getDataset().sources.containsKey( s ) ).collect( Collectors.toSet() );

		// open all raw sources
		Map< String, SourceAndConverter< ? > > sourceNameToSourceAndConverters = moBIE.openSourceAndConverters( datasetSources );

		// create transformed sources
		final List< SourceTransformer > sourceTransformers = view.getSourceTransforms();
		if ( sourceTransformers != null )
		for ( SourceTransformer sourceTransformer : sourceTransformers )
		{
			currentSourceTransformers.add( sourceTransformer );
			sourceTransformer.transform( sourceNameToSourceAndConverters );
		}

		// register all available sources
		moBIE.addSourceAndConverters( sourceNameToSourceAndConverters );

		// show the displays
		setMoBIESwingLookAndFeel();
		final List< SourceDisplay > sourceDisplays = view.getSourceDisplays();
		for ( SourceDisplay sourceDisplay : sourceDisplays )
			showSourceDisplay( sourceDisplay );
		resetSystemSwingLookAndFeel();

		// adjust viewer transform
		adjustViewerTransform( view );
	}

	public void adjustViewerTransform( View view )
	{
		if ( view.getViewerTransform() != null )
		{
			MoBIEViewerTransformChanger.changeViewerTransform( bdvHandle, view.getViewerTransform() );
		}
		else
		{
			if ( view.isExclusive() || currentSourceDisplays.size() == 1 )
			{
				// TODO: rethink what should happen here...
				final SourceDisplay sourceDisplay = currentSourceDisplays.get( currentSourceDisplays.size() - 1 );
				new ViewerTransformAdjuster( bdvHandle, ((AbstractSourceDisplay) sourceDisplay).sourceAndConverters.get( 0 ) ).run();
			}
		}
	}

	public Set< String > fetchSources( View view )
	{
		final Set< String > sources = new HashSet<>();
		final List< SourceDisplay > sourceDisplays = view.getSourceDisplays();
		for ( SourceDisplay sourceDisplay : sourceDisplays )
		{
			sources.addAll( sourceDisplay.getSources() );
		}
		for ( SourceTransformer sourceTransformer : view.getSourceTransforms() )
		{
			sources.addAll( sourceTransformer.getSources() );
		}
		return sources;
	}

	public synchronized void showSourceDisplay( SourceDisplay sourceDisplay )
	{
		if ( currentSourceDisplays.contains( sourceDisplay ) ) return;

		if ( sourceDisplay instanceof ImageSourceDisplay )
		{
			showImageDisplay( ( ImageSourceDisplay ) sourceDisplay );
		}
		else if ( sourceDisplay instanceof SegmentationSourceDisplay )
		{
			showSegmentationDisplay( ( SegmentationSourceDisplay ) sourceDisplay );
		}
		else if ( sourceDisplay instanceof AnnotatedIntervalDisplay )
		{
			showAnnotatedIntervalDisplay( ( AnnotatedIntervalDisplay ) sourceDisplay );
		}

		userInterface.addSourceDisplay( sourceDisplay );
		currentSourceDisplays.add( sourceDisplay );
	}

	private void removeAllSourceDisplays()
	{
		// create a copy of the currently shown displays...
		final ArrayList< SourceDisplay > currentSourceDisplays = new ArrayList<>( this.currentSourceDisplays ) ;

		// ...such that we can remove the displays without
		// modifying the list that we iterate over
		for ( SourceDisplay sourceDisplay : currentSourceDisplays )
		{
			// removes display from all viewers and
			// also from the list of currently shown sourceDisplays
			removeSourceDisplay( sourceDisplay );
		}
	}

	private void showImageDisplay( ImageSourceDisplay imageDisplay )
	{
		imageDisplay.sliceViewer = sliceViewer;
		imageDisplay.imageSliceView = new ImageSliceView( moBIE, imageDisplay, bdvHandle );
	}

	private void showAnnotatedIntervalDisplay( AnnotatedIntervalDisplay annotationDisplay )
	{
		annotationDisplay.sliceViewer = sliceViewer;
		annotationDisplay.tableRows = moBIE.loadAnnotatedIntervalTables( annotationDisplay );
		annotationDisplay.annotatedIntervalAdapter = new AnnotatedIntervalAdapter<>( annotationDisplay.tableRows );

		configureMoBIEColoringModel( annotationDisplay );
		annotationDisplay.selectionModel = new DefaultSelectionModel<>();
		annotationDisplay.coloringModel.setSelectionModel(  annotationDisplay.selectionModel );

		// set selected segments
		if ( annotationDisplay.getSelectedAnnotationIds() != null )
		{
			final List<AnnotatedIntervalTableRow> annotatedIntervals = annotationDisplay.annotatedIntervalAdapter.getAnnotatedIntervals( annotationDisplay.getSelectedAnnotationIds() );
			annotationDisplay.selectionModel.setSelected( annotatedIntervals, true );
		}

		showInSliceViewer( annotationDisplay );
		showInTableViewer( annotationDisplay );
		initScatterPlotViewer( annotationDisplay );

		SwingUtilities.invokeLater( () ->
		{
			WindowArrangementHelper.bottomAlignWindow( annotationDisplay.sliceViewer.getWindow(), annotationDisplay.tableViewer.getWindow() );
		} );
	}

	private void showInTableViewer( AnnotatedIntervalDisplay annotationDisplay )
	{
		HashMap<String, String> nameToTableDir = new HashMap<>();
		nameToTableDir.put( annotationDisplay.getName(), annotationDisplay.getTableDataFolder( TableDataFormat.TabDelimitedFile ) );
		annotationDisplay.tableViewer = new TableViewer<>( moBIE, annotationDisplay.tableRows, annotationDisplay.selectionModel, annotationDisplay.coloringModel, annotationDisplay.getName(), nameToTableDir, true ).show();
	}

	private void showSegmentationDisplay( SegmentationSourceDisplay segmentationDisplay )
	{
		segmentationDisplay.sliceViewer = sliceViewer;
		loadTablesAndCreateImageSegments( segmentationDisplay );

		if ( segmentationDisplay.tableRows != null )
		{
			segmentationDisplay.segmentAdapter = new SegmentAdapter( segmentationDisplay.tableRows );
		}
		else
		{
			segmentationDisplay.segmentAdapter = new SegmentAdapter();
		}

		configureMoBIEColoringModel( segmentationDisplay );
		segmentationDisplay.selectionModel = new DefaultSelectionModel<>();
		segmentationDisplay.coloringModel.setSelectionModel(  segmentationDisplay.selectionModel );

		// set selected segments
		if ( segmentationDisplay.getSelectedTableRows() != null )
		{
			final List< TableRowImageSegment > segments = segmentationDisplay.segmentAdapter.getSegments( segmentationDisplay.getSelectedTableRows() );
			segmentationDisplay.selectionModel.setSelected( segments, true );
		}

		showInSliceViewer( segmentationDisplay );

		if ( segmentationDisplay.tableRows != null )
		{
			showInTableViewer( segmentationDisplay );
			initScatterPlotViewer( segmentationDisplay );

			SwingUtilities.invokeLater( () ->
			{
				WindowArrangementHelper.bottomAlignWindow( segmentationDisplay.sliceViewer.getWindow(), segmentationDisplay.tableViewer.getWindow() );
			} );

			initVolumeViewer( segmentationDisplay );
		}
	}

	private void loadTablesAndCreateImageSegments( SegmentationSourceDisplay segmentationDisplay )
	{
		final List< String > tables = segmentationDisplay.getTables();

		if ( tables == null ) return;

		// primary table
		moBIE.loadPrimarySegmentsTables( segmentationDisplay );

		// secondary tables
		if ( tables.size() > 1 )
		{
			final List< String > additionalTables = tables.subList( 1, tables.size() );

			moBIE.appendSegmentsTables( segmentationDisplay, additionalTables );
		}

		for ( TableRowImageSegment segment : segmentationDisplay.tableRows )
		{
			if ( segment.labelId() == 0 )
			{
				throw new UnsupportedOperationException( "The table contains rows (image segments) with label index 0, which is not supported and will lead to errors. Please change the table accordingly." );
			}
		}
	}

	private void showInSliceViewer( SegmentationSourceDisplay segmentationDisplay )
	{
		segmentationDisplay.sliceView = new SegmentationSliceView<>( moBIE, segmentationDisplay, bdvHandle );
	}

	private void showInSliceViewer( AnnotatedIntervalDisplay annotatedIntervalDisplay )
	{
		annotatedIntervalDisplay.sliceView = new AnnotatedIntervalSliceView( moBIE, annotatedIntervalDisplay, bdvHandle );
	}

	private void initVolumeViewer( SegmentationSourceDisplay display )
	{
		display.segmentsVolumeViewer = new SegmentsVolumeViewer<>( display.selectionModel, display.coloringModel, display.sourceAndConverters, universeManager );
		Double[] resolution3dView = display.getResolution3dView();
		if ( resolution3dView != null ) {
			display.segmentsVolumeViewer.setVoxelSpacing( ArrayUtils.toPrimitive(display.getResolution3dView()) );
		}
		display.segmentsVolumeViewer.showSegments( display.showSelectedSegmentsIn3d() );
		display.coloringModel.listeners().add( display.segmentsVolumeViewer );
		display.selectionModel.listeners().add( display.segmentsVolumeViewer );

		for ( SourceAndConverter< ? > sourceAndConverter : display.sourceAndConverters )
		{
			sacService.setMetadata( sourceAndConverter, SegmentsVolumeViewer.VOLUME_VIEW, display.segmentsVolumeViewer  );
		}
	}

	public synchronized void removeSourceDisplay( SourceDisplay sourceDisplay )
	{
		if ( sourceDisplay instanceof SegmentationSourceDisplay )
		{
			final SegmentationSourceDisplay segmentationDisplay = ( SegmentationSourceDisplay ) sourceDisplay;
			segmentationDisplay.sliceView.close();
			if ( segmentationDisplay.tableRows != null )
			{
				segmentationDisplay.tableViewer.close();
				segmentationDisplay.scatterPlotViewer.close();
				segmentationDisplay.segmentsVolumeViewer.close();
			}
		}
		else if ( sourceDisplay instanceof ImageSourceDisplay )
		{
			final ImageSourceDisplay imageDisplay = ( ImageSourceDisplay ) sourceDisplay;
			imageDisplay.imageSliceView.close();
		}
		else if ( sourceDisplay instanceof AnnotatedIntervalDisplay )
		{
			// TODO: Code duplication (sourceDisplay instanceof SegmentationSourceDisplay)
			final AnnotatedIntervalDisplay annotatedIntervalDisplay = ( AnnotatedIntervalDisplay ) sourceDisplay;
			annotatedIntervalDisplay.sliceView.close();
			annotatedIntervalDisplay.tableViewer.close();
			annotatedIntervalDisplay.scatterPlotViewer.close();
		}

		userInterface.removeDisplaySettingsPanel( sourceDisplay );
		currentSourceDisplays.remove( sourceDisplay );
	}

	public Collection< SegmentationSourceDisplay > getSegmentationDisplays()
	{
		final List< SegmentationSourceDisplay > segmentationDisplays = getCurrentSourceDisplays().stream().filter( s -> s instanceof SegmentationSourceDisplay ).map( s -> ( SegmentationSourceDisplay ) s ).collect( Collectors.toList() );

		return segmentationDisplays;
	}

	public void close()
	{
		removeAllSourceDisplays();
		sliceViewer.getBdvHandle().close();
		universeManager.close();
	}
}
