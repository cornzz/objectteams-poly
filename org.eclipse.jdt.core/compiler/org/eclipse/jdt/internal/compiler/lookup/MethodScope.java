/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *     Stephan Herrmann - Contributions for
 *								bug 349326 - [1.7] new warning for missing try-with-resources
 *								bug 374605 - Unreasonable warning for enum-based switch statements
 *								bug 382353 - [1.8][compiler] Implementation property modifiers should be accepted on default methods.
 *								bug 382354 - [1.8][compiler] Compiler silent on conflicting modifier
 *     Jesper S Moller - Contributions for
 *							bug 382701 - [1.8][compiler] Implement semantic analysis of Lambda expressions & Reference expression
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.jdt.internal.compiler.impl.IrritantSet;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.problem.IProblemRechecker;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.WordValueAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutScope;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.MethodSignatureEnhancer;

/**
 * OTDT changes:
 *
 * What: Different rules for modifiers of role methods
 * Why:  Callout bindings open up new cases:
 * 	     + method is declared abstract but implemented by a callout binding,
 * 		   => must defere check for abstract method in concrete class until after mapping-transformation.
 *       + static methods are allowed and can even be abstract.
 * 	     + also "private static" is OK.
 *       New modifier AccCallin.
 *
 * What: Pass the MethodModel from AST to Binding
 * Where:createMethod().
 *
 * What: New query isCallinWrapper()
 *
 * ===============
 * Specific block scope used for methods, constructors or clinits, representing
 * its outermost blockscope. Note also that such a scope will be provided to enclose
 * field initializers subscopes as well.
 */
public class MethodScope extends BlockScope {

	public ReferenceContext referenceContext;
	public boolean isStatic; // method modifier or initializer one

	//fields used during name resolution
	public boolean isConstructorCall = false;
	public FieldBinding initializedField; // the field being initialized
	public int lastVisibleFieldID = -1; // the ID of the last field which got declared
	// note that #initializedField can be null AND lastVisibleFieldID >= 0, when processing instance field initializers.

	// flow analysis
	public int analysisIndex; // for setting flow-analysis id
	public boolean isPropagatingInnerClassEmulation;

	// for local variables table attributes
	public int lastIndex = 0;
	public long[] definiteInits = new long[4];
	public long[][] extraDefiniteInits = new long[4][];

	// annotation support
	public boolean insideTypeAnnotation = false;

	// inner-emulation
	public SyntheticArgumentBinding[] extraSyntheticArguments;

