package hk.hku.cecid.piazza.commons.io;

import java.io.IOException;

public class ChecksumException extends IOException
{
    private static final long serialVersionUID = 0xb5c28dd9cd0ca053L;

    public ChecksumException()
    {
    }

    public ChecksumException(String message)
    {
        super(message);
    }
}
