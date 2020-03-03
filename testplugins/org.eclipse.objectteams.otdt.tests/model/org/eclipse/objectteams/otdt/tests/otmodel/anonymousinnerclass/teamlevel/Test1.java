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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * 
 * testcase:
 * a team class with an attribut
 * attribut assignment with instantiation of an anonymous class
 * the anonymous class is an ordinary class with a method
 */
public class Test1 extends FileBasedModelTest
{
    
    private IType _teamJavaElem = null;
    
    public Test1(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test1.class);
        }
        junit.framework.TestSuite suite = new Suite(Test1.class
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
                "Test1_SampleTeam.java");
            _teamJavaElem = teamUnit.getType("Test1_SampleTeam");
            
            System.out.println("\nAnonymousInnerclassTeamLevelTest1:");
            System.out.println("Teamklasse: " +_teamJavaElem);
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
       
        IField teamlevelAttr = _teamJavaElem.getField("teamlevelAttr");
        assertNotNull(teamlevelAttr);
        assertTrue(teamlevelAttr.exists());
        
        IType anonymousType = teamlevelAttr.getType("",1);
        assertNotNull(anonymousType);
        assertTrue(anonymousType.exists());
    }
 
    private IType getAnonymousType() throws JavaModelException
    {
        if ((_teamJavaElem != null) && (_teamJavaElem.exists()))
        {
            IField teamlevelAttr = _teamJavaElem.getField("teamlevelAttr");
            
            if ((teamlevelAttr != null) && (teamlevelAttr.exists()))
            {
                IType anonymousType = teamlevelAttr.getType("",1);
                
                if ((anonymousType != null) && (anonymousType.exists()))
                {
                    return anonymousType;
                }
            }
        }
        return null;
    }        
    
    public void testContainmentOfMethodInAnonymousType() throws JavaModelException
    {
        IType anonymousType = getAnonymousType();
        assertNotNull(anonymousType);
        
        IMethod methodOfAnonymousType = anonymousType.getMethod("additionalMethod", new String[0]);
        assertNotNull(methodOfAnonymousType);
        assertTrue(methodOfAnonymousType.exists());
    }    
}
