package hk.hku.cecid.piazza.commons.module;

import hk.hku.cecid.piazza.commons.util.*;
import java.net.URLClassLoader;
import java.util.Properties;

public class ShutdownHookEmailModule extends Module
{
    private Thread shutdownThread;

    public ShutdownHookEmailModule(String descriptorLocation, boolean shouldInitialize)
    {
        super(descriptorLocation, shouldInitialize);
    }

    public ShutdownHookEmailModule(String descriptorLocation, ClassLoader loader, boolean shouldInitialize)
    {
        super(descriptorLocation, loader, shouldInitialize);
    }

    public ShutdownHookEmailModule(String descriptorLocation, ClassLoader loader)
    {
        super(descriptorLocation, loader);
    }

    public ShutdownHookEmailModule(String descriptorLocation)
    {
        super(descriptorLocation);
    }

    public void init()
    {
        super.init();
        try
        {
            synchronized(this)
            {
                if(shutdownThread == null)
                {
                    shutdownThread = createShutdownHookWorker();
                    Runtime.getRuntime().addShutdownHook(shutdownThread);
                }
            }
        }
        catch(Throwable t)
        {
            throw new ModuleException("Unable to initialize mail alert shutdown hook", t);
        }
    }

  	public synchronized void stop()
  	{
    	if (this.shutdownThread != null)
    	{
      		try
      		{
        		Runtime.getRuntime().removeShutdownHook(this.shutdownThread);
      		}
      		catch (Throwable t)
      		{
      		}
      		finally
      		{
        		this.shutdownThread = null;
      		}
    	}
  	}

    protected Thread getThread()
    {
        try
        {
            synchronized(this)
            {
                if(shutdownThread == null)
                    shutdownThread = createShutdownHookWorker();
            }
        }
        catch(Throwable t)
        {
            throw new ModuleException("Unable to retrieve mail alert shutdown hook", t);
        }
        return shutdownThread;
    }

    protected Thread createShutdownHookWorker()
        throws Throwable
    {
        Properties p = super.getParameters();
        String host = super.getRequiredParameter("host");
        String protocol = p.getProperty("protocol", "smtp");
        String username = p.getProperty("username");
        String password = p.getProperty("password");
        String from = p.getProperty("from", "commonDaemon@cecid.hku.hk");
        String tos = super.getRequiredParameter("to");
        String ccs = p.getProperty("cc");
        String subject = p.getProperty("subject", "JVM has been shutdown.");
        boolean verbose = StringUtilities.parseBoolean(p.getProperty("verbose"), false);
        ClassLoader threadCtxClassLoader = copyClassLoader();

    	Class[] argsType = { String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Boolean.TYPE };
        Object initargsType[] = {
            protocol, host, username, password, from, tos, ccs, subject, Boolean.valueOf(verbose)
        };
    	Instance shutdownThreadIns = new Instance(ShutdownHookEmailThread.class.getName(), threadCtxClassLoader, argsType, initargsType);
        Thread shutdownThread = (Thread)shutdownThreadIns.getObject();
        getLogger().debug("[SHUTDOWN] Email Hook: Delegating classloader: " + threadCtxClassLoader);
        shutdownThread.setContextClassLoader(threadCtxClassLoader);
        return shutdownThread;
    }

    private ClassLoader copyClassLoader()
    {
        ClassLoader ret = null;
        ClassLoader cl = getClass().getClassLoader();
        if(cl instanceof URLClassLoader)
        {
            URLClassLoader ucl = (URLClassLoader)cl;
            ret = URLClassLoader.newInstance(ucl.getURLs(), ucl.getParent());
        } else
        {
            ret = cl;
        }
        return ret;
    }
}
