package hk.hku.cecid.piazza.commons.net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SimpleHttpMonitor
    implements Runnable
{
    private InputStream fullHTTPInStream;
    private InputStream contentHTTPInStream;
    private ByteArrayOutputStream fullHTTPOutStream;
    private Map headers;
    private int contentLength;
    private int contentStartOffset;
    private String contentType;
    private int responseContentLength;
    private String responseContentType;
    private int port;
    private Thread serverThread;
    private ServerSocket sSocket;
    private volatile boolean stopped;
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    protected static final byte STATUS_200[] = "HTTP/1.1 200 OK ".getBytes();
    protected static final byte HD_SERVIER[] = "Server: WSC_HTTP_monitor".getBytes();
    protected static final byte HD_CT_LEN[] = "Content-Length: ".getBytes();
    protected static final byte HD_CT_TYPE[] = "Content-type: ".getBytes();
    protected static final byte CRLF[] = "\r\n".getBytes();

    public SimpleHttpMonitor(int port)
    {
        fullHTTPOutStream = new ByteArrayOutputStream();
        headers = new HashMap();
        contentLength = -1;
        contentStartOffset = -1;
        responseContentLength = 0;
        responseContentType = "text/plain";
        serverThread = new Thread(this);
        stopped = true;
        this.port = port;
        try
        {
            sSocket = new ServerSocket(this.port);
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
        }
    }

    public synchronized void start()
    {
        if(stopped)
        {
            serverThread.start();
            stopped = false;
        }
    }

    public synchronized void stop()
    {
        if(serverThread.isAlive() && !serverThread.isInterrupted())
        {
            stopped = true;
            serverThread.interrupt();
            try
            {
                if(!sSocket.isClosed())
                    sSocket.close();
            }
            catch(IOException ioex)
            {
                ioex.printStackTrace();
            }
        }
        serverThread = new Thread(this);
    }

    protected void onAccept(Socket socket)
    {
    }

    protected void onRequest(InputStream ins)
        throws IOException
    {
        parseHttpHeader(ins, fullHTTPOutStream);
        int len = getContentLength();
        contentStartOffset = fullHTTPOutStream.size();
        parseHttpBody(ins, fullHTTPOutStream, len);
        byte request[] = fullHTTPOutStream.toByteArray();
        if(contentStartOffset != -1)
            contentHTTPInStream = new ByteArrayInputStream(request, contentStartOffset, request.length);
        fullHTTPInStream = new ByteArrayInputStream(request);
        fullHTTPOutStream.close();
    }

    protected int onResponseLength()
    {
        return 0;
    }

    protected String onResponseContentType()
    {
        return "text/plain";
    }

    protected void onResponse(OutputStream os)
        throws IOException
    {
        os.write(STATUS_200);
        os.write(CRLF);
        os.write(HD_SERVIER);
        os.write(CRLF);
        os.write(HD_CT_LEN);
        os.write(String.valueOf(responseContentLength).getBytes());
        os.write(CRLF);
        os.write(HD_CT_TYPE);
        os.write(String.valueOf(responseContentType).getBytes());
        os.write(CRLF);
        os.write(CRLF);
    }

    public void run()
    {
        try
        {
            while (!(this.stopped))
            {
                Socket s = null;
                try
                {
                    s = this.sSocket.accept();
                    onAccept(s);
                    InputStream bsis = new BufferedInputStream(s.getInputStream());
                    onRequest(bsis);
                    this.responseContentLength = onResponseLength();
                    this.responseContentType = onResponseContentType();
                    OutputStream bsos = new BufferedOutputStream(s.getOutputStream());
                    onResponse(bsos);

                    bsos.flush();
                    bsos.close();
                }
                catch (IOException ioex)
                {
                    if (!(this.stopped))
                    {
                        ioex.printStackTrace();
                    }
                }
                finally
                {
                    if ((s != null) && (s.isClosed()))
                    {
                        s.close();
                    }
                }
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        finally
        {
            try
            {
                if ((this.sSocket != null) && (!(this.sSocket.isClosed())))
                {
                    this.sSocket.close();
                }
            }
            catch (IOException ioex)
            {
                ioex.printStackTrace();
            }
        }
    }

    public synchronized int getContentLength()
    {
        if (this.contentLength == -1)
        {
            String contentLen = (String)this.headers.get("Content-Length");
            if (contentLen != null)
            {
                try
                {
                    this.contentLength = Integer.parseInt(contentLen);
                    return this.contentLength;
                }
                catch (NumberFormatException nfe) {
                }
            }
        }
        return this.contentLength;
    }

    public String getContentType()
    {
        if(contentType == null)
            contentType = (String)headers.get("Content-Type");
        return contentType;
    }

    public Map getHeaders()
    {
        return headers;
    }

    public InputStream getInputStream()
    {
        return fullHTTPInStream;
    }

    public InputStream getContentStream()
    {
        return contentHTTPInStream;
    }

    private void resetData()
    {
        fullHTTPOutStream.reset();
        headers.clear();
        contentLength = -1;
        contentStartOffset = -1;
        contentType = null;
    }

    private void parseHttpHeader(InputStream sins, OutputStream capouts)
        throws IOException
    {
        if(sins == null)
            throw new NullPointerException("Missing 'SocketInputStream' for parsing Http line.");
        if(capouts == null)
            throw new NullPointerException("Missing 'CapturedOutputStream' for captugin Http line.");
        PushbackInputStream pbis = new PushbackInputStream(sins);
        char pc = '\uFFFF';
        do
        {
            char c;
            if((c = (char)pbis.read()) == '\uFFFF')
                break;
            if(c != '\r')
            {
                pbis.unread(c);
                parseHttpLine(pbis, capouts);
                continue;
            }
            pc = c;
            c = (char)pbis.read();
            if(c != '\n' || pc != '\r')
                continue;
            capouts.write(pc);
            capouts.write(c);
            break;
        } while(true);
    }

    private void parseHttpLine(InputStream sins, OutputStream capouts)
        throws IOException
    {
        char pc = '\uFFFF';
        boolean colonized = false;
        String name = null;
        String value = null;
        int len = 128;
        int count = 0;
        char cs[] = new char[len];
        do
        {
            char c;
            if((c = (char)sins.read()) == '\uFFFF')
                break;
            capouts.write(c);
            if(c == '\n' && pc == '\r')
            {
                if(colonized)
                {
                    value = (new String(cs, 1, count - 1)).trim();
                    headers.put(name, value);
                }
                break;
            }
            if(c == ':' && !colonized)
            {
                name = (new String(cs, 0, count)).trim();
                colonized = true;
                count = 0;
            }
            pc = c;
            cs[count++] = c;
            if(count == len)
            {
                char ncs[] = new char[len * 2];
                System.arraycopy(cs, 0, ncs, 0, count);
                cs = ncs;
            }
        } while(true);
    }

    private void parseHttpBody(InputStream sins, OutputStream capouts, int contentLength)
        throws IOException
    {
        for(int read = 0; read++ < contentLength;)
        {
            char c = (char)sins.read();
            capouts.write(c);
        }

    }
}
