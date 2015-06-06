/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package display;

import java.awt.*;
import java.awt.geom.Line2D;
import javax.swing.*;
import processor.Wav2graph;

/**
 *
 * @author computer
 */
public class GraphPanel extends JPanel{

    public double[] graph = new double[100000];
    int height=200;
    int width=600;
    double scale;

    public GraphPanel(double[] graph) {
        System.arraycopy(graph, 0, this.graph, 0, min(this.graph.length, graph.length));
        scale=((double)width/(double)graph.length);
        //System.out.println("Scale="+scale);
        //this.graph=graph;
        
    }




    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D gr = (Graphics2D) g;
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int maxHeight = this.getHeight();
        gr.setColor(Color.DARK_GRAY);
        gr.drawLine(0, maxHeight/2, this.getWidth(), maxHeight/2);

        int i;
        for(i=0; i<graph.length; i=i+2)
        {
            gr.draw(new Line2D.Double(i*0.5, maxHeight/2, i*0.5, maxHeight/2 - graph[i]/3));
            //System.out.println(graph[i]);
        }
        setPreferredSize(new Dimension((int) (i * 0.5),maxHeight));

    }
/*
    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(height, width);
    }
*/
    public static int min(int number1, int number2){
        if(number1 < number2)
            return number1;
        return number2;
    }

}


