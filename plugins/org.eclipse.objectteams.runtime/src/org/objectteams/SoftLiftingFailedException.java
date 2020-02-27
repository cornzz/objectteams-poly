/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2011 GK Software AG
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.objectteams;

/**
 * This exception signals that lifting failed due to unresolved
 * binding ambiguity. This variant is used when static analysis
 * could not find reason for such failure just in case the class
 * files have changed since.
 */
public class SoftLiftingFailedException extends RuntimeException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Object base;
	private String roleType;

	/**
	 * @param base the object that should be lifted
	 * @param roleType the name of the role type for which
	 *                 lifting was attempted.
	 */
	public SoftLiftingFailedException(Object base, String roleType) {
		this.base = base;
		this.roleType = roleType;
	}

	@Override
	public String getMessage() {
		return "\nFailed to lift '" + base + "' of " + base.getClass()
				+ " to type '" + roleType
				+ "'\nPerhaps some class files have changed since the enclosing team has been compiler? (See OT/J definition para. 2.3.4(c)).";
	}
}
