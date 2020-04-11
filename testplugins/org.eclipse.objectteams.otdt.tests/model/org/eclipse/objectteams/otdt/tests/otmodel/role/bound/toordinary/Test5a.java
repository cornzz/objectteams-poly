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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;

/**
 *
 * This class contains testing methods for a test setting with a role class with a method
 * and a method mapping
 * whereas the method (is abstract and) has no parameters,
 * the method mapping is a callout mapping (->)
 * and the role class is bound to a baseclass
 */
public class Test5a extends Test5_MethodMappingGeneral
{
    public Test5a(String name)
    {
        super(name);
        setMappingName("roleMethod -> baseMethod");
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test5a.class);
        }
        junit.framework.TestSuite suite = new Suite(Test5a.class
            .getName());
        return suite;
    }

    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        getTestSetting().setTeamClass("Team_5a");
    }

    public void testMappingCalloutProperty() throws JavaModelException
    {
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());

        IOTType roleOTElem = OTModelManager.getOTElement(getTestSetting().getRoleJavaElement());
        assertNotNull(roleOTElem);
        assertTrue(roleOTElem instanceof IRoleType);
        IRoleType roleRoleOTElem = (IRoleType) roleOTElem;

        IMethodMapping[] mappings = roleRoleOTElem.getMethodMappings();
        assertTrue(mappings.length == 1);

        assertTrue(mappings[0].getMappingKind() == IOTJavaElement.CALLOUT_MAPPING);
        assertTrue(mappings[0] instanceof ICalloutMapping);
    }


    public void testMappingPropertyBoundBaseMethod() throws JavaModelException
    {
//      TODO(jwl): Resource access hardcoded here!
        ICompilationUnit baseUnit = getCompilationUnit(
                getTestProjectDir(),
                "boundtoordinary",
                "boundtoordinary.basepkg",
                "SampleBase.java");

        IType baseJavaElem = baseUnit.getType("SampleBase");
        assertNotNull(baseJavaElem);
        assertTrue(baseJavaElem.exists());

        IMethod baseMethod = baseJavaElem.getMethod(getBaseMethodName(), new String[0]);
        assertNotNull(baseMethod);
        assertTrue(baseMethod.exists());

        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());

        IOTType roleOTElem = OTModelManager.getOTElement(getTestSetting().getRoleJavaElement());
        assertNotNull(roleOTElem);
        assertTrue(roleOTElem instanceof IRoleType);
        IRoleType roleRoleOTElem = (IRoleType) roleOTElem;

        IMethodMapping[] mappings = roleRoleOTElem.getMethodMappings();
        assertTrue(mappings.length == 1);
        assertTrue(mappings[0] instanceof ICalloutMapping);

        ICalloutMapping calloutMapping = (ICalloutMapping) mappings[0];
        assertNotNull(calloutMapping);
        IMethod boundBaseMethod = calloutMapping.getBoundBaseMethod();

        assertEquals(boundBaseMethod, baseMethod);
    }
}
