package hk.hku.cecid.piazza.commons.util;

import java.io.*;
import java.lang.management.*;

public class DiagnosticUtilities
{
    private static DiagnosticUtilities singleton;

    public DiagnosticUtilities()
    {
    }

    public static DiagnosticUtilities getInstance()
    {

		synchronized (DiagnosticUtilities.class)
        {
            if(singleton == null)
                singleton = new DiagnosticUtilities();
        }
        return singleton;
    }

    public static DiagnosticUtilities getNewInstance()
    {
        return new DiagnosticUtilities();
    }

    public void dumpAllThread()
        throws IOException
    {
        dumpAllThread(((OutputStream) (System.out)), 3);
    }

    public void dumpAllThread(OutputStream os, int stackTraceDepth)
        throws IOException
    {
        if(os == null)
        {
            throw new NullPointerException("Missing 'os' in the arugments.");
        } else
        {
            os.write(dumpAllThread0(stackTraceDepth).getBytes());
            os.flush();
            return;
        }
    }

    public void dumpAllThread(Writer w, int stackTraceDepth)
        throws IOException
    {
        if(w == null)
        {
            throw new NullPointerException("Missing 'writer' in the arugments.");
        } else
        {
            w.write(dumpAllThread0(stackTraceDepth));
            w.flush();
            return;
        }
    }

    private String dumpAllThread0(int stackTraceDepth)
    {
        if(stackTraceDepth < 0)
            throw new IllegalArgumentException("'stackTraceDepth' must greater than zero.");
		StringBuffer whole = new StringBuffer();
        ThreadMXBean threadBeans = ManagementFactory.getThreadMXBean();
        long tids[] = threadBeans.getAllThreadIds();
        for(int i = 0; i < tids.length; i++)
        {
            ThreadInfo tinfo = threadBeans.getThreadInfo(tids[i], stackTraceDepth);
            String lock = replaceEmpty("lk=", tinfo.getLockName());
            String lockOwnerName = replaceEmpty("lkowner=", tinfo.getLockOwnerName());
            String lockId = tinfo.getLockOwnerId() != -1L ? String.valueOf(tinfo.getLockOwnerId()) : "";
            String blockCount = replaceEmpty("bkCount=", Long.valueOf(tinfo.getBlockedCount()));
            String waitCount = replaceEmpty("wtCount=", Long.valueOf(tinfo.getWaitedCount()));
            String state = getResolvedThreadState(tinfo);

            StringBuffer sb = new StringBuffer();
			sb.append ("\"" + tinfo.getThreadName() + "\" " + "tid=" + tinfo.getThreadId() + " " + state + " " + lock + lockOwnerName + lockId + blockCount + waitCount + "\n");
            StackTraceElement sts[] = tinfo.getStackTrace();
            for(int j = 0; j < sts.length; j++)
                sb.append(" [" + j + "] " + sts[j] + "\n");

            sb.append("\n");
            whole.append(sb);
        }

        return whole.toString();
    }

    private String replaceEmpty(String prefix, Object obj)
    {
        if(obj == null)
            return "";
        if(obj instanceof String)
            return (prefix + (String)obj + " ");
        else
            return (prefix + obj.toString() + " ");
    }

    private String getResolvedThreadState(ThreadInfo tinfo)
    {
        String state = tinfo.getThreadState().toString();
        if(tinfo.isInNative())
            state = ("IS_IN_NATIVE(" + state + ")");
        else
        if(tinfo.isSuspended())
            state = ("SUSPENDED(" + state + ")");
        return state;
    }

    public static void main(String args[])
        throws IOException
    {
        getInstance().dumpAllThread();
    }
}
