import org.computronium.bakesale.barchart.BarChartModel;
import org.computronium.bakesale.barchart.BarChartPanel;
import org.computronium.bakesale.graph.GraphModel;
import org.computronium.bakesale.graph.GraphPanel;
import org.computronium.bakesale.piechart.PieChartModel;
import org.computronium.bakesale.piechart.PieChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class BakeSaleDemo extends JFrame {

    private PieChartPanel pieChartPanel;
    private GraphPanel graphPanel;
    private BarChartPanel barChartPanel;

    private BakeSaleDemo() {
        createComponents();
        layoutComponents();

        setTitle("Bake Sale Demo");

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                BakeSaleDemo.this.setVisible(false);
                BakeSaleDemo.this.dispose();
                System.exit(0);
            }
        });

    }

    public static void main(String[] args) {
        BakeSaleDemo mcd = new BakeSaleDemo();
        mcd.setVisible(true);
    }

    private void createComponents() {
        createPieChartPanel();
        createGraphPanel();
        createBarChartPanel();
    }

    private void createPieChartPanel() {
        pieChartPanel = new PieChartPanel();
        pieChartPanel.setTitle("Pie chart");
        pieChartPanel.setModel(new PieChartModel() {
            final Color[] COLORS = new Color[]{
                    new Color(255, 236, 0),
                    new Color(156, 206, 46),
                    new Color(249, 99, 13),
                    new Color(247, 174, 22),
                    new Color(88, 142, 3)};

            public int getCount() {
                return COLORS.length;
            }

            public String getName(int index) {
                return "Wedge " + index;
            }

            public double getValue(int index) {
                return 5 * (index + 1);
            }

            public Color getColor(int index) {
                return COLORS[index];
            }

            public boolean isOutset(int index) {
                return index == 2;
            }

        });
    }

    private void createGraphPanel() {
        graphPanel = new GraphPanel();
        graphPanel.setTitle("Graph Title");
        graphPanel.setXAxisLabel("X Axis Label");
        graphPanel.setYAxisLabel("Y Axis Label");

        graphPanel.addModel(new GraphModel() {
            public int getPointCount() {
                return 100;
            }

            public double getX(int index) {
                return index;
            }

            public double getY(int index) {
                return index * index;
            }

            public Color getColor() {
                return Color.BLUE;
            }
        });
        graphPanel.addModel(new GraphModel() {
            public int getPointCount() {
                return 1000;
            }

            public double getX(int index) {
                return index / 10f;
            }

            public double getY(int index) {
                return 4000 + Math.sin(index / 100f) * 3000 + index * 2;
            }

            public Color getColor() {
                return Color.GREEN;
            }
        });
    }

    private void createBarChartPanel() {
        barChartPanel = new BarChartPanel();
        barChartPanel.setTitle("Population Comparison");
        barChartPanel.setModel(new BarChartModel() {
            final String[] LABELS = new String[]{"China", "India", "USA", "Australia"};
            final int[] POPULATIONS = new int[]{1321, 1169, 303, 21};
            final Color[] COLORS = new Color[]{Color.RED, Color.ORANGE, Color.BLUE, Color.GREEN};

            public int getBarCount() {
                return LABELS.length;
            }

            public String getLabel(int index) {
                return LABELS[index];
            }

            public double getValue(int index) {
                return POPULATIONS[index];
            }

            public Color getColor(int index) {
                return COLORS[index];
            }
        });
        barChartPanel.setYAxisLabel("Population (millions)");
    }

    private void layoutComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Bar", barChartPanel);
        tabbedPane.add("Pie", pieChartPanel);
        tabbedPane.add("Graph", graphPanel);
        this.add(tabbedPane);
        setSize(600, 400);
    }

}
