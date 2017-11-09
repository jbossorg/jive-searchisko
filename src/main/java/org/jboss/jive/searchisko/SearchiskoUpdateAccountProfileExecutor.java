/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.jive.searchisko;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.jivesoftware.community.JiveGlobals;

/**
 * Executor for notifying Searchisko about changed usernames
 *
 * @author Libor Krzyzanek
 */
public class SearchiskoUpdateAccountProfileExecutor extends Thread {

	private static final Logger log = LogManager.getLogger(SearchiskoUpdateAccountProfileExecutor.class);

	private Set<String> accountsToUpdate;

	private boolean running = false;

	private int startOffsetInMs;

	public static final String CFG_KEY_SEARCHISKO_PROFILE_NOTIFY_ENABLED = "jbossorg.searchisko.profile.notify.enabled";

	public static final String CFG_KEY_SEARCHISKO_PROFILE_NOTIFY_INTERVAL = "jbossorg.searchisko.profile.notify.interval";

	protected static final String REST_API_DEFAULT = "/v2/rest/tasks/task/update_contributor_profile";


	public SearchiskoUpdateAccountProfileExecutor(Set<String> accountsToUpdate, int executorNumber,
												  int startOffsetInMs) {
		super("SearchiskoUpdateAccountProfileExecutor-" + executorNumber);
		this.accountsToUpdate = accountsToUpdate;
		this.startOffsetInMs = startOffsetInMs;
	}

	public static boolean isEnabled() {
		return JiveGlobals.getJiveBooleanProperty(CFG_KEY_SEARCHISKO_PROFILE_NOTIFY_ENABLED, true);
	}

	@Override
	public void run() {
		if (log.isInfoEnabled()) {
			log.info("Start execution with start Offset: " + startOffsetInMs + "ms");
		}
		if (startOffsetInMs > 0) {
			try {
				Thread.sleep(startOffsetInMs + (10 * 1000)); // offset + 10sec
			} catch (InterruptedException e1) {
				log.error("Interrupted. Quitting");
				return;
			}
		}

		running = true;

		while (running) {
			// Get interval in seconds. Default is 10 sec.
			final int intervalInSec = JiveGlobals.getJiveIntProperty(CFG_KEY_SEARCHISKO_PROFILE_NOTIFY_INTERVAL, 10);
			try {
				Thread.sleep(intervalInSec * 1000);
			} catch (InterruptedException e) {
				log.error("Interrupted. Quitting");
				running = false;
				break;
			}

			try {
				if (isEnabled()) {
					if (accountsToUpdate.size() > 0) {
						String searchiskoUrl = JiveGlobals.getJiveProperty(SearchiskoManagerImpl.CFG_KEY_SEARCHISKO_URL);
						String searchiskoRestApi = JiveGlobals.getJiveProperty(SearchiskoManagerImpl.CFG_KEY_SEARCHISKO_REST_API, REST_API_DEFAULT);
						String searchiskoName = JiveGlobals.getJiveProperty(SearchiskoManagerImpl.CFG_KEY_SEARCHISKO_NAME);
						String searchiskoPwd = JiveGlobals.getJiveProperty(SearchiskoManagerImpl.CFG_KEY_SEARCHISKO_PASSWORD);

						if (StringUtils.isBlank(searchiskoUrl)) {
							log.error("Configuration problem. Searchisko URL cannot be null");
							JiveGlobals.setJiveProperty(CFG_KEY_SEARCHISKO_PROFILE_NOTIFY_ENABLED, false);
						} else {
							HttpClient client = createDefaultClient(searchiskoUrl, searchiskoName, searchiskoPwd);

							submitData(client, accountsToUpdate, searchiskoUrl, searchiskoRestApi);
						}
					} else {
						log.trace("Nothing in the searchisko queue");
					}
				} else {
					log.debug("Searchisko notification is disabled");
				}
			} catch (Exception e) {
				log.error("Cannot update User in Searchisko.", e);
			}
		}
	}

	protected static HttpClient createDefaultClient(String searchiskoUrl, String searchiskoName, String searchiskoPwd) throws IOException, GeneralSecurityException {
		HttpClient client = new HttpClient();

		if (searchiskoName != null && searchiskoPwd != null) {
			client.getParams().setAuthenticationPreemptive(true);
			Credentials credentials = new UsernamePasswordCredentials(searchiskoName, searchiskoPwd);
			client.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM), credentials);
		}

//		It's assumed that SSL layer is configured properly and there is no need to use dummy ssl socket factory

//		if (searchiskoUrl.startsWith("https://")) {
//			String host = searchiskoUrl.substring(8);
//			Protocol myhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
//			client.getHostConfiguration().setHost(host, 443, myhttps);
//		}

		return client;
	}

	/**
	 * Submit data to searchisko
	 *
	 * @param accountsToUpdate set of usernames. Cannot be null or emtpy set
	 * @param searchiskoUrl
	 * @return searchisko result
	 * @throws IOException
	 */
	protected String submitData(HttpClient client, Set<String> accountsToUpdate, String searchiskoUrl, String searchiskoRestApi) throws IOException {
		log.info("Submit data to Searchisko");
		if (log.isDebugEnabled()) {
			log.debug("Accounts: " + accountsToUpdate);
		}

		if (accountsToUpdate == null || accountsToUpdate.size() == 0) {
			throw new RuntimeException("Cannot notify searchisko without usernames");
		}

		byte[] json = getJsonData(accountsToUpdate);

		PostMethod method = new PostMethod(searchiskoUrl + searchiskoRestApi);

		// no retry
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(0, false));
		// ignore cookies
		method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

		try {
			method.setRequestEntity(new ByteArrayRequestEntity(json, "application/json; charset=UTF-8"));

			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				if (statusCode == HttpStatus.SC_FORBIDDEN) {
					throw new IOException("Bad Credentials to access Searchisko");
				}
				throw new IOException("Cannot notify Searchisko. Response: " + method.getStatusLine());
			}

			// Read the response body.
			String response = method.getResponseBodyAsString(100);

			if (response.contains("id")) {
				if (log.isInfoEnabled()) {
					log.info("All accounts notified in searchisko. Response" + response);
				}
				accountsToUpdate.clear();
			}
			return response;
		} catch (HttpException e) {
			log.error("Fatal protocol violation during searchisko post", e);
			throw e;
		} catch (IOException e) {
			log.error("Fatal transport error", e);
			throw e;
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
	}

	protected static byte[] getJsonData(Set<String> accountsToUpdate) throws IOException {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("contributor_type_specific_code_type", "jbossorg_username");
		data.put("contributor_type_specific_code_value", accountsToUpdate);

		return new ObjectMapper().writeValueAsBytes(data);
	}

	public void stopExecution() {
		running = false;
	}


}
