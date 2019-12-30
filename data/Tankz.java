import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import crisford.swing.game.*;

public class Tankz extends JPanel implements ActionListener, ChangeListener, Runnable
{
  private GamePanel panel;
  private int angle = 90, velocity, gravity, a, b, fcounter = 0;
  private double ai, ail, aiu;
  private JLabel langle, lvelocity, lgravity, lcoordinates;
  private JButton reset, fire;
  private JSlider slider;
  private GameObject ground, human, computer;
  private boolean simulating = false;

  public Tankz()
  {
    setLayout(new BorderLayout());
    panel = new GamePanel(100000, Color.CYAN);
    add(panel, BorderLayout.CENTER);
    JPanel buttons = new JPanel();
    buttons.setLayout(new BorderLayout());
    fire = new JButton("Fire");
    reset = new JButton("Reset");
    fire.addActionListener(this);
    fire.setEnabled(false);
    reset.addActionListener(this);
    buttons.add(fire, BorderLayout.NORTH);
    buttons.add(reset, BorderLayout.SOUTH);
    add(buttons, BorderLayout.SOUTH);
    JFrame parameters = new JFrame("Parameters");
    Container c = parameters.getContentPane();
    c.setLayout(new GridLayout(4, 2));
    c.add(new JLabel("Angle:"));
    langle = new JLabel("90 degrees");
    c.add(langle);
    c.add(new JLabel("Velocity of shell:"));
    lvelocity = new JLabel("Press reset to start a game");
    c.add(lvelocity);
    c.add(new JLabel("Gravitational field strength:"));
    lgravity = new JLabel("Press reset to start a game");
    c.add(lgravity);
    c.add(new JLabel("Relative co-ordinates of target:"));
    lcoordinates = new JLabel("Press reset to start a game");
    c.add(lcoordinates);
    parameters.setSize(500, 500);
    JFrame slidingpane = new JFrame("Angle");
    slider = new JSlider(0, 180, 90);
    slider.addChangeListener(this);
    slidingpane.getContentPane().add(slider, BorderLayout.CENTER);
    slidingpane.setSize(500, 100);
    parameters.show();
    slidingpane.show();
  }

