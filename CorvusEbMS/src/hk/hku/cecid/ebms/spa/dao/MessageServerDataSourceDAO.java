/* 
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.ebms.spa.dao;

import hk.hku.cecid.ebms.spa.handler.MessageClassifier;
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.dao.DVO;
import hk.hku.cecid.piazza.commons.dao.ds.DataSourceDAO;
import hk.hku.cecid.piazza.commons.dao.ds.DataSourceProcess;
import hk.hku.cecid.piazza.commons.dao.ds.DataSourceTransaction;

/**
 * @author Donahue Sze
 * 
 */
public class MessageServerDataSourceDAO extends DataSourceDAO implements
        MessageServerDAO {

    public void storeMessage(final MessageDVO messageDVO,
            final RepositoryDVO repositoryDVO) throws DAOException {

        DataSourceProcess process = new DataSourceProcess(this) {
            protected void doTransaction(DataSourceTransaction tx)
                    throws DAOException {
                MessageDAO messageDAO = (MessageDAO) getFactory().createDAO(
                        MessageDAO.class);
                RepositoryDAO repositoryDAO = (RepositoryDAO) getFactory()
                        .createDAO(RepositoryDAO.class);

                messageDAO.setTransaction(tx);
                repositoryDAO.setTransaction(tx);

                messageDAO.addMessage(messageDVO);
                repositoryDAO.addRepository(repositoryDVO);
            }
        };

        process.start();
    }

    /*public void storeInboxMessage(final MessageDVO messageDVO,
            final InboxDVO inboxDVO) throws DAOException {
        DataSourceProcess process = new DataSourceProcess(this) {
            protected void doTransaction(DataSourceTransaction tx)
                    throws DAOException {
                MessageDAO messageDAO = (MessageDAO) getFactory().createDAO(
                        MessageDAO.class);
                InboxDAO inboxDAO = (InboxDAO) getFactory().createDAO(
                        InboxDAO.class);

                messageDAO.setTransaction(tx);
                inboxDAO.setTransaction(tx);

                messageDAO.updateMessage(messageDVO);
                inboxDAO.addInbox(inboxDVO);
            }
        };

        process.start();
    }*/

    public void storeOutboxMessage(final MessageDVO messageDVO,
            final RepositoryDVO repositoryDVO, final OutboxDVO outboxDVO)
            throws DAOException {

        DataSourceProcess process = new DataSourceProcess(this) {
            protected void doTransaction(DataSourceTransaction tx)
                    throws DAOException {
                MessageDAO messageDAO = (MessageDAO) getFactory().createDAO(
                        MessageDAO.class);
                RepositoryDAO repositoryDAO = (RepositoryDAO) getFactory()
                        .createDAO(RepositoryDAO.class);
                OutboxDAO outboxDAO = (OutboxDAO) getFactory().createDAO(
                        OutboxDAO.class);

                messageDAO.setTransaction(tx);
                repositoryDAO.setTransaction(tx);
                outboxDAO.setTransaction(tx);

                messageDAO.addMessage(messageDVO);
                repositoryDAO.addRepository(repositoryDVO);
                outboxDAO.addOutbox(outboxDVO);
            }
        };

        process.start();
    }

    public void clearMessage(final MessageDVO data) throws DAOException {

        DataSourceProcess process = new DataSourceProcess(this) {
            protected void doTransaction(DataSourceTransaction tx)
                    throws DAOException {
                MessageDAO messageDAO = (MessageDAO) getFactory().createDAO(
                        MessageDAO.class);
                InboxDAO inboxDAO = (InboxDAO) getFactory().createDAO(
                        InboxDAO.class);
                OutboxDAO outboxDAO = (OutboxDAO) getFactory().createDAO(
                        OutboxDAO.class);

                messageDAO.setTransaction(tx);
                inboxDAO.setTransaction(tx);
                outboxDAO.setTransaction(tx);

                messageDAO.updateMessage(data);
                String message_box = data.getMessageBox();
                if (message_box
                        .equalsIgnoreCase(MessageClassifier.MESSAGE_BOX_INBOX)) {
                    InboxDVO inboxDVO = (InboxDVO) inboxDAO.createDVO();
                    inboxDVO.setMessageId(data.getMessageId());
                    inboxDAO.deleteInbox(inboxDVO);
                } else {
                    OutboxDVO outboxDVO = (OutboxDVO) outboxDAO.createDVO();
                    outboxDVO.setMessageId(data.getMessageId());
                    outboxDAO.deleteOutbox(outboxDVO);
                }
            }
        };

        process.start();

    }

    /*
     * (non-Javadoc)
     * 
     * @see hk.hku.cecid.piazza.commons.dao.DAO#createDVO()
     */
    public DVO createDVO() {
        return new MessageServerDataSourceDVO();
    }

}