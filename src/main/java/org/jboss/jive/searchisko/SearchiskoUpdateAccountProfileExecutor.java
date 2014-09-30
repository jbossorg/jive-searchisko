/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.jive.searchisko;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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

	private static final String METHOD_POST = "POST";

	public static final String CHARSET_UTF8 = "UTF-8";

	private static final String REST_API = "/v1/rest/tasks/task/update_contributor_profile";


	public SearchiskoUpdateAccountProfileExecutor(Set<String> accountsToUpdate, int executorNumber,
												  int startOffsetInMs) {
		super("SearchiskoUpdateAccountProfileExecutor-" + executorNumber);
		this.accountsToUpdate = accountsToUpdate;
		this.startOffsetInMs = startOffsetInMs;
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
		final int intervalInSec = 5;

		while (running) {
			try {
				Thread.sleep(intervalInSec * 1000);
			} catch (InterruptedException e) {
				log.error("Interrupted. Quitting");
				running = false;
				break;
			}

			try {
				if (accountsToUpdate.size() > 0) {
					submitForm(accountsToUpdate);
				} else {
					log.trace("Nothing in the searchisko queue");
				}
			} catch (Exception e) {
				log.error("Cannot update User in Searchisko.", e);
			}
		}
	}

	protected void submitForm(Set<String> accountsToUpdate) throws IOException {
		log.info("Submit data to Searchisko");
		if (log.isDebugEnabled()) {
			log.debug("Accounts: " + accountsToUpdate);
		}
	}

	public void stopExecution() {
		running = false;
	}


}
