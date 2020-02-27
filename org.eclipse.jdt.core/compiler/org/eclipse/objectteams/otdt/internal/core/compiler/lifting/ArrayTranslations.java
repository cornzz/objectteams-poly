/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2012 Fraunhofer Gesellschaft, Munich, Germany,
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
 * $Id: ArrayTranslations.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lifting;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.Expression.DecapsulationState;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstConverter;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator.IRunInScope;

/**
 * This class handles the common part of array lifting and lowering.
 *
 * @author stephan
 * @version $Id: ArrayTranslations.java 23416 2010-02-03 19:59:31Z stephan $
 */
public abstract class ArrayTranslations {

	private static final char[] ROLE_ARRAY_ARG = "_OT$roleArray".toCharArray(); //$NON-NLS-1$
	BlockScope _scope;
	TypeBinding _providedType;
	TypeBinding _requiredType;
	Expression _teamExpr;
	Expression _expression;
	boolean     _isLifting;

	/**
	 * This method handles lowering of arrays of roles by generating an
	 * extra method that returns the lowered array.
	 *
	 * The following example shows code that will be generated
	 * for a threedimensional Role-type array:
	 *
	 * void myCallout(Role[][][] r)
	 * {
	 *   __OT$base.myBaseMethod(transformArray(r));
	 * }
	 *
	 * Base[][][] transformArray(Role[][][] role)
	 * {
	 *   Base[][][] result;
	 *
	 *   result = new Base[role.length][][];
	 *   for(;i0 < role.length;i0++){
	 * 	    result[i0] = new Base[role[i0].length][];
	 * 	    for(i1 < role[i0].length;){
	 *   		result[i0][i1] = new Base[role[i0][i1].length];
	 * 	     	for(i2 < role[i0][i1].length;){
	 * 	    		result[i0][i1][i2] = translate(role[i0][i1][i2]);
	 * 			}
	 * 	 	}
	 * 	 }
	 *   return result;
	 * }
	 * @param scope
	 * @param expression wrap this expression with the desired translation
	 * @param providedType given type of expression
	 * @param requiredType this should be produced by translation
	 * @param isLifting is a lifting translation required (else == lowering)
	 * @param deferredResolve if true we set up a special message send for custom resolving
	 * @return a message send to the appropriate translation method.
	 */
	Expression translateArray(BlockScope scope, Expression expression,
							TypeBinding providedType, TypeBinding requiredType,
							boolean isLifting, boolean deferredResolve)
	{
		this._scope      = scope;
		this._expression = expression;

		MethodBinding methodBinding = ensureTransformMethod(
										scope, this._teamExpr, providedType, requiredType, isLifting);

        // if expression is resolved but teamExpression is unresolved schedule special resolving:
        IRunInScope hook = null;
        if (!isLifting && deferredResolve && expression.resolvedType != null && this._teamExpr.resolvedType == null) {
        	hook = new IRunInScope() { @Override
			public void run(BlockScope blockScope) {
        		// resolving this expression was deferred:
        		ArrayTranslations.this._teamExpr.resolve(blockScope);
        	}};
        }

		AstGenerator gen = new AstGenerator(expression.sourceStart, expression.sourceEnd);
		MessageSend send = (hook != null)
			? gen.messageSendWithResolveHook(
				this._teamExpr,
				methodBinding,
				new Expression[] {expression},
				hook)
			: gen.messageSend(
				this._teamExpr,
				methodBinding.selector,
				new Expression[] {expression});

		if (!deferredResolve && expression.resolvedType != null) {
			// manual resolving since expression is already resolved:
			send.constant = Constant.NotAConstant;
	        send.binding = methodBinding;
		    send.actualReceiverType = this._teamExpr.resolveType(scope);
		    send.argumentTypes = new TypeBinding[] { providedType };
		    send.resolvedType   = methodBinding.returnType;
		}
	    return send;
	}

