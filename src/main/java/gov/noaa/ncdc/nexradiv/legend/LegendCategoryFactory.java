package gov.noaa.ncdc.nexradiv.legend;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetRemappedRaster;
import gov.noaa.ncdc.wct.decoders.goes.GoesColorFactory;
import gov.noaa.ncdc.wct.decoders.goes.GoesRemappedRaster;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradColorFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradSampleDimensionFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradUtilities;
import gov.noaa.ncdc.wct.ui.LocationTimeseriesProperties;

public class LegendCategoryFactory {

    private final static DecimalFormat fmt2 = new DecimalFormat("0.00");

    private static String[] dpaLabel = // must have same size as color array for dpa 
    {"< 0.10", "0.10 - 0.25", "0.25 - 0.50", "0.50 - 0.75",
        "0.75 - 1.00", "1.00 - 1.50", "1.50 - 2.00", "2.00 - 2.50",
        "2.50 - 3.00", "3.00 - 4.00", "> 4.00"};

    
    private static String[] getCategoryStrings(String palFile, int levels) throws Exception {

		ColorsAndValues[] cavs = NexradSampleDimensionFactory.getColorsAndValues(palFile);
		// check for empty 'colors' and only 'unique' palette entries
		if (cavs.length > 1 && cavs[0].getColors().length == 0) {
			return cavs[1].getLabels();
		}
		else {
			return cavs[0].getLabels();	
		}			
    	
    	
//		URL url = ResourceUtils.getInstance().getJarResource(
//            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
//            "/config/colormaps/"+palFile, null);
//	
//		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
//		ColorsAndValues rawCav = ColorLutReaders.parseWCTPal(br)[0];
//   		br.close();
//		ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(rawCav, levels);
//   		
////		System.out.println(rawCav);
////   		System.out.println(cav);
   		
//		return WCTUtils.flipArray(cav.getLabels(WCTUtils.DECFMT_0D00));
    }
    
    
    public static String[] getCategoryStrings(NexradHeader header, boolean classify) {

    	try {
    	
    		String paletteName = NexradSampleDimensionFactory.getDefaultPaletteName(header.getProductCode(), header.getVersion());
    		ColorsAndValues[] cavs = NexradSampleDimensionFactory.getColorsAndValues(paletteName);
    		
    		if (NexradSampleDimensionFactory.getPaletteOverride().get(paletteName) != null) {
    			cavs = NexradSampleDimensionFactory.getPaletteOverride().get(paletteName);  			
    		}
		
		

    		// check for empty 'colors' and only 'unique' palette entries
    		if (cavs.length > 1 && cavs[0].getColors().length == 0) {
    			String[] catLabels = cavs[1].getLabels();

    			// override labels for Level-III NEXRAD, where category ranges are defined in product header
    			if (header.getProductType() == NexradHeader.L3RADIAL ||
    					header.getProductType() == NexradHeader.L3RASTER ||
    					header.getProductType() == NexradHeader.L3VAD) {

    				for (int n=0; n<catLabels.length; n++) {
    					catLabels[n] = header.getDataThresholdString(catLabels.length-n) + "  ("+(catLabels.length-n)+") ";  // flip so low values are on the bottom
    				}
    			}

    			return catLabels;
    		}
    		else {
    			return cavs[0].getLabels();	
    		}			

    	} catch (Exception e) {
    		e.printStackTrace();
    		return new String[] {"X"};
    	}
        
    }
    
    
    private static Color[] getCategoryColors(String palFile, int levels) throws MalformedURLException, IOException, WCTException {

		URL url = ResourceUtils.getInstance().getJarResource(
				new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
				"/config/colormaps/"+palFile, null);

		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		ColorsAndValues rawCav = ColorLutReaders.parseWCTPal(br)[0];
   		br.close();
		ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(rawCav, levels);
		return WCTUtils.flipArray(cav.getColors());
    }
    
    
   
    
    
    
    
    
    
