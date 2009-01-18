package hk.hku.cecid.piazza.commons.os;

import hk.hku.cecid.piazza.commons.io.FileSystem;
import hk.hku.cecid.piazza.commons.module.Component;
import java.io.*;

public abstract class OSManager extends Component
{

    public OSManager()
    {
    }

    public abstract boolean isValidOS();

    public abstract String getName();

    public abstract long getDiskFreespace(FileSystem filesystem, Object aobj[])
        throws Exception;

    public abstract long getDiskFreespace(String s, Object aobj[])
        throws Exception;

    public abstract boolean createDummyFile(String s, long l, Object aobj[])
        throws Exception;

    public Process executeCommand(String command)
        throws Exception
    {
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        return process;
    }

    public Process executeCommandAsync(String command)
        throws IOException
    {
        Process process = Runtime.getRuntime().exec(command);
        return process;
    }

    public InputStream executeCommandAsInStream(String command)
        throws Exception
    {
        Process process = executeCommandAsync(command);
        process.getErrorStream().close();
        process.getOutputStream().close();
        return new BufferedInputStream(process.getInputStream());
    }

    public BufferedReader executeCommandAsReader(String command)
        throws Exception
    {
        return new BufferedReader(new InputStreamReader(executeCommandAsInStream(command)));
    }

    protected boolean releaseProcess(Process p)
    {
		if (p != null)
      	try {
        	p.getErrorStream().close();
        	p.getInputStream().close();
        	p.getOutputStream().close();
        	return true;
      	} catch (IOException ioe) {
        	return false;
      	}

		return false;
    }
}
