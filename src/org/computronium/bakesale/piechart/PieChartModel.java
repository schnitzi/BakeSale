package org.computronium.bakesale.piechart;

import java.awt.Color;

public interface PieChartModel
{
	public int getCount();
	public String getName( int index );
	public double getValue( int index );
	public boolean isOutset( int index );
	public Color getColor( int index );
}
