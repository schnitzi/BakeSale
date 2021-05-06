package org.computronium.bakesale.barchart;

import java.awt.Color;

public interface BarChartModel
{
	/**
	 * Returns the number of bars on the graph.
	 * @return the number of bars
	 */
	public int getBarCount();
	
	/**
	 * Returns the label for the bar at the given index.
	 * @param index    the index of the label to retrieve
	 * @return         the label
	 */
	public String getLabel( int index );
	
	/**
	 * Returns the value for the bar at the given index.
	 * @param index    the index of the value to retrieve
	 * @return         the value
	 */
	public double getValue( int index );
	
	/**
	 * Returns the color with which to draw the bar at the given index.
	 * @return the color for the given bar
	 */
	public Color getColor( int index );
}
