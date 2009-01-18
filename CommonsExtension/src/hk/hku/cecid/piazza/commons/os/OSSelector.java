// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   OSSelector.java

package hk.hku.cecid.piazza.commons.os;

import hk.hku.cecid.piazza.commons.module.Component;
import hk.hku.cecid.piazza.commons.util.Instance;
import java.util.Enumeration;
import java.util.Properties;

// Referenced classes of package hk.hku.cecid.piazza.commons.os:
//            OSManager

public class OSSelector extends Component
{

    public OSSelector()
    {
    }

    public OSManager getInstance()
    {
        if(osInstance == null)
            try
            {
                osInstance = initOSManager();
            }
            catch(Exception e)
            {
                return null;
            }
        return osInstance;
    }

    protected void init()
        throws Exception
    {
        osInstance = initOSManager();
        if(osInstance == null)
            throw new NullPointerException("OS Manager is empty.");
        else
            return;
    }

    protected OSManager initOSManager()
        throws Exception
    {
        Properties p = getParameters();
        String os = System.getProperty("os.name");
        for(Enumeration e = p.keys(); e.hasMoreElements();)
        {
            String osMap = e.nextElement().toString();
            String osClass = p.getProperty(osMap);
            if(os.toUpperCase().indexOf(osMap) >= 0)
            {
                Instance ins = new Instance(osClass, getClass().getClassLoader());
                Object osMan = ins.getObject();
                if(osMan instanceof OSManager)
                    return (OSManager)osMan;
            }
        }

        return null;
    }

    public void addExternalOSManager(String OSFamily, String className)
    {
        if(getParameters() == null)
            setParameters(new Properties());
        getParameters().setProperty(OSFamily, className);
    }

    private static OSManager osInstance;
}
