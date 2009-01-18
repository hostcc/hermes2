package hk.hku.cecid.piazza.commons.io;

import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class ArchiverNULL
    implements Archiver
{

    public ArchiverNULL()
    {
    }

    public boolean compress(File src, File dest, boolean includeItself)
        throws IOException
    {
        if(src == null || dest == null)
            throw new NullPointerException("Source or destination file is empty.");
        if(dest.isDirectory())
            throw new IllegalArgumentException("Destination archive is not a file.");
        else
            return false;
    }

    protected Iterator listFilesToArchive(File src)
    {
        Iterator ret;
        if(src.isFile())
        {
            ArrayList tmp = new ArrayList();
            tmp.add(src);
            ret = tmp.iterator();
        } else
        {
            FileSystem fs = new FileSystem(src);
            ret = fs.getFiles(true).iterator();
            fs = null;
        }
        return ret;
    }

    protected String getBaseArchivingDirectory(File src, boolean includeItself)
    {
        if(src.getParent() != null && src.isDirectory() && includeItself || src.isFile())
            return src.getParentFile().getAbsolutePath();
        else
            return src.getAbsolutePath();
    }

    public List listAsFile(File archive)
        throws IOException
    {
        return null;
    }

    public List listAsFilename(File archive)
        throws IOException
    {
        return null;
    }

    public boolean extract(File archive, File dest)
        throws IOException
    {
        if((archive == null) | (dest == null))
            throw new NullPointerException("Archive or destination file is empty.");
        if(dest.exists() && !dest.isDirectory())
            throw new IllegalArgumentException("Destination file is not directory");
        if(!dest.exists())
            dest.mkdirs();
        return false;
    }
}
