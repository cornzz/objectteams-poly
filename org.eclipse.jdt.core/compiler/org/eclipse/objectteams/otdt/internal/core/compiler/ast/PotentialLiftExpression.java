/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2014 Fraunhofer Gesellschaft, Munich, Germany,
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
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.DeclaredLifting;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.Lifting;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;


/**
 * NEW for OTDT.
 *
 * A wrapper for an expression that might or might not require lifting.
 *
 * Note: all elements generated in StandardElementGenerator.liftCall() must be
 * resolved manually, since PotentialLiftExpression must already resolve the expression.
 * So the wrapping expression can not use resolveType(..) again.
 *
 * @author stephan
 */
public class PotentialLiftExpression extends PotentialTranslationExpression {

    private Expression teamExpr;

    public boolean requireReverseOperation = false;

    // if a PotentialLiftExpression was created on behalf of a parameter mapping (callin),
    // we remember the responsible baseMethodSpec so that our analysis whether lifting is
    // indeed needed can be propagated via the MethodSpec to the CallinMethodMappingsAttribute.
    // Update (1.3.0M3): This is now done by a registered job (callback):
	private Runnable liftingConfirmJob;

	private TypeReference expectedTypeReference = null;

	/** Job to perform if analysis confirms that lifting is required. */
	public void onLiftingRequired(Runnable job) {
		this.liftingConfirmJob = job;
	}

    /**
     * Create a wrapper for an expression that might require lifting.
     *
     * @param teamExpr     expression suitable as call target for the lift call.
     * @param expression   expression to be wrapped
     * @param expectedType what the context expects from this expression
     */
	public PotentialLiftExpression(
			Expression  teamExpr,
			Expression  expression,
			TypeBinding expectedType)
	{
		super(expression, expectedType);
		this.operator = "lift"; //$NON-NLS-1$
		this.teamExpr = teamExpr;
	}

    /**
     * Create a wrapper for an expression that might require lifting.
     *
     * @param teamExpr        expression suitable as call target for the lift call.
     * @param expression      expression to be wrapped
     * @param expectedTypeRef what the context expects from this expression
     */
	public PotentialLiftExpression(
			Expression    teamExpr,
			Expression    expression,
			TypeReference expectedTypeRef)
	{
		super(expression, null);
		this.expectedTypeReference = expectedTypeRef;
		this.operator = "lift"; //$NON-NLS-1$
		this.teamExpr = teamExpr;
	}

	@Override
	public TypeBinding resolveType(BlockScope scope)
    {
    	this.constant = Constant.NotAConstant;
        TypeBinding providedType = this.expression.resolveType(scope);
        if (providedType == null)
        	return null; // no chance
        if (providedType.isParameterizedType())
        	providedType = providedType.erasure();

        if (this.expectedType == null)
        	this.expectedType = this.expectedTypeReference.resolvedType;
        this.checked = true;
        return internalResolveType(scope, providedType, false);
    }

