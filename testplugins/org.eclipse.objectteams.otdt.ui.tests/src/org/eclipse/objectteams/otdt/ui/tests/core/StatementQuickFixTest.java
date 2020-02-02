/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.core;

import java.util.ArrayList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.objectteams.otdt.ui.tests.core.rule.ProjectTestSetup;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StatementQuickFixTest extends OTQuickFixTest {
	
	@Rule
    public ProjectTestSetup projectsetup = new ProjectTestSetup();
	
	/* add a return statement to a role method (callin).
	 * See https://bugs.eclipse.org/311339
	 */
	@Test
	public void testAddReturnInCallinMethod() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public void foo() {};\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        @SuppressWarnings(\"basecall\")\n");
		buf.append("        callin boolean foo(){\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList<IJavaCompletionProposal> proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		String[] expectedProposals = new String[2];
		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        @SuppressWarnings(\"basecall\")\n");
		buf.append("        callin boolean foo(){\n");
		buf.append("            return false;\n"); // don't propose internal fields like _OT$cacheInitTrigger
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();
		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        @SuppressWarnings(\"basecall\")\n");
		buf.append("        callin void foo(){\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[1] = buf.toString();


		assertExpectedExistInProposals(proposals, expectedProposals);
	}

}
