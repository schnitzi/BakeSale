package org.computronium.bakesale.graph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel used to graph one or more sets of points. Each set of points you wish
 * to graph should implement its own GraphModel, and can be added using the
 * addModel() method.
 */
@SuppressWarnings("serial")
public class GraphPanel extends JPanel {
    private static final int GAP_BETWEEN_TITLE_AND_CHART = 5;

    private static final int GAP_BETWEEN_X_AXIS_LABEL_AND_X_AXIS = 10;

    private static final int HASH_MARK_SIZE = 5;

    private static final int MARGIN = 10;

    private Font titleFont = new Font("Arial", Font.BOLD, 16);

    private String title;

    private String xAxisLabel;

    private String yAxisLabel;

    private double dataMinX, dataMaxX, dataMinY, dataMaxY;

    private int xScale;

    private int scaleMinX;

    private int scaleMaxX;

    private int yScale;

    private int scaleMinY;

    private int scaleMaxY;

    private Rectangle visibleRect;

    private List<GraphModel> models = new ArrayList<>();

    private FontMetrics fontMetrics;

    private FontMetrics titleFontMetrics;

    private int graphLeft;

    private int graphRight;

    private int graphTop;

    private int graphBottom;

    /**
     * Class constructor.
     */
    public GraphPanel() {
        initialize();
    }

