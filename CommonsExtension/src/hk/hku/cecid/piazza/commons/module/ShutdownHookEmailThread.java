package hk.hku.cecid.piazza.commons.module;

import hk.hku.cecid.piazza.commons.net.ConnectionException;
import hk.hku.cecid.piazza.commons.net.MailSender;
import hk.hku.cecid.piazza.commons.util.DiagnosticUtilities;
import java.io.IOException;
import java.io.StringWriter;

public class ShutdownHookEmailThread extends Thread
{
    public static final String DEFAULT_SHUTDOWN_MAIL_SUBJECT = "JVM has been shutdown.";
    protected String from;
    protected String tos;
    protected String ccs;
    protected String subject;
    protected String protocol;
    protected String host;
    protected String username;
    protected String password;
    protected boolean verbose;

    public ShutdownHookEmailThread(String protocol, String host, String username, String password, String from, String tos, String ccs, 
            String subject, boolean verbose)
    {
        this.protocol = protocol;
        this.host = host;
        this.username = username;
        this.password = password;
        this.from = from;
        this.tos = tos;
        this.ccs = ccs;
        this.subject = subject;
        this.verbose = verbose;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public String getHost()
    {
        return host;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getFrom()
    {
        return from;
    }

    public String getTos()
    {
        return tos;
    }

    public String getCcs()
    {
        return ccs;
    }

    public String getSubject()
    {
        return subject;
    }

    public boolean getIsVerbose()
    {
        return verbose;
    }

    protected String onCreateMailNotificationSubject()
    {
        return subject;
    }

    protected String onCreateMailNotificationBody()
    {
        StringWriter sw = new StringWriter();
        try
        {
            DiagnosticUtilities.getNewInstance().dumpAllThread(sw, 3);
        }
        catch(IOException ioex)
        {
            String error = "Unable to generate thread dump due to : ";
            sw.append(error).append(ioex.toString());
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return sw.getBuffer().toString();
    }

    public void run()
    {
        try
        {
            String subject = onCreateMailNotificationSubject();
            String body = onCreateMailNotificationBody();
            MailSender mailSender = new MailSender(protocol, host, username, password);
            if(verbose)
                mailSender.setDebug(true);
            mailSender.send(from, tos, ccs, subject, body);
        }
        catch(ConnectionException ctex)
        {
            ctex.printStackTrace();
        }
    }
}
