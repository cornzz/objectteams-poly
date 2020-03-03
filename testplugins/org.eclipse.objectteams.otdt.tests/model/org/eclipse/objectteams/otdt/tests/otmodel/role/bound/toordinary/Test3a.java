/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
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
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toordinary;

import junit.framework.Test;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author kaschja
 * @version $Id: Test3a.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * This class contains testing methods for a test setting with a role class with a method
 * whereas the method has no parameters
 * and the role class is bound to a baseclass
 */
public class Test3a extends Test1
{
    protected final String METHOD_NAME   = "roleMethod"; 
    
    public Test3a(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test3a.class);
        }
        junit.framework.TestSuite suite = new Suite(Test3a.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        getTestSetting().setTeamClass("Team_3a");
    }
    
    public void testContainmentOfMethodInRoleClass() throws JavaModelException
    {
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());

        IMethod method = getTestSetting().getRoleJavaElement().getMethod(METHOD_NAME, new String[0]);
        assertNotNull(method);
        assertTrue(method.exists());        
    }
}
