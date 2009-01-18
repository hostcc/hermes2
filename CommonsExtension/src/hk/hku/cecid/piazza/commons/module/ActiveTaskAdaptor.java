package hk.hku.cecid.piazza.commons.module;

public class ActiveTaskAdaptor
    implements ActiveTask
{

    public ActiveTaskAdaptor()
    {
    }

    public void execute()
        throws Exception
    {
    }

    public int getMaxRetries()
    {
        return -1;
    }

    public long getRetryInterval()
    {
        return -1L;
    }

    public boolean isRetryEnabled()
    {
        return false;
    }

    public boolean isSucceedFast()
    {
        return false;
    }

    public void onAwake()
    {
    }

    public void onFailure(Throwable throwable)
    {
    }

    public void setRetried(int i)
    {
    }
}
