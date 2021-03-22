package de.embl.cba.mobie.ui;

import bdv.tools.brightness.ConverterSetup;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.mobie.bdv.BdvViewChanger;
import de.embl.cba.mobie.bookmark.BookmarkManager;
import de.embl.cba.mobie.bookmark.Location;
import de.embl.cba.mobie.image.SourceGroups;
import de.embl.cba.mobie.utils.Utils;
import de.embl.cba.tables.SwingUtils;
import de.embl.cba.tables.color.ColorUtils;
import de.embl.cba.tables.image.SourceAndMetadata;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static de.embl.cba.mobie.utils.ui.SwingUtils.*;

public class UserInterfaceComponentsProvider
{
	public static final String BUTTON_LABEL_VIEW = "view";
	public static final String BUTTON_LABEL_MOVE = "move";
	public static final String BUTTON_LABEL_HELP = "show";
	public static final String BUTTON_LABEL_SWITCH = "switch";
	public static final String BUTTON_LABEL_LEVEL = "level";
	public static final String BUTTON_LABEL_ADD = "add";

	private final SourcesDisplayManager displayManager;
	private final MoBIE moBIE;
	private final ArrayList< String > datasets;
	private ArrayList< String > sortedModalities;
	private int sourceSelectionPanelHeight;

	public UserInterfaceComponentsProvider( MoBIE moBIE )
	{
		this.moBIE = moBIE;
		this.displayManager = moBIE.getSourcesDisplayManager();
		this.datasets = moBIE.getDatasets();
	}

	// TODO: too complex, make own source selection class
	public JPanel createSourceSelectionPanel( SourcesDisplayManager sourcesDisplayManager )
	{
		HashMap< String, String > selectionNameAndModalityToSourceName = new HashMap<>();
		HashMap< String, ArrayList< String > > modalityToSelectionNames = new HashMap<>();

		for ( String sourceName : sourcesDisplayManager.getSourceNames() )
		{
			String modality = sourceName.split( "-" )[ 0 ];

			String selectionName = sourceName.replace( modality + "-", "" );

			final Metadata metadata = sourcesDisplayManager.getSourceAndDefaultMetadata( sourceName ).metadata();

			if ( metadata.type.equals( Metadata.Type.Segmentation ) )
			{
				if ( ! modality.contains( " segmentation" ) )
					modality += " segmentation";
			}
			else if ( metadata.type.equals( Metadata.Type.Mask ) )
			{
				if ( ! modality.contains( " segmentation" ) )
					modality += " segmentation";
			}

			selectionName = Utils.getSimplifiedSourceName( selectionName, false );

			selectionNameAndModalityToSourceName.put( selectionName + "-" + modality, sourceName  );

			if ( ! modalityToSelectionNames.containsKey( modality ) )
				modalityToSelectionNames.put( modality, new ArrayList<>(  ) );

			modalityToSelectionNames.get( modality ).add( selectionName);
		}

		sortedModalities = Utils.getSortedList( modalityToSelectionNames.keySet() );

		JPanel sourcesSelectionPanel = new JPanel( new BorderLayout() );
		sourcesSelectionPanel.setLayout( new BoxLayout( sourcesSelectionPanel, BoxLayout.Y_AXIS ) );
		for ( String modality : sortedModalities )
		{
			final String[] names = Utils.getSortedList( modalityToSelectionNames.get( modality ) ).toArray( new String[ 0 ] );
			final JComboBox< String > comboBox = new JComboBox<>( names );
			setComboBoxDimensions( comboBox );
			final JPanel selectionComboBoxAndButtonPanel = createSourceSelectionComboBoxAndButtonPanel( selectionNameAndModalityToSourceName, comboBox, modality );
			sourcesSelectionPanel.add( selectionComboBoxAndButtonPanel );
		}

		sourceSelectionPanelHeight = sortedModalities.size() * 40;

		return sourcesSelectionPanel;
	}

	public int getSourceSelectionPanelHeight()
	{
		return sourceSelectionPanelHeight;
	}

