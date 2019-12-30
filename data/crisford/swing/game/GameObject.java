package crisford.swing.game;

import java.awt.Graphics;
import java.awt.Color;

public class GameObject extends Thread
{
  private int x, y, width = 10, height = 10, sx, sy, xbound, ybound;
  private long rate;
  private boolean working = false, wrap;

  public GameObject(int x, int y, int sx, int sy, int xbound, int ybound, long rate, boolean wrap)
  {
    this.x = x;
    this.y = y;
    this.xbound = xbound;
    this.ybound = ybound;
    this.rate = rate;
    this.wrap = wrap;
    this.sx = sx;
    this.sy = sy;
    if (sx != 0 || sy != 0)
    {
      start();
    }
    working = true;
  }

  public GameObject(int x, int y)
  {
    this(x, y, 0, 0, 0, 0, 0, false);
  }

  public void draw(Graphics g) throws GameObjectException
  {
    if (!isWorking())
    {
      throw new GameObjectException();
    }
    g.setColor(Color.BLACK);
    g.fillRect(x, y, width, height);
  }

  protected void setWidth(int width)
  {
    this.width = width;
  }

  protected void setHeight(int height)
  {
    this.height = height;
  }

  public void setXSpeed(int speed)
  {
    sx = speed;
  }

  public void setYSpeed(int speed)
  {
    sy = speed;
  }

  public void setX(int x)
  {
    this.x = x;
  }

  public void setY(int y)
  {
    this.y = y;
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

  public boolean isWorking()
  {
    return working;
  }

  public void run()
  {
    try
    {
      while (working)
      {
        x += sx;
        y += sy;
        if (wrap)
        {
          if (x > xbound)
          {
            x = 0;
          }
          else if (x < 0)
          {
            x = xbound;
          }
          if (y > ybound)
          {
            y = 0;
          }
          else if (y < 0)
          {
            y = ybound;
          }
        }
        else
        {
          if (x > xbound || y > ybound || x < 0 || y < 0)
          {
            working = false;
            return;
          }
        }
        Thread.sleep(rate);
      }
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
      working = false;
      x = y = width = height = 0;
    }
  }

  public void close()
  {
    working = false;
  }
}