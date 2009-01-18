package hk.hku.cecid.piazza.commons.module.jsw;

import hk.hku.cecid.piazza.commons.module.ActiveModule;
import hk.hku.cecid.piazza.commons.util.Logger;
import hk.hku.cecid.piazza.commons.util.StringUtilities;
import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import org.tanukisoftware.wrapper.WrapperActionServer;

public class JSWActionServerModule extends ActiveModule
{
    private WrapperActionServer wrappingActionServer;
    private int listenPort;
    private boolean localConnectionOnly;
    private boolean isServerStarted;
    public static final int MAX_ACTION_ALLOWED = 255;
    private boolean actionEnabled[];
    private static Field actionsMapField;

    static 
    {
        try
        {

      		actionsMapField = WrapperActionServer.class.getDeclaredField("m_actions");
            actionsMapField.setAccessible(true);
        }
        catch(NoSuchFieldException e)
        {
            String error = "Unable to find \"m_actions\" in the JSW ActServer class, incompatible JSW version";
            System.err.println(error);
            e.printStackTrace();
        }
    }

	/*
    public static final class COMMAND extends Enum
    {

        public static final COMMAND[] values()
        {
            return (COMMAND[])$VALUES.clone();
        }

        public static COMMAND valueOf(String name)
        {
            return (COMMAND)Enum.valueOf(COMMAND.class, name);
        }

        public boolean getEnabled()
        {
            return enabled;
        }

        public String getPropertyKey()
        {
            return property;
        }

        public byte getCode()
        {
            return c;
        }

        public static final COMMAND SHUTDOWN;
        public static final COMMAND FORCE_HALT;
        public static final COMMAND RESTART;
        public static final COMMAND THREADDUMP;
        public static final COMMAND ACCESS_VIOLATION;
        public static final COMMAND JVM_HANG;
        public static final COMMAND UNEXPECT_HALT;
        private byte c;
        private String property;
        private boolean enabled;
        private Runnable r;
        private static final COMMAND $VALUES[];

        static 
        {
            SHUTDOWN = new COMMAND("SHUTDOWN", 0, (byte)83, "shutdownEnabled", true, null);
            FORCE_HALT = new COMMAND("FORCE_HALT", 1, (byte)72, "forceHaltEnabled", true, null);
            RESTART = new COMMAND("RESTART", 2, (byte)82, "restartEnabled", true, null);
            THREADDUMP = new COMMAND("THREADDUMP", 3, (byte)68, "threadDumpEnabled", true, null);
            ACCESS_VIOLATION = new COMMAND("ACCESS_VIOLATION", 4, (byte)86, "stimulateAccessViolationEnabled", false, null);
            JVM_HANG = new COMMAND("JVM_HANG", 5, (byte)71, "stimulateJVMHangEnabled", false, null);
            UNEXPECT_HALT = new COMMAND("UNEXPECT_HALT", 6, (byte)85, "stimulateUnexpectedHaltEnabled", false, null);
            $VALUES = (new COMMAND[] {
                SHUTDOWN, FORCE_HALT, RESTART, THREADDUMP, ACCESS_VIOLATION, JVM_HANG, UNEXPECT_HALT
            });
        }

        private COMMAND(String s, int i, byte c, String property, boolean defaultEnabled, Runnable r)
        {
            super(s, i);
            this.c = c;
            this.property = property;
            enabled = defaultEnabled;
            this.r = r;
        }
    }
*/

    public JSWActionServerModule(String descriptorLocation, boolean shouldInitialize)
    {
        super(descriptorLocation, shouldInitialize);
        listenPort = 9998;
        localConnectionOnly = true;
        isServerStarted = false;
        actionEnabled = new boolean[255];
    }

    public JSWActionServerModule(String descriptorLocation, ClassLoader loader, boolean shouldInitialize)
    {
        super(descriptorLocation, loader, shouldInitialize);
        listenPort = 9998;
        localConnectionOnly = true;
        isServerStarted = false;
        actionEnabled = new boolean[255];
    }

    public JSWActionServerModule(String descriptorLocation, ClassLoader loader)
    {
        super(descriptorLocation, loader);
        listenPort = 9998;
        localConnectionOnly = true;
        isServerStarted = false;
        actionEnabled = new boolean[255];
    }

