package recorer;

/*

Java Media APIs: Cross-Platform Imaging, Media and Visualization
Alejandro Terrazas
Sams, Published November 2002,
ISBN 0672320940
*/

import java.io.File;
import java.io.IOException;
import javax.media.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
//import javax.sound.sampled.AudioFormat;

public class DoAudioCapture {

  public static void recordvoice(String filename) throws UnsupportedAudioFileException, IOException, LineUnavailableException {

    Location2Location capture;
    CaptureDeviceInfo audioDevice;
    MediaLocator audioLocation;
    MediaLocator destination;

    audioDevice = CaptureDeviceManager.getDevice("DirectSoundCapture");
    audioLocation = audioDevice.getLocator();

    Format[] format = new Format[1];
    format[0] = new AudioFormat(AudioFormat.LINEAR);
    ContentDescriptor container = new ContentDescriptor(FileTypeDescriptor.WAVE);
    destination = new MediaLocator("file://"+filename);

    capture = new Location2Location(audioLocation, destination, format,container, 1.0);

    System.out.println("Started recording...");
    System.out.println("Read the following...");
    System.out.println("Hello Hello Hello Hello");
    capture.setStopTime(new Time(5.0));
    int waited = capture.transfer(35000);

    int state = capture.getState();
    System.out.println(state);
    System.out.println("Waited " + waited + " milliseconds. State now "
        + state);

    waited /= 1000;
    
    while (state != Location2Location.FINISHED && state != Location2Location.FAILED) 
    {
        
      try {
        Thread.sleep(10000);
      } catch (InterruptedException ie) {
      }
      System.out.println("Waited another 10 seconds, state = " + capture.getState());
      waited += 10;
    }

    System.out.println("Waited a total of " + waited + " seconds");
    //..........................................................

    /*AudioInputStream aIn = AudioSystem.getAudioInputStream(new File("C:\\Users\\Nikhil\\Desktop\\capturedSound.wav"));

    javax.sound.sampled.AudioFormat fmt = aIn.getFormat();
    System.err.println("Playing clip, format="+fmt);
    SourceDataLine aLine = (SourceDataLine)AudioSystem.getLine(new DataLine.Info(SourceDataLine.class,fmt));
    aLine.open(fmt);
    aLine.start();

    int n;
    byte[] buf = new byte[8200];
    do {
    n = aIn.read(buf, 0, buf.length);
    if (n>0)
    aLine.write(buf, 0, n);
    } while (n>0);
    for(byte i:buf)
    {
        System.out.println(i);
    }
    aLine.drain();
    aLine.close();







    System.exit(0);*/
  }
}
