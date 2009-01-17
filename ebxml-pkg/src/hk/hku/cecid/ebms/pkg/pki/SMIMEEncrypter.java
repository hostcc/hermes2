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
 * $Header: /home/cvsroot/ebxml-pkg/src/hk/hku/cecid/ebms/pkg/pki/SMIMEEncrypter.java,v 1.1 2005/07/28 09:36:24 dcmsze Exp $
 *
 * Code authored by:
 *
 * achong [2002-08-01]
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
 
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.jce.provider.JDKX509CertificateFactory;
import org.bouncycastle.mail.smime.generators.SMIMEEncryptedGenerator;
/**
A generator of SMIME encryption. It only works properly with BouncyCastle 's
JCE provider. Currently it only supports 168bit triple DES with CBC for 
encrypting the body parts. The DES key, which is a symmetric key, is encrypted
by RSA.
*/
public class SMIMEEncrypter {
    /**
    Create a encrypted MimeBodyPart.
	@param keyStorePath The path of keystore
	@param keyStorePass The password of the keystore    
    @param alias The alias of the certificate to be used for encryption
    @param mimeMessage The MimeMessage is the one to be encrypted
	@param session The authentication session    
    @throws KeyStoreException if the keystore is corrupted
    @throws CertificateEncodingException if an encoding error occurs
    @throws SMIMEException if it cannot generate the encrypted MimeBodyPart for
    other reasons. Actually this exception wraps the exceptions thrown by 
    SMIME library (BouncyCastle)
    */
    public static MimeMessage createEncryptedMimeMessage(String keyStorePath,
        String keyStorePass, String alias, MimeMessage mimeMessage,
        Session session) throws SMIMEException  {
        SMIMEHandler.initiate();
        try {
            CompositeKeyStore compositeKs = new CompositeKeyStore();
            compositeKs.addKeyStoreFile(keyStorePath , null, keyStorePass.
                    toCharArray());
            return createEncryptedMimeMessage(
                getCertificate(compositeKs, alias), mimeMessage, session);
        } catch (KeyStoreException e) {
            throw new SMIMEException(e.toString());
        } catch (CertificateEncodingException e) {
            throw new SMIMEException(e.toString());
        } catch (CertificateException e) {
            throw new SMIMEException(e.toString());
        }
    }
    
    public static MimeMessage createEncryptedMimeMessage(
        X509Certificate certificate, MimeMessage mimeMessage,
        Session session) throws SMIMEException {
        SMIMEHandler.initiate();
        try {
            SMIMEEncryptedGenerator generator = new SMIMEEncryptedGenerator();
            generator.addKeyTransRecipient(certificate);
            generator.setContent(mimeMessage, session);
            generator.setContentEncryptionAlgorithm(
                SMIMEEncryptedGenerator.DESEDE_192);
            return (MimeMessage) generator.generate();
        } catch (IOException e) {
            throw new SMIMEException(e.toString());
        } catch (MessagingException e) {
            throw new SMIMEException(e.toString());
        } catch (CMSException e) {
            throw new SMIMEException(e.toString());
        } catch (org.bouncycastle.mail.smime.SMIMEException e) {
            throw new SMIMEException(e.toString());
        } catch (GeneralSecurityException e) {
            throw new SMIMEException(e.toString());
        }
    }

    /** 
    Return the certificate of the specified alias. 
    @param alias The alias of the certificate
    @throws KeyStoreException if the keystore is corrupted
    @throws CertificateEncodingException if an encoding error occurs
    @throws CertificateException if we cannot get the BouncyCastle 's 
    implementation of Certificate
    */ 
    private static X509Certificate getCertificate(CompositeKeyStore compositeKs,
        String alias) throws KeyStoreException, CertificateEncodingException,
        CertificateException {
        java.security.cert.Certificate cert = compositeKs.getCertificate(alias);
        if (cert == null) {
            throw new CertificateException("Cannot load certificate");
        }
        /*
        We need to firsly get the encoded form of the certificate and then 
        instantiate certificate implementation of BouncyCastle 's JCE. It is 
        needed because BouncyCastle 's SMIME only works properly with its
        own certificate implementation!
        */        
        JDKX509CertificateFactory certFactory = new JDKX509CertificateFactory();
        return (java.security.cert.X509Certificate) certFactory.
        engineGenerateCertificate(new ByteArrayInputStream(cert.getEncoded())); 
    }
} 

