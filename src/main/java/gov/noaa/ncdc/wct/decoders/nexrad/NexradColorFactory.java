/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.wct.decoders.nexrad;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;

public class NexradColorFactory {


	public static Color[] getTransparentColors(String variableName, boolean classify, int alphaChannelValue) throws Exception {
		Color[] c = getColors(variableName, classify);  
		for (int i=1; i<c.length; i++) {
			if (c[i].getAlpha() > alphaChannelValue) {
				c[i] = new Color(c[i].getRed(), c[i].getGreen(), c[i].getBlue(), alphaChannelValue);
			}
		}      
		return c;
	}


	/**
	 * If levels = -1, then don't calculate equal colors and values - just use colors as stated.
	 * @param paletteName
	 * @param levels
	 * @return
	 */
	public static Color[] getColors(String paletteName, int levels) {
		Color[] c = null;
		try {
			URL url = ResourceUtils.getInstance().getJarResource(
					new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
					"/config/colormaps/"+paletteName, null);

			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			ColorsAndValues cav = null;
			if (paletteName.endsWith(".pal")) {
				cav = ColorLutReaders.parseGR2A(br);
				if (levels > 0) {
					cav = ColorsAndValues.calculateEqualColorsAndValues(cav, levels);
				}
			}
			else {
				cav = ColorLutReaders.parseWCTPal(br)[0];
				//			   System.out.println(cav);
				if (levels > 0) {
					cav = ColorsAndValues.calculateEqualColorsAndValues(cav, levels);
					//				   System.out.println(cav);
				}
			}

			c = cav.getColors();
			br.close();

			Double[] vals = cav.getValues();        
			if (vals[0] > vals[1]) {
				WCTUtils.flipArray(c); 
				WCTUtils.flipArray(vals);
			}

		} catch (Exception e) {
			e.printStackTrace();
			c = new Color[] {Color.WHITE};
		}
		return c;
	}



	public static Color[] getColors(String variableName, boolean classify) throws Exception {
		if (variableName.equalsIgnoreCase("Reflectivity") || 
				variableName.equalsIgnoreCase("TotalReflectivityDZ") ||
				variableName.startsWith("Total_Power") ||
				variableName.equalsIgnoreCase("TotalPower")) {
			return getColors(NexradHeader.LEVEL2_REFLECTIVITY, classify);
		}
		else if (variableName.equalsIgnoreCase("RadialVelocity") || 
				variableName.equalsIgnoreCase("Velocity") ||
				variableName.equalsIgnoreCase("RadialVelocityVR")) {
			return getColors(NexradHeader.LEVEL2_VELOCITY, classify);
		}
		else if (variableName.equalsIgnoreCase("SpectrumWidth") || 
				variableName.equalsIgnoreCase("Width") ||
				variableName.equalsIgnoreCase("SpectrumWidthSW")) {
			return getColors(NexradHeader.LEVEL2_SPECTRUMWIDTH, classify);
		}
		else if (variableName.equals("DifferentialReflectivity")) {
			return getColors(NexradHeader.LEVEL2_DIFFERENTIALREFLECTIVITY, classify);
		}
		else if (variableName.equals("CorrelationCoefficient")) {
			return getColors(NexradHeader.LEVEL2_CORRELATIONCOEFFICIENT, classify);
		}
		else if (variableName.equals("DifferentialPhase")) {
			return getColors(NexradHeader.LEVEL2_DIFFERENTIALPHASE, classify);
		}
		else {
			return getColors(NexradHeader.UNKNOWN, classify);
		}
	}


	public static Color[] getColors(int productCode) throws Exception {
		return getColors(productCode, true);
	}

	public static Color[] getColors(int productCode, boolean classify) throws Exception {
		return getColors(productCode, classify, 0.0f);
	}

	public static Color[] getColors(int productCode, boolean classify, float productVersion) throws Exception {


		String paletteName = NexradSampleDimensionFactory.getDefaultPaletteName(productCode, productVersion);
		ColorsAndValues[] cavs = NexradSampleDimensionFactory.getColorsAndValues(paletteName);
		
		if (NexradSampleDimensionFactory.getPaletteOverride().get(paletteName) != null) {
			cavs = NexradSampleDimensionFactory.getPaletteOverride().get(paletteName);
			// check for empty 'colors' and only 'unique' palette entries
			if (cavs.length > 1 && cavs[0].getColors().length == 0) {
				System.out.println("using custom palette: [1]"+paletteName);
//				cavs[1].flip();
				System.out.println(cavs[1].getFlippedCopyColors()[0] + " is first cavs[1].getColors()[0]");
				return cavs[1].getFlippedCopyColors();
			}
			else {
				System.out.println("using custom palette: [0]"+paletteName);
				return cavs[0].getFlippedCopyColors();	
			}	
		}
		else {
			// not sure why they need to be flipped...
			
			// check for empty 'colors' and only 'unique' palette entries
			if (cavs.length > 1 && cavs[0].getColors().length == 0) {
				cavs[1].flip();
				return cavs[1].getColors();
			}
			else {
				cavs[0].flip();
				return cavs[0].getColors();	
			}			
		}
		
	} // END static getColors(productCode)     


	public static Color[] getTransparentColors(int productCode, boolean classify, int alphaChannelValue) throws Exception {

		Color[] c = getColors(productCode, classify);  
		for (int i=0; i<c.length; i++) {
			if (c[i].getAlpha() > alphaChannelValue) {
				c[i] = new Color(c[i].getRed(), c[i].getGreen(), c[i].getBlue(), alphaChannelValue);
			}
		}      

		//      System.out.println("trans. colors: "+Arrays.toString(c));

		return c;
	}
} // END class
