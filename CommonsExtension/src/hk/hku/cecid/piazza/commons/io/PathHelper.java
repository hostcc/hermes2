package hk.hku.cecid.piazza.commons.io;

import java.io.File;
import java.io.IOException;

public class PathHelper
{

    public PathHelper()
    {
    }

    public static String getCanonicalPath(String basePath, String path)
        throws IOException
    {
        File found = null;
        if(!(new File(path)).isAbsolute())
            found = new File(basePath, path);
        else
            found = new File(path);
        return found.getCanonicalPath();
    }

    public static String getRelativePath(String basePath, String path)
        throws IOException
    {
        if(basePath == null || path == null || basePath.equalsIgnoreCase(path))
            return "";
        if(path.startsWith(basePath))
        {
            String relPath = path.substring(basePath.length(), path.length());
            return ("." + relPath);
        } else
        {
            return "";
        }
    }

    public static String getExtension(String path)
    {
        if(path == null)
            return null;
        int index = path.lastIndexOf(".");
        if(index != -1)
            return path.substring(index + 1, path.length());
        else
            return "";
    }

    public static String removeExtension(String path)
    {
        if(path != null)
        {
            int index = path.lastIndexOf(".");
            if(index != -1)
                return path.substring(0, index);
        }
        return path;
    }

    public static String getFilename(String path)
        throws IOException
    {
        String ret = "";
        File f = new File(path);
        if(!f.exists())
            throw new IOException("File does not exist.");
        if(f.exists() && f.isDirectory())
            throw new IOException("Path is a directory.");
        ret = removeExtension(path);
        int index = path.lastIndexOf(File.separator);
        if(index != -1)
            ret = path.substring(index + 1, ret.length());
        return ret;
    }

    public static void createPath(String path)
        throws IOException
    {
        File f = new File(path);
        if(!f.exists())
        {
            int index = path.lastIndexOf(File.separator);
            if(index != -1)
            {
                String dirs = path.substring(0, index);
                (new File(dirs)).mkdirs();
            }
        }
    }

    public static File renameTo(File source, String newName)
    {
        if(!source.exists())
        {
            return null;
        } else
        {
            String parentPath = source.getParent() != null ? source.getParent() : "";
            File target = new File(parentPath, newName);
            boolean ret = source.renameTo(target);
            return ret ? target : null;
        }
    }
}
