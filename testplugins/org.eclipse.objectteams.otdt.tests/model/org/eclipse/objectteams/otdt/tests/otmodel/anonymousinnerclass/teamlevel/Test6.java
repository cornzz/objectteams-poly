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
package org.eclipse.objectteams.otdt.tests.otmodel.anonymousinnerclass.teamlevel;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 *
 * testcase:
 * a team class with a method
 * instantiation of an anonymous class inside the method
 * the anonymous class is a team class with role class
 */
public class Test6 extends FileBasedModelTest
{

    private IType _teamJavaElem = null;

    public Test6(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test6.class);
        }
        junit.framework.TestSuite suite = new Suite(Test6.class
            .getName());
        return suite;
    }

    public void setUpSuite() throws Exception
    {
        setTestProjectDir("AnonymousInnerclass");
        super.setUpSuite();
    }

    protected void setUp() throws Exception
    {
		super.setUp();
        try
        {
            ICompilationUnit teamUnit = getCompilationUnit(
                "AnonymousInnerclass",
                "teamlevel",
                "teamlevel.teampkg",
                "Test6_TeamB.java");
            _teamJavaElem = teamUnit.getType("Test6_TeamB");

            System.out.println("\nAnonymousInnerclassTeamLevelTest6:");
            System.out.println("Teamklasse:\n" +_teamJavaElem);
        }
        catch (JavaModelException ex)
        {
            ex.printStackTrace();
        }
    }


    public void testExistenceOfAnonymousType() throws JavaModelException
    {
        assertNotNull(_teamJavaElem);
        assertTrue(_teamJavaElem.exists());

        IMethod teamlevelMethod = _teamJavaElem.getMethod("teamlevelMethod", new String[0]);
        assertNotNull(teamlevelMethod);
        assertTrue(teamlevelMethod.exists());

        IType anonymousType = teamlevelMethod.getType("",1);
        assertNotNull(anonymousType);
        assertTrue(anonymousType.exists());
    }

    private IType getAnonymousType() throws JavaModelException
    {
        if ((_teamJavaElem != null) && (_teamJavaElem.exists()))
        {
            IMethod teamlevelMethod = _teamJavaElem.getMethod("teamlevelMethod", new String[0]);

            if ((teamlevelMethod != null) && (teamlevelMethod.exists()))
            {
                IType anonymousType = teamlevelMethod.getType("",1);

                if ((anonymousType != null) && (anonymousType.exists()))
                {
                    return anonymousType;
                }
            }
        }
        return null;
    }

    public void testExistenceOfAnonymousTypeInOTModel() throws JavaModelException
    {
        IType anonymousTypeJavaElem = getAnonymousType();
        assertNotNull(anonymousTypeJavaElem);

        IOTType anonymousTypeOTElem = OTModelManager.getOTElement(anonymousTypeJavaElem);
        assertNotNull(anonymousTypeOTElem);
    }

    public void testTeamPropertyOfAnonymousType() throws JavaModelException
    {
        IType anonymousTypeJavaElem = getAnonymousType();
        assertNotNull(anonymousTypeJavaElem);

        IOTType anonymousTypeOTElem = OTModelManager.getOTElement(anonymousTypeJavaElem);
        assertNotNull(anonymousTypeOTElem);

        assertTrue(anonymousTypeOTElem.isTeam());
    }

    public void testContainmentOfRoleInAnonymousType() throws JavaModelException
    {
        IType anonymousTypeJavaElem = getAnonymousType();
        assertNotNull(anonymousTypeJavaElem);

        IOTType anonymousTypeOTElem = OTModelManager.getOTElement(anonymousTypeJavaElem);
        assertNotNull(anonymousTypeOTElem);
        assertTrue(anonymousTypeOTElem.isTeam());

//{OTModelUpdate
        IOTType[] innerTypes = (IOTType[]) anonymousTypeOTElem.getInnerTypes();
//haebor}

        assertNotNull(innerTypes);
        assertTrue(innerTypes.length == 1);
        assertTrue(innerTypes[0].isRole());
        assertEquals(innerTypes[0].getElementName(), "RoleClass");
    }
}
