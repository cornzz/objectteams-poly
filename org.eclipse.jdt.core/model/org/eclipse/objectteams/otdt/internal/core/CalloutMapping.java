/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2013 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany, and others.
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
package org.eclipse.objectteams.otdt.internal.core;


import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.IMethodSpec;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.internal.core.util.MethodData;


/**
 * Callout Mapping implementation
 * @author jwloka
 */
public class CalloutMapping extends AbstractCalloutMapping implements ICalloutMapping
{
	private boolean    isOverride;
	private IMethod    baseMethod;
	private MethodData baseMethodHandle; // Note: may be null!
	private int		   declaredModifiers;

    public CalloutMapping(int        declarationSourceStart,
    					  int        sourceStart,
    					  int        sourceEnd,
						  int        declarationSourceEnd,
						  IType  	 role,
						  IMethod	 corrJavaMethod,
                          MethodData roleMethodHandle,
                          MethodData baseMethodHandle,
                          boolean hasSignature,
                          boolean isOverride,
                          int     declaredModifiers,
                          boolean addAsChild)
    {
    	// FIXME(SH): can we use 'this' as the corrJavaMethod??
        this(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, CALLOUT_MAPPING, role, corrJavaMethod, roleMethodHandle, baseMethodHandle, hasSignature, isOverride, declaredModifiers, addAsChild);
    }

    protected CalloutMapping(
            int        declarationSourceStart,
			int        sourceStart,
			int		   sourceEnd,
			int        declarationSourceEnd,
			int        elementType,
			IType      parentRole,
			IMethod	   corrJavaMethod,
            MethodData roleMethodHandle,
            MethodData baseMethodHandle,
            boolean hasSignature,
            boolean isOverride,
            int	    declaredModifiers)
	{
		super(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, elementType, corrJavaMethod, parentRole, roleMethodHandle, hasSignature);

		this.isOverride = isOverride;
		this.baseMethodHandle = baseMethodHandle;
		this.declaredModifiers = declaredModifiers;
	}

    protected CalloutMapping(
            int        declarationSourceStart,
			int        sourceStart,
			int		   sourceEnd,
			int        declarationSourceEnd,
			int        elementType,
			IType      parentRole,
			IMethod	   corrJavaMethod,
            MethodData roleMethodHandle,
            MethodData baseMethodHandle,
            boolean hasSignature,
            boolean isOverride,
            int     declaredModifiers,
            boolean addAsChild)
	{
		super(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, elementType, corrJavaMethod, parentRole, roleMethodHandle, hasSignature, addAsChild);

		this.isOverride = isOverride;
		this.baseMethodHandle = baseMethodHandle;
		this.declaredModifiers = declaredModifiers;
	}

    @Override
	public boolean isOverride() {
    	return this.isOverride;
    }

	@Override
	@SuppressWarnings("nls")
	public String getElementName()
	{
		StringBuffer name = new StringBuffer(super.getElementName());
		name.append(" -> ");

		if (this.baseMethodHandle == null)
		{
			name.append("(unknown)");
		}
		else
		{
			if (hasSignature())
			{
				name.append(this.baseMethodHandle.toString());
			}
			else
			{
				name.append(this.baseMethodHandle.getSelector());
			}
		}

	    return name.toString();
	}

	@Override
	public int getMappingKind()
	{
		return CALLOUT_MAPPING;
	}

	@Override
	public int getDeclaredModifiers() {
		return this.declaredModifiers;
	}

	@Override
	public int getFlags() throws JavaModelException {
		return this.declaredModifiers;
	}

	@Override
	public IMethod getBoundBaseMethod() throws JavaModelException
	{
		// TODO (carp/jwl): does reconciling throw away the cached _baseMethod or will this stay forever?
		if (this.baseMethod == null)
		{
            this.baseMethod = findBaseMethod();
		}

		return this.baseMethod;
	}

	@Override
	public boolean equals(Object obj)
    {
		if(!(obj instanceof CalloutMapping))
		{
		    return false;
		}

		return super.equals(obj);
    }

    @Override
	@SuppressWarnings("nls")
	public String toString()
	{
		return "callout " + super.toString();
	}

	/**
	 * Performs resolving of bound base method
	 */
    private IMethod findBaseMethod() throws JavaModelException
    {
    	if (this.baseMethodHandle == null)
    		return null;

    	IRoleType roleClass = getDeclaringRole();
    	IType   baseClass   = roleClass.getBaseClass();
		IType[] typeParents = TypeHelper.getSuperTypes(baseClass);

    	return findMethod(typeParents, this.baseMethodHandle);
    }

	// added for the SourceTypeConverter
    @Override
	public IMethodSpec getBaseMethodHandle()
    {
    	return this.baseMethodHandle;
    }
    // ==== memento generation: ====
    @Override
    protected char getMappingKindChar() {
    	if (this.isOverride)
    		return 'O';
    	return 'o';
    }
    @Override
    protected void getBaseMethodsForHandle(StringBuffer buff) {
    	if (this.baseMethodHandle != null) // as documented, _baseMethodHandle can be null: no base methods to encode
    		getMethodForHandle(this.baseMethodHandle, buff);
    }
    // ====

	// implementation and alternate API of resolved(Binding)
	@Override
	public OTJavaElement resolved(char[] uniqueKey) {
		ResolvedCalloutMapping resolvedHandle =
			new ResolvedCalloutMapping(
					getDeclarationSourceStart(),
					getSourceStart(),
					getSourceEnd(),
			    	getDeclarationSourceEnd(),
			    	getElementType(),
			        (IType) getParent(),
			    	getIMethod(),
			        getRoleMethodHandle(),
			        this.baseMethodHandle,
			        hasSignature(),
			        isOverride(),
			        getDeclaredModifiers(),
					new String(uniqueKey));

		return resolvedHandle;
	}

	@Override
	public String[] getExceptionTypes() throws JavaModelException
	{
		if (   this.roleMethodHandle != null
			&& this.roleMethodHandle.hasSignature())
		{
			try {
				return getIMethod().getExceptionTypes();
			} catch (JavaModelException jme) {
				return new String[0]; // stealth for shorthand has no exception types
			}
		}
	    return getIMethod().getExceptionTypes();
	}

	@Override
	public ILocalVariable[] getParameters() throws JavaModelException {
		// TODO Auto-generated method stub
		// see Bug 338593 - [otmodel] Add new API to ease the retrieval of the parameter annotations for an IMethodMapping
		return null;
	}
}