    public JSWActionServerModule(String descriptorLocation)
    {
        super(descriptorLocation);
        listenPort = 9998;
        localConnectionOnly = true;
        isServerStarted = false;
        actionEnabled = new boolean[255];
    }

    public void init()
    {
        super.init();
        Properties p = super.getParameters();
        listenPort = StringUtilities.parseInt(p.getProperty("listenPort"), 9998);
        localConnectionOnly = StringUtilities.parseBoolean(p.getProperty("localConnectionOnly"), true);
//        COMMAND arr$[] = COMMAND.values();
  //      int len$ = arr$.length;
    //    for(int i$ = 0; i$ < len$; i$++)
   //     {
   //         COMMAND e = arr$[i$];
   //         actionEnabled[e.getCode()] = StringUtilities.parseBoolean(p.getProperty(e.getPropertyKey()), e.getEnabled());
   //     }

        if(localConnectionOnly)
            try
            {
                wrappingActionServer = new WrapperActionServer(listenPort, InetAddress.getByName("localhost"));
            }
            catch(UnknownHostException uhex)
            {
                getLogger().error("[JSW ActServer] Unable to find localhost", uhex);
            }
        else
            wrappingActionServer = new WrapperActionServer(listenPort);
        wrappingActionServer.enableShutdownAction(actionEnabled[83]);
        wrappingActionServer.enableHaltExpectedAction(actionEnabled[72]);
        wrappingActionServer.enableRestartAction(actionEnabled[82]);
        wrappingActionServer.enableThreadDumpAction(actionEnabled[68]);
        wrappingActionServer.enableAccessViolationAction(actionEnabled[86]);
        wrappingActionServer.enableAppearHungAction(actionEnabled[71]);
        wrappingActionServer.enableHaltUnexpectedAction(actionEnabled[85]);
    }

//    public boolean isActionEnabled(COMMAND c)
//    {
//        return actionEnabled[c.getCode()];
//    }

    public boolean getIsLocalConnectionOnly()
    {
        return localConnectionOnly;
    }

    public int getListenPort()
    {
        return listenPort;
    }

    public synchronized void start()
    {
        if(isServerStarted)
            return;
        super.start();
        super.getThread().setName("JSW-Control-ActionServer@" + Integer.toHexString(hashCode()));
        if(wrappingActionServer == null)
            throw new IllegalStateException("The 'JSW action server' has not initialized. Please call init()");
        try
        {
            wrappingActionServer.start();
            isServerStarted = true;
        }
        catch(IOException ioex)
        {
            getLogger().error("[JSW ActServer]: Start Error" + ioex.getMessage(), ioex);
        }
    }

    public synchronized void stop()
    {
    	if (!(this.isServerStarted)) {
      		return;
    	}

    	super.stop();

    	if (this.wrappingActionServer != null)
    	{
      		Thread stopServerHelper = new Thread(this, "JSW-Stop-ActionServer@" + Integer.toHexString(hashCode()))
      		{
        		public void run()
        		{
          			try
          			{
            			wrappingActionServer.stop();
          			}
          			catch (Exception ex)
         			{
            			getLogger().error("[JSW ActServer]: Stop Error" + ex.getMessage(), ex);
          			}
        		}

      		};
      		stopServerHelper.setDaemon(true);
      		stopServerHelper.start();

      		this.isServerStarted = false;
    	}
    }

    public boolean execute()
    {
        do
            try
            {
                Thread.sleep(0x7fffffffL);
            }
            catch(Throwable t)
            {
                return true;
            }
        while(true);
    }

    public void dumpEnabledAction()
    {
        dumpEnabledAction(((OutputStream) (System.out)));
    }

    public void dumpEnabledAction(OutputStream os)
    {
        if(os == null)
            throw new NullPointerException("Missing 'os' in the arguments.");
        Set actionsMapEntries = null;
        try
        {
            actionsMapEntries = ((Map)actionsMapField.get(wrappingActionServer)).entrySet();
            java.util.Map.Entry e;
            for(Iterator i$ = actionsMapEntries.iterator(); i$.hasNext();
                os.write(String.format("COMMAND '%c' : Runnable <%s>\n",
                    new Object[] { e.getKey(), ((Runnable)e.getValue()).toString() }).getBytes()))
                e = (java.util.Map.Entry)i$.next();
        }
        catch(Exception ex)
        {
            getLogger().error("Unable to dump enabled action due to:", ex);
        }
    }
}
