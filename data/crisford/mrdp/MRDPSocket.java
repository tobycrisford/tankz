package crisford.mrdp;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

public class MRDPSocket extends MulticastSocket implements Runnable
{
  private DatagramSocket outsocket;
  private Buffer[] buffers;
  private int rindex, windex, size, port, s_port, id, packetsr, hlength;
  private boolean running = false, lotr = true;
  private final byte DATA = 00000001, JOIN = 00000010;
  private MRDPController[] controllers;
  private byte[] address;
  private PrintStream error;

  public MRDPSocket(int port, int n, int size, int s_port, PrintStream error) throws IOException
  {
    super(port);
    this.port = port;
    this.s_port = s_port;
    buffers = new Buffer[n];
    rindex = windex = id = packetsr = 0;
    this.size = size;
    this.error = error;
    address = InetAddress.getLocalHost().getAddress();
    hlength = (address.length * 2) + 9;
    outsocket = new DatagramSocket();
    controllers = new MRDPController[2];
    for (int i = 0;i < controllers.length;i++)
    {
      controllers[i] = new MRDPController(this, hlength - 1, port, error);
    }
  }

  public MRDPSocket(int port, int n, int size, PrintStream error) throws IOException
  {
    this(port, n, size, 0, error);
  }

  public MRDPSocket(int port, int n, int size) throws IOException
  {
    this(port, n, size, System.err);
  }

  public MRDPSocket(int port) throws IOException
  {
    this(port, 100, 1000);
  }

  public MRDPSocket() throws IOException
  {
    this(7777);
  }

  public int getMaxPacketSize()
  {
    return size;
  }

  public int getPacketLoss()
  {
    if (packetsr == 0)
    {
      return 0;
    }
    int packetsl = 0;
    for (int i = 0;i < controllers.length;i++)
    {
      packetsl += controllers[i].getLost();
    }
    return (int) (((double) (packetsl) / (double) (packetsr)) * 100.0);
  }

  public static void intToBytes(byte[] array, int offset, int integer)
  {
    ByteBuffer buffer = ByteBuffer.wrap(array, offset, 4);
    buffer.putInt(integer);
  }

  public static int bytesToInt(byte[] array, int offset)
  {
    ByteBuffer buffer = ByteBuffer.wrap(array, offset, 4);
    return buffer.getInt();
  }

  protected Buffer locatePacket(byte[] header, int offset) throws IndexOutOfBoundsException
  {
    if (hlength - 1 > header.length - offset)
    {
      throw new IndexOutOfBoundsException("The supplied buffer cannot hold data of length: " + Integer.toString(hlength - 1));
    }
    int start = windex - 30;
    while (start < 0)
    {
      start = buffers.length + start;
    }
    int end = start - 1;
    if (end < 0)
    {
      end = buffers.length + end;
    }
    boolean found = false;
    int i = start;
    for (int ccounter = 0;ccounter < 1;i++)
    {
      if (i == buffers.length)
      {
        i = 0;
      }
      if (i == end)
      {
        ccounter++;
      }
      if (buffers[i] != null)
      {
        byte[] current = buffers[i].getData();
        found = true;
        for (int j = 1;j < hlength;j++)
        {
          if (current[j] != header[(j - 1) + offset])
          {
            found = false;
            break;
          }
        }
      }
      if (found)
      {
        break;
      }
    }
    if (!found)
    {
      return null;
    }
    return buffers[i];
  }

  public void joinGroup(InetAddress group) throws IOException
  {
    super.joinGroup(group);
    running = true;
    new Thread(this, "MRDPSocket").start();
    for (int i = 0;i < controllers.length;i++)
    {
      if (i % 2 == 0)
      {
        new MRDPMaintenance(this, group, port, s_port, controllers[i], JOIN, error).start();
      }
    }
  }

  protected void sendPacket(DatagramPacket packet) throws IOException
  {
    outsocket.send(packet);
  }

