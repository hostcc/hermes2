package hk.hku.cecid.piazza.commons.util;

import hk.hku.cecid.piazza.commons.dao.DVO;
import hk.hku.cecid.piazza.commons.dao.ds.DataSourceDVO;
import hk.hku.cecid.piazza.commons.module.Component;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @deprecated Class ClassExtractor is deprecated
 */

public class ClassExtractor extends Component
{

    public ClassExtractor()
    {
    }

    protected static String extractObjectToString(Object obj)
    {
        if(obj == null)
        {
            return "";
        } else
        {
            StringBuffer ret = new StringBuffer();
            ret.append("Class: " + obj.getClass().getName() + StringUtilities.LINE_SEPARATOR);
            return ret.toString();
        }
    }

    public static String extractDVOToString(DVO dvo)
    {
        if(!(dvo instanceof DataSourceDVO) || dvo == null)
            return "";
        StringBuffer ret = new StringBuffer();
        ret.append(extractObjectToString(dvo));
        String value;
        StringBuffer buf;
        for(Enumeration keys = ((DataSourceDVO)dvo).getData().keys(); keys.hasMoreElements(); ret.append("Key: " + buf.toString() + "Value: " + value + StringUtilities.LINE_SEPARATOR))
        {
            String key = keys.nextElement().toString();
            value = ((DataSourceDVO)dvo).getData().get(key).toString();
            buf = new StringBuffer(StringUtilities.repeat(" ", 40));
            buf.replace(0, key.length(), key);
        }

        return ret.toString();
    }

    public static String[] extractDVOToLog(DVO dvo)
    {
        if(!(dvo instanceof DataSourceDVO) || dvo == null)
            return null;
        else
            return StringUtilities.toArray(extractDVOToString(dvo), "\n", ((DataSourceDVO)dvo).getData().size());
    }
}
