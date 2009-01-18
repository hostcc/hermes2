package hk.hku.cecid.piazza.commons.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;

public class ByteBufferInputStream extends InputStream
{
    protected ByteBuffer buf;
    private ReadableByteChannel channel;

    private static class ByteBufferChannelImpl extends AbstractInterruptibleChannel
            implements ReadableByteChannel
    {
        ByteBufferInputStream in;
        private static final int TRANSFER_SIZE = 8192;
        private byte[] buf = new byte[0];
        private boolean open = true;
        private Object readLock = new Object();

        ByteBufferChannelImpl(ByteBufferInputStream in)
        {
            this.in = in;
        }

        public int read(ByteBuffer dst) throws IOException
        {
            if (dst == null)
                throw new NullPointerException();
            int len = dst.remaining();
            int totalRead = 0;
            int bytesRead = 0;
            synchronized (this.readLock)
            {
                if ((len >= this.in.buf.remaining()) && (this.in.buf.remaining() > 0))
                {
                    totalRead = this.in.buf.remaining();
                    dst.put(this.in.buf);
                }
                while (totalRead < len)
                {
                    int bytesToRead = Math.min(len - totalRead, 8192);
                    if (this.buf.length < bytesToRead)
                        this.buf = new byte[bytesToRead];
                    if ((totalRead > 0) && (this.in.available() <= 0))
                    break;
                    try
                    {
                        begin();
                        bytesRead = this.in.read(this.buf, 0, bytesToRead);
                    } finally
                    {
                        end(bytesRead > 0);
                    }
                    if (bytesRead < 0)
                        break;

                    totalRead += bytesRead;
                    dst.put(this.buf, 0, bytesRead);
                }
                if ((bytesRead >= 0) || (totalRead != 0))
                    return -1;
            }
            return totalRead;
        }

        protected void implCloseChannel()
            throws IOException
        {
            in.close();
            open = false;
        }
    }

    public ByteBufferInputStream(ByteBuffer buffer)
    {
        buf = buffer;
        buf.flip();
        buf.mark();
    }

    public int available()
        throws IOException
    {
        return buf.remaining();
    }

    public ReadableByteChannel getChannel()
    {
        synchronized (this)
        {
            if (this.channel == null)
                this.channel = new ByteBufferChannelImpl(this);
            return this.channel;
        }
    }

    public void close()
        throws IOException
    {
        buf.clear();
        buf.position(0);
        buf.flip();
        buf = null;
        if(channel != null && channel.isOpen())
            channel.close();
    }

    public synchronized void mark(int readlimit)
    {
        buf.mark();
    }

    public boolean markSupported()
    {
        return true;
    }

    public int read()
        throws IOException
    {
        if(buf.position() > buf.limit())
            return -1;
        else
            return buf.get();
    }

    public int read(byte b[])
        throws IOException
    {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len)
        throws IOException
    {
        if(b == null)
            throw new NullPointerException();
        if(off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0)
            throw new IndexOutOfBoundsException();
        int pos = buf.position();
        int count = buf.limit();
        if(pos >= count)
            return -1;
        if(pos + len > count)
            len = count - pos;
        if(len <= 0)
        {
            return 0;
        } else
        {
            buf.get(b, off, len);
            return len;
        }
    }

    public synchronized void reset()
        throws IOException
    {
        buf.reset();
    }

    public long skip(long n)
        throws IOException
    {
        super.skip(n);
        int pos = buf.position();
        int newPos = buf.position() + (int)n;
        int count = buf.limit();
        if(newPos > count)
            newPos = count;
        buf.position(newPos);
        return (long)(newPos - pos);
    }
}
