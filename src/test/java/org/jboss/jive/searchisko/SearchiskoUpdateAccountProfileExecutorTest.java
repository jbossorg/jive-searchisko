/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */

package org.jboss.jive.searchisko;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class SearchiskoUpdateAccountProfileExecutorTest {

	/**
	 * Test method to check how httpclient works
	 *
	 * @param args
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static void main(String[] args) throws IOException, GeneralSecurityException {
		Set<String> usernames = new HashSet<String>();
		usernames.add("username1");

		SearchiskoUpdateAccountProfileExecutor executor = new SearchiskoUpdateAccountProfileExecutor(usernames, 1, 0);

		String url = "https://dcp-jbossorgdev.rhcloud.com";
		String username = "jbossorg";
		String password = "";

		HttpClient client = SearchiskoUpdateAccountProfileExecutor.createDefaultClient(url, username, password);


		// http://stackoverflow.com/questions/23870899/sslprotocolexception-handshake-alert-unrecognized-name-client-side-code-worka
		System.setProperty("jsse.enableSNIExtension", "false");
		String response = executor.submitData(client, usernames, url, SearchiskoUpdateAccountProfileExecutor.REST_API_DEFAULT);
		System.out.println("Response: " + response);
	}

	@Test
	public void testGetJsonData() throws Exception {
		Set<String> usernames = new HashSet<String>();
		usernames.add("username1");

		JSONAssert.assertEquals("{  \"contributor_type_specific_code_type\" : \"jbossorg_username\", " +
						"\"contributor_type_specific_code_value\" : [\"username1\"] }",
				new String(SearchiskoUpdateAccountProfileExecutor.getJsonData(usernames)), false);


		usernames.add("username2");
		usernames.add("username3");

		JSONAssert.assertEquals("{  \"contributor_type_specific_code_type\" : \"jbossorg_username\", " +
						"\"contributor_type_specific_code_value\" : [\"username1\", \"username2\", \"username3\"] }",
				new String(SearchiskoUpdateAccountProfileExecutor.getJsonData(usernames)), false);
	}

	@Test(expected = RuntimeException.class)
	public void testSubmitData_noAccountsError() throws Exception {
		SearchiskoUpdateAccountProfileExecutor executor = new SearchiskoUpdateAccountProfileExecutor(null, 1, 0);
		executor.submitData(null, null, null, null);
	}

}