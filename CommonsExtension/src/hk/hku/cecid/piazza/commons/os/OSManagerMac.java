package hk.hku.cecid.piazza.commons.os;

import hk.hku.cecid.piazza.commons.io.FileSystem;
import hk.hku.cecid.piazza.commons.util.StringUtilities;
import java.io.BufferedReader;
import java.io.File;

public class OSManagerMac extends OSManager
{
    public static final long ONE_GB = 0x40000000L;

    public OSManagerMac()
    {
    }

    public boolean isValidOS()
    {
        return System.getProperty("os.name").toUpperCase().indexOf("MAC") >= 0;
    }

    public String getName()
    {
        return "Mac OS Manager v1.00";
    }

    public long getDiskFreespace(FileSystem fs, Object param[])
        throws Exception
    {
        return getDiskFreespace(fs.getRoot().getAbsolutePath(), param);
    }

    public long getDiskFreespace(String path, Object param[])
        throws Exception
    {
        String absPath = (new File(path)).getAbsolutePath();
        String command = ("df -k " + absPath);
        BufferedReader br = executeCommandAsReader(command);
        br.readLine();
        String line = br.readLine();
        br.close();
        String token[] = StringUtilities.tokenize(line, " ");
        if(token.length < 5)
            throw new ArrayIndexOutOfBoundsException("Missing required token with input " + line);
        else
            return StringUtilities.parseLong(StringUtilities.trim(token[3]), 0L) << 10;
    }

    public boolean createDummyFile(String absPath, long size, Object param[])
        throws Exception
    {
        if((new File(absPath)).exists())
            return false;
        if((new File(absPath)).exists())
            return false;
        long bs = 1L;
        int count = 1;
        if(size > 0x40000000L)
        {
            bs = 0x40000000L;
            count = (int)((size >> 30) + 1L);
        } else
        {
            count = (int)size;
        }
        Process p = executeCommand("dd if=/dev/zero of=" + absPath + " bs=" + bs + " count=" + count);
        releaseProcess(p);
        return true;
    }
}