    /**
     * Sets up some basic things.
     */
    private void initialize() {
        setBackground(Color.WHITE);
        setForeground(Color.BLACK);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                recalculateMetrics();
            }
        });
    }

    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        int x_midpoint = getScreenX((this.scaleMinX + this.scaleMaxX) / 2D);

        // Draw the title.
        if (title != null && !"".equals(title)) {
            Font save = g.getFont();
            g.setFont(titleFont);
            g.drawString(title, x_midpoint
                            - titleFontMetrics.stringWidth(title) / 2,
                    this.visibleRect.y + MARGIN + titleFontMetrics.getHeight());
            g.setFont(save);
        }

        int min_x_pos = getScreenX(scaleMinX);
        int max_x_pos = getScreenX(scaleMaxX);
        int min_y_pos = getScreenY(scaleMinY);
        int max_y_pos = getScreenY(scaleMaxY);

        // Draw the x axis plus hashmarks.
        g.drawLine(min_x_pos, min_y_pos, max_x_pos, min_y_pos);
        for (int x = scaleMinX; x <= scaleMaxX; x += xScale) {
            int x_pos = getScreenX(x);

            // Draw the hashmark.
            g.drawLine(x_pos, min_y_pos - HASH_MARK_SIZE, x_pos, min_y_pos
                    + HASH_MARK_SIZE);

            // Label the hashmark.
            String number_label = String.valueOf(x);
            g.drawString(number_label, x_pos
                    - fontMetrics.stringWidth(number_label) / 2, min_y_pos
                    + HASH_MARK_SIZE + fontMetrics.getHeight());
        }

        // Draw the x axis label.
        if (xAxisLabel != null && !"".equals(xAxisLabel) && fontMetrics != null) {
            g.drawString(xAxisLabel, x_midpoint
                            - fontMetrics.stringWidth(xAxisLabel) / 2,
                    this.visibleRect.y + this.visibleRect.height - MARGIN);
        }

        // Draw the y axis plus hashmarks.
        g.drawLine(min_x_pos, min_y_pos, min_x_pos, max_y_pos);
        for (int y = scaleMinY; y <= scaleMaxY; y += yScale) {
            int y_pos = getScreenY(y);

            // Draw the hashmark.
            g.drawLine(min_x_pos - HASH_MARK_SIZE, y_pos, min_x_pos
                    + HASH_MARK_SIZE, y_pos);

            // Label the hashmark.
            String number_label = String.valueOf(y);
            g.drawString(number_label, min_x_pos - HASH_MARK_SIZE
                    - fontMetrics.stringWidth(number_label) - 3, y_pos
                    + fontMetrics.getAscent() / 2);
        }

        // Draw the y axis label.
        if (yAxisLabel != null && !"".equals(yAxisLabel) && fontMetrics != null) {
            Graphics2D g2d = (Graphics2D) g;
            AffineTransform save_at = g2d.getTransform();
            AffineTransform at = new AffineTransform();
            int x = visibleRect.x + MARGIN + fontMetrics.getHeight();
            int y_midpoint = getScreenY((this.scaleMinY + this.scaleMaxY) / 2D);
            int y = y_midpoint + fontMetrics.stringWidth(yAxisLabel) / 2;
            at.setToRotation(-Math.PI / 2.0, x, y);
            g2d.setTransform(at);
            g2d.drawString(yAxisLabel, x, y);
            g2d.setTransform(save_at);
        }

        // Draw the data points.
        for (GraphModel model : models) {
            g.setColor(model.getColor());
            for (int index = 0; index < model.getPointCount() - 1; index++) {
                g.drawLine(getScreenX(model.getX(index)), getScreenY(model
                                .getY(index)), getScreenX(model.getX(index + 1)),
                        getScreenY(model.getY(index + 1)));
            }
        }
    }

    /**
     * Returns the screen x value for the given data x value.
     */
    private int getScreenX(double x) {
        return (int) (graphLeft + (x - scaleMinX) * (graphRight - graphLeft)
                / (scaleMaxX - scaleMinX));
    }

    /**
     * Returns the screen y value for the given data y value.
     */
    private int getScreenY(double y) {
        return (int) (graphBottom - (y - scaleMinY) * (graphBottom - graphTop)
                / (scaleMaxY - scaleMinY));
    }

    /**
     * Recomputes all the necessary parameters.
     */
    private void recalculateMetrics() {
        this.visibleRect = this.getVisibleRect();
        if (this.visibleRect.width == 0) {
            // Still initializing.
            return;
        }

        Graphics g = this.getGraphics();
        fontMetrics = g.getFontMetrics();
        titleFontMetrics = g.getFontMetrics(titleFont);

        findDataExtremes();

        findGraphBounds();

        findXScaleValues();

        findYScaleValues();

        // TODO:  There's a circular dependency here, where figuring out the
        // graph bounds depends on the x and y scale extremes, and the scale
        // values depend on the graph bounds.  Hence the need to call this
        // method again here, but it should be possible to do in one pass.
        findGraphBounds();

        repaint();
    }

    /**
     * Finds the minimum and maximum values for the actual graph part of the panel.
     */
    private void findGraphBounds() {
        graphLeft = this.visibleRect.x
                + fontMetrics.stringWidth(String.valueOf(scaleMaxY)) + MARGIN
                + HASH_MARK_SIZE;
        if (yAxisLabel != null && !"".equals(yAxisLabel)) {
            graphLeft += fontMetrics.getHeight() + 10;
        }

        int overhang_of_x_axis_number_labels = fontMetrics.stringWidth(String
                .valueOf(scaleMaxX)) / 2;
        graphRight = this.visibleRect.x + this.visibleRect.width
                - overhang_of_x_axis_number_labels - MARGIN;

        graphBottom = this.visibleRect.y + this.visibleRect.height - 5
                - fontMetrics.getHeight() - 5 // for the x axis number labels
                - HASH_MARK_SIZE - MARGIN;
        if (xAxisLabel != null && !"".equals(xAxisLabel)) {
            graphBottom -= fontMetrics.getHeight()
                    - GAP_BETWEEN_X_AXIS_LABEL_AND_X_AXIS;
        }

        int top_y_axis_number_label_overhang = fontMetrics.getHeight() / 2;
        graphTop = this.visibleRect.y + MARGIN
                + top_y_axis_number_label_overhang;
        if (title != null && !"".equals(title)) {
            graphTop += titleFontMetrics.getHeight()
                    + GAP_BETWEEN_TITLE_AND_CHART;
        }
    }

    /**
     * Finds all the minimums and maximums within the data set.
     */
    private void findDataExtremes() {
        dataMinX = Double.POSITIVE_INFINITY;
        dataMaxX = Double.NEGATIVE_INFINITY;
        dataMinY = Double.POSITIVE_INFINITY;
        dataMaxY = Double.NEGATIVE_INFINITY;
        for (GraphModel model : models) {
            for (int index = 0; index < model.getPointCount(); index++) {
                double x = model.getX(index);
                double y = model.getY(index);
                if (x < dataMinX)
                    dataMinX = x;
                if (x > dataMaxX)
                    dataMaxX = x;
                if (y < dataMinY)
                    dataMinY = y;
                if (y > dataMaxY)
                    dataMaxY = y;
            }
        }
    }

    /**
     * Figures out the proper scale to use for the x axis.
     */
    private void findXScaleValues() {
        Graphics g = this.getGraphics();
        // Calculate the optimal x increment.
        int multiplier = 5;
        xScale = 1;
        scaleMinX = (int) (dataMinX - (dataMinX % xScale));
        scaleMaxX = (int) (dataMaxX + xScale - (dataMaxX % xScale));
        double width_needed_for_x_label = fontMetrics.getStringBounds(
                String.valueOf(scaleMaxX), g).getWidth() + 5;
        while ((getScreenX(scaleMaxX) - getScreenX(scaleMinX))
                / ((dataMaxX - dataMinX) / xScale) < width_needed_for_x_label) {
            xScale *= multiplier;
            scaleMinX = (int) (dataMinX - (dataMinX % xScale));
            scaleMaxX = (int) (dataMaxX + xScale - (dataMaxX % xScale));
            width_needed_for_x_label = fontMetrics.getStringBounds(
                    String.valueOf(scaleMaxX), g).getWidth() + 5;
            multiplier = 7 - multiplier;
        }
    }

    /**
     * Figures out the proper scale to use for the y axis.
     */
    private void findYScaleValues() {
        double width_needed_for_y_label = fontMetrics.getHeight() + 5;

        // Calculate the optimal y increment.
        int multiplier = 5;
        yScale = 1;
        while (this.visibleRect.height / ((dataMaxY - dataMinY) / yScale) < width_needed_for_y_label) {
            yScale *= multiplier;
            multiplier = 7 - multiplier;
        }

        scaleMinY = (int) (dataMinY - (dataMinY % yScale));
        scaleMaxY = (int) (dataMaxY + yScale - (dataMaxY % yScale));
        // System.out.println( "y scale will go from " + graphStartY + " to " +
        // graphEndY + " by " + yScale );
    }

    /**
     * Adds another set of points (as represented by the given model) to the graph.
     */
    public void addModel(GraphModel model) {
        this.models.add(model);
        recalculateMetrics();
    }

    /**
     * Gets rid of all the current models.
     */
    public void clearModels() {
        this.models.clear();
    }

    /**
     * Retrieves the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Retrieves the label for the x axis.
     */
    public String getXAxisLabel() {
        return xAxisLabel;
    }

    /**
     * Sets the x axis label.
     */
    public void setXAxisLabel(String axisLabel) {
        xAxisLabel = axisLabel;
    }

    /**
     * Retrieves the y axis label.
     */
    public String getYAxisLabel() {
        return yAxisLabel;
    }

    /**
     * Sets the y axis label.
     */
    public void setYAxisLabel(String axisLabel) {
        yAxisLabel = axisLabel;
    }

    /**
     * Retrieves the title font.
     */
    public Font getTitleFont() {
        return titleFont;
    }

    /**
     * Sets the title font.
     */
    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
    }
}
