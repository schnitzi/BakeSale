package org.computronium.bakesale.graph;

import java.awt.*;

/**
 * Model class (in model-view-controller terms) for graphing a function.
 */
public interface GraphModel {
    /**
     * Returns the number of points in the graph.
     */
    int getPointCount();

    /**
     * Returns the x coordinate for the point at the given index.
     */
    double getX(int index);

    /**
     * Returns the y coordinate for the point at the given index.
     */
    double getY(int index);

    /**
     * Returns the color with which to draw this set of points.
     */
    Color getColor();
}
