package hk.hku.cecid.piazza.commons.io;

import java.io.*;
import java.nio.channels.*;
import java.util.*;
import java.util.zip.*;

public class ArchiverZIP extends ArchiverNULL
{

    public ArchiverZIP()
    {
    }

    public boolean isSupportArchive(File archive)
    {
	    try
   	 	{
      		return (new ZipFile(archive) != null);
		}
		catch (Exception e)
		{
		    return false;
    	}
    }

    public boolean compress(File src, File dest, boolean includeItself)
        throws IOException
    {
        super.compress(src, dest, includeItself);
        FileOutputStream fos = new FileOutputStream(dest);
        ZipOutputStream outs = new ZipOutputStream(fos);
        WritableByteChannel zipChannel = Channels.newChannel(outs);
        Iterator allFiles = listFilesToArchive(src);
        String dirpath = getBaseArchivingDirectory(src, includeItself);
        for(; allFiles.hasNext(); outs.closeEntry())
        {
            File srcFile = (File)allFiles.next();
            String filepath = srcFile.getAbsolutePath();
            String entryName = filepath.substring(dirpath.length() + 1).replace('\\', '/');
            ZipEntry zipEntry = new ZipEntry(entryName);
            zipEntry.setTime(srcFile.lastModified());
            outs.putNextEntry(zipEntry);
            FileChannel fc = (new FileInputStream(srcFile)).getChannel();
            long size = fc.size();
            long tSize = size;
            long sPos = 0L;
            do
            {
                long aSize;
                tSize = aSize = size - sPos;
                if(tSize > 0x7fffffffL)
                    aSize = 0x7ffffffeL;
                fc.transferTo(sPos, aSize, zipChannel);
                sPos += aSize;
            } while(tSize > 0x7fffffffL);
            fc.close();
            fc = null;
        }

        zipChannel.close();
        zipChannel = null;
        outs.close();
        outs = null;
        fos.close();
        fos = null;
        return true;
    }

    public long guessCompressedSize(File src)
        throws IOException
    {
        throw new IOException("Unsupported Operations.");
    }

    public long guessCompressedSize(FileSystem src)
        throws IOException
    {
        throw new IOException("Unsupported Operations.");
    }

    public boolean compress(FileSystem src, File dest)
        throws IOException
    {
        return compress(src.getRoot(), dest, true);
    }

    public List listAsFile(File archive)
        throws IOException
    {
        if(archive == null)
            throw new NullPointerException("Archive file is empty.");
        ZipFile zipFile = new ZipFile(archive);
        Enumeration zipEntries = zipFile.entries();
        ArrayList fileEntries = new ArrayList();
        for(; zipEntries.hasMoreElements(); fileEntries.add(new File(((ZipEntry)zipEntries.nextElement()).getName())));
        zipFile.close();
        zipFile = null;
        return fileEntries;
    }

    public List listAsFilename(File archive)
        throws IOException
    {
        if(archive == null)
            throw new NullPointerException("Archive file is empty.");
        ZipFile zipFile = new ZipFile(archive);
        Enumeration zipEntries = zipFile.entries();
        ArrayList fileEntries = new ArrayList();
        for(; zipEntries.hasMoreElements(); fileEntries.add(((ZipEntry)zipEntries.nextElement()).getName()));
        zipFile.close();
        zipFile = null;
        return fileEntries;
    }

    public boolean extract(File archive, File dest)
        throws IOException
    {
        super.extract(archive, dest);
        ZipFile zipFile = new ZipFile(archive);
        for(Enumeration zipEntries = zipFile.entries(); zipEntries.hasMoreElements();)
        {
            ZipEntry zipEntry = (ZipEntry)zipEntries.nextElement();
            if(zipEntry.isDirectory())
            {
                (new File(dest, zipEntry.getName())).mkdirs();
            } else
            {
                File destFile = new File(dest, zipEntry.getName());
                destFile.setLastModified(zipEntry.getTime());
                destFile.getParentFile().mkdirs();
                FileOutputStream outs = new FileOutputStream(destFile);
                InputStream ins = zipFile.getInputStream(zipEntry);
                NIOHandler.pipe(ins, outs);
                ins.close();
                outs.close();
                outs = null;
                ins = null;
            }
        }

        zipFile.close();
        zipFile = null;
        return true;
    }

    public boolean extract(File archive, FileSystem dest)
        throws IOException
    {
        return extract(archive, dest.getRoot());
    }
}
