/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2003-2009 Berlin Institute of Technology, Germany.
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
 *  This exception is used by the language implementation
 *  to signal a failed lifting due to a guard predicate that evaluated to false.
 *  @author Stephan Herrmann
 */
public class LiftingVetoException extends RuntimeException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	ITeam aTeam = null;
	Object base = null;

	public LiftingVetoException(ITeam aTeam, Object base) {
		this.aTeam = aTeam;
		this.base = base;
	}

    public LiftingVetoException() {
		super("");
    }

	@Override
	public String toString() {
		return "Team " + aTeam + " refuses to lift " + base
				+ "\n(this exception should not be seen in applications).";
	}
}
