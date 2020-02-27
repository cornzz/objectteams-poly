/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2005, 2006 Fraunhofer Gesellschaft, Munich, Germany,
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
 * $Id: SwitchOnBaseTypeGenerator.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions.WeavingScheme;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.WeakenedTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.Sorting;

/**
 * Creates an instanceof cascade as needed for lifting and for base predicate checks.
 *
 * @author stephan
 */
public abstract class SwitchOnBaseTypeGenerator implements IOTConstants {

    /**
     * Create the statement for one base type in the big cascade.
     *
	 * @param role       the most suitable role for the detected base type.
	 * @param gen        use for AST-generation
	 * @return           the generated statement or null
	 */
	protected abstract Statement createCaseStatement(RoleModel role, AstGenerator gen);

	/**
	 * Create a statement for handling an ambiguous base class.
	 */
	protected abstract Statement createStatementForAmbiguousBase(AstGenerator gen);

	/**
     * Hook into createSwitchStatement(), which should create a default branch, if needed.
     *
	 * @param staticRoleType expected role type.
	 * @param problemId 	 signal if lifting may fail at runtime
	 * @param gen            use for AST-generation
	 * @return           the generated statement or null
	 */
	protected abstract Statement createDefaultStatement(ReferenceBinding staticRoleType, int problemId, AstGenerator gen);

	/**
	 * Create the instanceof cascade based on a given base object.
	 * Note that the previous two methods are hooks which should create the actual
	 * statements for the cascade template.
	 *
	 * @param teamType       the team context
	 * @param staticRoleType we know we are about to lift to this role type or better
	 * @param caseObjects    one role model for each bound and relevant subtype of staticRoleType
	 * @param gen            use for AST-generation
	 * @return the assembled statement
	 */
	protected Statement createSwitchStatement(
			ReferenceBinding teamType,
			ReferenceBinding staticRoleType,
			RoleModel[]      caseObjects,
			int 			 problemId,
			AstGenerator     gen)
	{
		boolean hasBindingAmbiguity = (teamType.getTeamModel().canLiftingFail(staticRoleType) == IProblem.CallinDespiteBindingAmbiguity);
		if (caseObjects.length == 1 && !TeamModel.hasTagBit(teamType, TeamModel.HasAbstractRelevantRole) && !hasBindingAmbiguity) {
			// avoid instanceof alltogether.
			return createCaseStatement(caseObjects[0], gen);
		}
		ReferenceBinding staticBaseType = staticRoleType.baseclass();

		RoleModel[] rolesToSort = new RoleModel[caseObjects.length];
		System.arraycopy(caseObjects, 0, rolesToSort, 0, caseObjects.length);
		caseObjects = Sorting.sortRoles(rolesToSort);
		
		Statement[] stmts = new Statement[2];
	    Expression baseArg = gen.singleNameReference(baseVarName());
	    ReferenceBinding castType = null;
	    if (staticRoleType.baseclass() instanceof WeakenedTypeBinding)
	    	castType = ((WeakenedTypeBinding)staticRoleType.baseclass()).getStrongType();
    	else if (hasBindingAmbiguity || caseObjects[0].getWeavingScheme() == WeavingScheme.OTDRE)
    		castType = staticBaseType;
	    if (castType != null)
	    	baseArg = gen.castExpression(baseArg, gen.baseTypeReference(castType), CastExpression.RAW);
	    char[] LOCAL_BASE_NAME = "_OT$local$base".toCharArray(); //$NON-NLS-1$
		stmts[0] = gen.localVariable(LOCAL_BASE_NAME, gen.baseclassReference(staticBaseType), baseArg);
		
		IfStatement prevIf = null;

	    /* 
	     *   if (_OT$local$base instanceof MySubBaseA)
	     *       <action for MySubRoleA playedBy MySubBaseA>
	     *   else if (_OT$local$base instanceof MySubBaseB)
	     *       <action for MySubRoleB playedBy MySubBaseB>
	     *   ...
		 * This relies on most specific types to be handled first
	     */
	    for (int idx = caseObjects.length-1; idx >= 0; idx--) {
	        RoleModel object = caseObjects[idx];
	        if (object.hasBaseclassProblem())
	        	continue;

	        Statement s = (teamType.getTeamModel().isAmbiguousLifting(staticRoleType, object.getBaseTypeBinding()))
	        				? createStatementForAmbiguousBase(gen)
	        				: createCaseStatement(object, gen);
	        if (object.getBaseTypeBinding().equals(staticBaseType) && idx == 0 && !hasBindingAmbiguity) {
	        	// shortcut if last type matches the static type: no need for a final instanceof check
	        	if (prevIf == null)
	        		stmts[1] = s;
	        	else
	        		prevIf.elseStatement = s;
	        	return gen.block(stmts); // don't generate default.
	        } else {
		        if (s != null) {
			        Expression condition = gen.instanceOfExpression(gen.singleNameReference(LOCAL_BASE_NAME),
			        												gen.baseclassReference(object.getBaseTypeBinding(), true/*erase*/));
					IfStatement is = gen.ifStatement(condition, s);
			        if (prevIf == null)
			        	stmts[1] = is;				// this is the root "if"
			        else
			        	prevIf.elseStatement = is;	// hook into existing "if"
			        prevIf = is;
		        }
	        }
	    }
	    /*
	     * ...
	     * else
	     * 	<default action>
	     */
	    prevIf.elseStatement = createDefaultStatement(staticRoleType, problemId, gen); // prevIf should be set after at least one iteration into the else-branch

	    return gen.block(stmts);
	}

	/** What name should be used to address the base object? */
	char[] baseVarName() {
		return BASE; // default: "base" (predicate method argument)
	}
}
