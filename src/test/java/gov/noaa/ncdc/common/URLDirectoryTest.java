package gov.noaa.ncdc.common;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;


public class URLDirectoryTest {


    
    @Test
    public void testURLDirectoryListing() throws MalformedURLException {







            // Create a URL for the desired page
            URL url = new URL("http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar/DS.p19r1/SI.kgsp/");

            /*
        HTTPRandomAccessFile2 raf = new HTTPRandomAccessFile2(url);
        for (int i=0; i<10; i++) {
           System.out.println(raf.readLine());
        }        
             */
            /*
        // Read all the text returned by the server
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String str;
        while ((str = in.readLine()) != null) {
           System.out.println(str);
        }
             */
            String[] str = URLDirectory.getFileNames(url);
            for (int i=0; i<str.length; i++) {
                System.out.println(str[i]);
            }

            URL[] urls = URLDirectory.getURLs(url);
            for (int i=0; i<urls.length; i++) {
                System.out.println(urls[i]);
            }

    }


}