    public static Color[] getCategoryColors(NexradHeader header, boolean classify) {

    	try {
    		String paletteName = NexradSampleDimensionFactory.getDefaultPaletteName(header.getProductCode(), header.getVersion());
    		ColorsAndValues[] cavs = NexradSampleDimensionFactory.getColorsAndValues(paletteName);
    		
    		if (NexradSampleDimensionFactory.getPaletteOverride().get(paletteName) != null) {
    			cavs = NexradSampleDimensionFactory.getPaletteOverride().get(paletteName);  			
    		}
    	
    		// check for empty 'colors' and only 'unique' palette entries
    		if (cavs.length > 1 && cavs[0].getColors().length == 0) {
    			return cavs[1].getColors();
    		}
    		else {
    			return cavs[0].getColors();	
    		}			

    	
    	
    	} catch (Exception e) {
    		e.printStackTrace();
    		return new Color[] {Color.WHITE};
    	}

    }
    
    
    
    
    
    

    public static String[] getCategoryLabels(GoesRemappedRaster raster) throws Exception {
        GoesColorFactory gcf = GoesColorFactory.getInstance();
//        gcf.calculateEqualColorsAndValues(raster);
//        Double[] values = gcf.getEqualColorsAndValues().getValues();
        Double[] values = gcf.getColorsAndValues(raster).getValues();
//        System.out.println("LCF: "+Arrays.deepToString(values));
        String[] labels = new String[values.length];
        
        DecimalFormat fmt0 = new DecimalFormat("0");
        for (int n=0; n<values.length; n++) {
            labels[n] = fmt0.format(values[n]);
        }
        
        return labels;
    }
    
    public static Color[] getCategoryColors(GoesRemappedRaster raster) throws Exception {
        GoesColorFactory gcf = GoesColorFactory.getInstance();
//        gcf.calculateEqualColorsAndValues(raster);
//        return gcf.getEqualColorsAndValues().getColors();          
        return gcf.getColorsAndValues(raster).getColors();
    }

    
    
    
    
    
    
    
    
    
    public static String getLegendTitle(NexradHeader header, boolean classify) {
        String units = NexradUtilities.getUnits(header.getProductCode());
        if (header.getProductType() == NexradHeader.L3RADIAL || 
                header.getProductType() == NexradHeader.L3RASTER || 
                header.getProductType() == NexradHeader.L3DPA ||
                classify) {
            
        
            return "Legend: "+units+" (Category)";
        }
        else {
            return "Legend: "+units;
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    public static Color[] getCategoryColors(GridDatasetRemappedRaster remappedRaster) {
        Color[] colors = remappedRaster.getDisplayColors();
        return colors;
    }
    
    public static String[] getCategoryLabels(GridDatasetRemappedRaster remappedRaster) {
        DecimalFormat fmt = new DecimalFormat("0.00");
        double minVal = remappedRaster.getDisplayMinValue();
        double maxVal = remappedRaster.getDisplayMaxValue();            
        Color[] catColors = remappedRaster.getDisplayColors();
        
        String[] labels = new String[catColors.length];
        for (int i=0; i<catColors.length; i++) {
            labels[i] = fmt.format(minVal+((maxVal-minVal)/catColors.length*(catColors.length-i)));  
        }
        return labels;
        
    }
    
    public static Double[] getCategoryValues(GridDatasetRemappedRaster remappedRaster) {
        double minVal = remappedRaster.getGridMinValue();
        double maxVal = remappedRaster.getGridMaxValue();       
        if (! remappedRaster.isAutoMinMaxValues()) {
            minVal = remappedRaster.getDisplayMinValue();
            maxVal = remappedRaster.getDisplayMaxValue();
        }
       
        Color[] catColors = remappedRaster.getDisplayColors();
        Double[] values = new Double[catColors.length];
        for (int i=0; i<catColors.length; i++) {
            values[i] = minVal+(((maxVal-minVal)/(catColors.length-1))*(catColors.length-1-i));  
        }
        return values;
        
    }


	public static Color[] getCategoryColors(LocationTimeseriesProperties stationPointProps) {
		return stationPointProps.getCurrentSymbolizedColors();
	}


	public static Double[] getCategoryValues(LocationTimeseriesProperties stationPointProps) {
		return stationPointProps.getCurrentSymbolizedValues();
	}
	
	
	
}
