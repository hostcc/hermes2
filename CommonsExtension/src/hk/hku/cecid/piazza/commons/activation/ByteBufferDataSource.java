package hk.hku.cecid.piazza.commons.activation;

import hk.hku.cecid.piazza.commons.io.ByteBufferInputStream;
import java.io.*;
import java.nio.ByteBuffer;
import javax.activation.DataSource;

public class ByteBufferDataSource
    implements DataSource
{
    private ByteBuffer buffer;
    private String name;
    private String contentType;

    public ByteBufferDataSource(ByteBuffer buffer)
        throws IOException
    {
        this.buffer = buffer;
    }

    public InputStream getInputStream()
        throws IOException
    {
        return new ByteBufferInputStream(buffer);
    }

    public OutputStream getOutputStream()
        throws IOException
    {
        return null;
    }

    public String getName()
    {
        return name;
    }

    public String getContentType()
    {
        return contentType;
    }
}
