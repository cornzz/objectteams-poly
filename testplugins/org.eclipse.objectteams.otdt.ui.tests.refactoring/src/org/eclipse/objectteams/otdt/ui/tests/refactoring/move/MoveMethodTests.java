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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.refactoring.move;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author brcan
 *
 */
public class MoveMethodTests
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite(MoveMethodTests.class.getName());
        
        suite.addTest(MoveInstanceMethodTests.suite());
        suite.addTest(MoveStaticMethodTests.suite());
        
        return suite;
    }
}
