package hk.hku.cecid.piazza.commons.os;

import hk.hku.cecid.piazza.commons.io.FileSystem;
import hk.hku.cecid.piazza.commons.util.StringUtilities;
import java.io.BufferedReader;
import java.io.File;

public class OSManagerWindow extends OSManager
{

    public OSManagerWindow()
    {
    }

    public boolean isValidOS()
    {
        return System.getProperty("os.name").toUpperCase().indexOf("WINDOW") >= 0;
    }

    public String getName()
    {
        return "Window Family OS Manager v1.00";
    }

    public long getDiskFreespace(FileSystem fs, Object param[])
        throws Exception
    {
        return getDiskFreespace(fs.getRoot().getAbsolutePath(), null);
    }

    public long getDiskFreespace(String path, Object param[])
        throws Exception
    {
        String absPath = (new File(path)).getAbsolutePath();
        String command = ("fsutil volume diskfree " + absPath);
        BufferedReader br = executeCommandAsReader(command);
        String line = br.readLine();
        br.close();
        String token[] = StringUtilities.tokenize(line, ":");
        if(token.length < 2)
            throw new ArrayIndexOutOfBoundsException("Missing required token with input " + line);
        else
            return StringUtilities.parseLong(StringUtilities.trim(token[1]), 0L);
    }

    public boolean createDummyFile(String path, long size, Object param[])
        throws Exception
    {
        File f = new File(path);
        if(f.exists())
        {
            return false;
        } else
        {
            String absPath = f.getAbsolutePath();
            Process p = executeCommand("fsutil file createnew " + absPath + " " + size);
            releaseProcess(p);
            return true;
        }
    }
}
