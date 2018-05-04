/*******************************************************************************
 * Copyright (c) 2018 Bosch Software Innovations GmbH and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Bosch Software Innovations - initial creation
 ******************************************************************************/
package org.eclipse.californium.scandium.auth;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import org.eclipse.californium.elements.auth.PreSharedKeyIdentity;
import org.eclipse.californium.elements.auth.RawPublicKeyIdentity;
import org.eclipse.californium.elements.util.DatagramReader;
import org.eclipse.californium.elements.util.DatagramWriter;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Verifies behavior of {@link PrincipalSerializer}.
 *
 */
public class PrincipalSerializerTest {

	private static PublicKey publicKey;

	/**
	 * Creates a public key to be used in test cases.
	 */
	@BeforeClass
	public static void init() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			KeyPair keyPair = generator.generateKeyPair();
			publicKey = keyPair.getPublic();
		} catch (NoSuchAlgorithmException e) {
			// every VM is required to support RSA
		}
	}

	/**
	 * Verifies that a public key that has been serialized using the
	 * serialize method can be re-instantiated properly using the deserialize
	 * method.
	 * 
	 * @throws GeneralSecurityException if the key cannot be deserialized.
	 */
	@Test
	public void testSerializedRPKCanBeDeserialized() throws GeneralSecurityException {

		RawPublicKeyIdentity rpkIdentity = new RawPublicKeyIdentity(publicKey);

		// WHEN serializing the raw public key identity to a byte array
		DatagramWriter writer = new DatagramWriter();
		PrincipalSerializer.serialize(rpkIdentity, writer);

		// THEN the resulting byte array can be used to re-instantiate
		// the public key
		RawPublicKeyIdentity identity = (RawPublicKeyIdentity) PrincipalSerializer.deserialize(new DatagramReader(writer.toByteArray()));
		assertThat(identity.getKey(), is(publicKey));
		assertThat(identity.getKey().getAlgorithm(), is(publicKey.getAlgorithm()));
	}

	/**
	 * Verifies that a pre-shared key identity that has been serialized using the
	 * serialize method can be re-instantiated properly using the deserialize
	 * method.
	 */
	@Test
	public void testSerializedPSKIdentityCanBeDeserialized() {

		testSerializedPSKIdentityCanBeDeserialized(new PreSharedKeyIdentity("iot.eclipse.org", "acme"));
	}

	/**
	 * Verifies that a pre-shared key identity without a virtual host that has been
	 * serialized using the serialize method can be re-instantiated properly using
	 * the deserialize method.
	 */
	@Test
	public void testSerializedPSKIdentityWithoutHostCanBeDeserialized() {

		testSerializedPSKIdentityCanBeDeserialized(new PreSharedKeyIdentity("acme"));
	}

	private static void testSerializedPSKIdentityCanBeDeserialized(PreSharedKeyIdentity pskIdentity) {

		try {
			// WHEN serializing the identity to a byte array
			DatagramWriter writer = new DatagramWriter();
			PrincipalSerializer.serialize(pskIdentity, writer);

			// THEN the resulting byte array can be used to re-instantiate
			// the identity
			PreSharedKeyIdentity identity = (PreSharedKeyIdentity) PrincipalSerializer.deserialize(new DatagramReader(writer.toByteArray()));
			assertThat(identity, is(pskIdentity));
		} catch (GeneralSecurityException e) {
			// should not happen
			fail(e.getMessage());
		}
	}

}
