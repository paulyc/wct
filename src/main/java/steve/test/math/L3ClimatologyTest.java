package steve.test.math;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import ucar.unidata.io.InMemoryRandomAccessFile;
import ucar.unidata.io.UncompressInputStream;

public class L3ClimatologyTest {

	public static void main(String[] args) {
		try {
			process();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DecodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void process() throws DecodeException, IOException {
		
		URL url = new URL("https://storage.cloud.google.com/gcp-public-data-nexrad-l3/2020/04/06/KCRP/NWS_NEXRAD_NXL3_KCRP_20200406000000_20200406235959.tar.gz?alt=media");
		File f = new File("NWS_NEXRAD_NXL3_KCRP_20200406000000_20200406235959.tar.gz");
		FileUtils.copyURLToFile(url, f);
		
        TarArchiveInputStream tis = getTarInputStream(f.toURI().toURL());
        
        TarArchiveEntry tarEntry = null;
        while ((tarEntry = tis.getNextTarEntry()) != null) {

        	

            long size = tarEntry.getSize();
            int sizeRead = 0;

//            if (! quiet) outStream.println(tarEntry.getName() + " : "+tarEntry.getSize()+ " : "+tis.available());
        	 final byte[] data = new byte[(int) size];
                if (tarEntry.getSize() == 0) {
                    System.err.println("EMPTY 0 BYTE TAR ENTRY: " + tarEntry.getName());
                }
                else {
                    while (sizeRead < size){
                    	sizeRead += tis.read(data, sizeRead, data.length);
                    }
                    if (sizeRead != size) {
                    	System.err.println("ERROR READING ENTRY: " + tarEntry.getName());
                    }
                    

                    // Read header - if error, update in error column
//                    ucar.unidata.io.RandomAccessFile raf = new InMemoryRandomAccessFile(
//                            tarEntry.getName(), data);
//                    raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
                    

                    // Check for product column in database
                    // 012345678901234567890123456789012345
                    // KABQ_SDUS65_NTVFDX_200410052253
//                    if (! quiet) outStream.println("FOUND ENTRY: "+entryString);

                    String entryString = tarEntry.getName();

                    String productCode = entryString.substring(12, 15);
                    
                    
                    if (productCode.equalsIgnoreCase("N0Q")) {
                        System.out.println(productCode+"  "+entryString);
//                    	FileUtils.writeByteArrayToFile(new File(outputDir+File.separator+entryString), data);
                        DecodeL3Header header = new DecodeL3Header();
                        header.decodeHeader(new InMemoryRandomAccessFile(entryString, data));
                        System.out.println(header.getOpMode()+" "+header.getVCP());
                        
                    }
                    
                }






        }
		
		
	}
	
	

	public static TarArchiveInputStream getTarInputStream(URL url) throws MalformedURLException, IOException {
        TarArchiveInputStream tis;
        UncompressInputStream uis = null;
        GZIPInputStream gis = null;        

        InputStream is = url.openStream();
        
        // uncompress UNIX .Z if needed
        if (url.toString().endsWith(".Z") || url.toString().endsWith(".z")) {
            uis = new UncompressInputStream(is);
            tis = new TarArchiveInputStream(uis);
        }
        // uncompress gunzip .gz if needed
        else if (url.toString().endsWith(".GZ") || url.toString().endsWith(".gz")) {
            gis = new GZIPInputStream(is);
            tis = new TarArchiveInputStream(gis);
        }
        else {
            tis = new TarArchiveInputStream(is);
        }

        return tis;
	}
}
