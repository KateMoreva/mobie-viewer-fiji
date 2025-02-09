package de.embl.cba.mobie.source;

import com.google.gson.annotations.SerializedName;

public enum ImageDataFormat
{
	@SerializedName( "bdv.n5" )
	BdvN5,
	@SerializedName( "bdv.n5.s3" )
	BdvN5S3,
	@SerializedName( "openOrganelle.s3" )
	OpenOrganelleS3;

	@Override
	public String toString()
	{
		switch ( this )
		{
			case BdvN5:
				return "bdv.n5";
			case BdvN5S3:
				return "bdv.n5.s3";
			case OpenOrganelleS3:
				return "openOrganelle.s3";
			default:
				throw new UnsupportedOperationException( "Unknown file format: " + this );
		}
	}

	public static ImageDataFormat fromString( String string )
	{
		switch ( string )
		{
			case "bdv.n5":
				return BdvN5;
			case "bdv.n5.s3":
				return BdvN5S3;
			case "openOrganelle":
				return OpenOrganelleS3;
			default:
				throw new UnsupportedOperationException( "Unknown file format: " + string );
		}
	}

	public boolean isSupportedByProjectCreator()
	{
		switch ( this )
		{
			case BdvN5:
				return true;
			case BdvN5S3:
				return false;
			case OpenOrganelleS3:
				return false;
			default:
				return false;
		}
	}
}