	private JPanel createSourceSelectionComboBoxAndButtonPanel(
			HashMap< String, String > selectionNameAndModalityToSourceName,
			final JComboBox comboBox,
			final String modality )
	{
		final JPanel horizontalLayoutPanel = SwingUtils.horizontalLayoutPanel();

		if ( comboBox.getModel().getSize() == 0 ) return horizontalLayoutPanel;

		final JButton button = getButton( BUTTON_LABEL_ADD );
		button.addActionListener( e ->
		{
			SwingUtilities.invokeLater( () ->
			{
				final String selectedSource = ( String ) comboBox.getSelectedItem();
				final String sourceName = selectionNameAndModalityToSourceName.get( selectedSource + "-" + modality );
				displayManager.show( sourceName );
			} );
		} );

		final JLabel comp = getJLabel( modality );

		horizontalLayoutPanel.add( comp );
		horizontalLayoutPanel.add( comboBox );
		horizontalLayoutPanel.add( button );

		return horizontalLayoutPanel;
	}

	public JPanel createLevelingPanel( double[] levelingVector )
	{
		final double[] targetNormalVector = Arrays.copyOf( levelingVector, 3 );

		final JPanel horizontalLayoutPanel = SwingUtils.horizontalLayoutPanel();

		final JButton button = getButton( BUTTON_LABEL_LEVEL );
		horizontalLayoutPanel.add( button );

		// TODO: if below code is needed make an own Levelling class
//		final JButton changeReference = new JButton( "Set new level vector" );
//		horizontalLayoutPanel.add( changeReference );

//		final JButton defaultReference = new JButton( "Set default level vector" );
//		horizontalLayoutPanel.add( defaultReference );

//		changeReference.addActionListener( e -> {
//			targetNormalVector = BdvUtils.getCurrentViewNormalVector( bdv );
//			Utils.logVector( "New reference normal vector: ", targetNormalVector );
//		} );

//		defaultReference.addActionListener( e -> {
//			targetNormalVector = Arrays.copyOf( levelingVector, 3);
//			Utils.logVector( "New reference normal vector (default): ", levelingVector );
//		} );

		button.addActionListener( e -> BdvUtils.levelCurrentView( displayManager.getBdv(), targetNormalVector ) );

		return horizontalLayoutPanel;
	}

	public JPanel createBookmarksPanel( final BookmarkManager bookmarkManager )
	{
		final JPanel horizontalLayoutPanel = SwingUtils.horizontalLayoutPanel();
		final JButton button = getButton( BUTTON_LABEL_VIEW );
		final Set< String > bookmarkNames = bookmarkManager.getBookmarkNames();
		JComboBox comboBox = new JComboBox<>( bookmarkNames.toArray( new String[bookmarkNames.size()] ) );
		setComboBoxDimensions( comboBox );
		button.addActionListener( e -> bookmarkManager.setView( ( String ) comboBox.getSelectedItem() ) );
		bookmarkManager.setBookmarkDropDown( comboBox );

		horizontalLayoutPanel.add( getJLabel( "bookmark" ) );
		horizontalLayoutPanel.add( comboBox );
		horizontalLayoutPanel.add( button );

		return horizontalLayoutPanel;
	}

	public JPanel createMoveToLocationPanel( )
	{
		final JPanel horizontalLayoutPanel = SwingUtils.horizontalLayoutPanel();

		final JButton button = getButton( BUTTON_LABEL_MOVE );

		final JTextField jTextField = new JTextField( "120.5,115.3,201.5" );
		jTextField.setPreferredSize( new Dimension( COMBOBOX_WIDTH - 3, TEXT_FIELD_HEIGHT ) );
		jTextField.setMaximumSize( new Dimension( COMBOBOX_WIDTH - 3, TEXT_FIELD_HEIGHT ) );
		button.addActionListener( e -> BdvViewChanger.moveToLocation( displayManager.getBdv(), new Location( jTextField.getText() ) ) );

		horizontalLayoutPanel.add( getJLabel( "location" ) );
		horizontalLayoutPanel.add( jTextField );
		horizontalLayoutPanel.add( button );

		return horizontalLayoutPanel;
	}

