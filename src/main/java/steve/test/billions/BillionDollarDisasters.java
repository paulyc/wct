package steve.test.billions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;

import au.com.bytecode.opencsv.CSVReader;
import gov.noaa.ncdc.common.FeatureCache;


public class BillionDollarDisasters {

	public final static SimpleDateFormat SDF = new SimpleDateFormat("yyyyMM");


	static StringWriter rowWriter = new StringWriter();
	static StringWriter summaryWriter = new StringWriter();
	static int id = 0;

	public final static String IMGDIR = "C:\\work\\billions\\images";
	public final static String CSVDIR = "C:\\work\\billions\\csv";


	public static void main(String[] args) {
		//		processState("LA");
		process();
	}



	public static void process() {
		try {

			//	        URL mapDataURL = new URL(WCTConstants.MAP_DATA_JAR_URL);
			//			URL url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/states-from-census-county_500k-360.shp", null);
			URL url = new File("C:\\work\\wct\\overlays\\s_11au16.shp").toURI().toURL();  // NWS GIS page provided state shapefile
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("s_11au16");
			//			FeatureCollection fc = fs.getFeatures().collection();
			//			System.out.println(fs.getSchema().toString());
			FeatureCache fc = new FeatureCache(fs, "STATE");
			//			System.out.println(fc.getKeys().toString());

			processLocation("US", "US");

			// TODO - NEEDS WORK.  Same events counted multiple times if exist in multiple states.  Need to only count events once, similar to months.
			//			processLocation("Southeast", "VA", "NC", "SC", "GA", "AL", "FL");
			//			processLocation("Northeast", "PA", "MD", "DE", "NJ", "NY", "CT", "MA", "ME", "RI", "VT", "NH");
			//			processLocation("OhioValley", "OH", "WV", "KY", "TN", "IN", "IL", "MO");
			//			processLocation("UpperMidwest", "MI", "WI", "MN", "IA");
			//			processLocation("NorthernRockiesPlains", "ND", "SD", "MT", "WY", "NE");
			//			processLocation("South", "KS", "OK", "TX", "AR", "MS", "LA");
			//			processLocation("Southwest", "AZ", "NM", "UT", "CO");
			//			processLocation("West", "CA", "NV");
			//			processLocation("Northwest", "OR", "WA", "ID");

			ArrayList<String> keyList = new ArrayList<String>(fc.getKeys());
			Collections.sort(keyList);

			// collect all states into single totals (should match US)
			//			processLocation(null, keyList.toArray(new String[keyList.size()]));

			// iterate across each state
			for (String state : keyList) {
				processLocation(state, state);
			}


		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param outputName  if null, use current state entry
	 * @param statesAbbr2Letter
	 */
	public static void processLocation(String outputName, String... statesAbbr2Letter) {
		try {

			HashMap<String, int[]> monthCountsByEvent = new HashMap<String, int[]>();
			HashMap<String, int[]> eventCountsByMonth = new HashMap<String, int[]>();
			int[] overallEventCountsByMonth = null;


			for (String stateAbbr2Letter : statesAbbr2Letter) {
				InputStream is = new URL("https://www.ncdc.noaa.gov/billions/events-"+stateAbbr2Letter.toUpperCase()+"-1980-2019.csv").openStream();
				CSVReader csvReader = new CSVReader(new BufferedReader(new InputStreamReader(is)));
				List<String[]> lines = csvReader.readAll();

				// skip first 2 lines of title and header
				for (int n=2; n<lines.size(); n++) {

					String[] line = lines.get(n);
					//				System.out.println(line);

					//				System.out.println(line[2].substring(0, 6));
					//				System.out.println(line[3].substring(0, 6));
					if (line[2].substring(0, 6).equals(line[3].substring(0, 6))) {

						incrementMonth(monthCountsByEvent, line[1], line[2].substring(0, 6));
						processUniqueMonth(eventCountsByMonth, line[1], line[2].substring(0, 6));
						overallEventCountsByMonth = processOverallUniqueMonth(overallEventCountsByMonth, line[2].substring(0, 6), line);

						rowWriter.append(String.valueOf(id)+",");
						rowWriter.append((outputName == null) ? stateAbbr2Letter : outputName);
						for (int i=0; i<line.length; i++) {
							rowWriter.append(","+line[i]);
						}
						rowWriter.append(","+line[2].substring(0, 6)+"\n");
						id++;
					}
					else {
						//					System.out.println("skipping "+Arrays.deepToString(line));
						Calendar cal = Calendar.getInstance();
						cal.setTime(SDF.parse(line[2].substring(0, 6)));					

						Calendar endCal = Calendar.getInstance();
						endCal.setTime(SDF.parse(line[3].substring(0, 6)));
						endCal.add(Calendar.MONTH, 1);

						while (cal.before(endCal)) {
							rowWriter.append(String.valueOf(id)+","+stateAbbr2Letter);
							for (int i=0; i<line.length; i++) {
								rowWriter.append(","+line[i]);
							}
							rowWriter.append(","+SDF.format(cal.getTime())+"\n");


							//						System.out.println("adding: "+SDF.format(cal.getTime())+" "+Arrays.deepToString(line));

							incrementMonth(monthCountsByEvent, line[1], SDF.format(cal.getTime()));
							processUniqueMonth(eventCountsByMonth, line[1], SDF.format(cal.getTime()));
							overallEventCountsByMonth = processOverallUniqueMonth(overallEventCountsByMonth, SDF.format(cal.getTime()), line);

							cal.add(Calendar.MONTH, 1);

							id++;

						}



					}
					csvReader.close();
				}
			}


			for (String key : monthCountsByEvent.keySet()) {
				summaryWriter.append(outputName+","+key);
				for (int n : monthCountsByEvent.get(key)) {
					summaryWriter.append(","+n);
				}
				summaryWriter.append("\n");
			}


			

//			System.out.println(rowWriter.toString());
	//		System.out.println(summaryWriter.toString());
			FileUtils.writeStringToFile(new File(CSVDIR+File.separator+outputName+"_expanded_monthly_rows.csv"), rowWriter.toString());
			FileUtils.writeStringToFile(new File(CSVDIR+File.separator+outputName+"_monthcounts.csv"), summaryWriter.toString());

			// reset CSV buffers
			rowWriter.getBuffer().setLength(0);
			summaryWriter.getBuffer().setLength(0);
			
			
			

			// IDEA -- need to get number of times a month or % of times a month that had a B$D,  then number/% that had 2, or 3
			//  NEED TO COUNT MONTHS, NOT EVENTS




			JFreeChart chart = BillionDollarDisasterGraphs.createEventChart(
					BillionDollarDisasterGraphs.createDataset(monthCountsByEvent), 
					//					BillionDollarDisasterGraphs.createDataset(eventCountsByMonth), 
					"Number of Events", outputName+": Billion Dollar Disasters By Month from 1980-2019"
					);

			ChartUtilities.saveChartAsPNG(new File(IMGDIR+File.separator+outputName+".png"), chart, 800, 600);
			saveDatasetToCSV(chart, CSVDIR+File.separator+outputName);


			// TODO - NEEDS WORK
			//			chart = BillionDollarDisasterGraphs.createEventChart(
			////					BillionDollarDisasterGraphs.createNormalizedDataset(monthCountsByEvent, 40, statesAbbr2Letter.length),
			//					BillionDollarDisasterGraphs.createNormalizedDataset(eventCountsByMonth, 40, statesAbbr2Letter.length),
			//					"Percentage of Months with an Event", "Percentage of Months from 1980-2019 for each type of Billion Dollar Disaster"
			//				);
			//
			//			ChartUtilities.saveChartAsPNG(new File(OUTDIR+File.separator+outputName+"_normalized.png"), chart, 800, 600);




			chart = BillionDollarDisasterGraphs.createMonthFreqChart(
					BillionDollarDisasterGraphs.createNormalizedOverallDataset(overallEventCountsByMonth, 40, statesAbbr2Letter.length, false),
					"Percent (%)", outputName+": Percentage of Months from 1980-2019 with Any Billion Dollar Disaster", false
					);

			TextTitle subtitle1 = new TextTitle("Note: Each month is counted for disasters that span multiple months, such as Drought and Wildfire");
			chart.addSubtitle(subtitle1);

			ChartUtilities.saveChartAsPNG(new File(IMGDIR+File.separator+outputName+"_normalized_stacked_all.png"), chart, 800, 600);
			saveDatasetToCSV(chart, CSVDIR+File.separator+outputName+"_normalized_stacked_all");




			chart = BillionDollarDisasterGraphs.createMonthFreqChart(
					BillionDollarDisasterGraphs.createNormalizedOverallDataset(overallEventCountsByMonth, 40, statesAbbr2Letter.length, true),
					"Percent (%)", outputName+": Percentage of Months from 1980-2019 with Any Billion Dollar Disaster", true
					);

			subtitle1 = new TextTitle("Note: Each month is counted for disasters that span multiple months, such as Drought and Wildfire");
			chart.addSubtitle(subtitle1);

			ChartUtilities.saveChartAsPNG(new File(IMGDIR+File.separator+outputName+"_normalized_all.png"), chart, 800, 600);
			saveDatasetToCSV(chart, CSVDIR+File.separator+outputName+"_normalized_all");


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//			e.printStackTrace();
			System.err.println(e.getMessage());
			System.err.println("EXITING COLLECTION OF STATES: "+Arrays.deepToString(statesAbbr2Letter));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void incrementMonth(HashMap<String, int[]> monthCountsByEvent, String eventType, String yyyymm) {
		int[] monthCounts = monthCountsByEvent.get(eventType);
		if (monthCounts == null) {
			monthCounts = new int[12];
		}


		monthCounts[Integer.parseInt(yyyymm.substring(4, 6))-1]++;
		monthCountsByEvent.put(eventType, monthCounts);
	}

	public static void processUniqueMonth(HashMap<String, int[]> eventCountsByMonth, String eventType, String yyyymm) {
		int[] eventCounts = eventCountsByMonth.get(eventType);
		if (eventCounts == null) {
			eventCounts = new int[40*12];
		}


		long monthsBetween = ChronoUnit.MONTHS.between(
				YearMonth.from(LocalDate.parse("1980-01-01")), 
				YearMonth.from(LocalDate.parse(yyyymm.substring(0,  4)+"-"+yyyymm.substring(4,  6)+"-01"))
				);

		//			System.out.println(monthsBetween + "  months between 1980-01 and "+yyyymm); 

		eventCounts[(int)monthsBetween]++;

		eventCountsByMonth.put(eventType, eventCounts);
	}

	public static int[] processOverallUniqueMonth(int[] overallEventCountsByMonth, String yyyymm, String[] line) {
		if (overallEventCountsByMonth == null) {
			overallEventCountsByMonth = new int[40*12];
		}


		long monthsBetween = ChronoUnit.MONTHS.between(
				YearMonth.from(LocalDate.parse("1980-01-01")), 
				YearMonth.from(LocalDate.parse(yyyymm.substring(0,  4)+"-"+yyyymm.substring(4,  6)+"-01"))
				);

		//			System.out.println(monthsBetween + "  months between 1980-01 and "+yyyymm); 

		overallEventCountsByMonth[(int)monthsBetween]++;

		System.out.println("NEW OVERALL COUNT: overallEventCountsByMonth["+monthsBetween+"]: "+overallEventCountsByMonth[(int)monthsBetween] + "   "+Arrays.deepToString(line));

		return overallEventCountsByMonth;		
	}


	public static void saveDatasetToCSV(JFreeChart chart, String filename) {
		java.util.List<String> csv = new ArrayList<>();
		if (chart.getPlot() instanceof XYPlot) {
			Dataset dataset = chart.getXYPlot().getDataset();
			XYDataset xyDataset = (XYDataset) dataset;
			int seriesCount = xyDataset.getSeriesCount();
			for (int i = 0; i < seriesCount; i++) {
				int itemCount = xyDataset.getItemCount(i);
				for (int j = 0; j < itemCount; j++) {
					Comparable key = xyDataset.getSeriesKey(i);
					Number x = xyDataset.getX(i, j);
					Number y = xyDataset.getY(i, j);
					csv.add(String.format("%s, %s, %s", key, x, y));
				}
			}

		} else if (chart.getPlot() instanceof CategoryPlot) {
			Dataset dataset = chart.getCategoryPlot().getDataset();
			CategoryDataset categoryDataset = (CategoryDataset) dataset;
			int columnCount = categoryDataset.getColumnCount();
			int rowCount = categoryDataset.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				for (int j = 0; j < columnCount; j++) {
					Comparable key = categoryDataset.getRowKey(i);
					Number n = categoryDataset.getValue(i, j);
					csv.add(String.format("%s, %s", key, n));
				}
			}
		} else {
			throw new IllegalStateException("Unknown dataset");
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename + ".csv"));) {
			for (String line : csv) {
				writer.append(line);
				writer.newLine();
			}
		} catch (IOException e) {
			throw new IllegalStateException("Cannot write dataset", e);
		}
	}
}
