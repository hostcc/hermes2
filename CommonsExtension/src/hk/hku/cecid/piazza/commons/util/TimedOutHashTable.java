package hk.hku.cecid.piazza.commons.util;

import java.util.*;

public class TimedOutHashTable extends Hashtable
{
    private static final long serialVersionUID = 0x720731a54acbc22fL;
    private long sweepInterval;
    private Timer monitor;
    private TimedOutEntryListener listener;

    private class TimedOutTask extends TimerTask
    {

        public void run()
        {
            int len = size();
            if(len == 0)
                return;
            Object key = null;
            TimedOutEntry tmp = null;
            Enumeration e = keys();
            do
            {
                if(!e.hasMoreElements())
                    break;
                key = e.nextElement();
                tmp = (TimedOutEntry)get(key);
                if(tmp.expiredDate != null && System.currentTimeMillis() > tmp.expiredDate.getTime())
                {
                    if(listener != null)
                        listener.timeOut(key, tmp.obj);
                    remove(key);
                }
            } while(true);
        }

        private TimedOutTask()
        {
            super();
        }

    }

    private class TimedOutEntry
    {

        public boolean equals(Object obj)
        {
            if(obj instanceof TimedOutEntry)
            {
                TimedOutEntry t = (TimedOutEntry)obj;
                return this.obj.hashCode() == t.obj.hashCode();
            } else
            {
                return false;
            }
        }

        public int hashCode()
        {
            return obj.hashCode();
        }

        public String toString()
        {
            return (" expire:" + expiredDate.toString());
        }

        private Object obj;
        private Date expiredDate;

        protected TimedOutEntry(Object obj, Date expiredDate)
        {
            super();
            this.obj = obj;
            this.expiredDate = expiredDate;
        }
    }

    public TimedOutHashTable()
    {
        this(5000L);
    }

    public TimedOutHashTable(long sweepInterval)
    {
        this.sweepInterval = 5000L;
        monitor = new Timer(true);
        this.sweepInterval = sweepInterval;
        monitor.scheduleAtFixedRate(new TimedOutTask(), this.sweepInterval, this.sweepInterval);
    }

    public boolean contains(Object value)
    {
        return super.contains(new TimedOutEntry(value, null));
    }

    public boolean containsValue(Object value)
    {
        return super.containsValue(new TimedOutEntry(value, null));
    }

    public Object get(Object key)
    {
        Object obj = super.get(key);
        if(obj != null)
            return ((TimedOutEntry)obj).obj;
        else
            return null;
    }

    public Object put(Object key, Object value)
    {
        return super.put(key, new TimedOutEntry(value, null));
    }

    public Object put(Object key, Object value, long timeOutInMs)
    {
        return super.put(key, new TimedOutEntry(value, new Date(System.currentTimeMillis() + timeOutInMs)));
    }

    public Object put(Object key, Object value, Date timeOutDate)
    {
        return super.put(key, new TimedOutEntry(value, timeOutDate));
    }

    public void setListener(TimedOutEntryListener listener)
    {
        this.listener = listener;
    }

    public long getSweepInterval()
    {
        return sweepInterval;
    }

    public void complete()
    {
        monitor.cancel();
        monitor = null;
    }
}
