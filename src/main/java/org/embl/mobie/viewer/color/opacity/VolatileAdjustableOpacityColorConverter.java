/*-
 * #%L
 * TODO
 * %%
 * Copyright (C) 2018 - 2020 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.embl.mobie.viewer.color.opacity;

import org.embl.mobie.viewer.color.OpacityAdjuster;
import net.imglib2.Volatile;
import net.imglib2.converter.Converter;
import net.imglib2.display.ColorConverter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

public class VolatileAdjustableOpacityColorConverter< V extends Volatile< RealType > > implements ColorConverter, Converter< V, ARGBType >, OpacityAdjuster
{
	private final Converter< V, ARGBType > converter;
	private final ColorConverter colorConverter;
	private double opacity = 1.0;

	public VolatileAdjustableOpacityColorConverter( Converter< V, ARGBType > converter )
	{
		this.converter = converter;
		this.colorConverter = ( ColorConverter ) converter;
	}

	@Override
	public void convert( V realTypeVolatile, ARGBType output )
	{
		if ( realTypeVolatile.isValid() && realTypeVolatile.get().getRealDouble() == 0 )
		{
			// ...for the Accumulate projector to know where the source ends
			output.set( new ARGBType( ARGBType.rgba( 0, 0, 0, 0 ) ) );
		}
		else
		{
			converter.convert( realTypeVolatile, output );
			output.mul( opacity );
		}
	}

	@Override
	public void setOpacity( double opacity )
	{
		this.opacity = opacity;
	}

	@Override
	public double getOpacity()
	{
		return opacity;
	}

	@Override
	public ARGBType getColor()
	{
		return colorConverter.getColor();
	}

	@Override
	public void setColor( ARGBType c )
	{
		colorConverter.setColor( c );
	}

	@Override
	public boolean supportsColor()
	{
		return colorConverter.supportsColor();
	}

	@Override
	public double getMin()
	{
		return colorConverter.getMin();
	}

	@Override
	public double getMax()
	{
		return colorConverter.getMax();
	}

	@Override
	public void setMin( double min )
	{
		colorConverter.setMin( min );
	}

	@Override
	public void setMax( double max )
	{
		colorConverter.setMax( max );
	}
}