  public void stateChanged(ChangeEvent e)
  {
    if (!simulating)
    {
      angle = slider.getValue();
      langle.setText(Integer.toString(angle) + " degrees");
    }
  }

  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == reset)
    {
      fire.setEnabled(false);
      panel.clear();
      ground = new Ground(panel.getHeight() - (panel.getHeight() / 4), panel.getWidth());
      human = new RedRect((panel.getWidth() / 4) - 5, (panel.getHeight() - (panel.getHeight() / 4)) - 10, 10, 10);
      computer = new RedRect((int) (Math.random() * (double) (panel.getWidth() / 2)) + (panel.getWidth() / 2), (int) (Math.random() * (((double) (panel.getHeight()) * 0.75) - 10.0)), 10, 10);
      panel.put("ground", ground);
      panel.put("human", human);
      panel.put("computer", computer);
      int a = (computer.getX() - (human.getX() + human.getWidth())) / 10;
      int b = (human.getY() - computer.getY()) / 10;
      velocity = 1;
      gravity = 100;
      while (Math.pow((double) (velocity), 4.0) - (Math.pow((double) (gravity), 2.0) * Math.pow((double) (a), 2.0)) - (2.0 * (double) (gravity) * (double) (b) * Math.pow((double) (velocity), 2.0)) < 0)
      {
        velocity = (int) (Math.random() * 50.0) + 1;
        gravity = (int) (Math.random() * 100.0) + 10;
      }
      aiu = 180 + Math.atan((double) (velocity) / (0 - Math.sqrt(Math.pow((double) (velocity), 2.0) - (2 * (double) (gravity) * (0 - b)))));
      ail = 90.0;
      ai = (aiu + ail) / 2.0;
      lvelocity.setText(Integer.toString(velocity) + " m/s");
      lgravity.setText(Integer.toString(gravity) + "N/Kg");
      lcoordinates.setText("(" + Integer.toString(a) + ", " + Integer.toString(b) + ")");
      fire.setEnabled(true);
    }
    else if (e.getSource() == fire)
    {
      new Thread(this).start();
    }
  }

  public void run()
  {
    fire.setEnabled(false);
    reset.setEnabled(false);
    simulating = true;
    for (double i = 0.0;true;i += 0.01)
    {
      RedRect dot = new RedRect(((int) ((Math.cos(Math.toRadians((double) (angle))) * i * (double) (velocity)) * 10.0)) + human.getX() + human.getWidth(), human.getY() - (int) ((((double) (velocity) * i * Math.sin(Math.toRadians((double) (angle)))) - ((double) (gravity) * Math.pow(i, 2.0)) / 2) * 10.0), 1, 1);
      panel.put(Double.toString(i) + " " + Integer.toString(fcounter), dot);
      if (dot.getX() > computer.getX() && dot.getX() < (computer.getX() + computer.getWidth()) && dot.getY() > computer.getY() && dot.getY() < (computer.getY() + computer.getHeight()))
      {
        reset.setEnabled(true);
        JOptionPane.showMessageDialog(null, "Congratulations - You have won!");
        simulating = false;
        return;
      }
      else if (dot.getY() > ground.getY())
      {
        break;
      }
      try
      {
        Thread.sleep(10);
      }
      catch (InterruptedException ie)  {  }
    }
    try
    {
      Thread.sleep(2000);
    }
    catch (InterruptedException ie)  {  }
    for (double i = 0;true;i += 0.01)
    {
      RedRect dot = new RedRect(((int) ((Math.cos(Math.toRadians(ai)) * i * (double) (velocity)) * 10.0)) + computer.getX(), computer.getY() - (int) ((((double) (velocity) * i * Math.sin(Math.toRadians(ai))) - ((double) (gravity) * Math.pow(i, 2.0)) / 2) * 10.0), 1, 1);
      panel.put(Double.toString(0 - i) + " " + Integer.toString(fcounter), dot);
      if (dot.getX() > human.getX() && dot.getX() < (human.getX() + human.getWidth()) && dot.getY() > human.getY() && dot.getY() < (human.getY() + human.getHeight()))
      {
        reset.setEnabled(true);
        fire.setEnabled(false);
        JOptionPane.showMessageDialog(null, "You are dead!");
        simulating = false;
        return;
      }
      else if (dot.getY() > ground.getY())
      {
        if (dot.getX() > human.getX())
        {
          ail = ai;
          ai = (aiu + ail) / 2;
        }
        else
        {
          aiu = ai;
          ai = (aiu + ail) / 2;
        }
        break;
      }
      try
      {
        Thread.sleep(10);
      }
      catch (InterruptedException ie)  {  }
    }
    fcounter++;
    simulating = false;
    reset.setEnabled(true);
    fire.setEnabled(true);
  }
  
  public static void main(String[] args)
  {
    JFrame window = new JFrame("Tankz");
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
    window.getContentPane().add(new Tankz(), BorderLayout.CENTER);
    window.setSize(750, 750);
    window.show();
  }
}

class Ground extends GameObject
{
  public Ground(int pos, int width)
  {
    super(0, pos);
    setWidth(width);
    setHeight(5);
  }

  public void draw(Graphics g) throws GameObjectException
  {
    if (!isWorking())
    {
      throw new GameObjectException();
    }
    g.setColor(Color.BLACK);
    g.fillRect(getX(), getY(), getWidth(), getHeight());
  }
}

class RedRect extends GameObject
{
  public RedRect(int x, int y, int width, int height)
  {
    super(x, y);
    setWidth(width);
    setHeight(height);
  }

  public void draw(Graphics g) throws GameObjectException
  {
    if (!isWorking())
    {
      throw new GameObjectException();
    }
    g.setColor(Color.RED);
    g.fillRect(getX(), getY(), getWidth(), getHeight());
  }
}