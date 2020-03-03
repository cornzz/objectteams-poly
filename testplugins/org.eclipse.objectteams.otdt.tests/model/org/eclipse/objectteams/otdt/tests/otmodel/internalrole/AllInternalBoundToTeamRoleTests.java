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
package org.eclipse.objectteams.otdt.tests.otmodel.internalrole;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.objectteams.otdt.tests.otmodel.RetargetableFileBasedModelTest;
import org.eclipse.objectteams.otdt.tests.otmodel.TestDataHandler;
import org.eclipse.objectteams.otdt.tests.otmodel.TestSetting;

/**
 * @author jwloka 
 * @version $Id: AllInternalBoundToTeamRoleTests.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class AllInternalBoundToTeamRoleTests extends TestCase
{
    public AllInternalBoundToTeamRoleTests(String name) 
    {
        super(name);
    }

    public static Class[] getAllTestClasses()
    {
        return new Class[]
        {
            org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam.Test1.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam.Test2.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam.Test3a.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam.Test3b.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam.Test3c.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam.Test3d.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam.Test4a.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam.Test4b.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam.Test5a.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam.Test5b.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam.Test5c.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam.Test5d.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam.Test5e.class
        };
    }
    
    public static TestSetting getActualTestSetting()
    {
        return new TestSetting(
            				"InternalDefinedRole",
            				"boundtoteam",
            				"boundtoteam.teampkg");
    }
    
    
    public static Test suite()
    {
        TestSuite ts = new TestSuite(AllInternalBoundToTeamRoleTests.class.getName()){
        	@Override
        	public void run(TestResult result) {
        		RetargetableFileBasedModelTest.setProjectDirectory("InternalDefinedRole");
        		super.run(result);
        		RetargetableFileBasedModelTest.setProjectDirectory(null);
        	}
        };

        Class[] testClasses = getAllTestClasses();
        // Reset forgotten subsets of tests
//        AbstractJavaModelTests.testsNames = null;
//        AbstractJavaModelTests.testsNumbers = null;
//        AbstractJavaModelTests.testsRange = null;

        // call the suite() method and add the resulting suite to the suite
        for (int idx = 0; idx < testClasses.length; idx++)
        {
            Class<?> curTestClass = testClasses[idx];

            try
            {
                TestDataHandler.addTestSetting(curTestClass, getActualTestSetting());
                
                Method suiteMethod = curTestClass.getDeclaredMethod(
                    "suite", new Class[0]); //$NON-NLS-1$
                Test suite = (Test) suiteMethod.invoke(null, new Object[0]);
                ts.addTest(suite);
            }
            catch (IllegalAccessException ex)
            {
                ex.printStackTrace();
            }
            catch (InvocationTargetException ex)
            {
                ex.getTargetException().printStackTrace();
            }
            catch (NoSuchMethodException ex)
            {
                ex.printStackTrace();
            }
        }
        return ts;
    }
}
