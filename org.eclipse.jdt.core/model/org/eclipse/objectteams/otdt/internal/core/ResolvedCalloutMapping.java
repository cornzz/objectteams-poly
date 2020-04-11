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
 * $Id: ResolvedCalloutMapping.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.objectteams.otdt.internal.core.util.MethodData;

public class ResolvedCalloutMapping extends CalloutMapping {
	private String uniqueKey;

	/*
	 * See class comments.
	 */
	public ResolvedCalloutMapping(
			int        declarationStart,
			int        sourceStart,
			int		   sourceEnd,
			int        declarationEnd,
			int 	   elementType,
			IType  role,
			IMethod	corrJavaMethod,
            MethodData roleMethodHandle,
            MethodData baseMethodHandle,
            boolean hasSignature,
            boolean isOverride,
            int	    declaredModifiers,
	        String uniqueKey) {
		super(declarationStart, sourceStart, sourceEnd, declarationEnd, elementType, role, corrJavaMethod, roleMethodHandle, baseMethodHandle, hasSignature, isOverride, declaredModifiers, false);
		this.uniqueKey = uniqueKey;
	}

	@Override
	public String getKey() {
		return this.uniqueKey;
	}

	@Override
	public boolean isResolved() {
		return true;
	}
}
