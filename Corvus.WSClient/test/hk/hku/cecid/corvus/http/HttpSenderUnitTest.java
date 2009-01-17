/* 
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.corvus.http;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Iterator;

import junit.framework.TestCase;

import sun.misc.BASE64Encoder;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;

import hk.hku.cecid.testcase.FixtureStore;
import hk.hku.cecid.piazza.commons.util.FileLogger;
import hk.hku.cecid.piazza.commons.io.IOHandler;

import hk.hku.cecid.corvus.ws.data.KVPairData;

/** 
 * The <code>HttpSenderUnitTest</code> is unit test of <code>HttpSender</code>. 
 *
 * TODO: Inadequate Test-case for testing looping.
 *
 * @author 	Twinsen Tsang
 * @version 1.0.0
 * @since   JDK5.0, H2O 0908
 *
 * I fall back to Junit3 because this project may use under J2SE 1.4.2.
 */
public class HttpSenderUnitTest extends TestCase 
{	
	// Instance logger
	Logger logger = LoggerFactory.getLogger(this.getClass());  
	
	// Fixture name.
	public static final String TEST_LOG = "test.log";	
	// The fixture loader to load test-case
	private static ClassLoader FIXTURE_LOADER = FixtureStore.createFixtureLoader(false, HttpSenderUnitTest.class);
	
	// Parameters 
	public static final int 	TEST_PORT 		= 1999;	
	public static final String 	TEST_ENDPOINT 	= "http://localhost:" + TEST_PORT;
	
	/** The testing target which is a HttpSender */
	private HttpSender target;
	private FileLogger testClassLogger;
	
	private SimpleHttpMonitor monitor;

	/** Setup the fixture. */
	public void setUp() throws Exception {
		this.initTestTarget();
		logger = LoggerFactory.getLogger(this.getName());
		logger.info(this.getName() + " Start ");		
	}

	/** Initialize the test target which is a HTTP Sender. */
	public void initTestTarget() throws Exception 
	{
		URL logURL = FIXTURE_LOADER.getResource(TEST_LOG);
		if (logURL == null)
			throw new NullPointerException("Missing fixture " + TEST_LOG + " in the fixture path");
		
		File log = new File(logURL.getFile());
		this.testClassLogger = new FileLogger(log);		
		this.target = new HttpSender(this.testClassLogger, new KVPairData(0));
		this.target.setServiceEndPoint(TEST_ENDPOINT);
	}
	
	/** Test {@link HttpSender#getLoopTimes()} and {@link HttpSender#setLoopTimes(int)} **/
	public void testLoopTimeProperty() 
	{
		// Test whether it set property.
		this.target.setLoopTimes(10);
		assertEquals(10, this.target.getLoopTimes());
		// Test whether it guards value less than zero.
		this.target.setLoopTimes(-1);
		assertEquals(10, this.target.getLoopTimes());
	}
	
	/** Test {@link HttpSender#getUserObject()} **/
	public void testUserObjectProperty()
	{		
		Object usrObj = new Object();
		this.target.setUserObject(usrObj);
		assertEquals(usrObj, this.target.getUserObject());		
	}
	
	/** Test {@link HttpSender#setServiceEndPoint(URL)} **/
	public void testServiceEndpointProperty() throws MalformedURLException
	{	
		String urlStr = "http://localhost:1999";
		URL url = new URL(urlStr);		
		this.target.setServiceEndPoint(urlStr);
		assertEquals(url, this.target.getServiceEndPoint());		
	}
	
	/** Test whether {@link HttpSender#setServiceEndPoint(String)} with invalid data. */
	public void testServiceEndpointPropertyWithInvalidData() throws MalformedURLException
	{
		String urlStr = "invalid.endpoint";
		this.target.setServiceEndPoint(urlStr);
		// View log for check whether it failed to set.
	}
	
	/** Test whether the HTTP sender able to send the HTTP header to our monitor successfully. */
	public void testSendWithNoContent() throws Exception
	{				
		this.assertSend();	
	}
	
	/** Test whether the HTTP sender able to send the HTTP header with POST parameter to our monitor successfully. */
	public void testSendWithParameter() throws Exception 
	{
		this.target = new HttpSender(this.testClassLogger, new KVPairData(0)){

			public HttpMethod onCreateRequest() throws Exception {
				PostMethod method = new PostMethod("http://localhost:1999");
				method.addParameter("testparamName", "testparamValue");
				return method;
			}		
		};
		this.assertSend();	
	}
	
	/** Test whether the HTTP sender able to send the HTTP header with multi-part to our monitor successfully. */
	public void testSendWithMultipart() throws Exception 
	{
		this.target = new HttpSender(this.testClassLogger, new KVPairData(0)){

			public HttpMethod onCreateRequest() throws Exception 
			{
				PostMethod method = new PostMethod("http://localhost:1999");
				Part[] parts = {
					new StringPart("testparamName", "testparamValue")
				};
				method.setRequestEntity(
					new MultipartRequestEntity(parts, method.getParams())
				);
				return method;
			}		
		};
		this.assertSend();		
	}
	
	/** Test wether the HTTP sender able to send the HTTP header with 'Authorization' header. */
	public void testSendWithBasicAuthentication() throws Exception 
	{
		String user = "test";
		String password = "test";
		// Set the basic authentication.
		this.target.setBasicAuthentication(user, password);
		this.assertSend();
		
		String auth = (String) this.monitor.getHeaders().get("Authorization");
		String base64auth = "Basic " + new BASE64Encoder().encode((user + ":" + password).getBytes());
		assertNotNull("Missing the Basic Authorization.", auth);	
		assertEquals ("The Basic Authorization mis-match.", base64auth, auth);
	}
	
	/** Stop the HTTP monitor preventing JVM port binding **/ 
	public void tearDown() throws Exception {
		if (this.monitor != null){
			this.monitor.stop();		
			Thread.sleep(1500); // Make some delay for releasing the socket.
		}
		logger.info(this.getName() + " End ");
	}
	
	/*
	 * A Helper method for testing the HTTP sender. It start the HTTP monitor 
	 * and execute the HTTP sender. Then it print out the data received by the HTTP monitor. 
	 */
	private void assertSend() throws Exception 
	{
		this.monitor = new SimpleHttpMonitor(1999);
		// Start the TCP monitor.
		this.monitor.start();		
		Thread.sleep(1000);
		// Start the HTTP sender
		this.target.run();
		
		Map headers 	= this.monitor.getHeaders();
		assertFalse	("The HTTP header is empty, no data received yet.", headers.isEmpty());
		
		Map.Entry tmp	= null;
		Iterator itr 	= headers.entrySet().iterator();
		
		// Log the header information.
		logger.info("Header information");
		while (itr.hasNext()){
			tmp = (Map.Entry) itr.next();
			logger.info(tmp.getKey() + " : " + tmp.getValue());
		}
		
		InputStream mins;
		assertNotNull("The monitor has not received any data yet !.", (mins = this.monitor.getInputStream()));
		this.logger.info(IOHandler.readString(mins,null)); // Print the HTTP content to log.
	}
}
