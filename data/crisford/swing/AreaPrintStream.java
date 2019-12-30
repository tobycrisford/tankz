package crisford.swing;

import java.io.*;
import javax.swing.JTextArea;

public class AreaPrintStream extends PrintStream
{
  private JTextArea area;
  private String buffer;
  private boolean flush, closed = false, error = false;

  public AreaPrintStream(JTextArea area, boolean flush)
  {
    super(new BlankOutputStream());
    this.area = area;
    buffer = "";
    this.flush = flush;
  }

  public AreaPrintStream(JTextArea area)
  {
    this(area, true);
  }

  public void flush()
  {
    if (!closed)
    {
      area.append(buffer);
      buffer = "";
    }
    else
    {
      error = true;
    }
  }

  public void close()
  {
    flush();
    closed = true;
  }

  public boolean checkError()
  {
    return error;
  }

  public void write(int b)
  {
    buffer += new Byte(new Integer(b).byteValue()).toString();
  }

  public void write(byte[] b, int offset, int length)
  {
    for (int i = offset;i < length + offset;i++)
    {
      buffer += new Byte(b[i]).toString();
    }
  }

  public void print(boolean b)
  {
    buffer += new Boolean(b).toString();
  }

  public void print(char c)
  {
    buffer += Character.toString(c);
  }

  public void print(int i)
  {
    buffer += Integer.toString(i);
  }

  public void print(long l)
  {
    buffer += Long.toString(l);
  }

  public void print(float f)
  {
    buffer += Float.toString(f);
  }

  public void print(double d)
  {
    buffer += Double.toString(d);
  }

  public void print(char[] s)
  {
    buffer += new String(s);
  }

  public void print(String s)
  {
    buffer += s;
  }

  public void print(Object obj)
  {
    buffer += obj.toString();
  }

  public void println()
  {
    buffer += "\n";
    if (flush)
    {
      flush();
    }
  }

  public void println(boolean x)
  {
    print(x);
    println();
  }

  public void println(char x)
  {
    print(x);
    println();
  }

  public void println(int x)
  {
    print(x);
    println();
  }

  public void println(long x)
  {
    print(x);
    println();
  }

  public void println(float x)
  {
    print(x);
    println();
  }

  public void println(double x)
  {
    print(x);
    println();
  }

  public void println(char[] x)
  {
    print(x);
    println();
  }

  public void println(String x)
  {
    print(x);
    println();
  }

  public void println(Object x)
  {
    print(x);
    println();
  }
}

class BlankOutputStream extends OutputStream
{
  public BlankOutputStream()  {  }

  public void write(int b)  {  }
}