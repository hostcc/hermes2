package hk.hku.cecid.piazza.commons.util;

import java.util.*;
import javax.xml.soap.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SOAPUtilities
{

    public SOAPUtilities()
    {
    }

    public static boolean linkElements(SOAPMessage message, String parentTagName, String parentNsURI, String childTagName, String childNsURI)
        throws SOAPException
    {
        if(message == null)
            throw new NullPointerException("Missing SOAPMessage.");
        SOAPBody elementBody = message.getSOAPPart().getEnvelope().getBody();
        if(elementBody == null)
            throw new NullPointerException("Missing SOAPBody in message.");
        Node child = null;
        if(parentNsURI == null)
            parentNsURI = "";
        if(childNsURI == null)
            childNsURI = "";
        Node parent = elementBody.getElementsByTagNameNS(parentNsURI, parentTagName).item(0);
        child = elementBody.getElementsByTagNameNS(childNsURI, childTagName).item(0);
        if(parent == null)
            throw new NullPointerException("Missing parent element: " + parentTagName);
        if(child == null)
        {
            throw new NullPointerException("Missing parent element: " + childTagName);
        } else
        {
            parent.appendChild(child);
            return true;
        }
    }

    public static SOAPElement createElement(String tagName, String tagValue, String nsPrefix, String nsURI)
        throws SOAPException
    {
        SOAPElement soapElement = null;
        if(nsURI != null)
            soapElement = SOAPFactory.newInstance().createElement(tagName, nsPrefix, nsURI);
        else
            soapElement = SOAPFactory.newInstance().createElement(tagName);
        if(tagValue == null)
            tagValue = "";
        soapElement.addTextNode(tagValue);
        return soapElement;
    }

    public static SOAPElement createElement(String tagName, String tagValue, String nsPrefix, String nsURI, Hashtable attrSet)
        throws SOAPException
    {
        SOAPElement newElem = createElement(tagName, tagValue, nsPrefix, nsURI);
        if(newElem == null || attrSet.size() == 0)
            return newElem;
        Object value;
        javax.xml.soap.Name attrName;
        for(Enumeration keys = attrSet.keys(); keys.hasMoreElements(); newElem.addAttribute(attrName, value.toString()))
        {
            Object key = keys.nextElement();
            value = attrSet.get(key);
            attrName = SOAPFactory.newInstance().createName(key.toString(), null, null);
        }

        return newElem;
    }

    public static SOAPElement getElement(SOAPMessage message, String tagname, String nsURI, int whichOne)
        throws SOAPException
    {
        if(message == null)
            return null;
        SOAPBody elementBody = message.getSOAPPart().getEnvelope().getBody();
        if(elementBody == null)
            return null;
        NodeList nl = elementBody.getElementsByTagNameNS(nsURI, tagname);
        if(nl.getLength() <= whichOne)
            return null;
        else
            return (SOAPElement)nl.item(whichOne);
    }

    public static List getElementList(SOAPMessage message, String tagname, String nsURI)
        throws SOAPException
    {
        if(message == null)
            return null;
        SOAPBody elementBody = message.getSOAPPart().getEnvelope().getBody();
        if(elementBody == null)
            return null;
        NodeList nl = elementBody.getElementsByTagNameNS(nsURI, tagname);
        List list = new Vector();
        for(int i = 0; i < nl.getLength(); i++)
            if(nl.item(i) instanceof SOAPElement)
                list.add((SOAPElement)nl.item(i));

        return list;
    }

    public static int countElement(SOAPMessage message, String tagname, String nsURI)
        throws SOAPException
    {
        if(message == null)
            throw new NullPointerException("SOAP Message is null.");
        SOAPBody elementBody = message.getSOAPPart().getEnvelope().getBody();
        if(elementBody == null)
        {
            throw new NullPointerException("SOAP Body is null.");
        } else
        {
            NodeList nl = elementBody.getElementsByTagNameNS(nsURI, tagname);
            return nl.getLength();
        }
    }
}
