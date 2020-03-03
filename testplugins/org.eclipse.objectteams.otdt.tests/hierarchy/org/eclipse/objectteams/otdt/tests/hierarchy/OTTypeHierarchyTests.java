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
package org.eclipse.objectteams.otdt.tests.hierarchy;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author Michael Krueger (mkr)
 * @version $Id: OTTypeHierarchyTests.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 */
public class OTTypeHierarchyTests
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite("All OTTypeHierarchy Tests");

        //$JUnit-BEGIN$
        suite.addTest(org.eclipse.objectteams.otdt.tests.superhierarchy.AllTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.tests.subhierarchy.AllTests.suite());
        //$JUnit-END$

        return suite;
    }
}