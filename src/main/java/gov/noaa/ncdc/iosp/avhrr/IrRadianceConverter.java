/**
*      Copyright (c) 2007-2008 Work of U.S. Government.
*      No rights may be assigned.
*
* LIST OF CONDITIONS
* Redistribution and use of this program in source and binary forms, with or
* without modification, are permitted for any purpose (including commercial purposes) 
* provided that the following conditions are met:
*
* 1.  Redistributions of source code must retain the above copyright notice,
*     this list of conditions, and the following disclaimer.
*
* 2.  Redistributions in binary form must reproduce the above copyright notice,
*     this list of conditions, and the following disclaimer in the documentation
*    and/or materials provided with the distribution.
*
* 3.  In addition, redistributions of modified forms of the source or binary
*     code must carry prominent notices stating that the original code was
*     changed, the author of the revisions, and the date of the change.
*
* 4.  All publications or advertising materials mentioning features or use of
*     this software are asked, but not required, to acknowledge that it was
*     developed at the NOAA's National Climatic Data Center in Asheville, NC and to
*     credit the contributors.
*
* 5.  THIS SOFTWARE IS PROVIDED BY THE GOVERNMENT AND THE CONTRIBUTORS  "AS IS"
*     WITH NO WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED.  In no event
*     shall the Government or the Contributors be liable for any damages
*     suffered by the users arising out of the use of this software, even if
*     advised of the possibility of such damage.
*/
package gov.noaa.ncdc.iosp.avhrr;


/**
 * Calculates Radiance for Thermal Channels (3B, 4, 5) or AVHRR KLM Satellites
 * http://www2.ncdc.noaa.gov/docs/klm/html/c7/sec7-1.htm
 * @author afotos@noao.gov
 * 2/10/2008
 *
 */
public class IrRadianceConverter implements AvhrrConstants{

	/**
	 * calculates radiance for thermal channels
	 * section 7.1.2.3 / equation 7.1.2.3-1
	 * @param coeff1 - IR Operational Cal Coefficient 1(for channel 3b = 0)
	 * @param coeff2 - IR Operational Cal Coefficient 2
	 * @param coeff3 - IR Operational Cal Coefficient 3
	 * @param reading - counts for pixel
	 * @param version - dataset version (used for scaling of coefficients)
	 * @param channel - int channel 3b, 4 or 5
	 * @return float - radiance value for a pixel
	 */
	public static float calibrateIrData(int coeff1, int coeff2, int coeff3, int reading,int version, int channel) {
		double value;
		double a0 = coeff1 * 1E-6;
		double a1 = coeff2 * 1E-6;
		double a2;
		if(version == 2 || channel == 3){
			a2 = coeff3 * 1E-6;
		}else{
			a2 = coeff3 * 1E-7;
		}
		value = a0 + a1 * reading + a2 * reading * reading;
		return (float) value;
	}
	
}


