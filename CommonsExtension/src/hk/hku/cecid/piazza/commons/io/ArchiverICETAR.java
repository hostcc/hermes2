package hk.hku.cecid.piazza.commons.io;

import com.ice.tar.*;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.*;

public class ArchiverICETAR extends ArchiverNULL
{
    public static int TAR_ENTRY_SIZE = 500;

    public ArchiverICETAR()
    {
    }

    public boolean isSupportArchive(File archive)
    {
        return PathHelper.getExtension(archive.getAbsolutePath()).equalsIgnoreCase("TAR");
    }

    public boolean compress(File src, File dest, boolean includeItself)
        throws IOException
    {
        super.compress(src, dest, includeItself);
        FileOutputStream fos = new FileOutputStream(dest);
        TarOutputStream outs = new TarOutputStream(fos);
        java.nio.channels.WritableByteChannel tarChannel = Channels.newChannel(outs);
        Iterator allFiles = listFilesToArchive(src);
        String dirpath = getBaseArchivingDirectory(src, includeItself);
        while(allFiles.hasNext()) 
        {
            File srcFile = (File)allFiles.next();
            String filepath = srcFile.getAbsolutePath();
            String entryName = filepath.substring(dirpath.length() + 1).replace('\\', '/');
            TarEntry tarEntry = new TarEntry(srcFile);
            tarEntry.setModTime(srcFile.lastModified());
            tarEntry.getHeader().name = new StringBuffer(entryName);
            outs.putNextEntry(tarEntry);
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
                fc.transferTo(sPos, aSize, tarChannel);
                sPos += aSize;
            } while(tSize > 0x7fffffffL);
            outs.closeEntry();
            fc.close();
            fc = null;
        }
        outs.close();
        outs = null;
        fos.close();
        fos = null;
        return true;
    }

    public boolean compress(FileSystem src, File dest)
        throws IOException
    {
        return compress(src.getRoot(), dest, true);
    }

    public long guessCompressedSize(File src)
        throws IOException
    {
        Iterator allFiles = listFilesToArchive(src);
        long size = 0L;
        RandomAccessFile raf;
        for(; allFiles.hasNext(); raf.close())
        {
            raf = new RandomAccessFile((File)allFiles.next(), "r");
            size += raf.length() + (long)TAR_ENTRY_SIZE;
        }

        return (long)Math.ceil((double)size / 10240D) * 10240L;
    }

    public long guessCompressedSize(FileSystem src)
        throws IOException
    {
        return guessCompressedSize(src.getRoot());
    }

    public List listAsFile(File archive)
        throws IOException
    {
        TarInputStream tis = new TarInputStream(new FileInputStream(archive));
        TarEntryEnumerator tee = getTarEnumerator(tis);
        ArrayList fileEntries = new ArrayList();
        for(; tee.hasMoreElements(); fileEntries.add(new File(((TarEntry)tee.nextElement()).getName())));
        tis.close();
        tee = null;
        return fileEntries;
    }

    public List listAsFilename(File archive)
        throws IOException
    {
        TarInputStream tis = new TarInputStream(new FileInputStream(archive));
        TarEntryEnumerator tee = getTarEnumerator(tis);
        ArrayList fileEntries = new ArrayList();
        for(; tee.hasMoreElements(); fileEntries.add(((TarEntry)tee.nextElement()).getName()));
        tis.close();
        tee = null;
        return fileEntries;
    }

    public boolean extract(File archive, File dest)
        throws IOException
    {
        super.extract(archive, dest);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(archive));
        TarInputStream tis = new TarInputStream(bis);
        int count = 0;
        do
        {
            TarEntry entry = tis.getNextEntry();
            if(entry == null)
                break;
            String name = entry.getName();
            name = name.replace('/', File.separatorChar);
            File destFile = new File(dest, name);
            if(entry.isDirectory())
            {
                if(!destFile.exists() && !destFile.mkdirs())
                    throw new IOException("Error making directory path :" + destFile.getPath());
            } else
            {
                File subDir = new File(destFile.getParent());
                if(!subDir.exists() && !subDir.mkdirs())
                    throw new IOException("Error making directory path :" + subDir.getPath());
                FileOutputStream out = new FileOutputStream(destFile);
                byte rdbuf[] = new byte[32768];
                do
                {
                    int numRead = tis.read(rdbuf);
                    if(numRead == -1)
                        break;
                    out.write(rdbuf, 0, numRead);
                } while(true);
                out.close();
            }
            count++;
        } while(true);
        tis.close();
        tis = null;
        bis.close();
        bis = null;
        if(count == 0)
            throw new IOException("At least one file should be a TAR.");
        else
            return true;
    }

    public boolean extract(File archive, FileSystem dest)
        throws IOException
    {
        return extract(archive, dest.getRoot());
    }

    private TarEntryEnumerator getTarEnumerator(TarInputStream tis)
        throws IOException
    {
        if(tis == null)
            throw new NullPointerException("Tar Input Stream is empty.");
        else
            return new TarEntryEnumerator(tis);
    }
}
