/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */

package org.jboss.jive.searchisko.listener;

import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.event.UserEvent;
import com.jivesoftware.base.event.v2.InlineEventListener;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.jive.searchisko.SearchiskoManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Listener catch all UserEvent changes (first/last name etc.) and notify Searchisko about it
 *
 * @author Libor Krzyzanek
 */
public class NotifySearchiskoOnUserChangeListener implements InlineEventListener<UserEvent> {

	private static final Logger log = LogManager.getLogger(NotifySearchiskoOnUserChangeListener.class);

	@Autowired
	private SearchiskoManager searchiskoManager;

	@Autowired
	private UserManager userManager;

	@Override
	public void handle(UserEvent e) {
		if (UserEvent.Type.MODIFIED.compareTo(e.getType()) == 0) {
			if (log.isTraceEnabled()) {
				log.trace("User change event: " + e);
				log.trace("params: " + e.getParams());
				log.trace("user: " + e.getPayload());
			}
		}
		long userId = e.getPayload().getID();

		// First or Last name change or email
		if (e.getParams().get(UserEvent.USER_MODIFIED_NAME_FIRST) != null
				|| e.getParams().get(UserEvent.USER_MODIFIED_NAME_LAST) != null
				|| e.getParams().get(UserEvent.USER_MODIFIED_EMAIL) != null) {

			log.debug("First/last name or email changed.");

			try {
				String username = userManager.getUser(userId).getUsername();
				searchiskoManager.accountChanged(username);
			} catch (UserNotFoundException e1) {
				log.error("Cannot find username with id: " + userId);
				return;
			}
		}
	}

}