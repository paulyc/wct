 package gov.noaa.ncdc.wct.export;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTIospManager;
import gov.noaa.ncdc.wct.export.WCTExport.ExportFormat;
import gov.noaa.ncdc.wct.export.raster.WCTMathOp;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport.GeoTiffType;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import ucar.nc2.units.DateFormatter;

public class WCTMathBatch extends WCTExportBatch {


    private static final Logger logger = Logger.getLogger(WCTMathBatch.class.getName());    
    private HashMap<String, String> configReplacementsMap = new HashMap<String, String>();
    
    public enum MathOperationType { MAX, MIN, SUM, AVG, ABS_MAX, ABS_MAX_SIGNED, DIFF};
    
    private MathOperationType currentMathOperationType = null;
    
    
	public WCTMathBatch() {
		super();
	}
	

    /**
     *  Description of the Method
     *
     * @param  args  Description of the Parameter
     */
    public static void main(String args[]) {
    	if (args.length == 5) {
    	            
            Logger.getLogger("gov.noaa.ncdc").setLevel(Level.WARNING);
            

            // Start in batch mode
            WCTMathBatch batchExport = new WCTMathBatch();
            batchExport.runBatchMode(args);
        }
        else {
            printUsage();
        }
        System.exit(0);;
    }
	

    /**
     *  Called directly from main with program arguments
     *
     * @param  args  Description of the Parameter
     */
    public void runBatchMode(String[] args) {
        
        try {
        	if (args.length == 1 && args[0].equals("-version")) {
                checkVersion();
                return;
        	}
        	
        	

          System.out.println(":::::::::::::: mark1");
          System.out.println(Arrays.deepToString(args));
            
        	
            // MATH EXPORT
//        	Arg1 = listfile, Arg2 = outfile, Arg3 = output format, Arg4 = output operation, Arg5 = config xml file/url
            if (args.length == 5) {

                WCTIospManager.registerWctIOSPs();
                WCTExport exporter = new WCTExport();

                args[4] = parseConfigPathAndReplacements(args[4], configReplacementsMap);
                		
                
                
                URL listFileURL = null;
                // Check if URL or File supplied for input file
                if (args[0].startsWith("http://") ||
                        args[0].startsWith("https://") ||
                        args[0].startsWith("ftp://") ||
                        args[0].startsWith("file://")) {
                    
                	listFileURL = new URL(args[0]);        
                }
                else {
                	listFileURL = new File(args[0]).toURI().toURL();
                } 
        		List<String> fileListStrings = IOUtils.readLines(listFileURL.openStream());
                
        		
        		
//                File                                                                                                                                                                                                                                          = new File(args[1]);
                String outFile = args[1];
                
                ExportFormat format = readOutputFormat(args[2]);
                this.currentMathOperationType = readMathType(args[3]);
                
                
                URL configURL = null;
                // Check if URL or File supplied for input file
                if (args[4].startsWith("http://") ||
                        args[4].startsWith("https://") ||
                        args[4].startsWith("ftp://") ||
                        args[4].startsWith("file://")) {
                    
                	configURL = new URL(args[4]);        
                }
                else {
                    configURL = new File(args[4]).toURI().toURL();
                } 
                processConfigFile(exporter, configURL, configReplacementsMap);
                
                
                
//              DO MATH OPERATION OVER LOOP
                WCTRaster processRaster = null;
                int processedCountInLoop = 0;
                for (String str : fileListStrings) {
                	
                	if (processedCountInLoop == 0) {
                		  // Process first file
                        exporter.setOutputFormat(ExportFormat.WCT_RASTER_OBJECT_ONLY);
                        exporter.exportData(new URL(str), File.createTempFile("wct", ".obj"));
                        
                	} 

        			WCTRaster raster = exporter.getLastProcessedRaster();
    				processRaster = processLayer(exporter, processRaster, new URL(str), processedCountInLoop);
    				processedCountInLoop++;
                }
                
                
                
                
//              EXPORT DATA
                WCTRasterExport rasterExport = new WCTRasterExport();
        		if (format == ExportFormat.ARCINFOASCII) {
        			if (! outFile.endsWith(".asc")) {
        				outFile = outFile + ".asc";
        			}
        			rasterExport.saveAsciiGrid(new File(outFile), processRaster);
        		}
        		else if (format == ExportFormat.ARCINFOBINARY) {
        			if (! outFile.endsWith(".flt")) {
        				outFile = outFile + ".flt";
        			}
        			rasterExport.saveBinaryGrid(new File(outFile), processRaster, true);
        		}
        		else if (format == ExportFormat.GRIDDED_NETCDF) {
        			if (! outFile.endsWith(".nc")) {
        				outFile = outFile + ".nc";
        			}
        			rasterExport.saveNetCDF(new File(outFile), processRaster);
        		}
        		else if (format == ExportFormat.GEOTIFF_32BIT) {
        			if (! outFile.endsWith(".tif")) {
        				outFile = outFile + ".tif";
        			}
        			rasterExport.saveGeoTIFF(new File(outFile), processRaster, GeoTiffType.TYPE_32_BIT);
        		}
        		else if (format == ExportFormat.GEOTIFF_GRAYSCALE_8BIT) {
        			if (! outFile.endsWith(".tif")) {
        				outFile = outFile + ".tif";
        			}
        			rasterExport.saveGeoTIFF(new File(outFile), processRaster, GeoTiffType.TYPE_8_BIT);
        		}
                
                
                
                
            
            }
        } catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    

	
	private WCTRaster processLayer(WCTExport exporter, WCTRaster processRaster, URL dataURL, int overallIndex) {
		
		System.out.println("Processing: "+dataURL);
		try {
			SupportedDataType dataType = exporter.getLastProcessedDataType();
	        
//			exporter.exportData(dataURL, File.createTempFile("wct", ".obj"), dataTypeOverride, checkOpendap);
			exporter.exportData(dataURL, File.createTempFile("wct", ".obj"));

			WCTRaster raster = exporter.getLastProcessedRaster();

			// Create initial raster and reset bounds
			if (processRaster == null) {
				DateFormatter df = new DateFormatter();
				// create copy of raster that has value of 0.0 for all data
				processRaster = WCTMathOp.createEmptyRasterCopy(raster);
				processRaster.setLongName("WCT Math "+currentMathOperationType+" operation output grid of: "+raster.getLongName()+
						" -- First timestep: "+df.toDateTimeStringISO( new Date(raster.getDateInMilliseconds()) ));
				processRaster.setVariableName(raster.getVariableName());
				processRaster.setStandardName(raster.getStandardName());
				processRaster.setUnits(raster.getUnits());
			}
			// use last processed date for date dimension
			processRaster.setDateInMilliseconds(raster.getDateInMilliseconds());

			if (currentMathOperationType == MathOperationType.MAX) {
				WCTMathOp.max(processRaster, processRaster, raster, raster.getNoDataValue());
			}
			else if (currentMathOperationType == MathOperationType.MIN) {
				WCTMathOp.min(processRaster, processRaster, raster, raster.getNoDataValue());
			}
			else if (currentMathOperationType == MathOperationType.AVG) {
				WCTMathOp.average(processRaster, processRaster, overallIndex, raster, 1, raster.getNoDataValue());
			}
			else if (currentMathOperationType == MathOperationType.ABS_MAX) {
				WCTMathOp.absMax(processRaster, processRaster, raster, raster.getNoDataValue());
			}
			else if (currentMathOperationType == MathOperationType.ABS_MAX_SIGNED) {
				WCTMathOp.absMaxSigned(processRaster, processRaster, raster, raster.getNoDataValue());
			}
			else if (currentMathOperationType == MathOperationType.SUM) {
				WCTMathOp.sum(processRaster, processRaster, raster, raster.getNoDataValue());
			}
			else if (currentMathOperationType == MathOperationType.DIFF) {
				WCTMathOp.diff(processRaster, processRaster, raster, raster.getNoDataValue());
			}
			else {
				throw new WCTException("math operation type of "+currentMathOperationType+ " is not supported.");
			}

			//	            System.out.println(WCTMathOp.getStatsIgnoreNoData(raster));
			//	            System.out.println(WCTMathOp.getStatsIgnoreNoData(processRaster));

			//				scanResult = viewer.getDataSelector().getScanResults()[selectedIndices[n]];


			// check previous data type
			if (dataType != null && dataType != exporter.getLastProcessedDataType()) {
				logger.warning("WCT Math Tool Warning::::  Warning: New data type found: "+dataType);
			}
			dataType = exporter.getLastProcessedDataType();


			//				System.out.println("n="+n+"   "+scanResult.getDataType());
			System.out.println("n="+overallIndex+"   "+dataType);


		} catch (Exception e) {
			e.printStackTrace();
		}

		
		
		System.out.println("AFTER MATH OP, RASTER EXTENT: "+processRaster.getBounds());
		return processRaster;
	}
	
	
    
    
    
    public static void printUsage() {
    	String script = SystemUtils.IS_OS_WINDOWS ? "wct-math.bat" : "wct-math";
    	
        System.err.println("===================================================");
        System.err.println("WEATHER AND CLIMATE TOOLKIT - MATH EXPORTER: USAGE:");
        System.err.println(" ");
        System.err.println(" ");
        System.err.println(" infile/url - means a file or URL");
        System.err.println(" ");
        System.err.println(" MATH BATCH PROCESS: Arg1 = listfile, Arg2 = outfile, Arg3 = output format, Arg4 = output operation, Arg5 = config xml file/url");
        System.err.println("   Listfile format = infile/url (single file/url on each line");
        System.err.println(" ");
        System.err.println("===================================================");
        System.err.println(" ");
        System.err.println("Examples:");
        System.err.println(" 1. Process Max values from list of files in nexrad-file-list.txt. Export to 32-bit GeoTIFF in 'exported-data' directory and name 'max-reflectivity.tif'.");
        System.err.println("  "+script+" nexrad-file-list.txt exported-data/max-reflectivity.tif tif32 MAX wctBatchConfig.xml");
        System.err.println(" ");
//        System.err.println(" ");
//        System.err.println(" 2. List of Local NetCDF (or GRIB) file.  Export to NetCDF.");
//        System.err.println("  "+script+" my-list-file.txt model-data.nc nc AVERAGE wctBatchConfig.xml");
//        System.err.println(" ");
//        System.err.println(" ");
//        System.err.println(" 3. Level-II NEXRAD Data on Amazon (NOAA BDP collaboration).  Export to Shapefile in 'exported-data' directory.");
//        System.err.println("  "+script+" https://s3.amazonaws.com/noaa-nexrad-level2/2019/06/20/KAMX/KAMX20190620_000434_V06 exported-data shp wctBatchConfig.xml");
//        System.err.println(" ");
//        System.err.println(" ");
//        System.err.println("===================================================");
//        System.err.println(" ");
//        System.err.println("For more examples and information on XML configuration file used for defining filters refer to:");
//        System.err.println(" https://www.ncdc.noaa.gov/wct/batch.php");
//        System.err.println(" ");
//        System.err.println("===================================================");
    }

    

    /**
     *  Parses supported output math operation string and returns MathOperationType object
     *  
     *  Currently supported:  
     *  MAX
     *  MIN
     *  AVG
     *  SUM
     *  ABSMAX - maximum absolute value 
     *  
     *  
     *
     * @param  outputType  Description of the Parameter
     * @return               Description of the Return Value
     */
    public static MathOperationType readMathType(String mathType) {
        if (mathType.trim().toUpperCase().equals("MAX")) {
            return MathOperationType.MAX;
        }
        else if (mathType.trim().toUpperCase().equals("MIN")) {
            return MathOperationType.MIN;
        }
        else if (mathType.trim().toUpperCase().equals("ABS_MAX")) {
            return MathOperationType.ABS_MAX;
        }
        else if (mathType.trim().toUpperCase().equals("SUM")) {
            return MathOperationType.SUM;
        }
        else if (mathType.trim().toUpperCase().equals("AVG")) {
            return MathOperationType.AVG;
        }
        else {
            logger.warning("WEATHER AND CLIMATE TOOLKIT - MATH PROCESSING: ERROR - COULD NOT DETERMINE MATH OPERATION TYPE.  "
            		+ "\nCurrently supported: 'MAX', 'MIN', 'ABS_MAX', 'SUM', 'AVG'");
//            printOutputFormats();
            System.exit(1);
            return null;
        }
    }


	public MathOperationType getCurrentMathOperationType() {
		return currentMathOperationType;
	}


	public void setCurrentMathOperationType(MathOperationType currentMathOperationType) {
		this.currentMathOperationType = currentMathOperationType;
	}
    
    
}
