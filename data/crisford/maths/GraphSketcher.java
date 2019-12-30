package crisford.maths;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class GraphSketcher extends JPanel implements ChangeListener
{
  private double xscale = 1.0, yscale = 1.0;
  private JSlider xslider, yslider;
  private Function function;

  public GraphSketcher(Function function)
  {
    super();
    this.xscale = xscale;
    this.yscale = yscale;
    this.function = function;
    JFrame xscaler = new JFrame("x-axis");
    JFrame yscaler = new JFrame("y-axis");
    xslider = new JSlider(0, 100, 50);
    yslider = new JSlider(0, 100, 50);
    xslider.addChangeListener(this);
    yslider.addChangeListener(this);
    xscaler.getContentPane().add(xslider);
    yscaler.getContentPane().add(yslider);
    xscaler.setSize(400, 100);
    yscaler.setSize(400, 100);
    xscaler.show();
    yscaler.show();
  }

  public void stateChanged(ChangeEvent e)
  {
    if (e.getSource() == xslider)
    {
      xscale = (double) (xslider.getValue()) / 50.0;
    }
    else if (e.getSource() == yslider)
    {
      yscale = (double) (yslider.getValue()) / 50.0;
    }
    repaint();
  }

  public void paintComponent(Graphics g)
  {
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(Color.BLACK);
    g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
    g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
    g.setColor(Color.RED);
    for (int i = 0;i < getWidth();i++)
    {
      double[] y = function.evaluate(((double) (i) - (getWidth() / 2)) * xscale);
      for (int j = 0;j < y.length;j++)
      {
        g.fillRect(i, (getHeight() / 2) - (int) (y[j] / yscale), 1, 1);
      }
    }
  }

  public static void showInFrame(GraphSketcher g)
  {
    JFrame window = new JFrame("Graph Sketcher");
    window.getContentPane().add(g);
    window.addWindowListener
    (
      new WindowAdapter()
      {
        public void windowClosing(WindowEvent e)
        {
          System.exit(0);
        }
      }
    );
    window.setSize(500, 500);
    window.show();
  }
}