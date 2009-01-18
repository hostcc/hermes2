package hk.hku.cecid.piazza.commons.activation;

import hk.hku.cecid.piazza.commons.io.ByteBufferInputStream;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javax.activation.FileDataSource;

public class FileRegionDataSource extends FileDataSource
{
    private long position;
    private long size;

    public FileRegionDataSource(File file, long position, long size)
    {
        super(file);
        this.position = position;
        this.size = size;
    }

    public FileRegionDataSource(String filepath, long position, long size)
    {
        super(filepath);
        this.position = position;
        this.size = size;
    }

    public InputStream getInputStream()
        throws IOException
    {
        FileInputStream fis = new FileInputStream(getFile());
        FileChannel fc = fis.getChannel();
        ByteBuffer bb = ByteBuffer.allocateDirect((int)size);
        fc.read(bb, position);
        fc.close();
        fc = null;
        fis.close();
        fis = null;
        return new ByteBufferInputStream(bb);
    }

    public OutputStream getOutputStream()
        throws IOException
    {
        return super.getOutputStream();
    }
}
