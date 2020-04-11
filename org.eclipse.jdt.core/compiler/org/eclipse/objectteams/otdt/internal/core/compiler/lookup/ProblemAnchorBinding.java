/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2009 Stephan Herrmann
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * $Id$
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.Scope;


/**
 * Represents a team anchor for which a problem has been detected but not
 * immediately been reported.
 *
 * @author stephan
 * @since 1.4.0
 */
public class ProblemAnchorBinding extends TeamAnchor {

	TeamAnchor closestMatch;
	int problemId;

	public ProblemAnchorBinding(TeamAnchor closestMatch, int problemId) {
		super();
		this.closestMatch = closestMatch;
		this.problemId = problemId;
	}

	@Override
	protected TeamAnchor getClone() {
		return new ProblemAnchorBinding(this.closestMatch, this.problemId);
	}

	@Override
	public int kind() {
		return this.closestMatch.kind();
	}

	@Override
	public char[] readableName() {
		return this.closestMatch.readableName();
	}

	@Override
	public char[] internalName() {
		return this.closestMatch.internalName();
	}

	@Override
	public boolean isBaseAnchor() {
		return this.closestMatch.isBaseAnchor();
	}

	@Override
	public boolean isFinal() {
		return this.closestMatch.isFinal();
	}

	@Override
	public int problemId() {
		return this.problemId;
	}

	/**
	 * Check if given anchor is a ProblemAnchorBinding and report error if suitable.
	 * @param scope for reporting
	 * @param location for reporting
	 * @param anchor given anchor to check
	 * @param typeName for reporting
	 * @return true if anchor is valid, false if it is null or a ProblemAnchorBinding.
	 */
	public static boolean checkAnchor(Scope scope, ASTNode location, ITeamAnchor anchor, char[] typeName) {
		if (anchor == null)
			return false; // assume problem is already reported
		if (!anchor.isValidBinding()) {
			switch (anchor.problemId()) {
			case ProblemReasons.AnchorNotFinal:
				scope.problemReporter().anchorPathNotFinal(location, anchor, typeName);
				break;
			case ProblemReasons.NotFound:
				break; // already reported from TypeAnchorReference.findVariable()
			default: scope.problemReporter().missingImplementation(location, "Unexpected type anchor problem "+anchor.problemId());
			}
			return false;
		}
		return true;
	}
}
