package hk.hku.cecid.piazza.commons.io;

import hk.hku.cecid.piazza.commons.data.Data;

public class FileSplitterResult
    implements Data
{
    public String originalFilename;
    public long filesize;
    public long filesegment;
    public long segmentSizeInBytes;
    public long lastSegmentSizeInBytes;
    public long processingTimeInMs;

    public FileSplitterResult()
    {
        originalFilename = "";
        filesize = 0L;
        filesegment = 0L;
        segmentSizeInBytes = 0L;
        lastSegmentSizeInBytes = 0L;
        processingTimeInMs = 0L;
    }

    public FileSplitterResult(String originalFilename, long filesize, long filesegment, long segmentSizeInBytes, 
            long lastSegmentSizeInBytes, long processingTimeInMs)
    {
        this.originalFilename = "";
        this.filesize = 0L;
        this.filesegment = 0L;
        this.segmentSizeInBytes = 0L;
        this.lastSegmentSizeInBytes = 0L;
        this.processingTimeInMs = 0L;
        this.originalFilename = originalFilename;
        this.filesize = filesize;
        this.filesegment = filesegment;
        this.segmentSizeInBytes = segmentSizeInBytes;
        this.lastSegmentSizeInBytes = lastSegmentSizeInBytes;
        this.processingTimeInMs = processingTimeInMs;
    }
}
