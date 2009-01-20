package hk.hku.cecid.piazza.commons.soap;

import hk.hku.cecid.piazza.commons.data.Data;
import hk.hku.cecid.piazza.commons.module.Component;
import hk.hku.cecid.piazza.commons.util.*;
import java.net.*;
import java.util.List;
import javax.xml.soap.*;

public abstract class SOAPSender extends Component
    implements Runnable
{
    protected static final String NS_PREFIX = "tns";
    protected Logger log;
    protected Data properties;
    protected SOAPMessage request;
    protected SOAPMessage response;
    protected URL serviceEndPoint;
    private boolean isRequireXMLDecl;
    private boolean isRequestDirty;
    private int loopTimes;
    private int curTimes;
    private Object userObj;

    private class SOAPAuthenticator extends Authenticator
    {

        protected PasswordAuthentication getPasswordAuthentication()
        {
            return pwAuth;
        }

        private PasswordAuthentication pwAuth;

        protected SOAPAuthenticator(String username, String password)
        {
            super();
            pwAuth = new PasswordAuthentication(username, password.toCharArray());
        }
    }


    public SOAPSender()
    {
        request = null;
        response = null;
        serviceEndPoint = null;
        isRequireXMLDecl = false;
        isRequestDirty = true;
        loopTimes = 1;
        curTimes = 0;
        userObj = null;
    }

    public SOAPSender(Logger l, Data d)
    {
        request = null;
        response = null;
        serviceEndPoint = null;
        isRequireXMLDecl = false;
        isRequestDirty = true;
        loopTimes = 1;
        curTimes = 0;
        userObj = null;
        log = l;
        properties = d;
        try
        {
            request = MessageFactory.newInstance().createMessage();
        }
        catch(SOAPException se)
        {
            onError(se);
        }
    }

    public SOAPSender(Logger l, Data d, String endpoint)
    {
        this(l, d);
        try
        {
            serviceEndPoint = new URL(endpoint);
        }
        catch(MalformedURLException mue)
        {
            onError(mue);
        }
    }

    public SOAPSender(Logger l, Data d, URL endpoint)
    {
        this(l, d);
        serviceEndPoint = endpoint;
    }

    public void initializeMessage()
        throws Exception
    {
    }

    public void onStart()
    {
        curTimes = 0;
    }

    public void onEachLoopStart()
        throws Exception
    {
    }

    public SOAPMessage onCreateRequest()
        throws Exception
    {
        if(isRequestDirty())
        {
            resetSOAPRequest();
            initializeMessage();
        }
        return request;
    }

    public void onBeforeRequest(SOAPConnection soapconnection, SOAPMessage soapmessage)
        throws Exception
    {
    }

    public void onResponse()
        throws Exception
    {
    }

    public void onEnd()
    {
    }

    public void onError(Throwable t)
    {
        t.printStackTrace();
        if(log != null)
            log.logStackTrace(t);
    }

    public void setLoopTimes(int loopTimes)
    {
        if(loopTimes > 0)
            this.loopTimes = loopTimes;
    }

    public void setUserObject(Object obj)
    {
        userObj = obj;
    }

    public void setServiceEndPoint(URL endpoint)
    {
        if(endpoint != null)
            serviceEndPoint = endpoint;
    }

    public void setServiceEndPoint(String endpoint)
    {
        try
        {
            serviceEndPoint = new URL(endpoint);
        }
        catch(MalformedURLException mue)
        {
            if(log != null)
                log.logStackTrace(mue);
        }
    }

    public void setRequestDirty(boolean dirty)
    {
        isRequestDirty = dirty;
    }

    public void setRequireXMLDeclaraction(boolean require)
    {
        isRequireXMLDecl = require;
        isRequestDirty = true;
    }

    public void setBasicAuthentication(String username, String password)
    {
        Authenticator.setDefault(new SOAPAuthenticator(username, password));
    }

    public int getLoopTimes()
    {
        return loopTimes;
    }

    public int getCurrentLoopTimes()
    {
        return curTimes;
    }

    public Object getUserObject()
    {
        return userObj;
    }

    public URL getServiceEndPoint()
    {
        return serviceEndPoint;
    }

    public boolean isRequireXMLDeclaraction()
    {
        return isRequireXMLDecl;
    }

    public boolean isRequestDirty()
    {
        return isRequestDirty;
    }

    protected SOAPMessage getSOAPRequest()
    {
        return request;
    }

    protected void resetSOAPRequest()
        throws SOAPException
    {
        request = MessageFactory.newInstance().createMessage();
    }

    protected SOAPMessage getSOAPResponse()
    {
        return response;
    }

    protected void resetSOAPResponse()
        throws SOAPException
    {
        response = MessageFactory.newInstance().createMessage();
    }

    public boolean addRequestElementText(String tagName, String tagValue, String nsPrefix, String nsURI)
        throws SOAPException
    {
        if(request == null)
            return false;
        SOAPBody soapBody = request.getSOAPPart().getEnvelope().getBody();
        if(soapBody == null)
        {
            return false;
        } else
        {
            SOAPElement newElement = SOAPUtilities.createElement(tagName, tagValue, nsPrefix, nsURI);
            soapBody.addChildElement(newElement);
            return true;
        }
    }

    public boolean addRequestElementText(String parentTagName, String parentNsURI, String tagName, String tagValue, String nsPrefix, String nsURI)
        throws SOAPException
    {
        return addRequestElementText(tagName, tagValue, nsPrefix, nsURI) && SOAPUtilities.linkElements(request, parentTagName, parentNsURI, tagName, nsURI);
    }

    public String getRequestElementText(String tagname, String nsURI, int whichOne)
        throws SOAPException
    {
        SOAPElement elem = SOAPUtilities.getElement(request, tagname, nsURI, whichOne);
        if(elem != null)
            return elem.getValue();
        else
            return "";
    }

    public String getResponseElementText(String tagname, String nsURI, int whichOne)
        throws SOAPException
    {
        SOAPElement elem = SOAPUtilities.getElement(response, tagname, nsURI, whichOne);
        if(elem != null)
            return elem.getValue();
        else
            return "";
    }

    public int countResponseElementText(String tagname, String nsURI)
        throws SOAPException
    {
        return SOAPUtilities.countElement(response, tagname, nsURI);
    }

    public PropertyTree transformResponseContent()
        throws UtilitiesException, SOAPException
    {
        SOAPBody soapBody;
        soapBody = response.getSOAPBody();
        if(soapBody == null)
            return null;
		try {
        	return new PropertyTree(soapBody);
		}
		catch (Exception e)
		{
        	throw new UtilitiesException("Unable to transform soapBody due to", e);
		}
    }

    public String[] getResponseElementAsList(String tagname, String nsURI)
        throws SOAPException
    {
        List list = SOAPUtilities.getElementList(response, tagname, nsURI);
        String props[] = new String[list.size()];
        for(int i = 0; i < list.size(); i++)
            if(list.get(i) != null)
                props[i] = ((SOAPElement)list.get(i)).getValue();

        return props;
    }

    public void run()
    {
        SOAPMessage request = null;
        SOAPConnectionFactory factory = null;
        onStart();
        try
        {
            for(int i = 0; i < getLoopTimes(); i++)
            {
                curTimes = i;
                onEachLoopStart();
                request = onCreateRequest();
                factory = SOAPConnectionFactory.newInstance();
                SOAPConnection soapConn = factory.createConnection();
                onBeforeRequest(soapConn, request);
                if(isRequestDirty())
                {
                    if(isRequireXMLDecl)
                        this.request.setProperty("javax.xml.soap.write-xml-declaration", "true");
                    this.request.saveChanges();
                    setRequestDirty(false);
                }
                response = soapConn.call(request, serviceEndPoint);
                SOAPBody sb = response.getSOAPBody();
                if(sb.hasFault())
                    throw new SOAPException(sb.getFault().getFaultCode() + " " + sb.getFault().getFaultString());
                onResponse();
            }

            onEnd();
            resetSOAPRequest();
            resetSOAPResponse();
        }
        catch(Exception e)
        {
            onError(e);
        }
    }
}
