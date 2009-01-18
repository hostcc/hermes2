package hk.hku.cecid.piazza.commons.net;

public class InternalServerErrorException extends Exception
{
    private static final long serialVersionUID = 0xb5c28dd9cd0ca053L;

    public InternalServerErrorException()
    {
    }

    public InternalServerErrorException(String message)
    {
        super(message);
    }

    public InternalServerErrorException(Throwable cause)
    {
        super(cause);
    }

    public InternalServerErrorException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