	// remember suppressed warning re missing 'default:' to give hints on possibly related flow problems
	public boolean hasMissingSwitchDefault; // TODO(stephan): combine flags to a bitset?

public MethodScope(Scope parent, ReferenceContext context, boolean isStatic) {
	super(METHOD_SCOPE, parent);
	this.locals = new LocalVariableBinding[5];
	this.referenceContext = context;
	this.isStatic = isStatic;
	this.startIndex = 0;
}

String basicToString(int tab) {
	String newLine = "\n"; //$NON-NLS-1$
	for (int i = tab; --i >= 0;)
		newLine += "\t"; //$NON-NLS-1$

	String s = newLine + "--- Method Scope ---"; //$NON-NLS-1$
	newLine += "\t"; //$NON-NLS-1$
	s += newLine + "locals:"; //$NON-NLS-1$
	for (int i = 0; i < this.localIndex; i++)
		s += newLine + "\t" + this.locals[i].toString(); //$NON-NLS-1$
	s += newLine + "startIndex = " + this.startIndex; //$NON-NLS-1$
	s += newLine + "isConstructorCall = " + this.isConstructorCall; //$NON-NLS-1$
	s += newLine + "initializedField = " + this.initializedField; //$NON-NLS-1$
	s += newLine + "lastVisibleFieldID = " + this.lastVisibleFieldID; //$NON-NLS-1$
	s += newLine + "referenceContext = " + this.referenceContext; //$NON-NLS-1$
	return s;
}

/**
 * Spec : 8.4.3 & 9.4
 */
private void checkAndSetModifiersForConstructor(MethodBinding methodBinding) {
	int modifiers = methodBinding.modifiers;
	final ReferenceBinding declaringClass = methodBinding.declaringClass;
	if ((modifiers & ExtraCompilerModifiers.AccAlternateModifierProblem) != 0)
		problemReporter().duplicateModifierForMethod(declaringClass, (AbstractMethodDeclaration) this.referenceContext);

	if ((((ConstructorDeclaration) this.referenceContext).bits & ASTNode.IsDefaultConstructor) != 0) {
		// certain flags are propagated from declaring class onto constructor
		final int DECLARING_FLAGS = ClassFileConstants.AccEnum|ClassFileConstants.AccPublic|ClassFileConstants.AccProtected;
		final int VISIBILITY_FLAGS = ClassFileConstants.AccPrivate|ClassFileConstants.AccPublic|ClassFileConstants.AccProtected;
		int flags;
		if ((flags = declaringClass.modifiers & DECLARING_FLAGS) != 0) {
			if ((flags & ClassFileConstants.AccEnum) != 0) {
				modifiers &= ~VISIBILITY_FLAGS;
				modifiers |= ClassFileConstants.AccPrivate; // default constructor is implicitly private in enum
			} else {
				modifiers &= ~VISIBILITY_FLAGS;
				modifiers |= flags; // propagate public/protected
			}
		}
	}

	// after this point, tests on the 16 bits reserved.
	int realModifiers = modifiers & ExtraCompilerModifiers.AccJustFlag;

	// check for abnormal modifiers
	final int UNEXPECTED_MODIFIERS = ~(ClassFileConstants.AccPublic | ClassFileConstants.AccPrivate | ClassFileConstants.AccProtected | ClassFileConstants.AccStrictfp);
	if (declaringClass.isEnum() && (((ConstructorDeclaration) this.referenceContext).bits & ASTNode.IsDefaultConstructor) == 0) {
		final int UNEXPECTED_ENUM_CONSTR_MODIFIERS = ~(ClassFileConstants.AccPrivate | ClassFileConstants.AccStrictfp);
		if ((realModifiers & UNEXPECTED_ENUM_CONSTR_MODIFIERS) != 0) {
			problemReporter().illegalModifierForEnumConstructor((AbstractMethodDeclaration) this.referenceContext);
			modifiers &= ~ExtraCompilerModifiers.AccJustFlag | ~UNEXPECTED_ENUM_CONSTR_MODIFIERS;
		} else if ((((AbstractMethodDeclaration) this.referenceContext).modifiers & ClassFileConstants.AccStrictfp) != 0) {
			// must check the parse node explicitly
			problemReporter().illegalModifierForMethod((AbstractMethodDeclaration) this.referenceContext);
		}
		modifiers |= ClassFileConstants.AccPrivate; // enum constructor is implicitly private
	} else if ((realModifiers & UNEXPECTED_MODIFIERS) != 0) {
		problemReporter().illegalModifierForMethod((AbstractMethodDeclaration) this.referenceContext);
		modifiers &= ~ExtraCompilerModifiers.AccJustFlag | ~UNEXPECTED_MODIFIERS;
	} else if ((((AbstractMethodDeclaration) this.referenceContext).modifiers & ClassFileConstants.AccStrictfp) != 0) {
		// must check the parse node explicitly
		problemReporter().illegalModifierForMethod((AbstractMethodDeclaration) this.referenceContext);
	}

	// check for incompatible modifiers in the visibility bits, isolate the visibility bits
	int accessorBits = realModifiers & (ClassFileConstants.AccPublic | ClassFileConstants.AccProtected | ClassFileConstants.AccPrivate);
	if ((accessorBits & (accessorBits - 1)) != 0) {
		problemReporter().illegalVisibilityModifierCombinationForMethod(declaringClass, (AbstractMethodDeclaration) this.referenceContext);

		// need to keep the less restrictive so disable Protected/Private as necessary
		if ((accessorBits & ClassFileConstants.AccPublic) != 0) {
			if ((accessorBits & ClassFileConstants.AccProtected) != 0)
				modifiers &= ~ClassFileConstants.AccProtected;
			if ((accessorBits & ClassFileConstants.AccPrivate) != 0)
				modifiers &= ~ClassFileConstants.AccPrivate;
		} else if ((accessorBits & ClassFileConstants.AccProtected) != 0 && (accessorBits & ClassFileConstants.AccPrivate) != 0) {
			modifiers &= ~ClassFileConstants.AccPrivate;
		}
	}

//		// if the receiver's declaring class is a private nested type, then make sure the receiver is not private (causes problems for inner type emulation)
//		if (declaringClass.isPrivate() && (modifiers & ClassFileConstants.AccPrivate) != 0)
//			modifiers &= ~ClassFileConstants.AccPrivate;

	methodBinding.modifiers = modifiers;
}

/**
 * Spec : 8.4.3 & 9.4
 */
//{ObjectTeams: make arg final to ease the use in an anonymous class:
/* orig:
private void checkAndSetModifiersForMethod(MethodBinding methodBinding) {
  :giro */
private void checkAndSetModifiersForMethod(final MethodBinding methodBinding) {
// SH}
	int modifiers = methodBinding.modifiers;
	final ReferenceBinding declaringClass = methodBinding.declaringClass;
	if ((modifiers & ExtraCompilerModifiers.AccAlternateModifierProblem) != 0)
		problemReporter().duplicateModifierForMethod(declaringClass, (AbstractMethodDeclaration) this.referenceContext);

	// after this point, tests on the 16 bits reserved.
	int realModifiers = modifiers & ExtraCompilerModifiers.AccJustFlag;

//{ObjectTeams: push the callin flag to a CallinFlags attribute:
	if ((modifiers & ExtraCompilerModifiers.AccCallin) != 0)
		MethodModel.getModel(methodBinding.sourceMethod())
					.addAttribute(WordValueAttribute.callinFlagsAttribute(0));
	// one more modifier to check (outside the 16-bit range):
	if ((modifiers & ExtraCompilerModifiers.AccReadonly) != 0)
		problemReporter().illegalModifierForMethod((AbstractMethodDeclaration) this.referenceContext);
//SH}

	// set the requested modifiers for a method in an interface/annotation
//{ObjectTeams: synthetic role interfaces may have any access modifier.
/* orig:
	if (declaringClass.isInterface()) {
  :giro */
	if (declaringClass.isRegularInterface()) {
// SH}
		int expectedModifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccAbstract;
		// 9.4 got updated for JSR 335 (default methods), more permissive grammar plus:
		// "It is a compile-time error if an abstract method declaration contains either of the keywords strictfp or synchronized."
		if (compilerOptions().sourceLevel >= ClassFileConstants.JDK1_8 && !methodBinding.isAbstract()) {
			expectedModifiers |= (ClassFileConstants.AccSynchronized | ClassFileConstants.AccStrictfp);
		}
		boolean isDefaultMethod = (modifiers & ExtraCompilerModifiers.AccDefaultMethod) != 0; // no need to check validity, is done by the parser
		if ((realModifiers & ~expectedModifiers) != 0) {
			if ((declaringClass.modifiers & ClassFileConstants.AccAnnotation) != 0)
				problemReporter().illegalModifierForAnnotationMember((AbstractMethodDeclaration) this.referenceContext);
			else
				problemReporter().illegalModifierForInterfaceMethod((AbstractMethodDeclaration) this.referenceContext, isDefaultMethod);
		}
		if (isDefaultMethod && (modifiers & ClassFileConstants.AccAbstract) != 0) {
			problemReporter().abstractMethodNeedingNoBody((AbstractMethodDeclaration) this.referenceContext);
		}
		return;
	}

	// check for abnormal modifiers
	final int UNEXPECTED_MODIFIERS = ~(ClassFileConstants.AccPublic | ClassFileConstants.AccPrivate | ClassFileConstants.AccProtected
//{ObjectTeams: allow callin methods:
		| ExtraCompilerModifiers.AccCallin
// SH}
		| ClassFileConstants.AccAbstract | ClassFileConstants.AccStatic | ClassFileConstants.AccFinal | ClassFileConstants.AccSynchronized | ClassFileConstants.AccNative | ClassFileConstants.AccStrictfp);
	if ((realModifiers & UNEXPECTED_MODIFIERS) != 0) {
		problemReporter().illegalModifierForMethod((AbstractMethodDeclaration) this.referenceContext);
		modifiers &= ~ExtraCompilerModifiers.AccJustFlag | ~UNEXPECTED_MODIFIERS;
	}

	// check for incompatible modifiers in the visibility bits, isolate the visibility bits
	int accessorBits = realModifiers & (ClassFileConstants.AccPublic | ClassFileConstants.AccProtected | ClassFileConstants.AccPrivate);
	if ((accessorBits & (accessorBits - 1)) != 0) {
		problemReporter().illegalVisibilityModifierCombinationForMethod(declaringClass, (AbstractMethodDeclaration) this.referenceContext);

		// need to keep the less restrictive so disable Protected/Private as necessary
		if ((accessorBits & ClassFileConstants.AccPublic) != 0) {
			if ((accessorBits & ClassFileConstants.AccProtected) != 0)
				modifiers &= ~ClassFileConstants.AccProtected;
			if ((accessorBits & ClassFileConstants.AccPrivate) != 0)
				modifiers &= ~ClassFileConstants.AccPrivate;
		} else if ((accessorBits & ClassFileConstants.AccProtected) != 0 && (accessorBits & ClassFileConstants.AccPrivate) != 0) {
			modifiers &= ~ClassFileConstants.AccPrivate;
		}
	}

	// check for modifiers incompatible with abstract modifier
	if ((modifiers & ClassFileConstants.AccAbstract) != 0) {
		int incompatibleWithAbstract = ClassFileConstants.AccPrivate | ClassFileConstants.AccStatic | ClassFileConstants.AccFinal | ClassFileConstants.AccSynchronized | ClassFileConstants.AccNative | ClassFileConstants.AccStrictfp;
//{ObjectTeams: allow "abstract static" "private abstract" for callout
        if (declaringClass.isDirectRole())
            incompatibleWithAbstract ^= (ClassFileConstants.AccStatic|ClassFileConstants.AccPrivate);
// SH}
		if ((modifiers & incompatibleWithAbstract) != 0)
			problemReporter().illegalAbstractModifierCombinationForMethod(declaringClass, (AbstractMethodDeclaration) this.referenceContext);
		if (!methodBinding.declaringClass.isAbstract())
//{ObjectTeams: set a re-checker since method abstractness might go during callout transformation.
/* orig:
			problemReporter().abstractMethodInAbstractClass((SourceTypeBinding) declaringClass, (AbstractMethodDeclaration) this.referenceContext); 
  :giro */
			problemReporter().setRechecker(new IProblemRechecker() { public boolean shouldBeReported(IrritantSet[] foundIrritants) {
								return methodBinding.isAbstract();
							  }})
							 .abstractMethodInAbstractClass((SourceTypeBinding) declaringClass, (AbstractMethodDeclaration) this.referenceContext); 
//JH & MW & SH}
	}

	/* DISABLED for backward compatibility with javac (if enabled should also mark private methods as final)
	// methods from a final class are final : 8.4.3.3
	if (methodBinding.declaringClass.isFinal())
		modifiers |= AccFinal;
	*/
	// native methods cannot also be tagged as strictfp
	if ((modifiers & ClassFileConstants.AccNative) != 0 && (modifiers & ClassFileConstants.AccStrictfp) != 0)
		problemReporter().nativeMethodsCannotBeStrictfp(declaringClass, (AbstractMethodDeclaration) this.referenceContext);

	// static members are only authorized in a static member or top level type
	if (((realModifiers & ClassFileConstants.AccStatic) != 0) && declaringClass.isNestedType() && !declaringClass.isStatic())
//{ObjectTeams: allow static in roles
	  if (!methodBinding.declaringClass.isDirectRole())
// SH}
		problemReporter().unexpectedStaticModifierForMethod(declaringClass, (AbstractMethodDeclaration) this.referenceContext);

//{ObjectTeams: callin modifier:
	  if (   methodBinding.isCallin()
		  && !referenceMethod().isCopied)
	  {
		MethodDeclaration referenceMethod = (MethodDeclaration) this.referenceContext;
		// only allowed in direct role:
		if (!methodBinding.declaringClass.isDirectRole()) {
			problemReporter().callinInNonRole(methodBinding.declaringClass, referenceMethod);
			// revert callin specifica:
			referenceMethod.arguments = MethodSignatureEnhancer.maybeRetrenchArguments(referenceMethod);
			referenceMethod.modifiers &= ~ExtraCompilerModifiers.AccCallin;
			modifiers &= ~ExtraCompilerModifiers.AccCallin;
		} else {
			// must not be combined with visibility modifiers:
			if (accessorBits != 0)
				problemReporter().callinWithVisibility(referenceMethod, accessorBits);
		}
	}
// SH}
	methodBinding.modifiers = modifiers;
}

public void checkUnusedParameters(MethodBinding method) {
	if (method.isAbstract()
			|| (method.isImplementing() && !compilerOptions().reportUnusedParameterWhenImplementingAbstract)
			|| (method.isOverriding() && !method.isImplementing() && !compilerOptions().reportUnusedParameterWhenOverridingConcrete)
			|| method.isMain()) {
		// do not want to check
		return;
	}
	for (int i = 0, maxLocals = this.localIndex; i < maxLocals; i++) {
		LocalVariableBinding local = this.locals[i];
		if (local == null || ((local.tagBits & TagBits.IsArgument) == 0)) {
			break; // done with arguments
		}
		if (local.useFlag == LocalVariableBinding.UNUSED &&
//{ObjectTeams: don't report generated argument:
				!local.declaration.isGenerated &&
// SH}
				// do not report fake used variable
				((local.declaration.bits & ASTNode.IsLocalDeclarationReachable) != 0)) { // declaration is reachable
			problemReporter().unusedArgument(local.declaration);
		}
	}
}

/**
 * Compute variable positions in scopes given an initial position offset
 * ignoring unused local variables.
 *
 * Deal with arguments here, locals and subscopes are processed in BlockScope method
 */
public void computeLocalVariablePositions(int initOffset, CodeStream codeStream) {
	this.offset = initOffset;
	this.maxOffset = initOffset;

	// manage arguments
	int ilocal = 0, maxLocals = this.localIndex;
	while (ilocal < maxLocals) {
		LocalVariableBinding local = this.locals[ilocal];
		if (local == null || ((local.tagBits & TagBits.IsArgument) == 0)) break; // done with arguments

		// record user-defined argument for attribute generation
		codeStream.record(local);

		// assign variable position
		local.resolvedPosition = this.offset;

		if ((local.type == TypeBinding.LONG) || (local.type == TypeBinding.DOUBLE)) {
			this.offset += 2;
		} else {
			this.offset++;
		}
		// check for too many arguments/local variables
		if (this.offset > 0xFF) { // no more than 255 words of arguments
			problemReporter().noMoreAvailableSpaceForArgument(local, local.declaration);
		}
		ilocal++;
	}

	// sneak in extra argument before other local variables
	if (this.extraSyntheticArguments != null) {
		for (int iarg = 0, maxArguments = this.extraSyntheticArguments.length; iarg < maxArguments; iarg++){
			SyntheticArgumentBinding argument = this.extraSyntheticArguments[iarg];
			argument.resolvedPosition = this.offset;
			if ((argument.type == TypeBinding.LONG) || (argument.type == TypeBinding.DOUBLE)){
				this.offset += 2;
			} else {
				this.offset++;
			}
			if (this.offset > 0xFF) { // no more than 255 words of arguments
				problemReporter().noMoreAvailableSpaceForArgument(argument, (ASTNode)this.referenceContext);
			}
		}
	}
	this.computeLocalVariablePositions(ilocal, this.offset, codeStream);
}

/**
 * Error management:
 * 		keep null for all the errors that prevent the method to be created
 * 		otherwise return a correct method binding (but without the element
 *		that caused the problem) : i.e. Incorrect thrown exception
 */
MethodBinding createMethod(AbstractMethodDeclaration method) {
	// is necessary to ensure error reporting
	this.referenceContext = method;
	method.scope = this;
	SourceTypeBinding declaringClass = referenceType().binding;
	int modifiers = method.modifiers | ExtraCompilerModifiers.AccUnresolved;
	if (method.isConstructor()) {
		if (method.isDefaultConstructor())
			modifiers |= ExtraCompilerModifiers.AccIsDefaultConstructor;
		method.binding = new MethodBinding(modifiers, null, null, declaringClass);
		checkAndSetModifiersForConstructor(method.binding);
	} else {
//{ObjectTeams: changed to isRegularInterface: role interfaces don't force public
/*orig:
		if (declaringClass.isInterface()) {// interface or annotation type
 :giro */
		if (declaringClass.isRegularInterface()) {
// SH}
			if (method.isDefaultMethod()) {
				modifiers |= ClassFileConstants.AccPublic; // default method is not abstract
			} else {
				modifiers |= ClassFileConstants.AccPublic | ClassFileConstants.AccAbstract;
			}
		}
		method.binding =
			new MethodBinding(modifiers, method.selector, null, null, null, declaringClass);
		checkAndSetModifiersForMethod(method.binding);
	}
//{ObjectTeams: share the model if set
	if (method.model != null) {
		method.model.linkBinding(method.binding);
		//  need to transfer the clearPrivate flag, since RoleSplitter works before binding creation
		if (method.model._clearPrivateModifier)
			method.binding.tagBits |= TagBits.ClearPrivateModifier;
	}
// SH}
	this.isStatic = method.binding.isStatic();

	Argument[] argTypes = method.arguments;
	int argLength = argTypes == null ? 0 : argTypes.length;
	long sourceLevel = compilerOptions().sourceLevel;
	if (argLength > 0) {
		Argument argument = argTypes[--argLength];
		if (argument.isVarArgs() && sourceLevel >= ClassFileConstants.JDK1_5)
			method.binding.modifiers |= ClassFileConstants.AccVarargs;
		if (CharOperation.equals(argument.name, ConstantPool.This)) {
			problemReporter().illegalThisDeclaration(argument);
		}
		while (--argLength >= 0) {
			argument = argTypes[argLength];
			if (argument.isVarArgs() && sourceLevel >= ClassFileConstants.JDK1_5)
				problemReporter().illegalVararg(argument, method);
			if (CharOperation.equals(argument.name, ConstantPool.This)) {
				problemReporter().illegalThisDeclaration(argument);
			}
		}
	}
	if (method.receiver != null) {
		if (sourceLevel <= ClassFileConstants.JDK1_7) {
			problemReporter().illegalSourceLevelForThis(method.receiver);
		}
		if (method.receiver.annotations != null) {
			method.bits |= ASTNode.HasTypeAnnotations;
		}
	}

	TypeParameter[] typeParameters = method.typeParameters();
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850, If they exist at all, process type parameters irrespective of source level.
    if (typeParameters == null || typeParameters.length == 0) {
	    method.binding.typeVariables = Binding.NO_TYPE_VARIABLES;
	} else {
		method.binding.typeVariables = createTypeVariables(typeParameters, method.binding);
		method.binding.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
	}
	return method.binding;
}

public MethodBinding createAnonymousMethodBinding(LambdaExpression lambda) {
	
	SourceTypeBinding declaringClass = null; // for now.
	int modifiers = ClassFileConstants.AccPublic | ExtraCompilerModifiers.AccUnresolved;
	MethodBinding binding = new MethodBinding(modifiers, TypeConstants.ANONYMOUS_METHOD, null, null, null, declaringClass);

	Argument[] arguments = lambda.arguments;
	int argLength = arguments == null ? 0 : arguments.length;
	if (argLength > 0) {
		Argument argument = arguments[--argLength];
		if (argument.isVarArgs())
			binding.modifiers |= ClassFileConstants.AccVarargs;
		while (--argLength >= 0) {
			argument = arguments[argLength];
			if (argument.isVarArgs())
				problemReporter().illegalVarargInLambda(argument);
		}
	}
	return binding;
}

/**
 * Overridden to detect the error case inside an explicit constructor call:
class X {
	int i;
	X myX;
	X(X x) {
		this(i, myX.i, x.i); // same for super calls... only the first 2 field accesses are errors
	}
}
 */
public FieldBinding findField(TypeBinding receiverType, char[] fieldName, InvocationSite invocationSite, boolean needResolve) {

	FieldBinding field = super.findField(receiverType, fieldName, invocationSite, needResolve);
	if (field == null)
		return null;
	if (!field.isValidBinding())
		return field; // answer the error field
	if (field.isStatic())
		return field; // static fields are always accessible

	if (!this.isConstructorCall || receiverType != enclosingSourceType())
		return field;

	if (invocationSite instanceof SingleNameReference)
		return new ProblemFieldBinding(
			field, // closest match
			field.declaringClass,
			fieldName,
			ProblemReasons.NonStaticReferenceInConstructorInvocation);
	if (invocationSite instanceof QualifiedNameReference) {
		// look to see if the field is the first binding
		QualifiedNameReference name = (QualifiedNameReference) invocationSite;
		if (name.binding == null)
			// only true when the field is the fieldbinding at the beginning of name's tokens
			return new ProblemFieldBinding(
				field, // closest match
				field.declaringClass,
				fieldName,
				ProblemReasons.NonStaticReferenceInConstructorInvocation);
	}
	return field;
}

public boolean isInsideConstructor() {
	return (this.referenceContext instanceof ConstructorDeclaration);
}

public boolean isInsideInitializer() {
	return (this.referenceContext instanceof TypeDeclaration);
}

public boolean isInsideInitializerOrConstructor() {
	return (this.referenceContext instanceof TypeDeclaration)
		|| (this.referenceContext instanceof ConstructorDeclaration);
}

/**
 * Answer the problem reporter to use for raising new problems.
 *
 * Note that as a side-effect, this updates the current reference context
 * (unit, type or method) in case the problem handler decides it is necessary
 * to abort.
 */
public ProblemReporter problemReporter() {
	ProblemReporter problemReporter = referenceCompilationUnit().problemReporter;
	problemReporter.referenceContext = this.referenceContext;
	return problemReporter;
}

public final int recordInitializationStates(FlowInfo flowInfo) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) != 0) return -1;
	UnconditionalFlowInfo unconditionalFlowInfo = flowInfo.unconditionalInitsWithoutSideEffect();
	long[] extraInits = unconditionalFlowInfo.extra == null ?
			null : unconditionalFlowInfo.extra[0];
	long inits = unconditionalFlowInfo.definiteInits;
	checkNextEntry : for (int i = this.lastIndex; --i >= 0;) {
		if (this.definiteInits[i] == inits) {
			long[] otherInits = this.extraDefiniteInits[i];
			if ((extraInits != null) && (otherInits != null)) {
				if (extraInits.length == otherInits.length) {
					int j, max;
					for (j = 0, max = extraInits.length; j < max; j++) {
						if (extraInits[j] != otherInits[j]) {
							continue checkNextEntry;
						}
					}
					return i;
				}
			} else {
				if ((extraInits == null) && (otherInits == null)) {
					return i;
				}
			}
		}
	}

	// add a new entry
	if (this.definiteInits.length == this.lastIndex) {
		// need a resize
		System.arraycopy(
			this.definiteInits,
			0,
			(this.definiteInits = new long[this.lastIndex + 20]),
			0,
			this.lastIndex);
		System.arraycopy(
			this.extraDefiniteInits,
			0,
			(this.extraDefiniteInits = new long[this.lastIndex + 20][]),
			0,
			this.lastIndex);
	}
	this.definiteInits[this.lastIndex] = inits;
	if (extraInits != null) {
		this.extraDefiniteInits[this.lastIndex] = new long[extraInits.length];
		System.arraycopy(
			extraInits,
			0,
			this.extraDefiniteInits[this.lastIndex],
			0,
			extraInits.length);
	}
	return this.lastIndex++;
}