  public void receive(DatagramPacket packet) throws IOException
  {
    while (true)
    {
      while (rindex == windex)  {  }
      int mlength = buffers[rindex].getLength() - hlength;
      byte[] buffer = packet.getData();
      if (buffer.length - packet.getOffset() < mlength)
      {
        throw new IOException("Insuffecient buffer size - the next packet to be received cannot be placed in the supplied buffer");
      }
      byte[] r = new byte[address.length];
      System.arraycopy(buffers[rindex].getData(), 1, r, 0, r.length);
      byte[] addr = new byte[address.length];
      System.arraycopy(buffers[rindex].getData(), 1 + address.length, addr, 0, addr.length);
      InetAddress sender = InetAddress.getByAddress(addr), recipient = InetAddress.getByAddress(r);
      if (!sender.equals(InetAddress.getLocalHost()) || recipient.equals(InetAddress.getLocalHost()))
      {
        packet.setLength(mlength);
        packet.setAddress(InetAddress.getByAddress(addr));
        packet.setPort(MRDPSocket.bytesToInt(buffers[rindex].getData(), 1 + (address.length * 2)));
        System.arraycopy(buffers[rindex].getData(), hlength, buffer, packet.getOffset(), mlength);
        rindex++;
        if (rindex >= buffers.length)
        {
          rindex = 0;
        }
        break;
      }
      rindex++;
      if (rindex >= buffers.length)
      {
        rindex = 0;
      }
    }
  }

  public void send(DatagramPacket packet) throws IOException
  {
    int length = packet.getLength() + hlength;
    byte[] buffer = new byte[length];
    System.arraycopy(packet.getData(), packet.getOffset(), buffer, hlength, packet.getLength());
    buffer[0] = DATA;
    System.arraycopy(packet.getAddress().getAddress(), 0, buffer, 1, address.length);
    System.arraycopy(address, 0, buffer, 1 + address.length, address.length);
    intToBytes(buffer, 1 + (address.length * 2), outsocket.getPort());
    intToBytes(buffer, 5 + (address.length * 2), id);
    DatagramPacket out = new DatagramPacket(buffer, length, packet.getAddress(), packet.getPort());
    sendPacket(out);
    id++;
  }

  public void run()
  {
    byte[] buffer = new byte[size];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    while (running)
    {
      try
      {
        super.receive(packet);
        if (buffer[0] == DATA && packet.getLength() > hlength)
        {
          if (buffers[windex] != null)
          {
            buffers[windex].setData(buffer.clone());
            buffers[windex].setLength(packet.getLength());
          }
          else
          {
            buffers[windex] = new Buffer(buffer.clone(), packet.getLength());
          }
          windex++;
          if (windex >= buffers.length)
          {
            windex = 0;
          }
          byte[] ab = new byte[hlength - 1];
          System.arraycopy(buffer, 1, ab, 0, ab.length);
          for (int i = 0;i < controllers.length;i++)
          {
            if (!packet.getAddress().equals(controllers[i].getPeer()))
            {
              controllers[i].sendACK(ab);
            }
            while (controllers[i].getPeer().equals(controllers[i + 1].getPeer()))
            {
              i++;
            }
          }
          packetsr++;
        }
        else if (buffer[0] == JOIN)
        {
          if (packet.getLength() == 5 && !packet.getAddress().equals(InetAddress.getLocalHost()))
          {
            for (int i = 0;i < controllers.length;i++)
            {
              try
              {
                if (!controllers[i].getConnected() && i % 2 != 0)
                {
                  Socket socket = new Socket();
                  socket.connect(new InetSocketAddress(packet.getAddress(), bytesToInt(buffer, 1)), 1000);
                  controllers[i].setSocket(socket);
                  error.println("Connected passive controller to: " + packet.getAddress().toString());
                  break;
                }
                else if (lotr && i % 2 != 0)
                {
                  Socket socket = new Socket();
                  socket.connect(new InetSocketAddress(packet.getAddress(), bytesToInt(buffer, 1)), 1000);
                  controllers[i].setSocket(socket);
                  lotr = false;
                  error.println("Connected passive controller and overwrote an existing connection: " + packet.getAddress().toString());
                  break;
                }
              }
              catch (IOException e)
              {
                error.println("Unable to connect to peer " + packet.getAddress().toString() + " after receiving an advertisement packet");
              }
              catch (ControllerClosedException e)
              {
                error.println("Unable to connect to peer - the passive MRDPController has been closed");
              }
            }
          }
        }
      }
      catch (IOException e)
      {
        error.println("ERROR: The MRDPSocket encountered an IOException");
        e.printStackTrace(error);
        running = false;
      }
    }
  }

