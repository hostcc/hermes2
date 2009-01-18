package hk.hku.cecid.piazza.commons.net;

import hk.hku.cecid.piazza.commons.io.IOHandler;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public class FastHttpConnector extends HttpConnector
{

    public FastHttpConnector(Object destUrl)
        throws MalformedURLException
    {
        super(destUrl);
    }

    public InputStream send(InputStream request, HttpURLConnection connection)
        throws hk.hku.cecid.piazza.commons.net.ConnectionException
    {
        OutputStream outstream = null;
        try
        {
          if (request != null)
          {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            outstream = connection.getOutputStream();
            IOHandler.pipe(request, outstream);
          } else
          {
            connection.setRequestMethod("GET");
          }
          connection.connect();
          BufferedInputStream localBufferedInputStream = new BufferedInputStream(connection.getInputStream());

          return localBufferedInputStream;
        }
        catch (Exception e)
        {
        }
        finally
        {
          try
          {
            if (outstream != null)
              outstream.close();
          }
          catch (Exception e)
          {
          }
        }

        return null;
    }
}
