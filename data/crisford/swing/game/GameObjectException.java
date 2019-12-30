package crisford.swing.game;

public class GameObjectException extends Exception
{
  public GameObjectException(String message)
  {
    super(message);
  }

  public GameObjectException()
  {
    this("This Game Object has been stopped");
  }
}