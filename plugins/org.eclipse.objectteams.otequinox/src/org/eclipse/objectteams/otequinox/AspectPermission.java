/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009, 2014 Technical University Berlin, Germany.
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
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox;

/**
 * Possible values while negotiating aspect access (aspectBinding and forcedExport).
 * Note that order is relevant in this enum: higher index means higher priority.
 * 
 * @author stephan
 * @since 1.2.6
 */
public enum AspectPermission {
	/** Not influencing negotiation between other parties. */
	UNDEFINED,
	/** A permission is granted unless someone else denies it. */
	GRANT, 
	/** A permission is explicitly denied. Cannot be overridden. */
	DENY;
}
