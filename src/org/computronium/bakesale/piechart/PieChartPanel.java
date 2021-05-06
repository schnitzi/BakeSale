package org.computronium.bakesale.piechart;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class PieChartPanel extends JPanel
{
	public static final String NONE = "None";

	private static final int MARGIN = 10;

	private static final int LEGEND_MARGIN = 10;

	private static final float OUTSET_PERCENTAGE = 0.16f;

	private Font titleFont = new Font("Arial", Font.BOLD, 16);

	private String title;

	private Rectangle visibleRect;

	private PieChartModel model;

	private FontMetrics fontMetrics;

	private FontMetrics titleFontMetrics;

	private int centerX;

	private int centerY;

	private double[] rads;

	private double radius;

	private int legendX;

	private int legendY;

	private int legendWidth;

	private int legendHeight;

	private int colorBoxInLegendSize;

	public PieChartPanel()
	{
		initialize();
	}

	private void initialize()
	{
		setBackground(Color.WHITE);
		setForeground(Color.BLACK);

		addComponentListener(new ComponentAdapter()
		{
			public void componentResized( ComponentEvent e )
			{
				recalculateMetrics();
				repaint();
			}
		});
	}

	@Override
	public void paint( Graphics g )
	{
		super.paint(g);

		// Draw the title.
		if (title != null && !"".equals(title))
		{
			Font save = g.getFont();
			g.setFont(titleFont);
			g.drawString(title, centerX - titleFontMetrics.stringWidth(title)
					/ 2, this.visibleRect.y + MARGIN
					+ titleFontMetrics.getHeight());
			g.setFont(save);
		}

		// Draw the legend.
		Polygon border = new Polygon();
		border.addPoint(legendX, legendY);
		border.addPoint(legendX + legendWidth, legendY);
		border.addPoint(legendX + legendWidth, legendY + legendHeight);
		border.addPoint(legendX, legendY + legendHeight);
		g.setColor(Color.BLACK);
		g.drawPolygon(border);

		int fontHeight = fontMetrics.getHeight();
		for (int index = 0, y = legendY + fontHeight + 10; index < model.getCount(); index++, y += fontHeight + 10)
		{
			Polygon box = new Polygon();
			box.addPoint(legendX + 10, y - colorBoxInLegendSize + 3);
			box.addPoint(legendX + 10 + colorBoxInLegendSize, y - colorBoxInLegendSize + 3);
			box.addPoint(legendX + 10 + colorBoxInLegendSize, y + 3);
			box.addPoint(legendX + 10, y + 3);
			g.setColor(model.getColor(index));
			g.fillPolygon(box);
			g.setColor(Color.BLACK);
			g.drawPolygon(box);
			g.drawString(model.getName(index), legendX + LEGEND_MARGIN + colorBoxInLegendSize + 10, y);
		}

		// Draw the pie itself.
		for (int index = 0; index < model.getCount(); index++)
		{
			int offsetX = 0;
			int offsetY = 0;
			if (model.isOutset(index))
			{
				// Move this particular wedge out by a percentage of the radius
				// to highlight it.
				double radsmid = (rads[index] + rads[index + 1]) / 2;
				offsetX = (int) (radius * OUTSET_PERCENTAGE * Math.cos(radsmid));
				offsetY = (int) (radius * OUTSET_PERCENTAGE * Math.sin(radsmid));
			}

			Polygon p = new Polygon();
			p.addPoint(centerX + offsetX, centerY + offsetY);
			p
					.addPoint(getX(rads[index]) + offsetX, getY(rads[index])
							+ offsetY);

			for (double r = rads[index]; r < rads[index + 1]; r += 0.05)
			{
				p.addPoint(getX(r) + offsetX, getY(r) + offsetY);
			}
			p.addPoint(getX(rads[index + 1]) + offsetX, getY(rads[index + 1])
					+ offsetY);
			if (index == model.getCount() - 1)
			{
				p.addPoint(getX(rads[0]) + offsetX, getY(rads[0]) + offsetY);
			}
			g.setColor(model.getColor(index));
			g.fillPolygon(p);
			g.setColor(Color.BLACK);
			g.drawPolygon(p);
		}
	}

	private int getX( double r )
	{
		double x = centerX + Math.cos(r) * radius;
		return (int) x;
	}

	private int getY( double r )
	{
		double y = centerY + Math.sin(r) * radius;
		return (int) y;
	}

	private void recalculateMetrics()
	{
		this.visibleRect = this.getVisibleRect();
		if (this.visibleRect.width == 0)
		{
			// Still initializing.
			return;
		}

		Graphics g = this.getGraphics();
		fontMetrics = g.getFontMetrics();
		titleFontMetrics = g.getFontMetrics(titleFont);

		// Find the width of the legend, based on the longest of all the
		// wedge names.
		int maxNameWidth = 0;
		for (int i = 0; i < model.getCount(); i++)
		{
			int nameWidth = fontMetrics.stringWidth(model.getName(i));
			if (nameWidth > maxNameWidth)
			{
				maxNameWidth = nameWidth;
			}
		}
		colorBoxInLegendSize = fontMetrics.getHeight() + 4;
		legendWidth = LEGEND_MARGIN + colorBoxInLegendSize + 10 + maxNameWidth + 10;
		legendHeight = LEGEND_MARGIN + model.getCount() * (fontMetrics.getHeight() + 10);

		int pieAreaWidth = visibleRect.width - legendWidth - MARGIN;
		int pieAreaHeight = visibleRect.height;
		legendX = visibleRect.x + pieAreaWidth;
		legendY = visibleRect.y + visibleRect.height - MARGIN - legendHeight;
		int titleOffset = 0;

		if (title != null && !"".equals(title))
		{
			titleOffset = titleFontMetrics.getHeight() + 10;
			pieAreaHeight -= titleOffset;
		}

		centerX = visibleRect.x + pieAreaWidth / 2;
		centerY = visibleRect.y + titleOffset + pieAreaHeight / 2;
		radius = (pieAreaWidth < pieAreaHeight ? pieAreaWidth : pieAreaHeight)
				/ (1 + 2 * OUTSET_PERCENTAGE) / 2;

		double total = 0;
		for (int i = 0; i < model.getCount(); i++)
		{
			total += model.getValue(i);
		}
		rads = new double[model.getCount() + 1];
		rads[0] = 0;
		int running_total = 0;
		for (int i = 0; i < model.getCount(); i++)
		{
			running_total += model.getValue(i);
			rads[i + 1] = 2 * Math.PI * running_total / total;
		}
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
		recalculateMetrics();
	}

	public Font getTitleFont()
	{
		return titleFont;
	}

	public void setTitleFont( Font titleFont )
	{
		this.titleFont = titleFont;
		recalculateMetrics();
	}

	public void setModel( PieChartModel model )
	{
		this.model = model;
		recalculateMetrics();
	}
}
