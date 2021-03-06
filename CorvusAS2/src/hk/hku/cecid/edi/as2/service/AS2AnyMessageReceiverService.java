/* 
 * Copyright(c) 2009 Asstra Associated Traffic AG
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.edi.as2.service;

import hk.hku.cecid.edi.as2.AS2Processor;
import hk.hku.cecid.edi.as2.dao.MessageDAO;
import hk.hku.cecid.edi.as2.dao.MessageDVO;
import hk.hku.cecid.edi.as2.module.PayloadCache;
import hk.hku.cecid.edi.as2.module.PayloadRepository;
import hk.hku.cecid.piazza.commons.activation.ByteArrayDataSource;
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.io.IOHandler;
import hk.hku.cecid.piazza.commons.soap.SOAPFaultException;
import hk.hku.cecid.piazza.commons.soap.SOAPRequestException;
import hk.hku.cecid.piazza.commons.soap.SOAPResponse;
import hk.hku.cecid.piazza.commons.soap.WebServicesAdaptor;
import hk.hku.cecid.piazza.commons.soap.WebServicesRequest;
import hk.hku.cecid.piazza.commons.soap.WebServicesResponse;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Element;

/**
 * AS2AnyMessageReceiverListService
 * 
 * @author Ilia Sotnikov
 *  
 */
public class AS2AnyMessageReceiverService extends WebServicesAdaptor
{
    public void serviceRequested (WebServicesRequest request,
                                  WebServicesResponse response)
        throws SOAPRequestException, DAOException
    {

        Element[] bodies = request.getBodies ();
        String messageId = getText (bodies, "messageId");

        if (messageId == null)
            throw new SOAPFaultException (SOAPFaultException.SOAP_FAULT_CLIENT,
                                          "Missing request information");

        AS2Processor.core.log.info ("Any Message Receiver received " +
                                    "download request - Message ID: " + messageId);

        SOAPResponse soapResponse = (SOAPResponse) response.getTarget ();
        SOAPMessage soapResponseMessage = soapResponse.getMessage ();

        MessageDAO messageDao =
            (MessageDAO) AS2Processor.core.dao.createDAO (MessageDAO.class);
        MessageDVO messageDvo = (MessageDVO) messageDao.createDVO ();
        messageDvo.setMessageId (messageId);
        messageDvo.setMessageBox ("%");
        messageDvo.setAs2From ("%");
        messageDvo.setAs2To ("%");
        messageDvo.setPrincipalId ("%");
        messageDvo.setStatus ("%");

        List messagesList =
            messageDao.findMessagesByHistory (messageDvo, 1, 0);
        Iterator messagesIterator = messagesList.iterator ();

        while (messagesIterator.hasNext ())
        {
            MessageDVO targetMessageDvo =
                (MessageDVO) messagesIterator.next ();

            /* Ignore messages not in PROCESSED or DELIVERED status */
            String msgStatus = targetMessageDvo.getStatus ();
            if (   !msgStatus.equalsIgnoreCase (MessageDVO.STATUS_DELIVERED)
                && !msgStatus.equalsIgnoreCase (MessageDVO.STATUS_PROCESSED))
                continue;

            /* Get message box for the message */
            String msgBox = targetMessageDvo.getMessageBox ();

            /* Get appropriate message respository */
            PayloadRepository repository = null;
            if(msgBox.equalsIgnoreCase (MessageDVO.MSGBOX_IN))
                repository = AS2Processor
                       .getIncomingPayloadRepository ();
            if(msgBox.equalsIgnoreCase (MessageDVO.MSGBOX_OUT))
                repository = AS2Processor
                       .getOutgoingPayloadRepository ();
            if (repository == null)
                throw new DAOException ("Payload repository not found " +
                                        "for message box " + msgBox);

            Iterator payloadCachesIterator = repository.getPayloadCaches()
                    .iterator ();
            while (payloadCachesIterator.hasNext ())
            {
                PayloadCache cache = (PayloadCache) payloadCachesIterator
                        .next ();
                String cacheMessageID = cache.getMessageID ();
                if (cacheMessageID.equals (targetMessageDvo.getMessageId ()))
                {
                    try
                    {
                        FileInputStream fis =
                            new FileInputStream (cache.getCache ());

                        DataSource ds = null;
                        ByteArrayOutputStream baos =
                            new ByteArrayOutputStream ();

                        if (getParameters ().getProperty ("is_compress").equals ("true"))
                        {
                            DeflaterOutputStream dos =
                                new DeflaterOutputStream (baos);

                            IOHandler.pipe (fis, dos);
                            dos.finish ();
                            ds = new ByteArrayDataSource (baos.toByteArray (),
                                                          "application/deflate");
                        } else 
                        {
                            IOHandler.pipe (fis, baos);
                            ds = new ByteArrayDataSource (baos.toByteArray(),
                                                          cache.getContentType ());
                        }
                        DataHandler dh = new DataHandler (ds);

                        AttachmentPart attachmentPart
                            = soapResponseMessage.createAttachmentPart ();
                        attachmentPart.setContentId (messageId);
                        attachmentPart.setDataHandler (dh);
                        soapResponseMessage.addAttachmentPart (attachmentPart);

                        /* Set message status to DELIVERED and persist it only if:
                         * - it is in inbox
                         * - it has PROCESSED status
                         */
                        if (   msgBox.equalsIgnoreCase (MessageDVO.MSGBOX_IN)
                            && msgStatus.equalsIgnoreCase (MessageDVO.STATUS_PROCESSED))
                        {
                            MessageDAO dao =
                                (MessageDAO) AS2Processor.core.dao.createDAO (MessageDAO.class);
                            targetMessageDvo.setStatus (MessageDVO.STATUS_DELIVERED);
                            dao.persist (targetMessageDvo);
                        }
                    } catch (Exception e)
                    {
                        AS2Processor.core.log.error ("Error in collecting message", e);
                    }
                }
            }
        }

        generateReply(response, soapResponseMessage.countAttachments () > 0);
    }

    private void generateReply(WebServicesResponse response,
                               boolean isReturned)
        throws SOAPRequestException
    {
        try
        {
            SOAPElement responseElement =
                createText ("hasMessage",
                            Boolean.toString (isReturned),
                            "http://service.as2.edi.cecid.hku.hk/");
            response.setBodies (new SOAPElement[] { responseElement });
        } catch (Exception e)
        {
            throw new SOAPRequestException ("Unable to generate reply message", e);
        }
    }

    protected boolean isCacheEnabled()
    {
        return false;
    }
}
