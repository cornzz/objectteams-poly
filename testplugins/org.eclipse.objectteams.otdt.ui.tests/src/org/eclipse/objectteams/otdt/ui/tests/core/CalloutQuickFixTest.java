/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2016 Fraunhofer Gesellschaft, Munich, Germany,
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
package org.eclipse.objectteams.otdt.ui.tests.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.tests.core.rules.ProjectTestSetup;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test OT-specific quickfixes and quick assists (here: callout related issues).
 * @author stephan
 * @since 1.2.8
 */
@RunWith(JUnit4.class)
public class CalloutQuickFixTest extends OTQuickFixTest {

	@Rule
    public ProjectTestSetup projectsetup = new ProjectTestSetup();

	@Override
	protected void addOptions(Hashtable options) {
		super.addOptions(options);
		options.put(OTDTPlugin.OT_COMPILER_INFERRED_CALLOUT, JavaCore.WARNING);
	}
	
	/* Converting a field read to explicitly use a callout-to-field. */
	@Test
	public void testConvertFieldAccessToCalloutCall1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public int val;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(val);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList<IJavaCompletionProposal> proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);

		String[] expectedProposals = new String[3];
		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        int getVal() -> get int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(getVal());\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();

		assertEquals("proposal label", "Configure problem severity", proposals.get(1).getDisplayString());
		
		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        @SuppressWarnings(\"inferredcallout\")\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(val);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[2] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);
	}

		
	/* Converting a field read (this-qualified) to explicitly use a callout-to-field. */
	@Test
	public void testConvertFieldAccessToCalloutCall2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public int val;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(this.val);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList<IJavaCompletionProposal> proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		
		String[] expectedProposals = new String[3];

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        int getVal() -> get int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(this.getVal());\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();

		assertEquals("proposal label", "Configure problem severity", proposals.get(1).getDisplayString());

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        @SuppressWarnings(\"inferredcallout\")\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(this.val);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[2] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);

	}
	
	/* Converting a field assignment to explicitly use a callout-to-field. */
	@Test
	public void testConvertFieldAccessToCalloutCall3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public int val;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void foo(){\n");
		buf.append("        	val = 3;\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList<IJavaCompletionProposal> proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		
		String[] expectedProposals = new String[3];

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void setVal(int val) -> set int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	setVal(3);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();

		assertEquals("proposal label", "Configure problem severity", proposals.get(1).getDisplayString());

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        @SuppressWarnings(\"inferredcallout\")\n");
		buf.append("        void foo(){\n");
		buf.append("        	val = 3;\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[2] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);

	}
	
	/* Converting a field assignment (this-qualified) to explicitly use a callout-to-field. */
	@Test
	public void testConvertFieldAccessToCalloutCall4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public int val;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void foo(){\n");
		buf.append("        	this.val = 3;\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList<IJavaCompletionProposal> proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		
		String[] expectedProposals = new String[3];

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void setVal(int val) -> set int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	this.setVal(3);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		expectedProposals[0] = buf.toString();

		assertEquals("proposal label", "Configure problem severity", proposals.get(1).getDisplayString());

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        @SuppressWarnings(\"inferredcallout\")\n");
		buf.append("        void foo(){\n");
		buf.append("        	this.val = 3;\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[2] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);
	}
	
	
	/* Convert field access to call to existing callout-to-field. */
	@Test
	public void testConvertFieldAccessToCalloutCall5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public int val;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        int getVal() -> get int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(val);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList<IJavaCompletionProposal> proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		
		String[] expectedProposals = new String[3];


		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        int getVal() -> get int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(getVal());\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();

		assertEquals("proposal label", "Configure problem severity", proposals.get(1).getDisplayString());

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        int getVal() -> get int val;\n");
		buf.append("\n");
		buf.append("        @SuppressWarnings(\"inferredcallout\")\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(val);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[2] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);
	}

	/* Convert field assignment to call to existing callout-to-field. */
	@Test
	public void testConvertFieldAccessToCalloutCall6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public int val;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void setVal(int val) -> set int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	this.val = 3;\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList<IJavaCompletionProposal> proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 3);
		assertCorrectLabels(proposals);
		
		String[] expectedProposals = new String[3];


		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void setVal(int val) -> set int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	this.setVal(3);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();

		assertEquals("proposal label", "Configure problem severity", proposals.get(1).getDisplayString());

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void setVal(int val) -> set int val;\n");
		buf.append("\n");
		buf.append("        @SuppressWarnings(\"inferredcallout\")\n");
		buf.append("        void foo(){\n");
		buf.append("        	this.val = 3;\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[2] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);
	}
	

	/* Remove signatures from a callout to field. */
	@Test
	public void testRemoveSignatures1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    String foo;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        abstract String getFoo();\n");
		buf.append("		String getFoo() -> get String foo;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		int offset= buf.toString().indexOf("getFoo() -> get");
		AssistContext context= getCorrectionContext(cuteam, offset, 0);
		List<IJavaCompletionProposal> proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		String[] expectedProposals = new String[1];
		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        abstract String getFoo();\n");
		buf.append("		getFoo -> get foo;\n"); // removed signatures
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);
	}

	/* Add signatures to a callout. */
	@Test
	public void testAddSignatures1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    String foo(Object o) { return null; }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        abstract String getFoo(Boolean b);\n");
		buf.append("		getFoo -> foo;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		int offset= buf.toString().indexOf("getFoo -> foo");
		AssistContext context= getCorrectionContext(cuteam, offset, 0);
		List<IJavaCompletionProposal> proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		String[] expectedProposals = new String[1];
		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        abstract String getFoo(Boolean b);\n");
		buf.append("		String getFoo(Boolean b) -> String foo(Object o);\n"); // added signatures
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);
	}

	/* Add signatures to a callout to field. */
	@Test
	public void testAddSignatures2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    String foo;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        abstract String getFoo();\n");
		buf.append("		getFoo -> get foo;\n"); 
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		int offset= buf.toString().indexOf("getFoo -> get");
		AssistContext context= getCorrectionContext(cuteam, offset, 0);
		List<IJavaCompletionProposal> proposals= collectAssists(context, false);

		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		String[] expectedProposals = new String[1];
		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        abstract String getFoo();\n");
		buf.append("		String getFoo() -> get String foo;\n"); // added signatures
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);
	}

}
