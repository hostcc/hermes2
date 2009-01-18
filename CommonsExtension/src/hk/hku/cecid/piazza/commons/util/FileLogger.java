package hk.hku.cecid.piazza.commons.util;

import hk.hku.cecid.piazza.commons.module.Component;
import java.io.*;
import java.util.Properties;

public class FileLogger extends Component
    implements Logger
{
    private File logFile;
    private OutputStream outStream;
    private PrintStream logStream;
    private static final String defaultLogFileName = "log.txt";
    private String debugTag;
    private String errorTag;
    private String fatalTag;
    private String warnTag;
    private String infoTag;
    private String stackTraceTag;

    public FileLogger(String filepath)
        throws UtilitiesException
    {
        this(new File(filepath));
    }

    public FileLogger(File f)
        throws UtilitiesException
    {
        debugTag = "[Debug] ";
        errorTag = "[Error] ";
        fatalTag = "[Fatal] ";
        warnTag = "[Warn]\t ";
        infoTag = "[Info]\t ";
        stackTraceTag = "[Stack Trace] ";
        logFile = f;
        try
        {
            if(f.isDirectory())
            {
                logFile = new File(f, "log.txt");
            } else
            {
                if(!f.getParentFile().exists())
                    f.getParentFile().mkdirs();
                logFile.createNewFile();
            }
            outStream = new FileOutputStream(logFile);
            logStream = new PrintStream(outStream, true);
        }
        catch(IOException ioe)
        {
            throw new UtilitiesException("Could not open the log file \"" + logFile.getName() + "\" for writing. Logger not created.", ioe);
        }
    }

    protected void init()
    {
        String tagName[] = {
            "debugTag", "errorTag", "fatalTag", "warnTag", "infoTag"
        };
        String tagValue[] = new String[tagName.length];
        for(int i = 0; i < tagName.length; i++)
            tagValue[i] = getParameters().getProperty(tagName[i]);

        if(tagValue[0] != null)
            debugTag = tagValue[0];
        if(tagValue[1] != null)
            errorTag = tagValue[1];
        if(tagValue[2] != null)
            fatalTag = tagValue[2];
        if(tagValue[3] != null)
            warnTag = tagValue[3];
        if(tagValue[4] != null)
            infoTag = tagValue[4];
    }

    public void debug(Object msg)
    {
        log(debugTag + msg.toString());
    }

    public void debug(Object msg, Throwable t)
    {
        debug(msg);
        logStackTrace(t);
    }

    public void error(Object msg)
    {
        log(errorTag + msg.toString());
    }

    public void error(Object msg, Throwable t)
    {
        error(msg);
        logStackTrace(t);
    }

    public void fatal(Object msg)
    {
        log(fatalTag + msg.toString());
    }

    public void fatal(Object msg, Throwable t)
    {
        fatal(msg);
        logStackTrace(t);
    }

    public void info(Object msg)
    {
        log(infoTag + msg.toString());
    }

    public void info(Object msg, Throwable t)
    {
        info(msg);
        logStackTrace(t);
    }

    public void warn(Object msg)
    {
        log(warnTag + msg.toString());
    }

    public void warn(Object msg, Throwable t)
    {
        warn(msg);
        logStackTrace(t);
    }

    public void log(String s)
    {
        if(logStream != null)
            logStream.println(s);
    }

    public void logStackTrace(Throwable e)
    {
        if(e != null && logStream != null)
        {
            StackTraceElement list[] = e.getStackTrace();
            for(int index = 0; index < list.length; index++)
                log(stackTraceTag + list[index].toString());
        }
    }

    protected void finalize()
    {
        if(logStream != null)
            logStream.close();
        try
        {
            outStream.close();
        }
        catch(IOException e)
        {
            System.err.println("Failed to close the stream.");
        }
    }

    public String toString()
    {
        return super.toString();
    }
}
