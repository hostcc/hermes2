package hk.hku.cecid.piazza.commons.io;

import hk.hku.cecid.piazza.commons.module.Component;
import hk.hku.cecid.piazza.commons.util.StringUtilities;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Properties;

public class FileSplitter extends Component
{
    private int segmentSizeInBytes;
    private String outputPath;
    private FileSplitterResult result;
    private String pattern;
    private String processingPattern;

    public FileSplitter(int segmentSizeInBytes)
    {
        this.segmentSizeInBytes = 50240;
        outputPath = "output";
        result = null;
        pattern = "%f.%i";
        processingPattern = pattern;
        this.segmentSizeInBytes = segmentSizeInBytes;
    }

    protected void init()
        throws Exception
    {
        Properties props = getParameters();
        pattern = props.getProperty("pattern");
        segmentSizeInBytes = StringUtilities.parseInt(props.getProperty("segment-size"));
        outputPath = props.getProperty("output-directory");
    }

    public void setSegmentSizeInBytes(int newSegmentSize)
    {
        segmentSizeInBytes = newSegmentSize;
    }

    public void setOutputPath(String path)
        throws IOException
    {
        PathHelper.createPath(path);
        path = PathHelper.getCanonicalPath("./", path);
        char lastCharacter = path.charAt(path.length() - 1);
        if(lastCharacter != File.separatorChar)
            path = (path + File.separatorChar);
        outputPath = path;
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public void setProcessingPattern(String processingPattern)
    {
        this.processingPattern = processingPattern;
    }

    public int getSegmentSizeInBytes()
    {
        return segmentSizeInBytes;
    }

    public String getOutputPath()
    {
        return outputPath;
    }

    public String getPattern()
    {
        return pattern;
    }

    public String getProcessingPattern()
    {
        return processingPattern;
    }

    public FileSplitterResult getResultInfo()
    {
        return result;
    }

    private String getGeneratedPath(String pattern, File f, int segmentNumber)
    {
        String ret = this.pattern;
        ret = ret.replaceAll("%f", f.getName());
        ret = ret.replaceAll("%i", String.valueOf(segmentNumber));
        return (outputPath + ret);
    }

    public void split(String filepath)
        throws IOException
    {
        split(new File(filepath));
    }

    public void split(File file)
        throws IOException
    {
        result = new FileSplitterResult();
        long startTime = System.currentTimeMillis();
        if(file == null)
            throw new IOException("File is null or missing.");
        if(file.isDirectory())
            throw new IOException("File is direcotry.");
        FileInputStream fis = new FileInputStream(file);
        long fileSize = fis.available();
        long numOfSegment = fileSize / (long)segmentSizeInBytes;
        long lastSegmentSize = fileSize - numOfSegment * (long)segmentSizeInBytes;
        for(int i = 1; (long)i <= numOfSegment + 1L; i++)
        {
            String filepath = getGeneratedPath(processingPattern, file, i);
            FileOutputStream fos = new FileOutputStream(filepath);
            NIOHandler.pipe(fis, fos, (i - 1) * segmentSizeInBytes, segmentSizeInBytes);
            fos.close();
            PathHelper.renameTo(new File(filepath), getGeneratedPath(pattern, file, i));
        }

        fis.close();
        long endTime = System.currentTimeMillis();
        result.originalFilename = file.getName();
        result.filesize = fileSize;
        result.filesegment = numOfSegment + 1L;
        result.segmentSizeInBytes = segmentSizeInBytes;
        result.lastSegmentSizeInBytes = lastSegmentSize;
        result.processingTimeInMs = endTime - startTime;
    }

    public void analyzeFileSplitting(File file)
        throws IOException
    {
        result = new FileSplitterResult();
        if(file == null)
        {
            throw new IOException("File is null or missing.");
        } else
        {
            FileInputStream fis = new FileInputStream(file);
            long fileSize = fis.getChannel().size();
            long numOfSegment = fileSize / (long)segmentSizeInBytes;
            long lastSegmentSize = fileSize - numOfSegment * (long)segmentSizeInBytes;
            fis.close();
            result.originalFilename = file.getName();
            result.filesize = fileSize;
            result.filesegment = numOfSegment + 1L;
            result.segmentSizeInBytes = segmentSizeInBytes;
            result.lastSegmentSizeInBytes = lastSegmentSize;
            return;
        }
    }
}
