package steve.test.billions;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class BillionDollarDisasterGraphs {

		
	
	private static void test() {
		
		HashMap<String, int[]> monthCountsByEvent = new HashMap<String, int[]>();
		monthCountsByEvent.put("Floods", new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });
		monthCountsByEvent.put("Drought", new int[] { 10, 11, 22, 33, 4, 5, 6, 7, 8, 9, 10, 11 });
		monthCountsByEvent.put("Storms", new int[] { 20, 21, 32, 33, 4, 5, 6, 7, 8, 9, 10, 11 });


		final CategoryDataset dataset = createDataset(monthCountsByEvent);
		final JFreeChart chart = createEventChart(dataset, "", "");
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 350));

		JFrame frame = new JFrame();
		frame.setContentPane(chartPanel);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	protected static CategoryDataset createDataset(HashMap<String, int[]> monthCountsByEvent) {
		
		ArrayList<String> keyList = new ArrayList<String>(monthCountsByEvent.keySet());
		Collections.sort(keyList);
		
		String[] months = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int n=0; n<keyList.size(); n++) {
			int[] monthData = monthCountsByEvent.get(keyList.get(n));
			for (int i=0; i<monthData.length; i++) {
//				dataset.addValue(monthData[i], months[i], keyList.get(n));
				dataset.addValue(monthData[i], keyList.get(n), months[i]);
			}
		}
		return dataset;
		
	}
	
	/**
	 * NOT FOR STACKING - months may repeat across event types.  SHOULD BE SHOWN SIDE-BY-SIDE
	 * @param eventCountsByYearMonth
	 * @param numYears
	 * @param numStates
	 * @return
	 */
	protected static CategoryDataset createNormalizedDataset(HashMap<String, int[]> eventCountsByYearMonth, int numYears, int numStates) {
		
		ArrayList<String> keyList = new ArrayList<String>(eventCountsByYearMonth.keySet());
		Collections.sort(keyList);
		
		String[] months = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int n=0; n<keyList.size(); n++) {
			int[] yearmonthData = eventCountsByYearMonth.get(keyList.get(n));
			int[] monthsWithOne = new int[12];
			int[] monthsWithTwo = new int[12];
			int[] monthsWithThree = new int[12];
			
			for (int m=0; m<12; m++) {			

				for (int i=m; i<yearmonthData.length; i=i+12) {

					if (yearmonthData[i] > 0) monthsWithOne[m]++;
					if (yearmonthData[i] > 1) monthsWithTwo[m]++;
					if (yearmonthData[i] > 2) monthsWithThree[m]++;

				}
			

//				dataset.addValue(monthData[i], months[i], keyList.get(n));
				dataset.addValue((100.0*monthsWithOne[m])/numYears, keyList.get(n), months[m]);
				System.out.println("dataset.addValue("+((double)monthsWithOne[m])/numYears+", "+keyList.get(n)+", "+ months[m]+");");
			}
			
//			System.out.println(keyList.get(n)+"  "+Arrays.deepToString(a));
		}
		return dataset;
		
	}
	
	

	/**
	 * YES FOR STACKING
	 * @param eventCountsByYearMonth
	 * @param numYears
	 * @param numStates
	 * @return
	 */
	protected static CategoryDataset createNormalizedOverallDataset(int[] yearmonthData, int numYears, int numStates, boolean isCategoryOrMore) {
		
		
		System.out.println(Arrays.toString(yearmonthData));
		
		String[] months = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
			int[] monthsWithOne = new int[12];
			int[] monthsWithTwo = new int[12];
			int[] monthsWithThree = new int[12];
			int[] monthsWithFour = new int[12];
			int[] monthsWithFive = new int[12];
//			int[] monthsWithSix = new int[12];
			
			for (int m=0; m<12; m++) {			

				for (int i=m; i<yearmonthData.length; i=i+12) {

					if (isCategoryOrMore) {
						if (yearmonthData[i] >= 1) monthsWithOne[m]++;
						if (yearmonthData[i] >= 2) monthsWithTwo[m]++;
						if (yearmonthData[i] >= 3) monthsWithThree[m]++;
						if (yearmonthData[i] >= 4) monthsWithFour[m]++;
						if (yearmonthData[i] > 4) monthsWithFive[m]++;
						//					if (yearmonthData[i] > 5) monthsWithSix[m]++;
					}
					else {
						if (yearmonthData[i] == 1) monthsWithOne[m]++;
						if (yearmonthData[i] == 2) monthsWithTwo[m]++;
						if (yearmonthData[i] == 3) monthsWithThree[m]++;
						if (yearmonthData[i] == 4) monthsWithFour[m]++;
						if (yearmonthData[i] > 4) monthsWithFive[m]++;
						//					if (yearmonthData[i] > 5) monthsWithSix[m]++;
					}

				}
			

//				dataset.addValue(monthData[i], months[i], keyList.get(n));
				if (isCategoryOrMore) {
					dataset.addValue((100.0*monthsWithOne[m])/numYears, "Percent of months with one or more events in a month", months[m]);
				}
				else {
					dataset.addValue((100.0*monthsWithOne[m])/numYears, "Percent of months with one event in a month", months[m]);					
				}
				dataset.addValue((100.0*monthsWithTwo[m])/numYears, "...two events...", months[m]);
				dataset.addValue((100.0*monthsWithThree[m])/numYears, "...three events...", months[m]);
				dataset.addValue((100.0*monthsWithFour[m])/numYears, "...four events...", months[m]);
				dataset.addValue((100.0*monthsWithFive[m])/numYears, "...five or more...", months[m]);
//				System.out.println("dataset.addValue("+((double)monthsWithOne[m])/numYears+", Months with one, "+ months[m]+");");
//				System.out.println("dataset.addValue("+((double)monthsWithTwo[m])/numYears+", Months with two, "+ months[m]+");");
//				System.out.println("dataset.addValue("+((double)monthsWithThree[m])/numYears+", Months with three, "+ months[m]+");");
//				System.out.println("dataset.addValue("+((double)monthsWithFour[m])/numYears+", Months with four, "+ months[m]+");");
//				System.out.println("dataset.addValue("+((double)monthsWithSix[m])/numYears+", Months with six, "+ months[m]+");");
			}
			
//			System.out.println(keyList.get(n)+"  "+Arrays.deepToString(a));
		
		return dataset;
		
	}
	
	
	

	protected static JFreeChart createEventChart(final CategoryDataset dataset, String yTitle, String title) {

		final JFreeChart chart = ChartFactory.createStackedBarChart(
				title, "", yTitle,
				dataset, PlotOrientation.VERTICAL, true, true, false);

		chart.setBackgroundPaint(new Color(245, 245, 245));

		CategoryPlot plot = chart.getCategoryPlot();
		plot.getRenderer().setSeriesPaint(0, Color.decode("#C09000"));
		plot.getRenderer().setSeriesPaint(1, Color.decode("#0A47CC"));
		plot.getRenderer().setSeriesPaint(2, Color.decode("#0090C0"));
		plot.getRenderer().setSeriesPaint(3, Color.decode("#00B030"));
		plot.getRenderer().setSeriesPaint(4, Color.decode("#FFD147"));
		plot.getRenderer().setSeriesPaint(5, Color.decode("#FF810A"));
		plot.getRenderer().setSeriesPaint(6, Color.decode("#9000C0"));
		
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setDrawBarOutline(false);
		renderer.setShadowVisible(false);

		return chart;
	}
	

	protected static JFreeChart createMonthFreqChart(final CategoryDataset dataset, String yTitle, String title, boolean isCategoryOrMore) {

		JFreeChart chart = null;
		
		if (isCategoryOrMore) {
			chart = ChartFactory.createBarChart(
					title, "", yTitle,
					dataset, PlotOrientation.VERTICAL, true, true, false);
		} 
		else {
		    chart = ChartFactory.createStackedBarChart(
				title, "", yTitle,
				dataset, PlotOrientation.VERTICAL, true, true, false);
		}

		chart.setBackgroundPaint(new Color(245, 245, 245));

		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundAlpha(0.2f);
		plot.getRenderer().setSeriesPaint(0, Color.decode("#FFC300"));
		plot.getRenderer().setSeriesPaint(1, Color.decode("#FF5733"));
		plot.getRenderer().setSeriesPaint(2, Color.decode("#C70039"));
		plot.getRenderer().setSeriesPaint(3, Color.decode("#900C3E"));
		plot.getRenderer().setSeriesPaint(4, Color.decode("#571845"));
//		plot.getRenderer().setSeriesPaint(5, Color.decode("#FF810A"));
//		plot.getRenderer().setSeriesPaint(6, Color.decode("#9000C0"));
		
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setDrawBarOutline(false);
		renderer.setShadowVisible(false);
		renderer.setItemMargin(0);

		return chart;
	}

	public static void main(final String[] args) {

		test();
	}
}