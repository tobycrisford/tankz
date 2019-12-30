package crisford.swing.game;

import javax.swing.*;
import java.util.*;
import java.awt.*;

public class GamePanel extends JPanel
{
  private Hashtable objects;
  private Color background;
  private boolean running = false;

  public GamePanel(int capacity, Color background)
  {
    objects = new Hashtable(capacity);
    setBackground(background);
    running = true;
    new Painter(this).start();
  }

  public void setBackground(Color background)
  {
    this.background = background;
  }

  public Color getBackground()
  {
    return background;
  }

  public void put(Object key, GameObject object)
  {
    objects.put(key, object);
  }

  public GameObject get(Object key)
  {
    return (GameObject) (objects.get(key));
  }

  public void remove(Object key)
  {
    objects.remove(key);
  }

  public void clear()
  {
    objects.clear();
  }

  public Enumeration elements()
  {
    return objects.elements();
  }

  public Enumeration keys()
  {
    return objects.keys();
  }

  public boolean isRunning()
  {
    return running;
  }

  protected void paintComponent(Graphics g)
  {
    g.setColor(background);
    g.fillRect(0, 0, getWidth(), getHeight());
    Enumeration elements = objects.elements();
    Enumeration keys = objects.keys();
    while (elements.hasMoreElements())
    {
      GameObject object = (GameObject) (elements.nextElement());
      Object key = keys.nextElement();
      try
      {
        object.draw(g);
      }
      catch (GameObjectException e)
      {
        remove(key);
      }
    }
  }

  public void close()
  {
    running = false;
    Enumeration elements = elements();
    while (elements.hasMoreElements())
    {
      GameObject object = (GameObject) (elements.nextElement());
      object.close();
    }
    clear();
  }
}

class Painter extends Thread
{
  private GamePanel panel;

  public Painter(GamePanel panel)
  {
    this.panel = panel;
  }

  public void run()
  {
    while (panel.isRunning())
    {
      panel.repaint();
    }
  }
}