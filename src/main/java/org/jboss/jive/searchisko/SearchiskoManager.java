/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */

package org.jboss.jive.searchisko;

/**
 * Business logic for Searchisko integration
 *
 * @author Libor Krzyzanek
 */
public interface SearchiskoManager {

	/**
	 * Notify business logic about account change
	 *
	 * @param username
	 */
	public void accountChanged(String username);

}
