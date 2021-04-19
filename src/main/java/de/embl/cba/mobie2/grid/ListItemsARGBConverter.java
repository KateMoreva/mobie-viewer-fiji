package de.embl.cba.mobie2.grid;

import de.embl.cba.tables.color.ColoringModel;
import net.imglib2.Volatile;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.HashMap;
import java.util.List;

// TODO: move to table-utils
public class ListItemsARGBConverter< T > implements Converter<RealType, ARGBType>
{
	public static final int OUT_OF_BOUNDS_ROW_INDEX = -1;
	private final ColoringModel< T > coloringModel;
	private final List< T > list;
	private int backgroundARGBIndex; // default, background color
	private final HashMap< Integer, Integer > indexToColor;

	public ListItemsARGBConverter(
			List< T > list,
			ColoringModel< T > coloringModel )
	{
		this.list = list;
		this.coloringModel = coloringModel;
		backgroundARGBIndex = ARGBType.rgba( 0,0,0,255 );
		indexToColor = new HashMap<>();
	}

	@Override
	public void convert( RealType rowIndex, ARGBType color )
	{
		if ( rowIndex instanceof Volatile )
		{
			if ( ! ( ( Volatile ) rowIndex ).isValid() )
			{
				color.set( backgroundARGBIndex );
				return;
			}
		}

		final int index = ( int ) rowIndex.getRealDouble();

		if ( indexToColor.containsKey( index ))
		{
			color.set( indexToColor.get( index ) );
			return;
		}

		if ( index == OUT_OF_BOUNDS_ROW_INDEX )
		{
			color.set( backgroundARGBIndex );
			return;
		}

		final T item = list.get( index );

		if ( item == null )
		{
			color.set( backgroundARGBIndex );
		}
		else
		{
			coloringModel.convert( item, color );

			final int alpha = ARGBType.alpha( color.get() );
			if( alpha < 255 )
				color.mul( alpha / 255.0 );
		}
	}
}
