package crisford.swing.game;

import java.util.Enumeration;

public class CollisionDetector extends Thread
{
  private GamePanel game;
  private CollisionListener listener;
  private Object key;

  public CollisionDetector(GamePanel game, CollisionListener listener, Object key)
  {
    this.game = game;
    this.listener = listener;
    this.key = key;
  }

  public void run()
  {
    for (GameObject object = (GameObject) (game.get(key));true;object = (GameObject) (game.get(key)))
    {
      if (object != null)
      {
        Enumeration objects = game.elements();
        Enumeration keys = game.keys();
        while (objects.hasMoreElements())
        {
          GameObject element = (GameObject) (objects.nextElement());
          Object key = keys.nextElement();
          if (element != object && ((element.getX() + element.getWidth() > object.getX() && element.getX() < object.getX() + object.getWidth()) && (element.getY() + element.getHeight() > object.getY() && element.getY() < object.getY() + object.getHeight())))
          {
            listener.collisionDetected(key, element);
          }
        }
      }
    }
  }
}