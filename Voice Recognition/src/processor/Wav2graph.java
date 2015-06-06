/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package processor;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

/**
 *
 * @author computer
 */
public class Wav2graph {

    double[] graph1 = new double[1000000]; //for storing the graph
    double[] graph2 = new double[1000000]; //for overlapped windowing
    double powerSpectrumArray[][];  //for storing the powerSpectrum
    double powerSpectrumNormalizingFactor;
    public int numberInAframe;
    public int totalNumberOfFrames;
    AudioInputStream ais;
    AudioFormat format;
    double[] featureVector;



    //new ais created from the file, and format is initialized
    public Wav2graph(File wav) throws IOException, UnsupportedAudioFileException{
            System.out.println(wav.getAbsolutePath());
            ais = AudioSystem.getAudioInputStream(wav);
            format = ais.getFormat();
            numberInAframe = 0;
    }


    //returns number of samples per channel
    protected long getNumSamp() {
        return ais.getFrameLength();
    }


    //decodes the byte arrayy readBuffer, and saves first channels samples continuously in graph1
    //double 'max' is used to find the maximum value of the amplitude in the sample (debugging purpose)
    protected void decodeBytes(byte[] readBuffer) {
        int sampleSize = format.getSampleSizeInBits()/8;
        int[] sampleBytes = new int[sampleSize];    //temp array to store the decoded value of a sample
        int bufferIndex = 0;
        double max = 0;

        int n=readBuffer.length/sampleSize;

        for(int i=0;i<n; i++)
        {
            if(format.isBigEndian())
            {
                System.out.println("Big Endian");
                for(int j=0; j<sampleSize; j++)
                {
                    sampleBytes[j] = readBuffer[bufferIndex];
                    System.out.println("buffer value " + j + ":" + sampleBytes[j]);
                    bufferIndex++;
                }
            }

            else
            {
                for(int j=sampleSize -1; j >= 0; j--)
                {
                    //System.out.println(bufferIndex);
                    sampleBytes[j] = readBuffer[bufferIndex];
                    //System.out.println("buffer value " + i + ": "  + sampleBytes[j]);
                    bufferIndex++;
                }
            }

            int value = 0;
            for(int j=0; j<sampleSize; j++)
            {
                value <<= 8;
                value += (int)sampleBytes[j];
            }


            graph1[i] = (double)value;//(Math.pow(2.0, format.getSampleSizeInBits() - 1));
            if(graph1[i] > max)
                max = graph1[i];

        }

        bufferIndex+= (format.getChannels() - 1) * sampleSize;

        System.out.println("Max value of amplitude  :" + max);
    }






    protected void printDetails() {

        long numberOfSamplesPerChannel = getNumSamp();
        int sampleSizeInBytes = format.getSampleSizeInBits() / 8;
        System.out.println("Number of channels : " + format.getChannels());
        System.out.println("Number of samples per channel : "+ numberOfSamplesPerChannel);
        System.out.println("Sample size : " + sampleSizeInBytes + "bytes");
        System.out.println("Sampling rate : " + format.getFrameRate());
        System.out.println("Number of samples in a Frame : " + numberInAframe );
        System.out.println("Total number of frames : " + totalNumberOfFrames);

    }

    protected void applyHammingWindow() {

        //windowing without overlapping frames
        for(int i=0; i<graph1.length && i<graph2.length ; i++)
        {

            graph1[i] = graph1[i] * (0.54 -
                    0.46 * (Math.cos( 2 * Math.PI * (i%numberInAframe) / numberInAframe) ));


            //overlapping frames, first half frame is skipped
            if(i >= numberInAframe/2)
            {
                graph2[i] = graph2[i] * (0.54 -
                        0.46 * (Math.cos(2 * Math.PI * ((i - numberInAframe/2)%numberInAframe)
                        / numberInAframe)));
                
            }

        }
    }

    
    
    //returns value at the specified offset, in the frameNumberth frame. frameNumber starts from 0
    protected double getValue(int frameNumber, int offset) {

        int i;

        if(frameNumber%2 == 0)//if frame is in the first set, that is graph1
            {
                i = frameNumber * numberInAframe /2 //only half of the frames are in this set
                        + offset;
                return graph1[i];
            }

        //else if frame is in the second set (graph2)
        i = ((frameNumber - 1) * numberInAframe) /2 //half of the frames are in this set
                + (numberInAframe/2) + offset;
        return graph2[i];


        //check if the index calculation is right.....


    }

    }