	public MethodBinding ensureTransformMethod(
							BlockScope  scope,
							Expression  teamExpr,
							TypeBinding providedType,
							TypeBinding requiredType,
							boolean     isLifting)
	{
		this._providedType = providedType;
		this._requiredType = requiredType;
		this._isLifting    = isLifting;
		this._teamExpr     = teamExpr;
		this._scope        = scope;

		ReferenceBinding roleType = isLifting ?
				  ((ReferenceBinding)requiredType.leafComponentType())
				: ((ReferenceBinding)providedType.leafComponentType());

		char[] transformMethodName = getTransformMethodName(roleType, providedType.dimensions(), isLifting);

	    ReferenceBinding enclosingTeam = roleType.enclosingType().getRealType();
		MethodBinding[] transformMethods = enclosingTeam.getMethods(transformMethodName);
	    MethodBinding methodBinding = null;
	    if ((transformMethods != null) && (transformMethods.length != 0)) {
	        if (transformMethods.length > 1) {
	            throw new InternalCompilerError("duplicate transform methods generated");                //$NON-NLS-1$
	        }
	        methodBinding = transformMethods[0];
	    } else {
	    	TeamModel teamModel = roleType.roleModel.getTeamModel();
			TypeDeclaration teamDecl = teamModel.getAst();
	    	if (teamDecl == null)
				throw new InternalCompilerError(
						"need to create transform method, but have no source type: "+new String(teamModel.getBinding().readableName())); //$NON-NLS-1$

	        MethodDeclaration transformMethod =
	                generateTransformArrayMethod(teamDecl, transformMethodName, providedType.dimensions());
	        if (teamDecl.isRole())
	        	transformMethod.modifiers |= ClassFileConstants.AccPublic;
	        AstEdit.addMethod(teamDecl, transformMethod);
	        methodBinding = transformMethod.binding;
	        if (teamDecl.isRole()) {
	        	// if team is a role, also generate a ifc-part for the method and use that
	        	TypeDeclaration ifcPart = teamDecl.getRoleModel().getInterfaceAst();
	        	MethodDeclaration ifcMethod = AstConverter.genRoleIfcMethod(ifcPart, transformMethod);
	        	AstEdit.addMethod(ifcPart, ifcMethod);
	        	methodBinding = ifcMethod.binding;
	        }
	    }
		return methodBinding;
	}

	public static char[] getTransformMethodName(ReferenceBinding roleType, int dimensions, boolean isLifting) {
		return
			CharOperation.concat(
	                CharOperation.concat(
	                    isLifting ? IOTConstants._OT_LIFT_TO : IOTConstants.OT_TRANSFORM_ARRAY,
	                    roleType.sourceName()),
	                CharOperation.concat(
	                    IOTConstants.OT_DOLLAR_NAME,
	                    (""+dimensions).toCharArray())); //$NON-NLS-1$
	}
	// === the following methods mark ast nodes as allowing baseclass decapsulation ===
	// for lifting the input side allows decapsulation, for lowering its the output side.
	/** Mark nameRef for baseclass decapsulation, if input is the base side. */
	private NameReference decapsulationInput(NameReference nameRef) {
		if (this._isLifting)
			nameRef.baseclassDecapsulation = DecapsulationState.REPORTED;
		return nameRef;
	}
	/** Mark nameRef for baseclass decapsulation, if output is the base side. */
	private NameReference decapsulationOutput(NameReference nameRef) {
		if (!this._isLifting)
			nameRef.baseclassDecapsulation = DecapsulationState.REPORTED;
		return nameRef;
	}
	/** Mark typeRef for baseclass decapsulation, if input is the base side. */
	private void decapsulationInput(TypeReference typeRef) {
		if (this._isLifting)
			typeRef.setBaseclassDecapsulation(DecapsulationState.REPORTED);
	}
	/** Mark typeRef for baseclass decapsulation, if output is the base side. */
	private void decapsulationOutput(TypeReference typeRef) {
		if (!this._isLifting)
			typeRef.setBaseclassDecapsulation(DecapsulationState.REPORTED);
	}

	/**
	 * generate code for prevent null-pointer exceptions
	 * if(r1 == null)return null;
	 * if(r1[i0][i1] == null)continue;
	 * @param currentDimension
	 * @param arrayDimensions
	 * @return an if statement
	 */
	private IfStatement generateIfStatement(int currentDimension, int arrayDimensions) {

		SingleNameReference condLeft = new SingleNameReference(ROLE_ARRAY_ARG,0);
		decapsulationInput(condLeft);

		Expression lastArrayReference = condLeft;

		for (int idx = 0; idx < currentDimension; idx++) {
			SingleNameReference pos = new SingleNameReference(generateArrayIndexName(idx),0);
			ArrayReference nextArray = new ArrayReference(lastArrayReference,pos);
			lastArrayReference = nextArray;
		}

 		Expression condRight = new NullLiteral(0,0);
		Expression condition = new EqualExpression(lastArrayReference,condRight,OperatorIds.EQUAL_EQUAL);

		Statement thenStatement=null;
		if(currentDimension == 0)
		{
			thenStatement = new ReturnStatement(new NullLiteral(0,0), 0, 0);
		}
		else
		{
			thenStatement = new ContinueStatement(null,0,0);
		}
		IfStatement ifStatement = new IfStatement(condition,thenStatement,0,0);
		return ifStatement;
	}

