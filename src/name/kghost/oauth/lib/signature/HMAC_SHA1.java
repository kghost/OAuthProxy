/*
 * Copyright 2007 Netflix, Inc.
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

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import name.kghost.oauth.lib.OAuth;
import name.kghost.oauth.lib.OAuthException;

/**
 * The HMAC-SHA1 signature method.
 * 
 * @author John Kristian
 */
class HMAC_SHA1 extends OAuthSignatureMethod {

	@Override
	protected String getSignature(String baseString) throws OAuthException {
		try {
			return base64Encode(computeSignature(baseString));
		} catch (GeneralSecurityException e) {
			throw new OAuthException(e);
		} catch (UnsupportedEncodingException e) {
			throw new OAuthException(e);
		}
	}

	@Override
	protected boolean isValid(String signature, String baseString)
			throws OAuthException {
		try {
			byte[] expected = computeSignature(baseString);
			byte[] actual = decodeBase64(signature);
			return equals(expected, actual);
		} catch (GeneralSecurityException e) {
			throw new OAuthException(e);
		} catch (UnsupportedEncodingException e) {
			throw new OAuthException(e);
		}
	}

	private byte[] computeSignature(String baseString)
			throws GeneralSecurityException, UnsupportedEncodingException {
		SecretKey tmp = null;
		synchronized (this) {
			if (this.key == null) {
				String keyString = OAuth.percentEncode(getConsumerSecret())
						+ '&' + OAuth.percentEncode(getTokenSecret());
				Logger.getLogger("OAuth").info("Key :" + keyString);
				byte[] keyBytes = keyString.getBytes(ENCODING);
				this.key = new SecretKeySpec(keyBytes, MAC_NAME);
			}
			tmp = this.key;
		}
		Mac mac = Mac.getInstance(MAC_NAME);
		mac.init(tmp);
		byte[] text = baseString.getBytes(ENCODING);
		return mac.doFinal(text);
	}

	/** ISO-8859-1 or US-ASCII would work, too. */
	private static final String ENCODING = OAuth.ENCODING;

	private static final String MAC_NAME = "HmacSHA1";

	private SecretKey key = null;

	@Override
	public void setConsumerSecret(String consumerSecret) {
		synchronized (this) {
			key = null;
		}
		super.setConsumerSecret(consumerSecret);
	}

	@Override
	public void setTokenSecret(String tokenSecret) {
		synchronized (this) {
			key = null;
		}
		super.setTokenSecret(tokenSecret);
	}

}
