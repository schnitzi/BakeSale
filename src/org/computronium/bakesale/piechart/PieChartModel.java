package org.computronium.bakesale.piechart;

import java.awt.*;

/**
 * Model class (in model-view-controller terms) for a pie chart.
 */
public interface PieChartModel {

    /**
     * Return the number of pie pieces.
     */
    int getCount();

    /**
     * Returns the pie chart name.
     */
    String getName(int index);

    /**
     * Returns the value at the given index.#
     */
    double getValue(int index);

    /**
     * Returns whether the wedge at the given index is exploded out from the center.
     */
    boolean isOutset(int index);

    /**
     * Returns the color of the wedge at the given index.
     */
    Color getColor(int index);
}
