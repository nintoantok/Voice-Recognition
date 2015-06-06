/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package processor;

import display.GraphPanel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import recorer.DoAudioCapture;
import javax.swing.*;

/**
 *
 * @author admin
 */

//check whether no normalization gives better result
//instead of voice samples, try with an array, to check if the program creates frames properly,
//and to ensure that the getValue() of Wav2graph works correctly.

//sample skipping is not considered while calculating total number of samples!!!!
public class Main {

    static String recordedwave="C:\\Users\\Nikhil\\Desktop\\capturedSound.wav";
    static String databasefolder="C:\\Users\\Nikhil\\finalSamples";
    public static void recordvoice() throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
        DoAudioCapture.recordvoice(recordedwave);
    }

    public static void addtodatabase(String name) throws IOException, UnsupportedAudioFileException, LineUnavailableException
    {
        String newfile=databasefolder+System.getProperty("file.separator")+name+".wav";
        DoAudioCapture.recordvoice(newfile);
    }

    public static String recognize() throws IOException, UnsupportedAudioFileException, LineUnavailableException{
        return identify(databasefolder);
        /*
        
        File wav1 = new File("dund2.wav");
        Wav2graph sample1 = new Wav2graph(wav1);
        
        processSample(sample1);

        File wav2 = new File("bini4.wav");
        Wav2graph sample2 = new Wav2graph(wav2);

        processSample(sample2);

        double MSE = calculateMSE(sample1.featureVector, sample2.featureVector, 3, 15);
        System.out.println("MSE  :" + MSE);

        double ME = calculateME(sample1.featureVector, sample2.featureVector, 3, 15);
        System.out.println("ME  :" + ME);

        //outputToFile("out.txt", wav1.getName(), wav2.getName(), Double.toString(ME), Double.toString(MSE));
        //iterateInFolder(new File("."));

        /*for(int i=0; i<33; i++)
        {
            System.out.print(sample1.powerSpectrumArray[1][i] + "  ");
        }*/

   }

    public static void plotGraph(double[] graph, String title){

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLocation(50, 50);
        GraphPanel panel = new GraphPanel(graph);
        JScrollPane p=new JScrollPane(panel);
        p.setSize(600,200);
        JPanel pan=new JPanel();
        frame.add(p);
        frame.setVisible(true);

    }

    private static void getPowerSpectrum(Wav2graph sample) {

        double[] voiceSample = new double[sample.numberInAframe];
        //for(int i=0; i< sample.totalNumberOfFrames; i++)
        for(int i=0; i<sample.powerSpectrumArray.length; i++)
        {
            for(int j=0; j<sample.numberInAframe; j++)
                voiceSample[j] = sample.getValue(i, j);

            sample.powerSpectrumArray[i] = DFT.transform(voiceSample);
        }

    }


    private static void testFraming(Wav2graph sample){

        for(int i=0; i<sample.graph1.length; i++)
            sample.graph1[i] = i;

        for(int i=0; i<sample.graph2.length; i++)
            sample.graph2[i] = i;

        sample.numberInAframe = 200;
        sample.totalNumberOfFrames = (int) ((sample.graph1.length/sample.numberInAframe +
                (sample.graph2.length - sample.numberInAframe/2)/sample.numberInAframe))  ;

        int f = 1;
        int o = 89;
        System.out.println
                ("value at frame number" + f + " and offset " + o + " : " + sample.getValue(f ,o));
    }

    private static void normalizePowerSpectrum(Wav2graph sample) {

        //Average value of power at each f is going to be normalized to 200
        //sample.powerSpectrumNormalizingFactor = 100 * (sample.numberInAframe/2);

        for(int i=0; i<sample.powerSpectrumArray.length; i++)
        {
            double sumOfPowers = 0;
            double powerSpectrumNormalizingFactor = 100 * sample.powerSpectrumArray[i].length;
            for(int j=0; j<sample.powerSpectrumArray[i].length; j++)
            {
                sumOfPowers += sample.powerSpectrumArray[i][j];
            }

            double ratio = sumOfPowers / powerSpectrumNormalizingFactor;
            for(int j=0; j<sample.powerSpectrumArray[i].length; j++)
            {
                sample.powerSpectrumArray[i][j] = sample.powerSpectrumArray[i][j] / ratio;
            }

            if(i==0)
            {
                System.out.println("Sum of powers   :" + sumOfPowers);
                System.out.println("powerSpectrumArray[i].length   :" + sample.powerSpectrumArray[i].length);
                //System.out.println("Sum of powers   :" + sumOfPowers);
            }

        }
    }

    private static void displayPowerSpectrum(Wav2graph sample) {

        for(int i=0; i<5; i++)
            plotGraph(sample.powerSpectrumArray[i],"Power Spectrum");

    }

    private static void processSample(Wav2graph sample) throws IOException {
        long numberOfSamplesPerChannel = sample.getNumSamp();




        //checking the size of the voice input
        if(numberOfSamplesPerChannel * sample.format.getChannels()
                * sample.format.getSampleSizeInBits() /8 > Integer.MAX_VALUE){
            System.out.println("Sample is too long");
            return;
        }

        long totalNumOfSamples = numberOfSamplesPerChannel * sample.format.getChannels();



        //Temporarily edited to read only a small part of the sample
        System.out.println("Sample bits:"+sample.format.getSampleSizeInBits());
        int size=(int)totalNumOfSamples * sample.format.getSampleSizeInBits()/8;
        byte[] readBuffer = new byte[size];
        //byte[] readBuffer = new byte[100000];
        System.out.println("Total number of samples : " + totalNumOfSamples);
        System.out.println("Size is :"+size);
        //sample.ais.skip(100000);  //first few samples are skipped, to avoid silence


        int res = sample.ais.read(readBuffer, 0, (int) totalNumOfSamples
                * sample.format.getSampleSizeInBits()/8);

        //int res = sample.ais.read(readBuffer, 0, 100000);
        if(res == -1)
            System.out.println("Couldn't read from file");

        sample.decodeBytes(readBuffer);
        sample.graph1 = lowPassFilter(sample.graph1, 1000, sample.format.getFrameRate());

        //plotGraph(sample.graph1);

        //for windowing, graph2 is created, alternate frames are in graph1 and graph2
        //see getValue() and applyHammingWindow for details
        System.arraycopy(sample.graph1, 0, sample.graph2, 0, sample.graph2.length);

        //number of samples in a frame of 30 mS
        sample.numberInAframe = (int) (sample.format.getFrameRate() * 0.030);

        //totalNumberOfFrames consists of the overlapping frames also,
        //and so the "numberOfSamplesPerChannel - sample.numberInAframe /2" term
        sample.totalNumberOfFrames = (int) (2 * numberOfSamplesPerChannel - sample.numberInAframe /2)
                / sample.numberInAframe;





        sample.applyHammingWindow();


        //prints the file info
        //sample.printDetails();

        //testing framing
        //testFraming(sample);


        //displays the points in a panel
        //plotGraph(sample.graph1);
        sample.powerSpectrumArray = new double[700][sample.numberInAframe/2];
        getPowerSpectrum(sample);
        //displayPowerSpectrum(sample);
        //normalizePowerSpectrum(sample);
        //displayPowerSpectrum(sample);
        sample.featureVector = getFeatureVector(sample);
        //plotGraph(sample.featureVector);
        //testing DFT
        //DFT.testDFT();

    }

    private static double[] getFeatureVector(Wav2graph sample) {

        double[] vector = new double[sample.numberInAframe/2];

        for(int i=0; i<sample.powerSpectrumArray[0].length; i++)
        {
            int average = 0;
            for(int j=0; j<sample.powerSpectrumArray.length; j++)
            {

                average += sample.powerSpectrumArray[j][i];
            }

            vector[i] = average/sample.powerSpectrumArray.length;

        }
        return vector;
    }

    private static double calculateMSE(double[] featureVector1, double[] featureVector2, int start, int end) {


        double MSE = 0;
        for(int i=start; i<end; i++)
        {
            MSE += Math.pow((featureVector1[i] - featureVector2[i]), 2);
        }

        return MSE/(end - start);
    }


    private static double calculateME(double[] featureVector1, double[] featureVector2, int start, int end) {

        double ME = 0;
        for(int i=start; i<end; i++)
        {
            ME += Math.abs(featureVector1[i] - featureVector2[i]);
        }

        return ME/(end - start);
    }

    public static int min(int number1, int number2){
        if(number1 < number2)
            return number1;
        return number2;
    }

    private static void outputToFile(String fileName, String str1, String str2, String str3, String str4) throws IOException
    {

     try{

            String lineSeperator = System.getProperty("line.separator");
            FileWriter fstream = new FileWriter(fileName, true);
            BufferedWriter outpt = new BufferedWriter(fstream);


            outpt.write(str1 + "\t\t" + str2 + "\t\t" + str3 + "\t\t" + str4 + lineSeperator);
            outpt.close();
            }catch (Exception e){
                System.err.println("Error: " + e.getMessage()); }


     }



    private static void iterateInFolder(File folder) throws IOException
    {

        String[] fileList = null;
        if(folder.isDirectory())
        {
            fileList = folder.list();
        }

        for(int i=0; i<fileList.length; i++)
        {
            if(!(new File(fileList[i]).isDirectory()))
            outputToFile("files.txt", fileList[i], "", "", "");
        }
    }


    private static String identify(String folderName) throws IOException, UnsupportedAudioFileException
    {
        File wav1 = null, wav2 = null;
        Wav2graph sample1 = null, sample2 = null,matched=null;

        File folder = new File(folderName);
        String[] fileList = null;
        //iterateInFolder(folder);
        String outputFileName = folder.getAbsolutePath() + System.getProperty("file.separator") + "output"+System.getProperty("file.separator")+"out.txt";
        if(folder.isDirectory())
        {
            fileList = folder.list();
            //outputToFile("out.txt", folder.getAbsolutePath(), "", "", "");

        }

         wav1 = new File(recordedwave);
         sample1 = new Wav2graph(wav1);
         processSample(sample1);
         plotGraph(sample1.graph1,"Input");

         double min=100000.0;
         String minfile="";

        for(int i=0; i<fileList.length; i++)
        {
           // for(int j=i+1; j<fileList.length; j++)
            //{

               
                wav2 = new File(folderName + System.getProperty("file.separator") + fileList[i]);
                if(!(wav1.isDirectory() || wav2.isDirectory()))
                {
                    sample2 = new Wav2graph(wav2);

                    processSample(sample2);

                    double MSE = calculateMSE(sample1.featureVector, sample2.featureVector, 3, 15);

                    double ME = calculateME(sample1.featureVector, sample2.featureVector, 3, 15);

                    double sum=MSE+ME;
                    if(sum<min)
                    {
                        min=sum;
                        minfile=fileList[i];
                        matched=sample2;
                    }
                    outputToFile(outputFileName, wav1.getName(), wav2.getName(), Integer.toString((int) ME), Integer.toString((int) MSE));
                    outputToFile(outputFileName, "", "", "", "");
                }

            //}
            outputToFile(outputFileName, "", "", "", "");

        }
         plotGraph(matched.graph1,"Matched");
         String []x=minfile.split("[\\\\/]");
         String name=x[x.length-1];
         x=name.split("\\.");
         name=x[0];
         System.out.println("Your sound matches with the sound of "+name);
         return name;
    }




    private static double[] lowPassFilter(double[] signal, double cutOff, double samplingRate)
    {
        double[] output;
        double RC, dt, alpha;

        RC = 1/(2 * Math.PI * cutOff);
        dt = 1/samplingRate;
        alpha = dt / (dt + RC);

        output = new double[signal.length];
        output[0] = signal[0];

        for(int i=1; i<signal.length; i++)
        {
            output[i] = alpha * signal[i] + (1-alpha) * output[i-1];
        }
        return output;
    }


}
