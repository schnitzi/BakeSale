package org.computronium.bakesale.barchart;

import java.awt.*;

/**
 * Model class (in model-view-controller terms) for a bar chart.
 */
public interface BarChartModel {
    /**
     * Returns the number of bars on the graph.
     */
    int getBarCount();

    /**
     * Returns the label for the bar at the given index.
     */
    String getLabel(int index);

    /**
     * Returns the value for the bar at the given index.
     */
    double getValue(int index);

    /**
     * Returns the color with which to draw the bar at the given index.
     */
    Color getColor(int index);
}
