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
package org.eclipse.objectteams.otdt.debug.tests.core;

import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.objectteams.otdt.debug.tests.AbstractOTDTDebugTest;

/**
 * Tests strata.
 */
public class StratumTests extends AbstractOTDTDebugTest {

	public StratumTests(String name) {
		super(name);
	}

	/**
	 * Test available strata on a type.
	 *
	 * @throws Exception
	 */
	public void testAvailableStrata() throws Exception {
		String typeName = "Breakpoints";
		createLineBreakpoint(81, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaReferenceType type = ((IJavaStackFrame)thread.getTopStackFrame()).getReferenceType();
			String[] strata = type.getAvailableStrata();
			assertEquals("Wrong number of available strata", 2, strata.length);
			assertEquals("Wrong strata", "jdt", strata[0]);
			assertEquals("Wrong strata", "Java", strata[1]);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Test default stratum on a type.
	 *
	 * @throws Exception
	 */
	public void testDefaultStratum() throws Exception {
		String typeName = "Breakpoints";
		createLineBreakpoint(81, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaReferenceType type = ((IJavaStackFrame)thread.getTopStackFrame()).getReferenceType();
			String stratum = type.getDefaultStratum();
			assertEquals("Wrong strata", "Java", stratum);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Test set / get default stratum on a java debug target.
	 *
	 * @throws Exception
	 */
	public void testSetGetDefaultStratum() throws Exception {
		String typeName = "Breakpoints";
		createLineBreakpoint(81, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaDebugTarget debugTarget= (IJavaDebugTarget)thread.getDebugTarget();
			String stratum= debugTarget.getDefaultStratum();
			assertNull("Default strata should be 'null'", stratum);
			debugTarget.setDefaultStratum("strataTest");
			stratum= debugTarget.getDefaultStratum();
			assertEquals("Wrong strata", "strataTest", stratum);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testGetLineInStratum() throws Exception {
		String typeName= "Breakpoints";
		createLineBreakpoint(81, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaStackFrame stackFrame= (IJavaStackFrame)thread.getTopStackFrame();
			int lineNumber= stackFrame.getLineNumber("Java");
			assertEquals("Wrong line number", 81, lineNumber);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testGetSourceNameInStratum() throws Exception {
		String typeName= "Breakpoints";
		createLineBreakpoint(81, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaStackFrame stackFrame= (IJavaStackFrame)thread.getTopStackFrame();
			String sourceName= stackFrame.getSourceName("Java");
			assertEquals("Wrong source name", "Breakpoints.java", sourceName);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
