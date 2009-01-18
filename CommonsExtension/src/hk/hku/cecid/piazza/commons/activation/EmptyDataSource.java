package hk.hku.cecid.piazza.commons.activation;

import hk.hku.cecid.piazza.commons.io.NullInputStream;
import hk.hku.cecid.piazza.commons.io.NullOutputStream;
import java.io.*;
import javax.activation.DataSource;

public class EmptyDataSource
    implements DataSource
{
    private String name;
    private String contentType;

    public EmptyDataSource()
    {
        name = "";
        contentType = "application/octet-stream";
    }

    public EmptyDataSource(String name, String contentType)
    {
        this.name = name;
        this.contentType = contentType;
    }

    public InputStream getInputStream()
        throws IOException
    {
        return new NullInputStream();
    }

    public OutputStream getOutputStream()
        throws IOException
    {
        return new NullOutputStream();
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getName()
    {
        return name;
    }
}
