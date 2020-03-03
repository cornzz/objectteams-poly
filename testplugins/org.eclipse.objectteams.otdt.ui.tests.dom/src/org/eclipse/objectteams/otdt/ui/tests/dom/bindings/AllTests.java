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
package org.eclipse.objectteams.otdt.ui.tests.dom.bindings;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author anklam
 *
 * @version $Id: AllTests.java 23496 2010-02-05 23:20:15Z stephan $
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"All DOM-Bindings Tests");
		//$JUnit-BEGIN$
		suite.addTest(MethodMappingBindingTest.suite());
		//$JUnit-END$
		return suite;
	}
}
