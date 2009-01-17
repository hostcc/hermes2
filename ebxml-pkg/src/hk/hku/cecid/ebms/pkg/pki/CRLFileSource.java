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
 * $Header: /home/cvsroot/ebxml-pkg/src/hk/hku/cecid/ebms/pkg/pki/CRLFileSource.java,v 1.1 2005/07/28 09:36:24 dcmsze Exp $
 *
 * Code authored by:
 *
 * kcyee [2002-04-30]
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
/**
 * This class extends CRLSource to add initialization procedure for loading a 
 * file-based CRL.
 *
 * @author kcyee
 * @version $Revision: 1.1 $
 */
public class CRLFileSource extends CRLSource {

    /**
     * The file holding the CRL
     */
    protected File crlFile;

    /**
     * Default constructor. It initializes the object. But the object 
     * is still unusable until init() is called.
     */
    public CRLFileSource() {
        super();
        crlFile = null;
    }

    /**
     * Constructor with the file name of the CRL passed in. It initializes 
     * the object. But the object is still unusable until init() is called.
     *
     * @param crlFile the file name of the CRL
     */
    public CRLFileSource(String crlFile) {
        this(new File(crlFile));
    }

    /**
     * Constructor with the file object holding the CRL passed in. It 
     * initializes the object. But the object is still unusable until init() 
     * is called.
     *
     * @param crlFile the file object of the file holding the CRL
     */
    public CRLFileSource(File crlFile) {
        super();
        this.crlFile = crlFile;
    }

    /**
     * Initializes the object. The CRL file is being loaded into the 
     * internal CRL object.
     *
     * @throws CRLException Initialization error occurs
     */
    public void init() throws CRLException {
        if (crlFile == null || !crlFile.exists() || !crlFile.isFile()) {
            throw new CRLException("Error loading file: " + crlFile + ".\n");
        }

        try {
            InputStream inStream = new FileInputStream(crlFile);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            crl = (X509CRL) cf.generateCRL(inStream);
            inStream.close();
        }
        catch (IOException e) {
            throw new CRLException("IO exception when loading crl file.\n"
                + e.getMessage());
        }
        catch (CertificateException e) {
            throw new CRLException(
                "Certificate exception when loading crl file.\n" 
                + e.getMessage());
        }

        ready = true;
    }
}
