package org.computronium.bakesale.graph;

import java.awt.Color;

/**
 * Interface for one set of points on the graph.
 * 
 * @author mschnitzius
 */

public interface GraphModel
{
	/**
	 * Returns the number of points in the graph.
	 * @return the number of points
	 */
	public int getPointCount();
	
	/**
	 * Returns the x coordinate for the point at the given index.
	 * @param index    the index of the x coordinate to retrieve
	 * @return         the x coordinate
	 */
	public double getX( int index );
	
	/**
     * Returns the y coordinate for the point at the given index.
     * @param index    the index of the y coordinate to retrieve
     * @return         the y coordinate
	 */
	public double getY( int index );

	/**
	 * Returns the color with which to draw this set of points.
	 * @return the color for this set of points
	 */
	public Color getColor();
}
