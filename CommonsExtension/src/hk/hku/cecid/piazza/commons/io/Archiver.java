package hk.hku.cecid.piazza.commons.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface Archiver
{

    public abstract boolean isSupportArchive(File file);

    public abstract boolean compress(File file, File file1, boolean flag)
        throws IOException;

    public abstract boolean compress(FileSystem filesystem, File file)
        throws IOException;

    public abstract long guessCompressedSize(File file)
        throws IOException;

    public abstract long guessCompressedSize(FileSystem filesystem)
        throws IOException;

    public abstract List listAsFile(File file)
        throws IOException;

    public abstract List listAsFilename(File file)
        throws IOException;

    public abstract boolean extract(File file, File file1)
        throws IOException;

    public abstract boolean extract(File file, FileSystem filesystem)
        throws IOException;
}
