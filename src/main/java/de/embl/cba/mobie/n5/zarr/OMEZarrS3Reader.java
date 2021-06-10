package de.embl.cba.mobie.n5.zarr;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import de.embl.cba.mobie.n5.S3Reader;
import de.embl.cba.mobie.n5.source.Sources;
import ij.IJ;
import mpicbg.spim.data.SpimData;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.Cast;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OMEZarrS3Reader extends S3Reader
{
	public OMEZarrS3Reader( String serviceEndpoint, String signingRegion, String bucketName )
	{
		super(serviceEndpoint, signingRegion, bucketName);
	}
	public SpimData readKey( String key ) throws IOException
	{
		N5OMEZarrImageLoader.logChunkLoading = true;
		N5S3OMEZarrImageLoader imageLoader = new N5S3OMEZarrImageLoader( serviceEndpoint, signingRegion, bucketName, key );
		SpimData spimData = new SpimData( null, Cast.unchecked( imageLoader.getSequenceDescription() ), imageLoader.getViewRegistrations() );
		return spimData;
	}

	public static SpimData readURL( String url ) throws IOException
	{
		final String[] split = url.split( "/" );
		String serviceEndpoint = Arrays.stream( split ).limit( 3 ).collect( Collectors.joining( "/" ) );
		String signingRegion = "us-west-2";
		String bucketName = split[ 3 ];
		final String key = Arrays.stream( split ).skip( 4 ).collect( Collectors.joining( "/") );
		final OMEZarrS3Reader reader = new OMEZarrS3Reader( serviceEndpoint, signingRegion, bucketName );
		return reader.readKey( key );
	}

	public static void main( String[] args ) throws IOException
	{
		//showMyosin();
		//showAll();
		//readI2KGif();
		//showIDR0();
		showIDR1();
	}

	public static void showIDR0() throws IOException
	{
		//  /idr/zarr/v0.1/6001237.zarr
		N5OMEZarrImageLoader.logChunkLoading = true;
		OMEZarrS3Reader reader = new OMEZarrS3Reader( "https://s3.embassy.ebi.ac.uk", "us-west-2", "idr" );
		SpimData image = reader.readURL( "zarr/v0.1/6001237.zarr" );
		List< BdvStackSource< ? > > sources = BdvFunctions.show( image );
		sources.get( 0 ).setColor( new ARGBType( ARGBType.rgba( 0,0,255,255 ) ) );
		sources.get( 0 ).setDisplayRange( 0, 3000 );
		sources.get( 1 ).setColor( new ARGBType( ARGBType.rgba( 0,255,0,255 ) ) );
		sources.get( 1 ).setDisplayRange( 0, 3000 );
		sources.get( 2 ).setColor( new ARGBType( ARGBType.rgba( 255,0,0,255 ) ) );
		sources.get( 2 ).setDisplayRange( 0, 3000 );
		sources.get( 3 ).setColor( new ARGBType( ARGBType.rgba( 255,255,255,255 ) ) );
		sources.get( 3 ).setDisplayRange( 0, 3000 );
		//sources.get( 4 ).setDisplayRange( 0, 100 );
		Sources.showAsLabelMask( sources.get( 4 ) );
	}

	public static void readI2KGif() throws IOException
	{
		// https://play.minio.io:9000/i2k2020/gif.zarr
		N5OMEZarrImageLoader.logChunkLoading = true;
		OMEZarrS3Reader reader = new OMEZarrS3Reader( "https://play.minio.io:9000", "us-west-2", "i2k2020" );
		SpimData image = reader.readURL( "gif.zarr" );
		BdvFunctions.show( image );
	}

	public static void showAll() throws IOException
	{
		N5OMEZarrImageLoader.logChunkLoading = true;
		OMEZarrS3Reader reader = new OMEZarrS3Reader( "https://s3.embl.de", "us-west-2", "i2k-2020" );
		SpimData myosin = reader.readURL( "prospr-myosin.ome.zarr" );
		List< BdvStackSource< ? > > myosinBdvSources = BdvFunctions.show( myosin );
		SpimData em = reader.readURL( "em-raw.ome.zarr" );
		List< BdvStackSource< ? > > sources = BdvFunctions.show( em, BdvOptions.options().addTo( myosinBdvSources.get( 0 ).getBdvHandle() ) );
		Sources.showAsLabelMask( sources.get( 1 ) );
		Sources.viewAsHyperstack( sources.get( 0 ), 4 );
	}

	public static void showMyosin() throws IOException
	{
		N5OMEZarrImageLoader.logChunkLoading = true;
		OMEZarrS3Reader reader = new OMEZarrS3Reader( "https://s3.embl.de", "us-west-2", "i2k-2020" );
		SpimData myosin = reader.readURL( "prospr-myosin.ome.zarr" );
		BdvFunctions.show( myosin );
	}

	public static void showIDR1() throws IOException
	{
		N5OMEZarrImageLoader.logChunkLoading = true;
		OMEZarrS3Reader reader = new OMEZarrS3Reader( "https://s3.embassy.ebi.ac.uk", "us-west-2", "idr" );
		SpimData data = reader.readURL( "zarr/v0.1/9822151.zarr" );
		BdvFunctions.show( data, BdvOptions.options().is2D() ).get( 0 ).setDisplayRange( 3000, 15000 );
	}
}
