package hk.hku.cecid.piazza.commons.os;

import hk.hku.cecid.piazza.commons.io.FileSystem;
import hk.hku.cecid.piazza.commons.util.StringUtilities;
import java.io.*;
import java.nio.channels.FileChannel;

public class OSManagerLinux extends OSManager
{
    public static final long ONE_GB = 0x40000000L;

    public OSManagerLinux()
    {
    }

    public boolean isValidOS()
    {
        return System.getProperty("os.name").toUpperCase().indexOf("LINUX") >= 0;
    }

    public String getName()
    {
        return "Linux OS Manager v1.00";
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

    public synchronized boolean createDummyFile(String absPath, long size, Object param[])
        throws Exception
    {
        if((new File(absPath)).exists())
            return false;
        FileInputStream fis = new FileInputStream("/dev/zero");
        FileOutputStream fos = new FileOutputStream(absPath, true);
        long bytesWritten = 0L;
        byte bytes[] = new byte[8192];
        do
        {
            if((bytesWritten += fis.read(bytes)) == -1L)
                break;
            fos.write(bytes);
        } while(bytesWritten < size);
        FileChannel foc = fos.getChannel();
        foc.truncate(size);
        foc.close();
        foc = null;
        fos.close();
        fos = null;
        fis.close();
        fis = null;
        return true;
    }
}