  public synchronized void close()
  {
    try
    {
      for (int i = 0;i < controllers.length;i++)
      {
        controllers[i].close();
      }
    }
    catch (IOException e)
    {
      error.println("ERROR: Unable to close MRDPControllers");
      e.printStackTrace(error);
    }
    running = false;
    byte[] buffer = {00000000};
    try
    {
      DatagramPacket flush = new DatagramPacket(buffer, 1, InetAddress.getLocalHost(), port);
      sendPacket(flush);
      Thread.sleep(100);
    }
    catch (IOException e)
    {
      error.println("ERROR: Unable to send flush packet to close MRDPSocket");
      e.printStackTrace(error);
    }
    catch (InterruptedException e)
    {
      error.println("ERROR: Unable to wait for flush packet to arrive - continuing anyway...");
      e.printStackTrace(error);
    }
    super.close();
  }
}

class Buffer
{
  private byte[] buffer;
  private int length;

  public Buffer(byte[] buffer, int length) throws IndexOutOfBoundsException
  {
    setData(buffer);
    setLength(length);
  }

  public Buffer(byte[] buffer) throws IndexOutOfBoundsException
  {
    this(buffer, buffer.length);
  }

  public void setData(byte[] buffer)
  {
    if (buffer.length < length)
    {
      length = buffer.length;
    }
    this.buffer = buffer;
  }

  public void setLength(int length) throws IndexOutOfBoundsException
  {
    if (length > buffer.length)
    {
      throw new IndexOutOfBoundsException("The current buffer is not large enough to store data of length: " + Integer.toString(length));
    }
    this.length = length;
  }

  public byte[] getData()
  {
    return buffer;
  }

  public int getLength()
  {
    return length;
  }
}

class MRDPController extends Thread
{
  private final byte ACK = 00000001, NACK = 00000010, DATA = 00000011;
  private MRDPSocket mrdp;
  private int length, lost, mport;
  private InputStream in;
  private OutputStream out;
  private SocketAddress peer;
  private boolean connected, closed;
  private PrintStream error;

  public MRDPController(MRDPSocket mrdp, int length, int mport, PrintStream error)
  {
    this.mrdp = mrdp;
    this.length = length + 1;
    this.mport = mport;
    this.error = error;
    connected = closed = false;
    lost = 0;
    start();
  }

  public void setSocket(Socket socket) throws ControllerClosedException, IOException
  {
    if (closed)
    {
      throw new ControllerClosedException();
    }
    if (connected)
    {
      in.close();
      out.close();
    }
    in = socket.getInputStream();
    out = socket.getOutputStream();
    peer = socket.getRemoteSocketAddress();
    connected = true;
  }

  public boolean getConnected()
  {
    return connected;
  }

  public int getLost()
  {
    return lost;
  }

  public SocketAddress getPeer()
  {
    return peer;
  }

  private void send(byte[] buffer, int len) throws IOException
  {
    if (connected)
    {
      out.write(buffer, 0, len);
      out.flush();
    }
  }

  public void sendACK(byte[] b) throws IOException
  {
    if (b.length != length - 1)
    {
      throw new IOException("Invalid ACK packet - wrong length");
    }
    byte[] buffer = new byte[b.length + 1];
    System.arraycopy(b, 0, buffer, 1, b.length);
    buffer[0] = ACK;
    send(buffer, buffer.length);
  }

