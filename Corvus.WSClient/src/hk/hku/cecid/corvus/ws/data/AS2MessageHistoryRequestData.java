/* 
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.corvus.ws.data;

public class AS2MessageHistoryRequestData extends MessageHistoryRequestData {

public static final String CONFIG_PREFIX = "as2-msg-history-request/config";
	
	public static final String PARAM_PREFIX  = "as2-msg-history-request/param";

	public static final String [] PARAM_AS2_KEY_SET = 
	{
		"messageId", "as2From", "as2To"
	};
		
	public void setAS2FromParty (String value){
		props.put(PARAM_AS2_KEY_SET[1], value);
	}
	
	public String getAS2FromParty(){
		return (String)props.get(PARAM_AS2_KEY_SET[1]);
	}
	
	public void setAS2ToParty (String value){
		props.put(PARAM_AS2_KEY_SET[2], value);
	}
	
	public String getAS2ToParty(){
		return (String)props.get(PARAM_AS2_KEY_SET[2]);
	}
	
	public void setMessageId (String value){
		props.put(PARAM_AS2_KEY_SET[0], value);
	}
	
	public String getMessageId(){
		return (String)props.get(PARAM_AS2_KEY_SET[0]);
	}
	
	public AS2MessageHistoryRequestData() {
		super((PARAM_AS2_KEY_SET.length +
				PARAM_KEY_SET.length +
				CONFIG_KEY_SET.length));
	}

}
