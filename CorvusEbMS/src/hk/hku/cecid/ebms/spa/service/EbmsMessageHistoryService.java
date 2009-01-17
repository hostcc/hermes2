package hk.hku.cecid.ebms.spa.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.soap.SOAPElement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.ebms.spa.dao.MessageDAO;
import hk.hku.cecid.ebms.spa.dao.MessageDVO;
import hk.hku.cecid.ebms.spa.handler.MessageClassifier;
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.soap.SOAPRequest;
import hk.hku.cecid.piazza.commons.soap.SOAPRequestException;
import hk.hku.cecid.piazza.commons.soap.SOAPResponse;
import hk.hku.cecid.piazza.commons.soap.WebServicesAdaptor;
import hk.hku.cecid.piazza.commons.soap.WebServicesRequest;
import hk.hku.cecid.piazza.commons.soap.WebServicesResponse;

public class EbmsMessageHistoryService extends WebServicesAdaptor{
	
	public static int MAX_NUMBER = 2147483647;
	public static String NAMESPACE = "http://service.ebms.edi.cecid.hku.hk/";
	
	  public void serviceRequested(WebServicesRequest request, WebServicesResponse response) 
	  		throws SOAPRequestException, DAOException{
		  
		  Element[] bodies = request.getBodies();
	      String msgId = getText(bodies, "messageId");
	      String msgBox = getText(bodies, "messageBox");
	      String convId = getText(bodies, "conversationId");
	      String cpaId = getText(bodies, "cpaId");
	      String service = getText(bodies, "service");
	      String action = getText(bodies, "action");
	      String status = getText(bodies, "status");
	      String limit = getText(bodies, "limit");
        
		  EbmsProcessor.core.log
          .info("Message History Query received request - "+
        		  "MessageID : " + (msgId ==null?"NULL":msgId)
                  + ", MessageBox: " + (msgBox ==null?"NULL":msgBox)
                  + ", CovID: " + 	(convId ==null?"NULL":convId)
                  + ", CpaID: " + 	(cpaId ==null?"NULL":cpaId)
                  + ", Service: " + (service ==null?"NULL":service)
                  + ", Action: " + 	(action ==null?"NULL":action)
                  + ", Status: " +  (status ==null?"NULL":status)
                  +", Number of Messages: " + (limit ==null?"NULL":limit));
		  
		  int resultLimit = -1;
		  try{
			  resultLimit = Integer.parseInt(limit);
			  if(resultLimit < 1 ){
				  resultLimit = MAX_NUMBER;
			  }
		  }catch (NumberFormatException e) {
			  resultLimit = MAX_NUMBER;
		  }
		  
		  try{
			  MessageDAO msgDAO =  
				  		(MessageDAO)EbmsProcessor.core.dao.createDAO(MessageDAO.class);
			  MessageDVO criteriaDVO = (MessageDVO)msgDAO.createDVO();
			  criteriaDVO.setMessageId(msgId);
			  criteriaDVO.setMessageBox(checkMessageBox(msgBox));
			  criteriaDVO.setConvId(convId);
			  criteriaDVO.setCpaId(cpaId);
			  criteriaDVO.setService(service);
			  criteriaDVO.setAction(action);
			  criteriaDVO.setStatus(checkMessageStatus(status));
			  
			  List results = msgDAO.findMessagesByHistory(criteriaDVO, resultLimit, 0);
			  generateReply(response, results);
		  }catch(DAOException daoExp){
			  throw new DAOException("Unable to query the repository", daoExp);
		  } catch (SOAPRequestException e) {
			  throw e;
		  }
	  }
	  
	  private String checkMessageStatus(String input)throws SOAPRequestException{
		  if(input !=null &&
				  !(input.equalsIgnoreCase(MessageClassifier.INTERNAL_STATUS_RECEIVED) ||
				  input.equalsIgnoreCase(MessageClassifier.INTERNAL_STATUS_PENDING) ||
				  input.equalsIgnoreCase(MessageClassifier.INTERNAL_STATUS_PROCESSING) ||
				  input.equalsIgnoreCase(MessageClassifier.INTERNAL_STATUS_PROCESSED) ||
				  input.equalsIgnoreCase(MessageClassifier.INTERNAL_STATUS_PROCESSED_ERROR) ||
				  input.equalsIgnoreCase(MessageClassifier.INTERNAL_STATUS_DELIVERED) ||
				  input.equalsIgnoreCase(MessageClassifier.INTERNAL_STATUS_DELIVERY_FAILURE))){
			  String errMsg = "No such message status, you have entered ["+input+"]";
			  throw new SOAPRequestException(errMsg);
		  }else{
			  return (input==null?input:input.toUpperCase());
		  }
	  }
	  
	  private String checkMessageBox(String input)throws SOAPRequestException{
		  if(input !=null &&
				  !(input.equalsIgnoreCase(MessageClassifier.MESSAGE_BOX_INBOX) ||
				  input.equalsIgnoreCase(MessageClassifier.MESSAGE_BOX_OUTBOX))){
			  String errMsg = "Wrong Message Box entered, you have entered: ["+ input+"]";
			  throw new SOAPRequestException(errMsg);
		  }else{
			  return  (input==null?input:input.toLowerCase());
		  }
	  }
	  
	    
	    /**
	     * 
	     * @param response
	     * @param messageList
	     * @throws SOAPRequestException
	     */
	    private void generateReply(WebServicesResponse response,
	    	List  messageList) throws SOAPRequestException {
	        try {
	            SOAPElement rootElement = createElement("messageList", "",
	            		NAMESPACE, "MessageList");

	            Iterator messagesIterator = messageList.iterator();

	            for (int i = 0; messagesIterator.hasNext(); i++) {
	                MessageDVO currentMessage = (MessageDVO) messagesIterator.next();

	                // Create Message Element and append Value of MessageID and MessageBox 
	                SOAPElement msgElement = createElement("messageElement", "", NAMESPACE, "MessageElement")	;                
	                SOAPElement childElement_MsgId = createText("messageId", currentMessage.getMessageId(), NAMESPACE);                	               
	                SOAPElement childElement_MsgBox = createText("messageBox", currentMessage.getMessageBox(), NAMESPACE);
	                msgElement.addChildElement(childElement_MsgId);
	                msgElement.addChildElement(childElement_MsgBox);
	                
	                rootElement.addChildElement(msgElement);
	            }

	            response.setBodies(new SOAPElement[] { rootElement });
	        } catch (Exception e) {
	            throw new SOAPRequestException("Unable to generate reply message", e);
	        }
	    }
}
