/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * $Id$
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Fraunhofer FIRST - Initial API and implementation
 * 	   Technical University Berlin - Initial API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.dom.rewrite.describing;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class CallinMappingDeclarationTest extends AstRewritingDescribingTest {

	public CallinMappingDeclarationTest(String name) {
		super(name);
	}

	public CallinMappingDeclarationTest(String name, int apilevel) {
		super(name, apilevel);
	}

	@SuppressWarnings("deprecation")
	public static Test suite() {
		if (true) {
			return createSuite(CallinMappingDeclarationTest.class, AST.JLS3);
		}
		TestSuite suite= new Suite("one test");
		suite.addTest(new CallinMappingDeclarationTest("test0009")); // FIXME? Used?
		return suite;
	}


	// copied from org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingMethodDeclTest
	// and syntactically adjusted for callins
	public void testModifiersAST3WithAnnotations() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team abstract class T {\n");
		buf.append("public abstract class E playedBy F {\n");
		buf.append("    Object foo1() <- after Object bfoo1();\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    @Deprecated\n");
		buf.append("    Object foo2() <- after Object bfoo2();\n");
		buf.append("    @ToBeRemoved\n");
		buf.append("    Object foo3() <- after Object bfoo3();\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    @ToBeRemoved\n");
		buf.append("    @Deprecated\n");
		buf.append("    Object foo4() <- after Object bfoo4();\n");
		buf.append("}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		RoleTypeDeclaration type= findRoleTypeDeclaration(findTypeDeclaration(astRoot, "T"), "E");

		{ // insert annotation first before normal
			CallinMappingDeclaration mappingDecl= findCallinMappingDeclaration(type, "foo1");
			ListRewrite listRewrite= rewrite.getListRewrite(mappingDecl, CallinMappingDeclaration.MODIFIERS2_PROPERTY);
			MarkerAnnotation annot= ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Override"));
			listRewrite.insertFirst(annot, null);
		}
		{ // insert annotation first before annotation
			CallinMappingDeclaration mappingDecl= findCallinMappingDeclaration(type, "foo2");
			ListRewrite listRewrite= rewrite.getListRewrite(mappingDecl, CallinMappingDeclaration.MODIFIERS2_PROPERTY);
			MarkerAnnotation annot= ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Override"));
			listRewrite.insertFirst(annot, null);
		}
		{ // remove annotation before normal
			CallinMappingDeclaration mappingDecl= findCallinMappingDeclaration(type, "foo3");
			ListRewrite listRewrite= rewrite.getListRewrite(mappingDecl, CallinMappingDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.remove((ASTNode) mappingDecl.modifiers().get(0), null);
		}
		{ // remove annotation before annotation
			CallinMappingDeclaration mappingDecl= findCallinMappingDeclaration(type, "foo4");
			ListRewrite listRewrite= rewrite.getListRewrite(mappingDecl, CallinMappingDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.remove((ASTNode) mappingDecl.modifiers().get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team abstract class T {\n");
		buf.append("public abstract class E playedBy F {\n");
		buf.append("    @Override\n");
		buf.append("    Object foo1() <- after Object bfoo1();\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    @Override\n");
		buf.append("    @Deprecated\n");
		buf.append("    Object foo2() <- after Object bfoo2();\n");
		buf.append("    Object foo3() <- after Object bfoo3();\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    @Deprecated\n");
		buf.append("    Object foo4() <- after Object bfoo4();\n");
		buf.append("}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}


}
