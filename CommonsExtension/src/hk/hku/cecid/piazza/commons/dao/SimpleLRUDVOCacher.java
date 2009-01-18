package hk.hku.cecid.piazza.commons.dao;

import hk.hku.cecid.piazza.commons.module.Component;
import hk.hku.cecid.piazza.commons.util.StringUtilities;
import java.util.*;

public class SimpleLRUDVOCacher extends Component
    implements DVOCacher
{
    private transient Object lock;
    private Map cacheEntries;
    private int maxSize;
    private int cacheMiss;
    private int cacheHit;

    public SimpleLRUDVOCacher()
    {
        lock = new Object();
    }

    public SimpleLRUDVOCacher(int cacheSize)
    {
        lock = new Object();
        Properties p = new Properties();
        p.setProperty("cache-size", String.valueOf(cacheSize));
        setParameters(p);
        try
        {
            init();
        }
        catch(Exception e) { }
    }

    protected void init()
        throws Exception
    {
        super.init();
        Properties p = getParameters();
        maxSize = StringUtilities.parseInt(p.getProperty("cache-size"), 10);
	    this.cacheEntries = new LinkedHashMap(this.maxSize, 0.75F, true)
    	{
      		private static final long serialVersionUID = 554434288301737810L;

      		public boolean removeEldestEntry() {
                return size() > maxSize;
      		}
    	};
    }

    public void putDVO(String key, DVO cacheItem)
        throws CacheException
    {
        if(key == null)
            throw new NullPointerException("The key field is null.");
        if(cacheEntries.containsKey(key))
            throw new CacheException("The cacheItem: " + cacheItem + "is exist in the cache memory in this key: " + key);
        synchronized(lock)
        {
            cacheEntries.put(key, cacheItem);
        }
    }

    public void putOrUpdateDVO(String key, DVO cacheItem)
    {
        if(key == null)
            throw new NullPointerException("The key field is null.");
        synchronized(lock)
        {
            cacheEntries.put(key, cacheItem);
        }
    }

    public void removeDVO(String key)
    {
        if(key == null)
            throw new NullPointerException("The key field is null.");
        synchronized(lock)
        {
            cacheEntries.remove(key);
        }
    }

    public void removeAll()
    {
        synchronized(lock)
        {
            cacheEntries.clear();
        }
    }

    public DVO getDVO(String key)
    {
        if(key == null)
            throw new NullPointerException("The key field is null.");
        DVO ret;
        synchronized(lock)
        {
            ret = (DVO)cacheEntries.get(key);
            if(ret != null)
                cacheHit++;
            else
                cacheMiss++;
        }
        return ret;
    }

    public int maxSize()
    {
        return maxSize;
    }

    public int activeSize()
    {
        return cacheEntries.size();
    }

    public double efficieny()
    {
        return ((double)cacheHit / (double)(cacheHit + cacheMiss)) * 100D;
    }
}
