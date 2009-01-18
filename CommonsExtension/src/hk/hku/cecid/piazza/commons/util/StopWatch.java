package hk.hku.cecid.piazza.commons.util;

import java.sql.Timestamp;

public class StopWatch
{
    private long startTime;
    private long endTime;
    private static final Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

    public StopWatch()
    {
    }

    public static final Timestamp getCurrentTimestamp()
    {
        currentTimestamp.setTime(System.currentTimeMillis());
        return currentTimestamp;
    }

    public void start()
    {
        startTime = System.currentTimeMillis();
    }

    public void stop()
    {
        endTime = System.currentTimeMillis();
    }

    public void reset()
    {
        startTime = endTime = 0L;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public long getElapsedTime()
    {
        return endTime - startTime;
    }

    public double getElapsedTimeInSecond()
    {
        return ((double)endTime - (double)startTime) / 1000D;
    }
}
