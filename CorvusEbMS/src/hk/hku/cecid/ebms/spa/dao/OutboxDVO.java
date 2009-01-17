/* 
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.ebms.spa.dao;

import hk.hku.cecid.piazza.commons.dao.DVO;

/**
 * @author Donahue Sze
 * 
 */
public interface OutboxDVO extends DVO {

    /**
     * @return Returns the ackRequested.
     */
    public String getMessageId();

    /**
     * @param messageId The messageId to set.
     */
    public void setMessageId(String messageId);

    /**
     * @return Returns the retried.
     */
    public int getRetried();

    /**
     * @param retried The retried to set.
     */
    public void setRetried(int retried);

}