package org.computronium.bakesale.barchart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;

/**
 * A panel used to display a bar graph.
 */
@SuppressWarnings("serial")
public class BarChartPanel extends JPanel {
    private static final int GAP_BETWEEN_TITLE_AND_CHART = 5;

    private static final int GAP_BETWEEN_X_AXIS_LABELS_AND_X_AXIS = 5;

    private static final int GAP_BETWEEN_Y_AXIS_LABEL_AND_Y_AXIS = 10;

    private static final int GAP_BETWEEN_HASH_MARK_AND_VALUE_LABEL = 3;

    private static final int HASH_MARK_SIZE = 5;

    private static final int MARGIN = 10;

    private static final int PERCENT_WIDTH_OF_EACH_BAR = 40;

    private static final Color BACKGROUND_COLOR = Color.WHITE;

    private static final Color FOREGROUND_COLOR = Color.BLACK;

    private Font titleFont = new Font("Arial", Font.BOLD, 16);

    private BarChartModel model;

    private String title;

    private String yAxisLabel;

    private Rectangle visibleRect;

    private FontMetrics fontMetrics;

    private FontMetrics titleFontMetrics;

    private int yScale;

    private int scaleMinY;
    private int scaleMaxY;

    private int graphBottom;
    private int graphTop;

    /**
     * Class constructor.
     */
    public BarChartPanel() {
        initialize();
    }

