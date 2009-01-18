package hk.hku.cecid.piazza.commons.dao;

public class CacheException extends Exception
{
    private static final long serialVersionUID = 0x71504076b4add803L;

    public CacheException()
    {
    }

    public CacheException(String message)
    {
        super(message);
    }

    public CacheException(Throwable cause)
    {
        super(cause);
    }

    public CacheException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
