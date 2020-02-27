/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * $Id: FakedBaseMessageSend.java 14480 2006-10-08 15:08:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Expression.DecapsulationState;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

/**
 * This visitor marks all relevant nodes as allowing baseclass decapsulation.
 *
 * @author stephan
 * @since 1.1.2
 */
public class BaseScopeMarker extends ASTVisitor {
	@Override
	public void endVisit(SingleTypeReference typeReference, BlockScope scope) {
		typeReference.setBaseclassDecapsulation(DecapsulationState.REPORTED);
	}
	@Override
	public void endVisit(SingleNameReference nameReference, BlockScope scope) {
		// marking even non-type references shouldn't hurt and helps SelectionOnSingleName
		nameReference.setBaseclassDecapsulation(DecapsulationState.REPORTED);
	}
	@Override
	public void endVisit(QualifiedNameReference nameReference, BlockScope scope) {
		// don't check for isType, because also non-type references could
		// report InvisibleReceiverType, which we must prevent.
		nameReference.setBaseclassDecapsulation(DecapsulationState.REPORTED);
	}
	@Override
	public void endVisit(QualifiedTypeReference typeReference, BlockScope scope) {
		typeReference.setBaseclassDecapsulation(DecapsulationState.REPORTED);
	}
}
