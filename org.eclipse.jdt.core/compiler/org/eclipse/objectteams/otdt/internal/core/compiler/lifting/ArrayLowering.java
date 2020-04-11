/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
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
 * $Id: ArrayLowering.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lifting;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;


/**
 * moved here from org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.
 *
 * @author stephan
 * @version $Id: ArrayLowering.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class ArrayLowering extends ArrayTranslations {

	public ArrayLowering(Expression teamExpression) {
		this._teamExpr = teamExpression;
	}

	/** API for Lowering. */
	Expression lowerArray(
			BlockScope  scope,
			Expression  expression,
			TypeBinding providedType,
			TypeBinding requiredType,
			boolean    deferredResolve)
	{
		// TODO (SH): check if we need to use the team anchor of a RoleTypeBinding
		//            as receiver for the translation call.
		ReferenceBinding teamBinding = ((ReferenceBinding)providedType.leafComponentType()).enclosingType();
		if (this._teamExpr == null)
			this._teamExpr = new AstGenerator(expression).qualifiedThisReference(teamBinding);
		if (!deferredResolve)
			this._teamExpr.resolveType(scope);
		return translateArray(scope, expression, providedType, requiredType, /*isLifting*/false, deferredResolve);
	}

	@Override
	public MethodBinding ensureTransformMethod(BlockScope scope, Expression teamExpr, TypeBinding providedType, TypeBinding requiredType, boolean isLifting) {
		TypeBinding providedLeaf = providedType.leafComponentType();
		TypeBinding matchingBase = ((ReferenceBinding)providedLeaf).baseclass();
		TypeBinding requiredLeaf = requiredType.leafComponentType();
		if (TypeBinding.notEquals(matchingBase, requiredLeaf) && matchingBase.isCompatibleWith(requiredLeaf, scope)) {
			requiredType = scope.environment().createArrayType(matchingBase, requiredType.dimensions());
		}
		return super.ensureTransformMethod(scope, teamExpr, providedType, requiredType, isLifting);
	}

	/* implement hook. */
	@Override
	Expression translation(Expression rhs, TypeBinding providedType, TypeBinding requiredType, AstGenerator gen) {
		return new Lowering().lowerExpression(this._scope, rhs, providedType, requiredType, this._teamExpr, false, true);
	}
}