  public void run()
  {
    while (true)
    {
      if (connected)
      {
        try
        {
          byte[] buffer = new byte[length];
          for (int i = 0;i < buffer.length;i += in.read(buffer, i, length - i))  {  }
          if (buffer[0] == ACK)
          {
            if (mrdp.locatePacket(buffer, 1) == null)
            {
              buffer[0] = NACK;
              send(buffer, buffer.length);
              lost++;
            }
          }
          else if (buffer[0] == NACK)
          {
            Buffer packet = mrdp.locatePacket(buffer, 1);
            if (packet != null)
            {
              byte[] data = new byte[packet.getLength() + buffer.length];
              data[0] = DATA;
              MRDPSocket.intToBytes(data, 1, packet.getLength());
              System.arraycopy(packet.getData(), 0, data, buffer.length, packet.getLength());
              send(data, data.length);
            }
          }
          else if (buffer[0] == DATA)
          {
            byte[] data = new byte[MRDPSocket.bytesToInt(buffer, 1)];
            for (int i = 0;i < data.length;i += in.read(data, i, data.length - i))  {  }
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), mport);
            mrdp.sendPacket(packet);
          }
        }
        catch (IOException e)
        {
          error.println("An IO Exception occured while communicating with peer - closing connection (expect packet loss)");
          try
          {
            in.close();
            out.close();
          }
          catch (IOException io)
          {
            error.println("ERROR: Unable to close I/O streams");
            io.printStackTrace(error);
          }
          connected = false;
        }
      }
    }
  }

  public synchronized void close() throws IOException
  {
    if (connected)
    {
      out.close();
      in.close();
      connected = false;
    }
    closed = true;
  }
}

class MRDPMaintenance extends Thread
{
  private MRDPSocket socket;
  private MRDPController controller;
  private DatagramPacket join;
  private int port;
  private PrintStream error;

  public MRDPMaintenance(MRDPSocket socket, InetAddress group, int port, int s_port, MRDPController controller, byte header, PrintStream error)
  {
    this.socket = socket;
    this.controller = controller;
    byte[] buffer = new byte[5];
    buffer[0] = header;
    join = new DatagramPacket(buffer, buffer.length, group, port);
    this.port = s_port;
    this.error = error;
  }

  public void run()
  {
    ServerSocket server = null;
    try
    {
      try
      {
        server = new ServerSocket(port, 1);
      }
      catch (IOException e)
      {
        if (port != 0)
        {
          server = new ServerSocket(0, 1);
          error.println("Unable to accept connections on the preferred port - using instead: " + Integer.toString(server.getLocalPort()));
        }
        else
        {
          throw e;
        }
      }
      server.setSoTimeout(2000);
      MRDPSocket.intToBytes(join.getData(), 1, server.getLocalPort());
    }
    catch (IOException e)
    {
      error.println("ERROR: Unable to start MRDPMaintenance");
      e.printStackTrace(error);
      return;
    }
    while (true)
    {
      try
      {
        while (!controller.getConnected())
        {
          error.println("MRDPController is not connected - multicasting JOIN packet to members of the multicast group (expect packet loss until the controller is connected)");
          socket.sendPacket(join);
          try
          {
            controller.setSocket(server.accept());
          }
          catch (SocketTimeoutException e)  {  }
        }
      }
      catch (ControllerClosedException e)
      {
        break;
      }
      catch (IOException e)
      {
        error.println("ERROR: Stopping an MRDPMaintenance thread - this thread is vital to aid packet retransmission");
        e.printStackTrace(error);
        break;
      }
    }
  }
}

class ControllerClosedException extends Exception
{
  public ControllerClosedException(String message)
  {
    super(message);
  }

  public ControllerClosedException()
  {
    this("The MRDPController has been closed");
  }
}