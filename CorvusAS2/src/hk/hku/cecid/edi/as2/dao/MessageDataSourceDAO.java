/* 
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.edi.as2.dao;

import hk.hku.cecid.piazza.commons.dao.DVO;
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.dao.ds.DataSourceDAO;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

/**
 * @author Donahue Sze
 *  
 */
public class MessageDataSourceDAO extends DataSourceDAO implements MessageDAO {

    public List findMessageByOriginalMessageID(String oriMessageID, String oriMessageBox)
    throws DAOException {
        String mdnMessageBox = oriMessageBox.equals(MessageDVO.MSGBOX_IN)? 
                MessageDVO.MSGBOX_OUT:MessageDVO.MSGBOX_IN;
        List result = super.find("find_messages_by_original_message_id",
                new Object[] { oriMessageID, mdnMessageBox });
        return result;
    }

    public List findMessagesByStatus(String status, String messageBox)
            throws DAOException {
        return super.find("find_messages_by_status", new Object[]{status,
                messageBox});
    }

    public List findMessagesByHistory(MessageDVO data, int numberOfMessage,
            int offset) throws DAOException {
        return super.find("find_messages_by_history", new Object[] {
                data.getMessageId(), data.getMessageBox(), data.getAs2From(),
                data.getAs2To(), data.getPrincipalId(), data.getStatus(),
                new Integer(numberOfMessage), new Integer(offset) });

    }
    
	public List findMessagesByTime(int time_period, MessageDVO data, int numberOfMessage, int offset) throws DAOException {
		List messages = findMessagesByHistory(data,numberOfMessage,offset);
		Iterator itr = messages.iterator();
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.add(GregorianCalendar.MONTH,-time_period);
		long cutOffDate = calendar.getTimeInMillis();
		Timestamp time = new Timestamp(cutOffDate);
		ArrayList result = new ArrayList();
		MessageDVO temp;
		for(int dvoi = 0;itr.hasNext();dvoi++){
			if((temp = (MessageDVO)itr.next()).getTimeStamp().after(time))
				result.add(temp);
		}
		return result;
	}
	
	public List findMessagesBeforeTime(int time_period) throws DAOException {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.add(GregorianCalendar.MONTH,-time_period);
		long cutOffDate = calendar.getTimeInMillis();
		Timestamp time = new Timestamp(cutOffDate);
		Object[] param = {time};
		return super.find("get_all_before_time",param);
	}

    public int findNumberOfMessagesByHistory(MessageDVO data) 
            throws DAOException {
        try {
            List result = super.executeRawQuery(
                    super.getFinder("find_no_of_messages_by_history"),
                    new Object[] {
                            data.getMessageId(),
                            data.getMessageBox(),
                            data.getAs2From(),
                            data.getAs2To(),
                            data.getPrincipalId(),
                            data.getStatus()}
                            );
            List resultEntry = (List)result.get(0);
            return ((Number)resultEntry.get(0)).intValue();
        }
        catch (Exception e) {
            throw new DAOException("Unable to find the number of messages by history", e);
        }
    }

    public int recoverProcessingMessages() 
            throws DAOException {
        return super.update("recover_pr_msg", null);
    }
    public DVO createDVO() {
        return new MessageDataSourceDVO();
    }
    
}