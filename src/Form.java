import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class Form extends JFrame {
    private JTextField pC1TextField;
    private JTextField pC2TextField;
    private JButton calculateButt;
    private JPanel rootPanel;
    private JPanel drawPanel;
    private JTextField chanceOfFakeAlertField;
    private JTextField chanceOfSkippingDetectionField;
    private JTextField summaryErrorOfClassificationField;
    private JLabel label2;
    private JLabel label3;

    private static final int COUNT_POINTS = 10000;
    private static final int OFFSET = 100;
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 400;
    private static final int SCALE = 500;

    public Form() {
        super("Lab 3");
        setContentPane(rootPanel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        calculateButt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculate();
            }
        });
    }

    private void createUIComponents() {
        drawPanel = new JPanel();
        drawPanel.setLayout(new BorderLayout());
    }

    private void calculate() {
        double pc1 = Double.parseDouble(pC1TextField.getText());
        double pc2 = Double.parseDouble(pC2TextField.getText());

        if (validateParams(pc1, pc2)) {
            Random random = new Random();

            int[] points1 = new int[COUNT_POINTS];
            int[] points2 = new int[COUNT_POINTS];
            double mx1 = 0;
            double mx2 = 0;

            for (int i = 0 ; i < COUNT_POINTS; i++) {
                points1[i] = random.nextInt(800) - OFFSET;
                points2[i] = random.nextInt(800) + OFFSET;

                mx1 += points1[i];
                mx2 += points2[i];
            }
            //параметры дл€ формулы (мю)
            mx1 /= COUNT_POINTS;
            mx2 /= COUNT_POINTS;

            double sigma1 = 0;
            double sigma2 = 0;

            for (int i = 0 ; i < COUNT_POINTS; i++) {
                sigma1 += Math.pow(points1[i] - mx1, 2);
                sigma2 += Math.pow(points2[i] - mx2, 2);
            }

            //параметры дл€ формулы (сигма)
            sigma1 = Math.sqrt(sigma1/ COUNT_POINTS);
            sigma2 = Math.sqrt(sigma2/ COUNT_POINTS);

            double[] result1 = new double[WIDTH];
            double[] result2 = new double[WIDTH];
            result1[0] = (Math.exp(-0.5 * Math.pow((-OFFSET - mx1) / sigma1, 2)) /
                    (sigma1 * Math.sqrt(2 * Math.PI)) * pc1);

            result2[0] = (Math.exp(-0.5 * Math.pow((-OFFSET - mx2) / sigma2, 2)) /
                            (sigma2 * Math.sqrt(2 * Math.PI)) * pc2);


            int D = 0;

            XYSeries series1 = new XYSeries("P(c1)");
            XYSeries series2 = new XYSeries("P(c2)");

            for (int x = 1; x < WIDTH; x++) {
                //подставл€ем все параметры в формулу, OFFSET дл€ более коррректного отображени€ графиков
                result1[x] =
                        (Math.exp(-0.5*Math.pow((x-OFFSET - mx1)/sigma1, 2))/
                                (sigma1*Math.sqrt(2*Math.PI))*pc1);

                result2[x] =
                        (Math.exp(-0.5*Math.pow((x-OFFSET - mx2)/sigma2, 2))/
                                (sigma2*Math.sqrt(2 * Math.PI))*pc2);

                //провер€ем пересекаютс€ ли графики (0.002 - возможна€ погрешность)
                if (Math.abs(result1[x] * SCALE - result2[x] * SCALE) < 0.002)
                {
                    // D - это точка пересечени€
                    D = x;
                }

                series1.add(x, (HEIGHT - (int)(result1[x] * HEIGHT * SCALE)));
                series2.add(x, (HEIGHT - (int)(result2[x] * HEIGHT * SCALE)));
            }

            //находим веро€тность ложной тревоги (суммируем веро€тность до точки пересечени€)
            double error1 = 0;
            for (int i = 0 ; i < D; i++) {
                error1 += result2[i];
            }
            //находим веро€тность пропуска обнаружени€
            double error2 = 0;
            if (pc1 > pc2) { //смотр€ на входные веро€тности, выбираем какой из графиков будем использовать
                for (int i = D; i < WIDTH; i++) {
                    error2 += result2[i];
                }
            }
            else {
                for (int i = D; i < WIDTH; i++) {
                    error2 += result1[i];
                }
            }

            XYSeries crossLine = new XYSeries("");
            crossLine.add(D, 0);
            crossLine.add(D, HEIGHT);

            chanceOfFakeAlertField.setText(String.valueOf(error1));
            chanceOfSkippingDetectionField.setText(String.valueOf(error2));
            summaryErrorOfClassificationField.setText(String.valueOf(error1 + error2));

            final XYSeriesCollection data = new XYSeriesCollection();
            data.addSeries(series1);
            data.addSeries(series2);
            data.addSeries(crossLine);
            final JFreeChart chart = ChartFactory.createXYLineChart(
                    "Density distribution",
                    "X",
                    "Y",
                    data,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            final ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            chartPanel.getChart().getXYPlot().getRangeAxis().setInverted(true);
            drawPanel.add(chartPanel, BorderLayout.CENTER);
            drawPanel.validate();
        }
    }

    private boolean validateParams(double pc1, double pc2) {
        if (pc1 > 1 || pc1 < 0) {
            return false;
        }
        if (pc2 > 1 || pc2 < 0) {
            return false;
        }

        if (pc1 + pc2 != 1) {
            return false;
        }
        return true;
    }
}
