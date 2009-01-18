package hk.hku.cecid.piazza.commons.io;

import java.io.IOException;
import java.io.InputStream;

public class NullInputStream extends InputStream
{

    public NullInputStream()
    {
    }

    public int available()
        throws IOException
    {
        return -1;
    }

    public boolean markSupported()
    {
        return false;
    }

    public int read(byte b[], int off, int len)
        throws IOException
    {
        return -1;
    }

    public int read(byte b[])
        throws IOException
    {
        return -1;
    }

    public synchronized void reset()
        throws IOException
    {
    }

    public long skip(long n)
        throws IOException
    {
        return 0L;
    }

    public int read()
        throws IOException
    {
        return -1;
    }
}
