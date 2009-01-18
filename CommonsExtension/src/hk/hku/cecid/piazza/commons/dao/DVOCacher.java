package hk.hku.cecid.piazza.commons.dao;

public interface DVOCacher
{

    public abstract void putDVO(String s, DVO dvo)
        throws CacheException;

    public abstract void putOrUpdateDVO(String s, DVO dvo);

    public abstract void removeDVO(String s);

    public abstract void removeAll();

    public abstract DVO getDVO(String s);

    public abstract int maxSize();

    public abstract int activeSize();

    public abstract double efficieny();
}
