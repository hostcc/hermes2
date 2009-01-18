package hk.hku.cecid.piazza.commons.io;

import hk.hku.cecid.piazza.commons.util.StringUtilities;
import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileJoiner
{
    private String lookupDirectory;
    private String outputPath;
    private String processingOutputPath;
    private boolean deleteSegmentUponJoin;
    private String pattern;
    private Pattern regularPattern;

    public FileJoiner()
    {
        deleteSegmentUponJoin = false;
        pattern = "%f.%i";
        regularPattern = Pattern.compile(pattern);
    }

    public void setLookupDirectory(String path)
    {
        lookupDirectory = path;
    }

    public void setDeleteSegmentUponJoin(boolean delete)
    {
        deleteSegmentUponJoin = delete;
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
        compilePattern(pattern);
    }

    public void setOutputPath(String outputPath)
    {
        this.outputPath = outputPath;
    }

    public void setProcessingOutputPath(String processingOutputPath)
    {
        this.processingOutputPath = processingOutputPath;
    }

    public String getLookupDirectory(String path)
    {
        return lookupDirectory;
    }

    public String getPattern()
    {
        return pattern;
    }

    public String getRegularPattern()
    {
        return regularPattern.pattern();
    }

    public String getOutputDirecotry()
    {
        return outputPath;
    }

    public String getProcessingOutputPath()
    {
        return processingOutputPath;
    }

    private int getSegmentNo(String filename)
    {
        String actualPattern = pattern;
        int index = -1;
        int capturingGroup = 0;
        index = actualPattern.indexOf("%f");
        capturingGroup = index == -1 ? 1 : 3;
        Matcher matcher = regularPattern.matcher(filename);
        if(matcher.find())
            return StringUtilities.parseInt(matcher.group(capturingGroup), 0x80000000);
        else
            return 0x80000000;
    }

    public boolean isDeleteSegmentUponJoin()
    {
        return deleteSegmentUponJoin;
    }

    protected void compilePattern(String pattern)
    {
        if(pattern == null)
            return;
        String escapePatternStr = "([\\D&&\\W&&[^\\%]&&[^\\.]&&[^\\@]&&[^\\-]])";
        Pattern escapePattern = Pattern.compile(escapePatternStr);
        Matcher matcher = escapePattern.matcher(pattern);
        StringBuffer sb = new StringBuffer();
        for(; matcher.find(); matcher.appendReplacement(sb, "\\\\\\" + matcher.group(1)));
        matcher.appendTail(sb);
        String actualPattern = sb.toString();
        actualPattern = actualPattern.replaceAll("%f", "([a-zA-z_0-9]*)");
        actualPattern = actualPattern.replaceAll("%i", "(([0-9]*))");
        regularPattern = Pattern.compile(actualPattern);
    }

    public void join()
        throws IOException
    {
        join(new File(lookupDirectory));
    }

    public void join(File directory)
        throws IOException
    {
        long startTime = System.currentTimeMillis();
        String userDir = System.getProperty("user.dir");
        System.out.println("Pattern                    " + getPattern());
        System.out.println("Regular Pattern            " + regularPattern.pattern());
        System.out.println("File directory to be found " + directory.getCanonicalPath());
        System.out.println("Delete segment upon join   " + deleteSegmentUponJoin);

   		final FileJoiner ownReference = this;

    	File[] files = directory.listFiles(new FilenameFilter()
    	{
      		public boolean accept(File file, String name)
      		{
        		if (name != null) {
		        	System.out.println(name);
		        	Matcher m = Pattern.compile(ownReference.getRegularPattern()).matcher(name);
          			return m.find();
        		}
        		return false;
      		}
		});

	    long segmentCount = files.length;
    	Arrays.sort(files, new Comparator()
    	{
      		public int compare(Object o1, Object o2)
      		{
        		if ((o1 instanceof File) && (o2 instanceof File)) {
          			File[] files = { (File)o1, (File)o2 };
          			int[] ext = new int[2];
          			for (int i = 0; i < 2; ++i) {
            			ext[i] = ownReference.getSegmentNo(files[i].getName());
            			System.out.println("Segment Number:" + ext[i]);
          			}
          			if (ext[0] > ext[1]) return 1;
          			if (ext[0] == ext[1]) return 0;
          			if (ext[0] < ext[1]) return -1;
        		}
        		return -1;
      		}
    	});

        String outputFilePath = PathHelper.getCanonicalPath(userDir, processingOutputPath);
        File outputFile = new File(outputFilePath);
        FileOutputStream fos = new FileOutputStream(outputFile);
        for(int i = 0; (long)i < segmentCount; i++)
        {
            System.out.println("File " + i + " " + files[i].getName());
            FileInputStream fis = new FileInputStream(files[i]);
            NIOHandler.pipe(fis, fos);
            fis.close();
        }

        fos.close();
        fos = null;
        String actualOutputPath = PathHelper.getCanonicalPath(userDir, outputPath);
        System.out.println("Ori    Path " + outputFile.getName());
        System.out.println("Actual Path " + actualOutputPath);
        if(!outputFile.renameTo(new File(actualOutputPath)))
            throw new IOException("Can not rename the joined file" + outputFile.getName());
        if(deleteSegmentUponJoin)
        {
            for(int i = 0; (long)i < segmentCount; i++)
                files[i].delete();

        }
        long endTime = System.currentTimeMillis();
        System.out.println("The joining processing takes " + (double)(endTime - startTime) / 1000D + "(s).");
    }
}
