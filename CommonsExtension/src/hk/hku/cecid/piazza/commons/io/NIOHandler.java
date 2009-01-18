package hk.hku.cecid.piazza.commons.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class NIOHandler
{
    public static final int MAX_BUFFER_SIZE = 0xa00000;
    public static final int DEFAULT_PAGE_SIZE = 65535;

    public NIOHandler()
    {
    }

    public static void pipe(FileInputStream fins, OutputStream out)
        throws IOException
    {
        WritableByteChannel outChan = Channels.newChannel(out);
        FileChannel fileChan = fins.getChannel();
        fins.getChannel().transferTo(0L, fileChan.size(), outChan);
        outChan = null;
    }

    public static void pipe(FileInputStream fins, OutputStream out, int startPosition, long size)
        throws IOException
    {
        WritableByteChannel outChan = Channels.newChannel(out);
        fins.getChannel().transferTo(startPosition, size, outChan);
        outChan = null;
    }

    public static void pipe(InputStream ins, OutputStream out)
        throws IOException
    {
        if(ins == null || out == null)
            return;
        ReadableByteChannel inChan = Channels.newChannel(ins);
        WritableByteChannel outChan = Channels.newChannel(out);
        int size = ins.available();
        if(size > 0xa00000)
            size = 0xa00000;
        ByteBuffer buffers;
        for(buffers = ByteBuffer.allocateDirect(size); inChan.read(buffers) != -1; buffers.clear())
        {
            buffers.flip();
            outChan.write(buffers);
            out.flush();
        }

        inChan = null;
        outChan = null;
        buffers = null;
    }

    public static byte[] readBytes(InputStream ins)
        throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream(ins.available());
        pipe(ins, out);
        byte bytes[] = out.toByteArray();
        out.close();
        return bytes;
    }

    public static ByteBuffer readByteBuffer(InputStream ins)
        throws IOException
    {
        System.out.println(ins.available());
        ReadableByteChannel inChan = Channels.newChannel(ins);
        ByteBuffer buffers = ByteBuffer.allocate(ins.available());
        inChan.read(buffers);
        inChan = null;
        return buffers;
    }

    public static void writeBytes(byte bytes[], OutputStream out)
        throws IOException
    {
        ByteArrayInputStream ins = new ByteArrayInputStream(bytes);
        pipe(ins, out);
    }

    public static void writeBytes(ByteBuffer src, OutputStream out)
        throws IOException
    {
        WritableByteChannel outChan = Channels.newChannel(out);
        outChan.write(src);
        outChan = null;
    }
}