	/**
	 * generates an array index name . e.g. "i0","i1"...
	 * @param dimension
	 * @return the name
	 */
	private static char[] generateArrayIndexName(int dimension)
	{
		return new String(IOTConstants.OT_DOLLAR).concat("i".concat(String.valueOf(dimension))).toCharArray();	 //$NON-NLS-1$
	}
	/**
	 * 	for(int i0=0;i0 < role.length;i0++)
	 *	{
	 *		result = new Base[role.length][][];
	 *		for(...){
	 *			...
	 *				for(int i2=0;i2 < role[i0][i1].length;i2++){
	 * 		    		result[i0][i1][i2] = translate(role[i0][i1][i2]);
	 * 				}
	 * 			...
	 * 		}
	 *	}
	 * @param currentDimension counter for recursion only. invoke initially with 0.
	 * @param arrayDimensions maximum of dimensions of the array
	 * @return the new created ForStatement with all subcycles
	 */
	private ForStatement generateForStatement(int currentDimension, int arrayDimensions, AstGenerator gen)
	{

		Statement[] init = new Statement[1];
		char[] name = generateArrayIndexName(currentDimension);

		LocalDeclaration initializer = new LocalDeclaration(name,0,0);
		initializer.initialization = IntLiteral.buildIntLiteral("0".toCharArray(),0,0); //$NON-NLS-1$
		initializer.type = new SingleTypeReference(TypeConstants.INT, 0);
		init[0] = initializer;

		SingleNameReference condLeft = new SingleNameReference(name,0);

		FieldReference  condRight = new FieldReference(IOTConstants.LENGTH,0);
		SingleNameReference roleNameReference = gen.singleNameReference(ROLE_ARRAY_ARG);
		decapsulationInput(roleNameReference);

		Expression lastArrayReference = roleNameReference;

		for (int idx = 0; idx < currentDimension; idx++) {
			SingleNameReference pos = new SingleNameReference(generateArrayIndexName(idx),0);
			ArrayReference nextArray = new ArrayReference(lastArrayReference,pos);
			lastArrayReference = nextArray;
		}
		condRight.receiver =  lastArrayReference;

		Expression 	cond = new BinaryExpression(condLeft,condRight,OperatorIds.LESS);
		Statement[] inc = new Statement[1];

		inc[0]= new PostfixExpression(new SingleNameReference(name,0),IntLiteral.One,OperatorIds.PLUS,0);

		Block action = new Block(0);

		// result = new Base[role.length][][];
		Assignment arrayInstantiation =
			generateArrayInstantiation(currentDimension+1, arrayDimensions, gen);

		// if(r1 == null)continue;
		IfStatement ifStatement =
			generateIfStatement(currentDimension+1, arrayDimensions);

		if(currentDimension<arrayDimensions-1)
		{
			ForStatement innerForStatement = generateForStatement(currentDimension + 1, arrayDimensions, gen);
			action.statements = new Statement[3];
			action.statements[0] = ifStatement;
			action.statements[1] = arrayInstantiation;
			action.statements[2] = innerForStatement;
		}
		else
		{
			action.statements = new Statement[2];
			action.statements[0] = ifStatement;
			action.statements[1] = arrayInstantiation;
		}

		// for(;i0 < role.length;i0++)
		ForStatement outerForStatement = new ForStatement(init,cond, inc, action, true,0,0);

		return outerForStatement;
	}
	/**
	 * creates AST-Element
	 *
	 * if currentDimension == 1 -> result = new Base[role.length][][];
	 * if currentDimension == 2 -> result[i0] =  new Base[role[i0].length][][];
	 * if currentDimension == arrayDimensions -> result[i0][i1][i2] = role[i0][i1][i2]._OT$Base;
	 * @param currentDimension some dimensions must have special treatment
	 * @param arrayDimensions maximum of dimensions of the array
	 * @return the new created assignment
	 */
	private Assignment generateArrayInstantiation(int currentDimension, int arrayDimensions, AstGenerator gen)
	{

		SingleNameReference resultNameReference = new SingleNameReference(IOTConstants.OT_RESULT,0);
		decapsulationOutput(resultNameReference);

		Expression lastArrayReference = resultNameReference;

		for (int idx = 0; idx < currentDimension; idx++) {
			SingleNameReference pos = new SingleNameReference(generateArrayIndexName(idx),0);
			ArrayReference nextArray = new ArrayReference(lastArrayReference,pos);
			lastArrayReference = nextArray;
		}
		Expression lhsReference = lastArrayReference;

		lastArrayReference = decapsulationInput(gen.singleNameReference(ROLE_ARRAY_ARG));

		for (int idx = 0; idx < currentDimension; idx++) {
			SingleNameReference pos = new SingleNameReference(generateArrayIndexName(idx),0);
			ArrayReference nextArray = new ArrayReference(lastArrayReference,pos);
			lastArrayReference = nextArray;
		}
		Expression rhsReference = lastArrayReference;

		if(currentDimension == arrayDimensions)
		{
			return new Assignment(
					lhsReference,
					translation(rhsReference, this._providedType.leafComponentType(), this._requiredType.leafComponentType(), gen),
					0);
		}
		else
		{
			//new Required[role[i0].length][][];
			FieldReference lengthFieldReference = new FieldReference(IOTConstants.LENGTH,0);
			lengthFieldReference.receiver = rhsReference;


			TypeReference reqTypeReference = gen.typeReference(this._requiredType.leafComponentType());
			decapsulationOutput(reqTypeReference);

			ArrayAllocationExpression reqAllocationExpression = new ArrayAllocationExpression();
			reqAllocationExpression.type = reqTypeReference;
			reqAllocationExpression.dimensions = new Expression[arrayDimensions-currentDimension];
			reqAllocationExpression.dimensions[0] = lengthFieldReference;

			Assignment assignment = new Assignment(lhsReference,reqAllocationExpression,0);
			return assignment;
		}
	}

