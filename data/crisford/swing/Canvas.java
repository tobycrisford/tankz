package crisford.swing;

import javax.swing.*;
import java.awt.*;

public class Canvas extends JPanel
{
  private Shape[] shapes;
  private Shape current;
  private int counter = 0;

  public Canvas(int size)
  {
    shapes = new Shape[size];
    current = new Shape();
  }

  public Canvas()
  {
    this(50);
  }

  public void changeShape(int x, int y, int width, int height, Color color, String name)
  {
    current.setPosition(x, y);
    current.setDimensions(width, height);
    current.setColor(color);
    current.setType(name);
    repaint();
  }

  public void write()
  {
    if (counter < shapes.length)
    {
      shapes[counter] = current;
      counter++;
    }
    current = new Shape();
    repaint();
  }

  public void remove()
  {
    if (counter > 0)
    {
      counter--;
      shapes[counter] = null;
    }
    repaint();
  }

  public void clear()
  {
    counter = 0;
    repaint();
  }

  private void drawShape(Shape shape, Graphics g)
  {
    g.setColor(shape.getColor());
    if (shape.getType().equals("oval"))
    {
      g.fillOval(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
    }
    else if (shape.getType().equals("rectangle"))
    {
      g.fillRect(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
    }
    else if (shape.getType().equals("triangleh"))
    {
      int[] x = new int[3];
      x[0] = shape.getX();
      x[1] = shape.getX() + (shape.getWidth() / 2);
      x[2] = shape.getX() + shape.getWidth();
      int[] y = new int[3];
      y[0] = shape.getY() + shape.getHeight();
      y[1] = shape.getY();
      y[2] = shape.getY() + shape.getHeight();
      g.fillPolygon(x, y, 3);
    }
    else if (shape.getType().equals("trianglew"))
    {
      int[] x = new int[3];
      x[0] = shape.getX();
      x[1] = shape.getX() + shape.getWidth();
      x[2] = shape.getX();
      int[] y = new int[3];
      y[0] = shape.getY();
      y[1] = shape.getY() + (shape.getHeight() / 2);
      y[2] = shape.getY() + shape.getHeight();
      g.fillPolygon(x, y, 3);
    }
    else if (shape.getType().equals("line"))
    {
      g.drawLine(shape.getX(), shape.getY(), shape.getX() + shape.getWidth(), shape.getY() + shape.getHeight());
    }
  }

  public void paintComponent(Graphics g)
  {
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, getWidth(), getHeight());
    for (int i = 0;i < counter;i++)
    {
      drawShape(shapes[i], g);
    }
    if (current.getType() != null)
    {
      drawShape(current, g);
    }
  }

  public Shape[] getShapes()
  {
    return shapes.clone();
  }
}