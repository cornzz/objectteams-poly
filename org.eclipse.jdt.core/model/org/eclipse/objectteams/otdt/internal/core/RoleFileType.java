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
 * $Id: RoleFileType.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.objectteams.otdt.core.IRoleFileType;

/**
 * @author gis
 */
public class RoleFileType extends RoleType implements IRoleFileType
{
    public RoleFileType(IType correspondingJavaType,
			IJavaElement parent,
			int flags,
			String baseClassName,
			String baseClassAnchor)
	{
	    super(correspondingJavaType, parent, flags, baseClassName, baseClassAnchor);
	}

    @Override
	public IType getTeamJavaType() {
        // a role file's team is not its parent (which is an ICompilationUnit)!
        try {
            return getJavaProject().findType(getPackageFragment().getElementName());
        }
    	catch (JavaModelException ex)
    	{
    	    Util.log(ex);
    	}

    	return null;    	
    }
    
    @Override
	public boolean isRoleFile()
    {
        return true;
    }
    
    @Override
    public String getFullyQualifiedName(char enclosingTypeSeparator) {
    	IType teamType = this.getTeamJavaType();
    	if (teamType == null)
    		return null; // inconsistency detected
    	String enclName = teamType.getFullyQualifiedName(enclosingTypeSeparator);
    	return enclName+String.valueOf(enclosingTypeSeparator)+this.getElementName();
    }
}
