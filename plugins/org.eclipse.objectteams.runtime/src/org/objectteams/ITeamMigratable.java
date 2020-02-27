/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2008 Berlin Institute of Technology, Germany.
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
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.objectteams;

/**
 * Marker interface: if a role declares to implement this interface
 * the compiler will generate the method defined herein, and prepare
 * the role so that the migration will indeed be possible.
 * Note, that a migratable role does not obey the family guarantee.
 *
 * @author stephan
 * @since 1.2.5
 */
public interface ITeamMigratable {
	/**
	 * Migrate the current role to the otherTeam.
	 *
	 * @param otherTeam new team that should adopt this role
	 * @return the migrated (and re-typed) role (actually of type R<@otherTeam>).  FIXME(SH)
	 */
	<R> R migrateToTeam(final ITeam otherTeam);
}
