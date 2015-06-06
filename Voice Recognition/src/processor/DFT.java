/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package processor;

import display.GraphPanel;
import javax.swing.JFrame;

/**
 *
 * @author admin
 */
public class DFT {

    static double[] transform(double[] intensity){
        //intensity.length/2 is the highest freqency embedded in the intensity
        int n = intensity.length;
        double[] transform = new double[15];
        for(int i=0; i<transform.length; i++){

            //find FFT at each point
            double cosComponent = 0;
            double sinComponent = 0;

            for(int j=0; j<n; j++){
                double theta = ((2 * Math.PI)/n) * (i * j);
                cosComponent += intensity[j] * Math.cos(theta);
                sinComponent += intensity[j] * Math.sin(theta);
            }

            transform[i] = (Math.abs(Math.sqrt
                    (Math.pow(cosComponent, 2) + Math.pow(sinComponent, 2)))/n);


            //display the corrusponding power (for debugging)
            if(transform[i] !=0){
                //System.out.println(i + "th frequencies amplitude is " + transform[i]);
            }
        }
        return transform;
    }

    public static void testDFT()
    {

        double[] array = new double[1000];
        for(int i=0; i<array.length; i++){
            if((i/100) %2 ==0 )
                array[i] = 5000;
            else
                array[i] = 0;
        }

        array = DFT.transform(array);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocation(383, 184);
        GraphPanel panel = new GraphPanel(array);
        frame.add(panel);
        frame.setVisible(true);
    }

}
