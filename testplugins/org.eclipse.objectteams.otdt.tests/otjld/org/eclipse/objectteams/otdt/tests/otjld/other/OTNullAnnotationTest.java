/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2014 Stephan Herrmann
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
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otjld.other;

import junit.framework.Test;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDNullAnnotationTest;

public class OTNullAnnotationTest extends AbstractOTJLDNullAnnotationTest {
	public OTNullAnnotationTest(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testExplicitTeamAnchor1" };
//			TESTS_NUMBERS = new int[] { 561 };
//			TESTS_RANGE = new int[] { 1, 2049 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_5);
	}

	public static Class testClass() {
		return OTNullAnnotationTest.class;
	}
	
	public void testNullableBase() {
		runNegativeTestWithLibs(
			new String[] {
				"bug443299a/MyTeam.java",
				"package bug443299a;\n" +
				"public team class MyTeam {\n" +
				"	protected class MyRole playedBy MyBase {}\n" +
				"	void test() {\n" +
				"		new MyRole(null);\n" +
				"	}\n" +
				"}\n",
				"bug443299a/MyBase.java",
				"package bug443299a;\n" +
				"public class MyBase {}\n"
			}, 
			getCompilerOptions(),
			"----------\n" + 
			"1. WARNING in bug443299a\\MyTeam.java (at line 5)\n" + 
			"	new MyRole(null);\n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"Argument to lifting constructor MyRole(MyBase) is not a freshly created base object (of type bug443299a.MyBase); may cause a DuplicateRoleException at runtime (OTJLD 2.4.1(c)).\n" + 
			"----------\n" + 
			"2. ERROR in bug443299a\\MyTeam.java (at line 5)\n" + 
			"	new MyRole(null);\n" + 
			"	           ^^^^\n" + 
			"Null type mismatch: required \'@NonNull MyBase\' but the provided value is null\n" + 
			"----------\n");
	}
	
	public void testBug444231() {
		if (this.complianceLevel >= ClassFileConstants.JDK1_8) return; // not ready for type annotations, yet
		Map<String,String> options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_FATAL_OPTIONAL_ERROR, JavaCore.DISABLED);
		runConformTestWithLibs(
			new String[] {
				"b/Base444231.java",
				"package b;\n" +
				"public class Base444231 {}\n",
				"p/Team444231Super.java",
				"package p;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"import base b.Base444231;\n" +
				"public team class Team444231Super {\n" +
				"	protected class R playedBy Base444231 {\n" +
				"		protected R(@NonNull String s) { base(); }\n" +
				"	}\n" +
				"}\n",
				"p/Team444231Sub.java",
				"package p;\n" +
				"public team class Team444231Sub extends Team444231Super {\n" +
				"}\n"
			},
			options,
			"");
		runNegativeTestWithLibs(
			new String[] {
				"p/Team444231SubSub.java",
				"package p;\n" +
				"import p.Team444231Sub;\n" +
				"public team class Team444231SubSub extends Team444231Sub {\n" +
				"	void test() {\n" +
				"		String val = null;\n" +
				"		new R(val);\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" + 
			"1. ERROR in p\\Team444231SubSub.java (at line 6)\n" + 
			"	new R(val);\n" + 
			"	      ^^^\n" + 
			"Null type mismatch: required '@NonNull String' but the provided value is null\n" + 
			"----------\n");
	}
	
	public void testBug444231_ambiguous() {
		if (this.complianceLevel >= ClassFileConstants.JDK1_8) return; // not ready for type annotations, yet
		runNegativeTestWithLibs(
			new String[] {
				"b/Base444231.java",
				"package b;\n" +
				"public class Base444231 {}\n",
				"p/Team444231Super.java",
				"package p;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"import base b.Base444231;\n" +
				"public team class Team444231Super {\n" +
				"	protected class R playedBy Base444231 {\n" +
				"		protected R(@NonNull String s) { base(); }\n" +
				"	}\n" +
				"}\n",
				"p/Team444231Sub.java",
				"package p;\n" +
				"public team class Team444231Sub extends Team444231Super {\n" +
				"	void test() {\n" +
				"		new R(null);\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" + 
			"1. ERROR in p\\Team444231Sub.java (at line 4)\n" + 
			"	new R(null);\n" + 
			"	^^^^^^^^^^^\n" + 
			"The constructor Team444231Sub.R(null) is ambiguous\n" + 
			"----------\n");
	}
}
