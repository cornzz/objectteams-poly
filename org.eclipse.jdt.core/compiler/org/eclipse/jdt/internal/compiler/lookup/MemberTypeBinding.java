/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.HashSet;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;

/**
 * OTDT changes:
 *
 * Support for "playedBy" declaration:
 * ===================================
 * API:  Method baseclass() and hasBaseclassProblem()
 *
 * What: Field baseclass is actually a quad-state
 * 	     null             : not resolved yet
 *       NoBaseClass      : Resolved to no baseclass.
 *       ProblemBaseClass : Resolve failed.
 *       <other>          : resolved, valid baseclass.
 *
 */
public final class MemberTypeBinding extends NestedTypeBinding {

public MemberTypeBinding(char[][] compoundName, ClassScope scope, SourceTypeBinding enclosingType) {
	super(compoundName, scope, enclosingType);
	this.tagBits |= TagBits.MemberTypeMask;
}

public MemberTypeBinding(MemberTypeBinding prototype) {
	super(prototype);
//{ObjectTeams: role?
	if (prototype.roleModel != null)
		this.roleModel = new RoleModel(this);
// SH}
}

void checkSyntheticArgsAndFields() {
	if (!isPrototype()) throw new IllegalStateException();
//{ObjectTeams: role ifcs require synth args too
/* orig:
	if (isStatic()) return;
	if (isInterface()) return;
  :giro */
	if(isInterface()) {
		if (isRole())
			addSyntheticArgument(this.enclosingType);
		return;
	}
	if (isStatic()) return;
// SH}
	if (!isPrototype()) {
		((MemberTypeBinding) this.prototype).checkSyntheticArgsAndFields();
		return;
	}
	this.addSyntheticArgumentAndField(this.enclosingType);
}
/* Answer the receiver's constant pool name.
*
* NOTE: This method should only be used during/after code gen.
*/

@Override
public char[] constantPoolName() /* java/lang/Object */ {

	if (this.constantPoolName != null)
		return this.constantPoolName;

	if (!isPrototype()) {
		return this.prototype.constantPoolName();
	}

	return this.constantPoolName = CharOperation.concat(enclosingType().constantPoolName(), this.sourceName, '$');
}

@Override
public TypeBinding clone(TypeBinding outerType) {
	MemberTypeBinding copy = new MemberTypeBinding(this);
	copy.enclosingType = (SourceTypeBinding) outerType;
	return copy;
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#initializeDeprecatedAnnotationTagBits()
 */
@Override
public void initializeDeprecatedAnnotationTagBits() {
	if (!isPrototype()) {
		this.prototype.initializeDeprecatedAnnotationTagBits();
		return;
	}
	if ((this.tagBits & TagBits.DeprecatedAnnotationResolved) == 0) {
		super.initializeDeprecatedAnnotationTagBits();
		if ((this.tagBits & TagBits.AnnotationDeprecated) == 0) {
			// check enclosing type
			ReferenceBinding enclosing;
			if (((enclosing = enclosingType()).tagBits & TagBits.DeprecatedAnnotationResolved) == 0) {
				enclosing.initializeDeprecatedAnnotationTagBits();
			}
			if (enclosing.isViewedAsDeprecated()) {
				this.modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
				this.tagBits |= (enclosing.tagBits & TagBits.AnnotationTerminallyDeprecated);
			}
		}
	}
}
//{ObjectTeams: search baseclass on demand
private static final ReferenceBinding NoBaseclass = new SourceTypeBinding();
private static final ReferenceBinding ProblemBaseClass = new SourceTypeBinding();

static {
    NoBaseclass.compoundName = new char[][]{"No Baseclass".toCharArray()}; //$NON-NLS-1$
    NoBaseclass.model = new TypeModel(NoBaseclass);
    NoBaseclass.model.setState(ITranslationStates.STATE_FINAL);
    ProblemBaseClass.compoundName = new char[][]{"Problem Baseclass".toCharArray()}; //$NON-NLS-1$
    ProblemBaseClass.model = new TypeModel(ProblemBaseClass);
    ProblemBaseClass.model.setState(ITranslationStates.STATE_FINAL);
}

private boolean hasCheckedBaseclassCircularity = false; // TODO(SH): convert to a tagbit?
@Override
public ReferenceBinding baseclass() {
	if (!isDirectRole())
		return null;

    // BinaryTypeBindings have their PlayedBy in the Attribute
    if (this.baseclass == null) {
    	this.baseclass = NoBaseclass; // mark that we have searched
        if (this.roleModel != null) {
        	// if superinterfaces where not setup during role splitting,
        	// but only after copy inheritance (setupInterfaceFromExtends),
        	// the base class must be searched in the class part.
        	if (isSynthInterface()) {
        		ReferenceBinding classPart = getRealClass();
        		if (classPart != null && classPart.baseclass != null)
        			this.baseclass = classPart.baseclass;
        		if (this.baseclass != NoBaseclass && this.baseclass != ProblemBaseClass) //$IDENTITY-COMPARISON$
        			return this.baseclass;
        	}
            if (!Dependencies.ensureRoleState(this.roleModel, ITranslationStates.STATE_LENV_DONE_FIELDS_AND_METHODS))
            {
            	this.scope.problemReporter().searchingBaseclassTooEarly(this);
            	RoleModel.setTagBit(this, RoleModel.BaseclassHasProblems);
            	this.baseclass = ProblemBaseClass;
            	return null;
            }
        }

        if (   superclass() != null
            && !CharOperation.equals(superclass().compoundName, TypeConstants.JAVA_LANG_OBJECT))
        {
            if (superclass().isSourceRole())
                this.baseclass = superclass().baseclass();
        }
        if (this.baseclass == null)
        	this.baseclass= NoBaseclass;
        checkRefineBaseFromSuperInterfaces();
    }
    if (!this.roleModel._playedByEnclosing && !isSynthInterface()) {
	    String circle = hasBaseclassCircularity();
	    if (circle != null) {
	    	circle = new String(readableName())+circle;
	    	this.scope.problemReporter().baseclassCircularity(circle, this.scope.referenceContext);
	    	this.roleModel._playedByEnclosing = true;
	    }
    }
    return rawBaseclass();
}
// do not perform any lookup/analysis but filter out NobBaseclass/ProblemBaseClass
@Override
public ReferenceBinding rawBaseclass() {
    return ((this.baseclass == NoBaseclass) || (this.baseclass == ProblemBaseClass)) //$IDENTITY-COMPARISON$
    ? null: this.baseclass;
}
private String hasBaseclassCircularity() {
	if (this.hasCheckedBaseclassCircularity)
		return null;
	this.hasCheckedBaseclassCircularity = true;
	if (this.baseclass == null)
		return null;
	HashSet<ReferenceBinding> upEnclosing = new HashSet<ReferenceBinding>(); // set of all enclosings and their supers
	ReferenceBinding out = this;
	while ((out  = out.enclosingType()) != null) {
		ReferenceBinding up = out;
		while (up != null && up.id != TypeIds.T_JavaLangObject) {
			upEnclosing.add(up);
			up = up.superclass();
		}
	}
	return internalHasBaseclassCircularity(upEnclosing, new HashSet<ReferenceBinding>(), this.baseclass);
}
@SuppressWarnings("nls")
private String internalHasBaseclassCircularity(
		HashSet<ReferenceBinding> upEnclosing,
		HashSet<ReferenceBinding> visitedTypes,
		ReferenceBinding type)
{
	if (type == null || type == NoBaseclass || type == ProblemBaseClass) //$IDENTITY-COMPARISON$
		return null;
	// completing a cycle that started from the focus role?
	if (visitedTypes.contains(type))
		return "";
	// completing a cycle that started from "above" the focus role (enclosing types and their supers)?
	if (upEnclosing.contains(type))
		return "->" + new String(type.readableName());
	visitedTypes.add(type);
	String circle = null;
	if (type.rawBaseclass() != null) {
		circle = internalHasBaseclassCircularity(upEnclosing, visitedTypes, type.baseclass);
		if (circle != null)
			return "->"+new String(type.shortReadableName())+circle;
		visitedTypes.remove(type.baseclass);
	}
	ReferenceBinding[] members = type.memberTypes();
	if (members == null)
		return null;
	for (ReferenceBinding member : members) {
		circle = internalHasBaseclassCircularity(upEnclosing, visitedTypes, member);
		if (circle != null)
			return "->"+new String(type.shortReadableName())+circle;
		visitedTypes.remove(member);
	}
	return null;
}
public void checkRefineBaseFromSuperInterfaces()
{
	TypeDeclaration roleDecl = this.roleModel.getAst();
	if (roleDecl == null)
		return;
	// loop over superinterfaces in order to find the most specific baseclass
	if (this.superInterfaces != null) {
	    for (int i=0; i<this.superInterfaces.length; i++) {
	        ReferenceBinding superIfc = this.superInterfaces[i];
	        int sStart = roleDecl.declarationSourceStart;
	        int sEnd   = roleDecl.declarationSourceEnd;
	        if (   (roleDecl.superInterfaces != null)
	            && (roleDecl.superInterfaces.length > i))
	        {
	            sStart = roleDecl.superInterfaces[i].sourceStart;
	            sEnd   = roleDecl.superInterfaces[i].sourceEnd;
	        }
	        if (superIfc.isDirectRole()) {
	            superIfc.baseclass(); // ensure initialized
	            this.baseclass = checkRefineBase(
	                            roleDecl.scope,
	                            sStart, sEnd,
	                            this.baseclass,
	                            superIfc.baseclass); // raw field access!
	            if (this.baseclass == ProblemBaseClass) //$IDENTITY-COMPARISON$
	            	RoleModel.setTagBit(this, RoleModel.BaseclassHasProblems);
	            // detect situation of OTJLD 2.4.3:
	            if (   this.superclass != null
	            	&& this.superclass.isRole()
	            	&& rawBaseclass() != null
	            	&& TypeBinding.equalsEquals(this.baseclass, this.superclass.baseclass))
	            	this.superclass.roleModel._supercededBy = this;
	        }
	    }
	}
}
/** Helper for the above. */
private ReferenceBinding checkRefineBase(
        ClassScope       classScope,
        int sStart, int sEnd,
        ReferenceBinding current,
        ReferenceBinding next)
{
    if (next    == null || next    == NoBaseclass) return current; //$IDENTITY-COMPARISON$
    if (current == null || current == NoBaseclass) return next; //$IDENTITY-COMPARISON$
    if (current == ProblemBaseClass || next == ProblemBaseClass) return ProblemBaseClass; //$IDENTITY-COMPARISON$
    if (current.isCompatibleWith(next)) return current;
    if (next.isCompatibleWith(current)) return next;
    if (current.isRole() && current.roleModel.hasTSuperRole(next.getRealType())) return current;
    if (   RoleTypeBinding.isRoleWithExplicitAnchor(current)
    	&& RoleTypeBinding.isRoleWithExplicitAnchor(next))
    {
    	// check for implicit refinement of base anchored baseclasses (OTJLD 2.7(d)):
    	ITeamAnchor currentAnchor = ((RoleTypeBinding)current).getAnchor();
    	ITeamAnchor nextAnchor = ((RoleTypeBinding)next).getAnchor();
    	if (currentAnchor.isBaseAnchor() && nextAnchor.isBaseAnchor()) {
    		ReferenceBinding nextDeclaringType = ((FieldBinding)nextAnchor).getDeclaringClass().getRealType();
			ReferenceBinding currentDeclaringType = ((FieldBinding)currentAnchor).getDeclaringClass().getRealType();
			if (   current.getRealType().isCompatibleWith(next.getRealType())
    			&& currentDeclaringType.isCompatibleWith(nextDeclaringType))
   				return current;
    		if (   next.getRealType().isCompatibleWith(current.getRealType())
    			&& nextDeclaringType.isCompatibleWith(currentDeclaringType))
   				return next;
    	}
    }
    classScope.problemReporter().incompatibleBaseclasses(
    							classScope.referenceContext, sStart, sEnd, current, next);
	RoleModel.setTagBit(this, RoleModel.BaseclassHasProblems);
    if (isSynthInterface())
    	RoleModel.setTagBit(this.roleModel.getClassPartBinding(), RoleModel.BaseclassHasProblems);
    return ProblemBaseClass;
}
//SH}
@Override
public String toString() {
	if (this.hasTypeAnnotations()) {
		return annotatedDebugName();
    } else {
    	return "Member type : " + new String(sourceName()) + " " + super.toString(); //$NON-NLS-2$ //$NON-NLS-1$
    }
}
@Override
public ModuleBinding module() {
	return this.enclosingType.module();
}
}
