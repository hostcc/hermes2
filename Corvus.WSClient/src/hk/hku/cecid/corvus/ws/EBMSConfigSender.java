/* 
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.corvus.ws;

import java.util.Date;
import java.util.Map;

import hk.hku.cecid.corvus.ws.data.DataFactory;
import hk.hku.cecid.corvus.ws.data.EBMSConfigData;

import hk.hku.cecid.piazza.commons.data.Data;
import hk.hku.cecid.piazza.commons.util.FileLogger;
import hk.hku.cecid.piazza.commons.util.PropertyTree;
import hk.hku.cecid.piazza.commons.soap.SOAPSender;

/**
 * The <code>EBMSConfigSender</code> is a client sender sending SOAP web
 * services request to B2BCollector <code>EBMS</code> plugin for configurating
 * the performance factor.
 * 
 * The web service parameters are defined in the below:
 * 
 * <pre> 
 *  &lt;active-module-status&gt; true | false &lt;/active-module-status&gt;
 *  &lt;incollector-interval&gt;15000&lt;/incollector-interval&gt;
 *  &lt;incollector-maxthread&gt;0&lt;/incollector-maxthread&gt;
 *  &lt;outcollector-interval&gt;15000&lt;/outcollector-interval&gt;
 *  &lt;outcollector-maxthread&gt;0&lt;/outcollector-maxthread&gt;
 *  &lt;mailcollector-interval&gt;15000&lt;/mailcollector-interval&gt;
 *  &lt;mailcollector-maxthread&gt;0&lt;/mailcollector-maxthread&gt;
 * </pre>
 * 
 * @author Twinsen Tsang
 * @version 1.0.2
 * @since	Elf 0818
 */
public class EBMSConfigSender extends SOAPSender
{				
	private final String NS_URI = EBMSMessageSender.NS_URI;
	
	/**
	 * The result status for the last successful web services call. 
	 */
	private String lastSuccessfulConfigStatus;
	
	/**
	 * Explicit Constructor.
	 * 
	 * @param l		The logger used for log message and exception.
	 * @param data	The EBMS Configuration parameters.
	 */
	public EBMSConfigSender(FileLogger l, EBMSConfigData data)
	{	
		super(l, (Data)data, data.getSendEndpoint());
		this.setLoopTimes(1);		
	}	
	
	/**
	 * [@EVENT] The method <code>onStart</code> log all new configuration.  
	 */	
	public void onStart(){
		if (!(this.properties instanceof EBMSConfigData))
			return;

		EBMSConfigData data = (EBMSConfigData) this.properties;		
		if (log != null)
		{		
			// Log all information for this sender.	
			this.log.log("EbMS Configurator Client init at " + new Date().toString());
			this.log.log("");
			this.log.log("Sending EbMS Config SOAP Message with following configuration");						
			this.log.log("------------------------------------------------------------------");
			if (data != null)
				this.log.log(data.toString());
			this.log.log("------------------------------------------------------------------");
			this.log.log("");	
		}		
		// Initialize the message.
		try{
			this.initializeMessage();
			this.setRequestDirty(false);
		}catch(Exception e){
			if (this.log != null)
				this.log.log("Unable to initialize the SOAP Message");
			this.onError(e);
		}		
	}	
		
	/**
	 * The SOAPRequest in the creation stage should be liked this.
	 * 
	 * @throws Exceptions
	 */
	public void initializeMessage() throws Exception
	{	
		if (!(this.properties instanceof EBMSConfigData)){
			return;
		}		
		EBMSConfigData data = (EBMSConfigData) this.properties;		
		Map map 	       = data.getProperties();
		int len			   = EBMSConfigData.PARAM_KEY_SET.length;
		// All key are conformed to the WSDL schema. so use 
		// KV-iteration is ok.
		for (int i = 0; i < len; i++){
			String key = EBMSConfigData.PARAM_KEY_SET[i];
			String value = (String) map.get(key);
			this.addRequestElementText(key, value, NS_PREFIX, NS_URI);
		}			
	}		
	
	/**
	 * Get the SOAP Body and analyze the result of configuration.<p>
	 * The result of SOAP body: 
	 * <pre>
	 *  	&lt;status&gt;success | fail&lt;/status&gt;
	 *  </pre>
	 */	
	public void onResponse() throws Exception {
		EBMSConfigData data = (EBMSConfigData) this.properties;
		// Get the first element with name "status".
		this.lastSuccessfulConfigStatus = 
			this.getResponseElementText("status", NS_URI, 0);				
		if (this.log != null)
			log.log("Configuration Result: " + this.lastSuccessfulConfigStatus);
	}
	
	/**
	 * @return Get the result status for the last successful web services call. 
	 */
	public String getStatus(){
		return this.lastSuccessfulConfigStatus;
	}
	
	/**
	 * The main method is for CLI mode.
	 */
	public static void main(String [] args){
		try{			
			if (args.length < 2){
				System.out.println("Usage: ebms-config [config-xml] [log-path]");
				System.out.println();
				System.out.println("Example: ebms-config ./config/ebms-config/ebms-request.xml ./logs/ebms-config.log");
				System.exit(1);
			}					
			System.out.println("----------------------------------------------------");
			System.out.println("        EBMS Configuration Updater          ");
			System.out.println("----------------------------------------------------");
			
			// Initialize the logger.
			System.out.println("Initialize logger .. ");
			FileLogger logger = new FileLogger(new java.io.File(args[1]));	
			
			// Initialize the query parameter.
			System.out.println("Importing  EBMS sending parameters ... ");	
			EBMSConfigData ecd = DataFactory.getInstance()
				.createEBMSConfigDataFromXML(
					new PropertyTree(
						new java.io.File(args[0]).toURL()));
			
			// Initialize the sender.
			System.out.println("Initialize EBMS configuration updater ... "); 			
			EBMSConfigSender sender = new EBMSConfigSender(logger, ecd);
			
			System.out.println("Sending    EBMS-config sending request ... ");
			sender.run();			
						
			System.out.println();
			System.out.println("                    Sending Done:                   ");
			System.out.println("----------------------------------------------------");
			System.out.println("The result of query: " + sender.getStatus());
			System.out.println();
			System.out.println("Please view log for details .. ");
			
		}catch(Exception e){
			e.printStackTrace(System.err);
		}		
	}
}