/*
 * Copyright 2007 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package name.kghost.oauth.lib.signature;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import name.kghost.oauth.config.persistent.OAuthConsumer;
import name.kghost.oauth.lib.OAuth;
import name.kghost.oauth.lib.OAuthException;
import name.kghost.oauth.lib.signature.pem.PEMReader;
import name.kghost.oauth.lib.signature.pem.PKCS1EncodedKeySpec;

/**
 * The RSA-SHA1 signature method. A consumer that wishes to use public-key
 * signatures on messages does not need a shared secret with the service
 * provider, but it needs a private RSA signing key. You create it like this:
 * 
 * OAuthConsumer c = new OAuthConsumer(callback_url, consumer_key, null,
 * provider); c.setProperty(RSA_SHA1.PRIVATE_KEY, consumer_privateRSAKey);
 * 
 * consumer_privateRSAKey must be an RSA signing key and of type
 * java.security.PrivateKey, String, byte[] or InputStream. The key must either
 * PKCS#1 or PKCS#8 encoded.
 * 
 * A service provider that wishes to verify signatures made by such a consumer
 * does not need a shared secret with the consumer, but it needs to know the
 * consumer's public key. You create the necessary OAuthConsumer object (on the
 * service provider's side) like this:
 * 
 * OAuthConsumer c = new OAuthConsumer(callback_url, consumer_key, null,
 * provider); c.setProperty(RSA_SHA1.PUBLIC_KEY, consumer_publicRSAKey);
 * 
 * consumer_publicRSAKey must be the consumer's public RSAkey and of type
 * java.security.PublicKey, String, or byte[]. In the latter two cases, the key
 * must be X509-encoded (byte[]) or X509-encoded and then Base64-encoded
 * (String).
 * 
 * Alternatively, a service provider that wishes to verify signatures made by
 * such a consumer can use a X509 certificate containing the consumer's public
 * key. You create the necessary OAuthConsumer object (on the service provider's
 * side) like this:
 * 
 * OAuthConsumer c = new OAuthConsumer(callback_url, consumer_key, null,
 * provider); c.setProperty(RSA_SHA1.X509_CERTIFICATE, consumer_cert);
 * 
 * consumer_cert must be a X509 Certificate containing the consumer's public key
 * and be of type java.security.cert.X509Certificate, String, or byte[]. In the
 * latter two cases, the certificate must be DER-encoded (byte[]) or PEM-encoded
 * (String).
 * 
 * @author Dirk Balfanz
 * 
 */
public class RSA_SHA1 extends OAuthSignatureMethod {
	private PrivateKey privateKey = null;
	private PublicKey publicKey = null;

	@Override
	protected void initialize(OAuthConsumer consumer) throws OAuthException {
		super.initialize(consumer);
		try {
			InputStream stream = new ByteArrayInputStream(consumer.getSecret()
					.getBytes("UTF-8"));

			PEMReader reader = new PEMReader(stream);
			byte[] bytes = reader.getDerBytes();
			String marker = reader.getBeginMarker();

			KeySpec keySpec;
			if (PEMReader.PUBLIC_X509_MARKER.equals(reader.getBeginMarker())) {
				keySpec = new X509EncodedKeySpec(bytes);
				KeyFactory fac = KeyFactory.getInstance("RSA");
				publicKey = fac.generatePublic(keySpec);
			} else if (PEMReader.CERTIFICATE_X509_MARKER.equals(reader
					.getBeginMarker())) {
				CertificateFactory fac = CertificateFactory.getInstance("X509");
				ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				X509Certificate cert = (X509Certificate) fac
						.generateCertificate(in);
				publicKey = cert.getPublicKey();
			} else {
				if (PEMReader.PRIVATE_PKCS1_MARKER.equals(marker)) {
					keySpec = (new PKCS1EncodedKeySpec(bytes)).getKeySpec();
				} else if (PEMReader.PRIVATE_PKCS8_MARKER.equals(marker)) {
					keySpec = new PKCS8EncodedKeySpec(bytes);
				} else {
					throw new IOException("Invalid PEM file: Unknown marker "
							+ "for private key " + reader.getBeginMarker());
				}

				KeyFactory fac = KeyFactory.getInstance("RSA");
				privateKey = fac.generatePrivate(keySpec);
				publicKey = fac.generatePublic(keySpec);
			}
		} catch (GeneralSecurityException e) {
			throw new OAuthException(e);
		} catch (IOException e) {
			throw new OAuthException(e);
		}
	}

	@Override
	protected String getSignature(String baseString) throws OAuthException {
		try {
			byte[] signature = sign(baseString.getBytes(OAuth.ENCODING));
			return base64Encode(signature);
		} catch (UnsupportedEncodingException e) {
			throw new OAuthException(e);
		} catch (GeneralSecurityException e) {
			throw new OAuthException(e);
		}
	}

	@Override
	protected boolean isValid(String signature, String baseString)
			throws OAuthException {
		try {
			return verify(decodeBase64(signature), baseString
					.getBytes(OAuth.ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new OAuthException(e);
		} catch (GeneralSecurityException e) {
			throw new OAuthException(e);
		}
	}

	private byte[] sign(byte[] message) throws GeneralSecurityException {
		if (privateKey == null) {
			throw new IllegalStateException("need to set private key with "
					+ "OAuthConsumer.setProperty when "
					+ "generating RSA-SHA1 signatures.");
		}
		Signature signer = Signature.getInstance("SHA1withRSA");
		signer.initSign(privateKey);
		signer.update(message);
		return signer.sign();
	}

	private boolean verify(byte[] signature, byte[] message)
			throws GeneralSecurityException {
		if (publicKey == null) {
			throw new IllegalStateException("need to set public key with "
					+ " OAuthConsumer.setProperty when "
					+ "verifying RSA-SHA1 signatures.");
		}
		Signature verifier = Signature.getInstance("SHA1withRSA");
		verifier.initVerify(publicKey);
		verifier.update(message);
		return verifier.verify(signature);
	}
}