/**
 *  Answer the reference method of this scope, or null if initialization scope.
 */
public AbstractMethodDeclaration referenceMethod() {
	if (this.referenceContext instanceof AbstractMethodDeclaration) return (AbstractMethodDeclaration) this.referenceContext;
	return null;
}

/**
 *  Answer the reference type of this scope.
 * It is the nearest enclosing type of this scope.
 */
public TypeDeclaration referenceType() {
	return ((ClassScope) this.parent).referenceContext;
}

//{ObjectTeams: new queries:
	public MethodBinding referenceMethodBinding() {
		if (this.referenceContext instanceof AbstractMethodDeclaration) return ((AbstractMethodDeclaration) this.referenceContext).binding;
		return null;
	}
	/**
     * Does this scope represent a callin wrapper
     * (either the mapping declaration or the generated wrapper method)?
	 */
	public boolean isCallinWrapper() {
		if (this.referenceContext instanceof CallinMappingDeclaration)
			return true;
		if (!(this.referenceContext instanceof AbstractMethodDeclaration))
			return false;
		AbstractMethodDeclaration method = (AbstractMethodDeclaration)this.referenceContext;
		return method.isMappingWrapper._callin();
	}
	
	public CallinCalloutScope getDeclaringMappingScope() {
		AbstractMethodDeclaration method = referenceMethod();
		TypeDeclaration type = referenceType();
		if (method == null || type == null)
			return null;
		if (type.callinCallouts != null) {
			for (AbstractMethodMappingDeclaration mapping : type.callinCallouts) 
				if (mapping.isCallout() && mapping.roleMethodSpec.resolvedMethod == method.binding)
					return mapping.scope;
		}
		if (type.memberTypes != null) { // search for callin bindings in member roles
			for (TypeDeclaration member : type.memberTypes)
				if (member.callinCallouts != null)
					for (AbstractMethodMappingDeclaration mapping : member.callinCallouts)
						if (mapping.isCallin()) {
							CallinMappingDeclaration callinMapping = (CallinMappingDeclaration) mapping;
							if (callinMapping.wrappers != null)
								for (AbstractMethodDeclaration wrapper : callinMapping.wrappers)
									if (wrapper == method)
										return mapping.scope;
						}				
		}
		return null;
	}
// SH}
}