	// in testOnly mode we avoid error reporting and AST generation, just signal nonnull if lifting is required
	private TypeBinding internalResolveType(BlockScope scope, TypeBinding providedType, boolean testOnly) {
		this.resolvedType = this.expectedType; // be optimistic.
        TypeBinding compatibleType = compatibleType(scope, providedType);
        if (compatibleType != null)
        	return testOnly ? null : compatibleType;

        TypeBinding roleSideType;
        TypeBinding baseType;
        if (providedType.isArrayType()) {
            if (!(this.expectedType instanceof ArrayBinding))
                throw new InternalCompilerError("mapping array to scalar"); //$NON-NLS-1$
            baseType = providedType.leafComponentType();
            roleSideType     = this.expectedType.leafComponentType();
        } else {
            baseType = providedType;
            roleSideType     = this.expectedType;
        }
        ReferenceBinding roleType = null;
        if (!(baseType instanceof ReferenceBinding)) {
        	return testOnly ? null : reportIncompatibility(scope, providedType);
        } else {
            roleType = (ReferenceBinding)roleSideType;
            if (   !roleType.isDirectRole()
                || !(baseType instanceof ReferenceBinding))
            	return testOnly ? null : reportIncompatibility(scope, providedType);
        }

        // reset Config, because below we want to check loweringRequired.
        Config oldConfig = Config.createOrResetConfig(this);
        try {
	        ReferenceBinding expectedBase = roleType.baseclass();
	        if(expectedBase != null && expectedBase.isParameterizedType())
	        	expectedBase = (ReferenceBinding) expectedBase.erasure();
			if (   expectedBase == null							// roleType is assigned unless incompatibilityFound, in which case we return above
	        	|| !baseType.isCompatibleWith(expectedBase))
	        {
	        	TypeBinding adjustedRole = TeamModel.getRoleToLiftTo(scope, baseType, roleType, true, this.expression);
	        	if (adjustedRole != null) {
	        		this.expectedType = adjustedRole;
	        	} else {
	        		if (providedType.isTypeVariable()) {
	        			ReferenceBinding roleBound = ((TypeVariableBinding)providedType).roletype;
	        			if (roleBound != null && roleBound.isRole()) {
	        				generateDynamicLiftCall(scope, roleBound);
	        				return this.resolvedType;
	        			}
	        		}

		            scope.problemReporter().typeMismatchErrorPotentialLift(
		                    this.expression, providedType, this.expectedType, baseType);
		            this.resolvedType = null;
		            return null;
	        	}
	        }
	        if (   roleType.isHierarchyInconsistent()
	        	|| roleType.roleModel.hasBaseclassProblem())
	        {
	        	if (testOnly) return null;
	        	// don't install unresolvable liftcall
	        	scope.problemReporter().referenceContext.tagAsHavingErrors();
	        	return this.resolvedType;
	        }
	        if (Config.getLoweringRequired())
	            throw new InternalCompilerError("Lifting would also require lowering!"); //$NON-NLS-1$
        } finally {
        	Config.removeOrRestore(oldConfig, this);
        }
        if (testOnly) return this.expectedType;

        TypeBinding expectedBaseclass = ((ReferenceBinding)this.expectedType.leafComponentType()).baseclass();
        if (this.expectedType.isArrayType())
        	expectedBaseclass = new ArrayBinding(expectedBaseclass, this.expectedType.dimensions(), scope.environment());
		// further conversions (cast for generic)?
		checkOtherConversions(scope, expectedBaseclass, providedType);

        // successfully recognized the need for lifting, create the lift-call now:

		// propagate the need for translation:
		if (this.liftingConfirmJob != null)
			this.liftingConfirmJob.run();

        this.rawExpression = this.expression;
        this.operator = "lift"; // redundant; //$NON-NLS-1$
        this.expression = genLiftCall(scope, providedType);
        return this.resolvedType;
    }

	private MessageSend genLiftCall(BlockScope scope, TypeBinding providedType) {
        MessageSend liftCall = Lifting.liftCall(scope, this.teamExpr, this.expression, providedType, this.expectedType, this.requireReverseOperation);
        liftCall.actualReceiverType = this.teamExpr.resolveType(scope);
        if (this.teamExpr.resolvedType instanceof ReferenceBinding) // i.e. not-null
        	liftCall.binding = ((ReferenceBinding)this.teamExpr.resolvedType).getMethod(scope, liftCall.selector);
        if (liftCall.binding == null) // can't process (analyze,generate) if lift method is missing
        {
        	if (liftCall.actualReceiverType != null && TeamModel.hasRoFiCache((ReferenceBinding)liftCall.actualReceiverType))
        		scope.problemReporter().unresolvedLifting(this, providedType, this.expectedType);
        	else
        		scope.problemReporter().referenceContext.tagAsHavingErrors();
        }
        liftCall.constant = Constant.NotAConstant;
        liftCall.resolvedType =
      	    this.resolvedType = RoleTypeCreator.maybeWrapUnqualifiedRoleType(this.expectedType, scope, this);
        liftCall.argumentTypes = new TypeBinding[] { providedType };
		return liftCall;
	}

	/* Translating parameterized declared lifting requires delegation to lift_dynamic method: */
	private void generateDynamicLiftCall(BlockScope scope, ReferenceBinding roleType) {
		// lift_dynamic method has already been generated from DeclaredLifting.transformMethodWithDeclaredLifting

		AstGenerator gen = new AstGenerator(this.expression);

        this.rawExpression = this.expression;
		Expression[] args = new Expression[] { this.expression };
		this.expression = gen.messageSend(this.teamExpr, DeclaredLifting.dynamicLiftSelector(roleType), args);
		this.resolvedType = this.expression.resolveType(scope); // or resolve bits individually ? (expression is already resolved?)
	}

	public static boolean isLiftingRequired(BlockScope scope, TypeBinding requiredType, TypeBinding providedType, Expression location) {
		PotentialLiftExpression ple = new PotentialLiftExpression(null, location, requiredType);
		return ple.internalResolveType(scope, providedType, true) != null;
	}
}