	public JPanel createInfoPanel( String projectLocation, String publicationURL )
	{
		final JPanel horizontalLayoutPanel = SwingUtils.horizontalLayoutPanel();

		final JButton button = getButton( BUTTON_LABEL_HELP );

		final MoBIEInfo moBIEInfo = new MoBIEInfo( projectLocation, publicationURL );

		final JComboBox< String > comboBox = new JComboBox<>( moBIEInfo.getInfoChoices() );
		setComboBoxDimensions( comboBox );

		button.addActionListener( e -> {
			moBIEInfo.showInfo( ( String ) comboBox.getSelectedItem() );
		} );
		comboBox.setPrototypeDisplayValue( MoBIE.PROTOTYPE_DISPLAY_VALUE  );

		horizontalLayoutPanel.setSize( 0, 80 );
		final ImageIcon icon = createMobieIcon( 80 );
		final JLabel moBIE = new JLabel( "                   " );
		moBIE.setIcon( icon );

		horizontalLayoutPanel.add( moBIE );
		horizontalLayoutPanel.add( comboBox );
		horizontalLayoutPanel.add( button );

		return horizontalLayoutPanel;
	}

	public ImageIcon createMobieIcon( int size )
	{
		final URL resource = UserInterfaceComponentsProvider.class.getResource( "/mobie.jpeg" );
		final ImageIcon imageIcon = new ImageIcon( resource );
		final Image scaledInstance = imageIcon.getImage().getScaledInstance( size, size, Image.SCALE_SMOOTH );
		return new ImageIcon( scaledInstance );
	}

	public JPanel createDatasetSelectionPanel( )
	{
		final JPanel horizontalLayoutPanel = SwingUtils.horizontalLayoutPanel();

		final JButton button = getButton( BUTTON_LABEL_SWITCH );

		final String[] choices = datasets.stream().toArray( String[]::new );
		final JComboBox< String > comboBox = new JComboBox<>( choices );
		comboBox.setSelectedItem( moBIE.getDataset() );
		setComboBoxDimensions( comboBox );
		button.addActionListener( e -> switchDataset( ( String ) comboBox.getSelectedItem() ) );
		comboBox.setPrototypeDisplayValue( MoBIE.PROTOTYPE_DISPLAY_VALUE  );

		horizontalLayoutPanel.add( getJLabel( "dataset" ) );
		horizontalLayoutPanel.add( comboBox );
		horizontalLayoutPanel.add( button );

		return horizontalLayoutPanel;
	}

	// TODO: The project manager should do this!
	private void switchDataset( String dataset )
	{
		// TODO: make sure the Swing UI sources panel is fully visible before instantiating the new BDV
		moBIE.close();
		new MoBIE( moBIE.getProjectLocation(), moBIE.getOptions().dataset( dataset ) );
	}

