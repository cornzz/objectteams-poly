/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2012 Fraunhofer Gesellschaft, Munich, Germany,
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

import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.core.util.MethodData;

/**
 * Common super-class of callout and callout-to-field.
 * Purpose: implement a view of this mapping that can be used for searching.
 * This view pretends to be a method
 * (actually short-hand callouts indeed are translated to a method ;-)
 *
 * @author stephan
 */
public abstract class AbstractCalloutMapping extends MethodMapping implements IMethod {

	public AbstractCalloutMapping(int declarationSourceStart,
								  int sourceStart,
								  int sourceEnd,
								  int declarationSourceEnd,
								  int        elementType,
								  IMethod    corrJavaMethod,
								  IType      parentRole,
								  MethodData roleMethodHandle,
								  boolean    hasSignature)
	{
		super(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd,
			  elementType, corrJavaMethod, parentRole,
			  roleMethodHandle,
			  hasSignature);
	}

	public AbstractCalloutMapping(int declarationSourceStart,
								  int sourceStart,
								  int sourceEnd,
								  int declarationSourceEnd,
								  int elementType,
								  IMethod corrJavaMethod,
								  IType parentRole,
								  MethodData roleMethodHandle,
								  boolean hasSignature,
								  boolean addAsChild)
	{
		super(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd,
				  elementType, corrJavaMethod, parentRole,
				  roleMethodHandle,
				  hasSignature, addAsChild);
	}

    public IMethodMapping getOriginalMethodMapping()
    {
        return this;
    }

    @Override
	protected IRoleType getDeclaringRole() {
 	   return (IRoleType) OTModelManager.getOTElement((IType) getParent());
    }

	@Override
	public IMemberValuePair getDefaultValue() throws JavaModelException {
		// callout mappings are not used in annotation types ;-)
		return null;
	}
}
