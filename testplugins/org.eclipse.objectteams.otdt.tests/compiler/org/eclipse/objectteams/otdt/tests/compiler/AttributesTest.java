/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2018 Stephan Herrmann
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

import junit.framework.Test;
import junit.framework.TestCase;

public class AttributesTest extends AbstractOTJLDTest {

	String versionString = null;

	public AttributesTest(String name) {
		super(name);
	}
	public static Class<? extends TestCase> testClass() {
		return AttributesTest.class;
	}
	protected void setUp() throws Exception {
		super.setUp();
		this.versionString = (this.complianceLevel < ClassFileConstants.JDK9)
				? "version 1.8 : 52.0"
				: (this.complianceLevel < ClassFileConstants.JDK10 ? "version 9 : 53.0" : "version 10 : 54.0");
	}


	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "test012";
//		TESTS_NAMES = new String[] { "testBug359495" };
//		TESTS_NUMBERS = new int[] { 53 };
//		TESTS_RANGE = new int[] { 23 -1,};
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	private void assertSubstring(String actualOutput, String expectedOutput) {
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}
	
	// https://bugs.eclipse.org/537772 - Attribute OTCompilerVersion should not be set for o.o.Team
	public void testAttributesInOOTeam() throws Exception {
		Map<String,String> customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_PureJavaOnly, CompilerOptions.ENABLED);
		runConformTest(
				new String[] {
					"org/objectteams/Team.java",
					"package org.objectteams;\n" +
					"public class Team {\n" +
					"}\n"
				},
				customOptions);
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = OUTPUT_DIR + File.separator + "org/objectteams/Team.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.SYSTEM);

		String expectedOutput =
				"// Compiled from Team.java ("+versionString+", super bit)\n" + 
				"public class org.objectteams.Team {\n" + 
				"  Constant pool:\n" + 
				"    constant #1 class: #2 org/objectteams/Team\n" + 
				"    constant #2 utf8: \"org/objectteams/Team\"\n" + 
				"    constant #3 class: #4 java/lang/Object\n" + 
				"    constant #4 utf8: \"java/lang/Object\"\n" + 
				"    constant #5 utf8: \"<init>\"\n" + 
				"    constant #6 utf8: \"()V\"\n" + 
				"    constant #7 utf8: \"Code\"\n" + 
				"    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V\n" + 
				"    constant #9 name_and_type: #5.#6 <init> ()V\n" + 
				"    constant #10 utf8: \"LineNumberTable\"\n" + 
				"    constant #11 utf8: \"LocalVariableTable\"\n" + 
				"    constant #12 utf8: \"this\"\n" + 
				"    constant #13 utf8: \"Lorg/objectteams/Team;\"\n" + 
				"    constant #14 utf8: \"SourceFile\"\n" + 
				"    constant #15 utf8: \"Team.java\"\n" + 
				"    constant #16 utf8: \"OTClassFlags\"\n" + 
				"  \n" + 
				"  // Method descriptor #6 ()V\n" + 
				"  // Stack: 1, Locals: 1\n" + 
				"  public Team();\n" + 
				"    0  aload_0 [this]\n" + 
				"    1  invokespecial java.lang.Object() [8]\n" + 
				"    4  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 2]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 5] local: this index: 0 type: org.objectteams.Team\n" + 
				"\n" + 
				"  Attribute: OTClassFlags Length: 2\n" + 
				"}";

		assertSubstring(actualOutput, expectedOutput);
}
}
