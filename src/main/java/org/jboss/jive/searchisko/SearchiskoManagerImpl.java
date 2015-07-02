/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */

package org.jboss.jive.searchisko;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.jivesoftware.base.event.v2.EventListener;
import com.jivesoftware.community.lifecycle.ApplicationState;
import com.jivesoftware.community.lifecycle.ApplicationStateChangeEvent;

/**
 * Implementation of business logic based on separate thread (executor)
 *
 * @author Libor Krzyzanek
 */
public class SearchiskoManagerImpl implements SearchiskoManager, EventListener<ApplicationStateChangeEvent> {

	public static final String CFG_KEY_SEARCHISKO_URL = "jbossorg.searchisko.url";

	public static final String CFG_KEY_SEARCHISKO_REST_API = "jbossorg.searchisko.rest.api.update.profile";

	public static final String CFG_KEY_SEARCHISKO_NAME = "jbossorg.searchisko.auth.name";

	public static final String CFG_KEY_SEARCHISKO_PASSWORD = "jbossorg.searchisko.auth.password";

	/**
	 * Set of usernames that needs to be updated
	 */
	private Set<String> accountsToUpdate = Collections.synchronizedSet(new HashSet<String>());

	private SearchiskoUpdateAccountProfileExecutor executor1;

	@Override
	public void handle(ApplicationStateChangeEvent e) {
		if (e.getNewState().equals(ApplicationState.RUNNING)) {
			executor1 = new SearchiskoUpdateAccountProfileExecutor(
					accountsToUpdate, 1, 0);
			executor1.setContextClassLoader(this.getClass().getClassLoader());
			executor1.setDaemon(true);
			executor1.start();
		}
		if (e.getNewState().equals(ApplicationState.SHUTDOWN)) {
			executor1.stopExecution();
		}
	}

	@Override
	public void accountChanged(String username) {
		accountsToUpdate.add(username);
	}

}
