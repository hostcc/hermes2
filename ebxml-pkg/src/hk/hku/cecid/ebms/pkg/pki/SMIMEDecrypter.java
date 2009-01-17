/*
 * Copyright(c) 2002 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the Academic Free License Version 1.0
 *
 * Academic Free License
 * Version 1.0
 *
 * This Academic Free License applies to any software and associated 
 * documentation (the "Software") whose owner (the "Licensor") has placed the 
 * statement "Licensed under the Academic Free License Version 1.0" immediately 
 * after the copyright notice that applies to the Software. 
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of the Software (1) to use, copy, modify, merge, publish, perform, 
 * distribute, sublicense, and/or sell copies of the Software, and to permit 
 * persons to whom the Software is furnished to do so, and (2) under patent 
 * claims owned or controlled by the Licensor that are embodied in the Software 
 * as furnished by the Licensor, to make, use, sell and offer for sale the 
 * Software and derivative works thereof, subject to the following conditions: 
 *
 * - Redistributions of the Software in source code form must retain all 
 *   copyright notices in the Software as furnished by the Licensor, this list 
 *   of conditions, and the following disclaimers. 
 * - Redistributions of the Software in executable form must reproduce all 
 *   copyright notices in the Software as furnished by the Licensor, this list 
 *   of conditions, and the following disclaimers in the documentation and/or 
 *   other materials provided with the distribution. 
 * - Neither the names of Licensor, nor the names of any contributors to the 
 *   Software, nor any of their trademarks or service marks, may be used to 
 *   endorse or promote products derived from this Software without express 
 *   prior written permission of the Licensor. 
 *
 * DISCLAIMERS: LICENSOR WARRANTS THAT THE COPYRIGHT IN AND TO THE SOFTWARE IS 
 * OWNED BY THE LICENSOR OR THAT THE SOFTWARE IS DISTRIBUTED BY LICENSOR UNDER 
 * A VALID CURRENT LICENSE. EXCEPT AS EXPRESSLY STATED IN THE IMMEDIATELY 
 * PRECEDING SENTENCE, THE SOFTWARE IS PROVIDED BY THE LICENSOR, CONTRIBUTORS 
 * AND COPYRIGHT OWNERS "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE 
 * LICENSOR, CONTRIBUTORS OR COPYRIGHT OWNERS BE LIABLE FOR ANY CLAIM, DAMAGES 
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE. 
 *
 * This license is Copyright (C) 2002 Lawrence E. Rosen. All rights reserved. 
 * Permission is hereby granted to copy and distribute this license without 
 * modification. This license may not be modified without the express written 
 * permission of its copyright owner. 
 */

/* ===== 
 *
 * $Header: /home/cvsroot/ebxml-pkg/src/hk/hku/cecid/ebms/pkg/pki/SMIMEDecrypter.java,v 1.1 2005/07/28 09:36:24 dcmsze Exp $
 *
 * Code authored by:
 *
 * achong [2002-08-02]
 *
 * Code reviewed by:
 *
 * username [YYYY-MM-DD]
 *
 * Remarks:
 *
 * =====
 */

package hk.hku.cecid.ebms.pkg.pki;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.mail.smime.parsers.SMIMEEncryptedParser;
/**
A class for decrypting MimeBodyPart that was encrypted by SMIME.
*/
public class SMIMEDecrypter extends SMIMEHandler {

    public final static String SMIME_ENCRYPTED =
    "application/pkcs7-mime";
    /**
    Decrypts a MimeBodyPart that was encrypted by SMIME.
	@param keyStorePath The path of keystore
	@param keyStorePass The password of the keystore
    @param alias The alias of the private key for decryption. The private key
    should be associated with a certificate chain, whose target certificate 
    contains the corresponding public key.
    @param keyPass The password for the private key entry
    @param mimeMessage The encrypted MimeMessage to be decrypted
    @param session The authentication session
    @throws KeyStoreException if the keystore is corrupted
    @throws NoSuchAlgorithmException if the keystore cannot be read
    @throws UnrecoverableKeyException if the keystore cannot be read
    @throws SMIMEException if the internal SMIME library (BouncyCastle) throws
    a Exception when decryption. SMIMEException wraps the exception thrown by
    the internal SMIME library.
    */
    public static MimeMessage decryptMimeMessage(String keyStorePath,
        String keyStorePass, String alias, String keyPass, 
        MimeMessage mimeMessage, Session session) throws KeyStoreException,
        NoSuchAlgorithmException, UnrecoverableKeyException,
        hk.hku.cecid.ebms.pkg.pki.SMIMEException {
        SMIMEHandler.initiate();
        CompositeKeyStore compositeKs = new CompositeKeyStore();
        compositeKs.addKeyStoreFile(keyStorePath , null, keyStorePass.
        toCharArray());
        PrivateKey privateKey = (PrivateKey) compositeKs.getKey(alias, keyPass.
        toCharArray());
        java.security.cert.Certificate [] certs = compositeKs.
        getCertificateChain(alias);
        java.security.cert.Certificate cert = certs[0];
        SMIMEEncryptedParser parser = new SMIMEEncryptedParser(session);
        try {
            return (MimeMessage) parser.decrypt(mimeMessage,
                (java.security.cert.X509Certificate) cert, privateKey);
        }
        catch(IOException e) {
            throw new hk.hku.cecid.ebms.pkg.pki.SMIMEException("Cannot decrypt" +
            " MimeMessage", e);
        }
        catch(MessagingException e) {
            throw new hk.hku.cecid.ebms.pkg.pki.SMIMEException("Cannot decrypt" +
            " MimeMessage", e);
        }
        catch(GeneralSecurityException e) {
            throw new hk.hku.cecid.ebms.pkg.pki.SMIMEException("Cannot decrypt" +
            " MimeMessage", e);
        }
        catch(CMSException e) {
            throw new hk.hku.cecid.ebms.pkg.pki.SMIMEException("Cannot decrypt" +
            " MimeMessage", e);
        }
        catch(org.bouncycastle.mail.smime.SMIMEException e) {
            throw new hk.hku.cecid.ebms.pkg.pki.SMIMEException("Cannot decrypt" +
            " MimeMessage", e);
        }
    }
    
    /**
    It returns whether the MimeBodyPart is encrypted by SMIME. Note that it 
    only checks whether the content type string starts with 
    "application/pkcs7-mime", it does not tell whether the MimeBodyPart can 
    be correctly decrypted.
    @param mimeMessage The MimeMessage to be tested
    @throws MessagingException thrown by MimeBodyPart.getContentType(). The
    Javamail 's API does not tell us on what condition we get this exception
    */
    public boolean isSMIMEEncrypted(MimeMessage mimeMessage) throws 
    MessagingException {
        if (mimeMessage.getContentType().toLowerCase().startsWith(
        SMIME_ENCRYPTED)) {
            return true;
        }
        else {
            return false;
        }
    }
}
