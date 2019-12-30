package crisford.swing;

import java.awt.Color;

public class Shape
{
  private int x, y, width, height;
  private Color color;
  private String name;

  public Shape()  {  }

  public void setPosition(int x, int y)
  {
    this.x = x;
    this.y = y;
  }

  public void setDimensions(int width, int height)
  {
    this.width = width;
    this.height = height;
  }

  public void setColor(Color color)
  {
    this.color = color;
  }

  public void setType(String name)
  {
    this.name = name;
  }

  public int getX()
  {
    return x;
  }

  public int getY()
  {
    return y;
  }

  public int getWidth()
  {
    return width;
  }

  public int getHeight()
  {
    return height;
  }

  public Color getColor()
  {
    return color;
  }

  public String getType()
  {
    return name;
  }
}