    /**
     * Initializes a bunch of things.
     */
    private void initialize() {
        setBackground(BACKGROUND_COLOR);
        setForeground(FOREGROUND_COLOR);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                recalculateMetrics();
                repaint();
            }
        });
    }

    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        g.setColor(FOREGROUND_COLOR);

        int x_midpoint = this.visibleRect.x + this.visibleRect.width / 2;

        // Draw the title.
        if (title != null && !"".equals(title)) {
            Font original_font = g.getFont();
            g.setFont(titleFont);
            g.drawString(title, x_midpoint
                            - titleFontMetrics.stringWidth(title) / 2,
                    this.visibleRect.y + MARGIN + titleFontMetrics.getHeight());
            g.setFont(original_font);
        }

        boolean show_y_axis_label = yAxisLabel != null
                && !"".equals(yAxisLabel) && fontMetrics != null;

        // Figure where on the screen is the minimum x value (where the
        // vertical y access will be drawn).  This will depend on how big
        // the number labels are on the y access hash marks.  So assume
        // the biggest y value is the longest, when drawn as a string.
        int min_x_pos = this.visibleRect.x
                + fontMetrics.stringWidth(String.valueOf(scaleMaxY)) + MARGIN
                + HASH_MARK_SIZE;
        if (show_y_axis_label) {
            min_x_pos += fontMetrics.getHeight()
                    + GAP_BETWEEN_Y_AXIS_LABEL_AND_Y_AXIS;
        }
        int max_x_pos = this.visibleRect.x + this.visibleRect.width - MARGIN;

        int min_y_pos = getScreenY(scaleMinY);
        int max_y_pos = getScreenY(scaleMaxY);

        // Draw the x axis line
        g.drawLine(min_x_pos, min_y_pos, max_x_pos, min_y_pos);

        // Draw the y axis plus hashmarks.
        g.drawLine(min_x_pos, min_y_pos, min_x_pos, max_y_pos);
        for (int y = scaleMinY; y <= scaleMaxY; y += yScale) {
            int y_pos = getScreenY(y);

            // Draw the hashmark.
            g.drawLine(min_x_pos - HASH_MARK_SIZE, y_pos, min_x_pos
                    + HASH_MARK_SIZE, y_pos);

            // Label the hashmark.
            String value_label = String.valueOf(y);
            g.drawString(value_label, min_x_pos - HASH_MARK_SIZE
                    - fontMetrics.stringWidth(value_label)
                    - GAP_BETWEEN_HASH_MARK_AND_VALUE_LABEL, y_pos
                    + fontMetrics.getAscent() / 2);
        }

        // Draw the y axis label.
        if (show_y_axis_label) {
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

        // Draw the bars.
        int total_width_for_each_bar = (max_x_pos - min_x_pos - HASH_MARK_SIZE)
                / model.getBarCount();
        int padding_on_each_side_of_bar = total_width_for_each_bar
                * (100 - PERCENT_WIDTH_OF_EACH_BAR) / 100 / 2;
        for (int index = 0; index < model.getBarCount(); index++) {
            // Figure out the polygon for drawing the bar.
            Polygon bar = new Polygon();
            int area_left = min_x_pos + HASH_MARK_SIZE + index
                    * total_width_for_each_bar;
            int bar_left = area_left + padding_on_each_side_of_bar;
            int bar_right = area_left + total_width_for_each_bar
                    - padding_on_each_side_of_bar;
            int bar_top = getScreenY(model.getValue(index));
            bar.addPoint(bar_left, min_y_pos);
            bar.addPoint(bar_left, bar_top);
            bar.addPoint(bar_right, bar_top);
            bar.addPoint(bar_right, min_y_pos);

            // Draw the colored inside of the bar.
            g.setColor(model.getColor(index));
            g.fillPolygon(bar);

            // Draw the border around it.
            g.setColor(FOREGROUND_COLOR);
            g.drawPolygon(bar);

            // Draw the label underneath the bar.
            g.drawString(model.getLabel(index), area_left
                            + total_width_for_each_bar / 2
                            - fontMetrics.stringWidth(model.getLabel(index)) / 2,
                    min_y_pos + GAP_BETWEEN_X_AXIS_LABELS_AND_X_AXIS + fontMetrics.getHeight());
        }
    }

    /**
     * Retrieves the actual screen y coordinate for the given data value.
     *
     * @param value the data value
     * @return the screen coord for that value
     */
    private int getScreenY(double value) {
        return (int) (graphBottom - (value - scaleMinY) * (graphBottom - graphTop)
                / (scaleMaxY - scaleMinY));
    }

    /**
     * Recomputes all the values necessary to draw the chart.
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

        double dataMaxY = Double.NEGATIVE_INFINITY;
        double dataMinY = Double.NEGATIVE_INFINITY;
        for (int index = 0; index < model.getBarCount(); index++) {
            double y = model.getValue(index);
            if (y > dataMaxY)
                dataMaxY = y;
        }

        // What's the minimum width needed for each number along the y axis,
        // so that they don't overlap?
        double width_needed_for_y_label = fontMetrics.getHeight() + 5;

        // Calculate the optimal y increment.  We want to try nice numbers
        // that increase like 1, 5, 10, 50, 100, 500, etc...  So to find
        // the next "nice" number, we multiply the previous number by 5,
        // then 2, then 5, then 2, etc.  So the multiplier switches between
        // 5 and 2.  The easiest way to do this is to set it to seven minus
        // the current value.
        int multiplier = 5;
        yScale = 1;
        while (this.visibleRect.height / (dataMaxY / yScale) < width_needed_for_y_label) {
            yScale *= multiplier;
            multiplier = 7 - multiplier;
        }

        scaleMinY = (int) (dataMinY - (dataMinY % yScale));
        scaleMaxY = (int) (dataMaxY + yScale - (dataMaxY % yScale));

        graphBottom = this.visibleRect.y + this.visibleRect.height - MARGIN -
                fontMetrics.getHeight() - GAP_BETWEEN_X_AXIS_LABELS_AND_X_AXIS - HASH_MARK_SIZE;

        int top_y_axis_number_label_overhang = fontMetrics.getHeight() / 2;
        graphTop = this.visibleRect.y + MARGIN
                + top_y_axis_number_label_overhang;
        if (title != null && !"".equals(title)) {
            graphTop += titleFontMetrics.getHeight() + GAP_BETWEEN_TITLE_AND_CHART;
        }
    }

    /**
     * Sets the model to use when drawing the chart.
     *
     * @param model the new model
     */
    public void setModel(BarChartModel model) {
        this.model = model;
        recalculateMetrics();
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return Returns the titleFont.
     */
    public Font getTitleFont() {
        return titleFont;
    }

    /**
     * @param titleFont The titleFont to set.
     */
    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
    }

    /**
     * @return Returns the yAxisLabel.
     */
    public String getYAxisLabel() {
        return yAxisLabel;
    }

    /**
     * @param axisLabel The yAxisLabel to set.
     */
    public void setYAxisLabel(String axisLabel) {
        yAxisLabel = axisLabel;
    }

}