	public static JCheckBox createBigDataViewerVisibilityCheckbox(
			int[] dims,
			List< SourceAndMetadata< ? > > sourceAndMetadataList,
			boolean isVisible )
	{
		JCheckBox checkBox = new JCheckBox( "S" );
		checkBox.setSelected( isVisible );
		checkBox.setPreferredSize( new Dimension( dims[ 0 ], dims[ 1 ] ) );

		checkBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				final String groupId = sourceAndMetadataList.get( 0 ).metadata().groupId;

				if ( groupId != null )
				{
					final List< SourceAndMetadata< ? > > sourceAndMetadataGroupList = SourceGroups.getSourcesAndMetadata( groupId );
					for ( SourceAndMetadata< ? > sourceAndMetadata : sourceAndMetadataGroupList )
					{
						sourceAndMetadata.metadata().bdvStackSource.setActive( checkBox.isSelected() );
					}

					// TODO: below is not working...
//					final SourceGroup sourceGroup = SourceGroupings.getSourceGroup( sourceAndMetadataList.get( 0 ).metadata().groupId );
//
//					final BdvHandle bdvHandle = sourceAndMetadataList.get( 0 ).metadata().bdvStackSource.getBdvHandle();
//					bdvHandle.getViewerPanel().state().setGroupActive( sourceGroup, checkBox.isSelected() );
				}
				else
				{
					for ( SourceAndMetadata< ? > sourceAndMetadata : sourceAndMetadataList )
					{
						sourceAndMetadata.metadata().bdvStackSource.setActive( checkBox.isSelected() );
					}
				}
			}
		} );

		return checkBox;
	}

	public static JCheckBox createVolumeViewVisibilityCheckbox(
			SourcesDisplayManager sourcesDisplayManager,
			int[] dims,
			SourceAndMetadata< ? > sam,
			boolean isVisible )
	{
		JCheckBox checkBox = new JCheckBox( "V" );
		checkBox.setSelected( isVisible );
		checkBox.setPreferredSize( new Dimension( dims[ 0 ], dims[ 1 ] ) );

		checkBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				new Thread( () -> {
					sam.metadata().showImageIn3d = checkBox.isSelected();
					sam.metadata().showSelectedSegmentsIn3d = checkBox.isSelected();
					sourcesDisplayManager.updateSegments3dView( sam, sourcesDisplayManager );
					sourcesDisplayManager.updateSource3dView( sam, sourcesDisplayManager, false );
				}).start();
			}
		} );

		return checkBox;
	}

	// TODO: currently I am in fact not using the List< SourceAndMetadata< ? >  >
	//   at all, but rely entirely on the groupId
	public static JButton createBrightnessButton( String sourceName, int[] buttonDimensions, List< SourceAndMetadata< ? > > sourceAndMetadataList, final double rangeMin, final double rangeMax )
	{
		JButton button = new JButton( "B" );
		button.setPreferredSize( new Dimension(
				buttonDimensions[ 0 ],
				buttonDimensions[ 1 ] ) );

		button.addActionListener( e ->
		{
			final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();

			final String groupId = sourceAndMetadataList.get( 0 ).metadata().groupId;

			if ( groupId != null )
			{
				final List< SourceAndMetadata< ? > > sourceAndMetadataGroupList = SourceGroups.getSourcesAndMetadata( groupId );
				for ( SourceAndMetadata< ? > sourceAndMetadata : sourceAndMetadataGroupList )
				{
					final List< ConverterSetup > setups = sourceAndMetadata.metadata().bdvStackSource.getConverterSetups();
					for ( ConverterSetup setup : setups )
					{
						converterSetups.add( setup );
					}
				}
			}
			else
			{
				for ( SourceAndMetadata< ? > sourceAndMetadata : sourceAndMetadataList )
				{
					final List< ConverterSetup > setups = sourceAndMetadata.metadata().bdvStackSource.getConverterSetups();
					for ( ConverterSetup setup : setups )
					{
						converterSetups.add( setup );
					}
				}
			}

			UserInterface.showBrightnessDialog(
					sourceName,
					converterSetups,
					rangeMin,
					rangeMax );
		} );

		return button;
	}

	public JPanel createDisplaySettingsPanel( SourceAndMetadata< ? > sam, SourcesDisplayManager displayManager )
	{
		return createDisplaySettingsPanel( Arrays.asList( sam ), displayManager );
	}

	public JPanel createDisplaySettingsPanel( List< SourceAndMetadata< ? > > sourceAndMetadataList, SourcesDisplayManager displayManager )
	{
		final Metadata metadataFirstSource = sourceAndMetadataList.get( 0 ).metadata();
		final String panelName = metadataFirstSource.groupId != null ? metadataFirstSource.groupId : metadataFirstSource.displayName;

		JPanel panel = new JPanel();

		panel.setLayout( new BoxLayout(panel, BoxLayout.LINE_AXIS) );
		panel.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 10 ) );
		panel.add( Box.createHorizontalGlue() );
		setPanelColor( metadataFirstSource, panel );

		JLabel sourceNameLabel = new JLabel( panelName );
		sourceNameLabel.setHorizontalAlignment( SwingUtilities.CENTER );

		int[] buttonDimensions = new int[]{ 50, 30 };
		int[] viewSelectionDimensions = new int[]{ 50, 30 };

		panel.add( sourceNameLabel );

		JButton colorButton = createColorButton( displayManager, panel, buttonDimensions, sourceAndMetadataList );

		final JButton brightnessButton = createBrightnessButton( panelName, buttonDimensions, sourceAndMetadataList, 0.0, 65535.0);

		final JButton removeButton = createRemoveButton( displayManager, sourceAndMetadataList, buttonDimensions );

		final JCheckBox bigDataViewerVisibilityCheckbox = createBigDataViewerVisibilityCheckbox( viewSelectionDimensions, sourceAndMetadataList, true );

		// TODO: Can we adapt this for source groups?
		final JCheckBox volumeVisibilityCheckbox =
			    createVolumeViewVisibilityCheckbox(
						displayManager,
						viewSelectionDimensions,
						sourceAndMetadataList.get( 0 ),
						sourceAndMetadataList.get( 0 ).metadata().showImageIn3d || sourceAndMetadataList.get( 0 ).metadata().showSelectedSegmentsIn3d );

		panel.add( colorButton );
		panel.add( brightnessButton );
		panel.add( removeButton );
		panel.add( volumeVisibilityCheckbox );
		panel.add( bigDataViewerVisibilityCheckbox );

		for ( SourceAndMetadata< ? > sam : sourceAndMetadataList )
		{
			sam.metadata().bdvStackSource.getConverterSetups().get( 0 ).setupChangeListeners().add( setup -> {
				// color changed listener
				sam.metadata().color = setup.getColor().toString();
				final Color color = ColorUtils.getColor( sam.metadata().color );
				if ( color != null )
				{
					panel.setOpaque( true );
					panel.setBackground( color );
				}
			} );
		}
		return panel;
	}

	private JButton createColorButton( SourcesDisplayManager displayManager, JPanel parentPanel, int[] buttonDimensions, List< SourceAndMetadata< ? > > sams )
	{
		JButton colorButton = new JButton( "C" );

		colorButton.setPreferredSize( new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

		colorButton.addActionListener( e ->
		{
			Color color = JColorChooser.showDialog( null, "", null );

			if ( color == null ) return;

			final String groupId = sams.get( 0 ).metadata().groupId;

			if ( groupId != null )
			{
				final List< SourceAndMetadata< ? > > sourceAndMetadataList = SourceGroups.getSourcesAndMetadata( groupId );
				for ( SourceAndMetadata< ? > sourceAndMetadata : sourceAndMetadataList )
				{
					displayManager.setSourceColor( sourceAndMetadata, color );
				}
			}
			else
			{
				for ( SourceAndMetadata< ? > sam : sams )
				{
					displayManager.setSourceColor( sam, color );
				}
			}

			parentPanel.setBackground( color );
		} );

		return colorButton;
	}

	private void setPanelColor( Metadata metadata, JPanel panel )
	{
		final Color color = ColorUtils.getColor( metadata.color );
		if ( color != null )
		{
			panel.setOpaque( true );
			panel.setBackground( color );
		}
	}

	private JButton createRemoveButton(
			SourcesDisplayManager displayManager,
			List< SourceAndMetadata< ? > > sourceAndMetadataList,
			int[] buttonDimensions )
	{
		JButton removeButton = new JButton( "X" );
		removeButton.setPreferredSize( new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

		removeButton.addActionListener( e ->
		{
			final String groupId = sourceAndMetadataList.get( 0 ).metadata().groupId;

			if ( groupId != null )
			{
				final List< SourceAndMetadata< ? > > sourceAndMetadataGroupList = SourceGroups.getSourcesAndMetadata( groupId );
				for ( SourceAndMetadata< ? > sourceAndMetadata : sourceAndMetadataGroupList )
				{
					displayManager.removeSourceFromViewers( sourceAndMetadata );
				}
			}
			else
			{
				for ( SourceAndMetadata< ? > sourceAndMetadata : sourceAndMetadataList )
				{
					displayManager.removeSourceFromViewers( sourceAndMetadata );
				}
			}
		} );

		return removeButton;
	}
}
