//package steve.test;
//
//import java.io.File;
//
//import org.jcodec.api.FrameGrab;
//import org.jcodec.common.io.NIOUtils;
//import org.jcodec.common.model.Picture;
//
//import com.madgag.gif.fmsware.AnimatedGifEncoder;
//
//public class Mp4ToGifTest {
//
//	public static void main(String[] args) {
//        try {
//        	
//        	
//        	
//        
////            AnimatedGifEncoder e = new AnimatedGifEncoder();
////            e.setQuality(1);
////            e.start(outFile.toString());
////            e.setDelay(delayBetweenFramesInMillis);
////            e.setRepeat(0);
//            
//            
//            File file = new File("C:\\work\\wct-tests\\ams-examples\\zoom_0.mp4");
//            FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
//            Picture picture;
//            while (null != (picture = grab.getNativeFrame())) {
//                System.out.println(picture.getWidth() + "x" + picture.getHeight() + " " + picture.getColor());
//            }
//            
//        } catch (Exception e) {
//        	e.printStackTrace();
//        }
//            
//	}
//}