	/**
	 * Hook method to fill in the atomic translation of one object.
	 * @param rhs
	 * @param providedType
	 * @param requiredType
	 * @param gen generator for creating new AST nodes
	 * @return expression translating a single object
	 */
	abstract Expression translation(Expression rhs, TypeBinding providedType, TypeBinding requiredType, AstGenerator gen);
	private MethodDeclaration generateTransformArrayMethod(
				TypeDeclaration teamType,
				char[] transformMethodName,
				int arrayDimensions)
	{
		AstGenerator gen = new AstGenerator(teamType.sourceStart, teamType.sourceEnd);

		MethodDeclaration transformArrayMethod = gen.method(
								teamType.compilationResult(),
								0, // modifiers
								this._requiredType,
								transformMethodName,
								new Argument[] {
										gen.argument(
						                    ROLE_ARRAY_ARG,
						                    gen.typeReference(this._providedType))
								});
		decapsulationInput(transformArrayMethod.arguments[0].type);
		decapsulationOutput(transformArrayMethod.returnType);

        // if(r1 == null)return null;
        IfStatement ifStatement =
            generateIfStatement(0, arrayDimensions);

        //  Base[][][] result = null;
        LocalDeclaration reqArrayDeclaration =
            gen.localVariable(IOTConstants.OT_RESULT, this._requiredType, gen.nullLiteral());
        decapsulationOutput(reqArrayDeclaration.type);

        //  result = new Base[role.length][][];
        Assignment arrayInstantiation =
            generateArrayInstantiation(0,arrayDimensions, gen);

        // for(...){...}
        ForStatement forStatement =
            generateForStatement(0,arrayDimensions, gen);

        // return result;
        ReturnStatement returnStatement =
            gen.returnStatement(gen.singleNameReference(IOTConstants.OT_RESULT));


        transformArrayMethod.setStatements(new Statement[] {
        		ifStatement,
        		reqArrayDeclaration,
        		arrayInstantiation,
        		forStatement,
        		returnStatement});

		return  transformArrayMethod;
	}
}
