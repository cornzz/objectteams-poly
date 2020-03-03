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
package org.eclipse.objectteams.otdt.ui.tests.dom.rewrite.describing;

import java.util.List;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class TypeDeclarationTest extends AstRewritingDescribingTest {

	public TypeDeclarationTest(String name) {
		super(name);
	}

	public TypeDeclarationTest(String name, int apilevel) {
		super(name, apilevel);
	}

	@SuppressWarnings("deprecation")
	public static Test suite() {
		return createSuite(TypeDeclarationTest.class, AST.JLS3);
	}
	
	
	public void testRoleTypeDeclChanges1() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class T {\n");
		buf.append("    public class R extends java.util.List implements Runnable, Serializable playedBy String {\n");
		buf.append("    }\n");		
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T.java", buf.toString(), false, null);			

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		
		{  // change to protected team, rename type, rename supertype, rename first interface, rename base-class
			TypeDeclaration teamType= findTypeDeclaration(astRoot, "T");
			RoleTypeDeclaration roleType= findRoleTypeDeclaration(teamType, "R");

			// change flags
			rewrite.getListRewrite(roleType, RoleTypeDeclaration.MODIFIERS2_PROPERTY).replace(
												(Modifier)roleType.modifiers().get(0), 
												ast.newModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD), 
												null);

			// change to team
			rewrite.set(roleType, RoleTypeDeclaration.TEAM_PROPERTY, Boolean.TRUE, null);

			// change name
			SimpleName name= roleType.getName();
			SimpleName newName= ast.newSimpleName("R1");
			rewrite.replace(name, newName, null);
			
			Type superClass= roleType.getSuperclassType();
			assertTrue("Has super type", superClass != null);
			SimpleType newSuperclass= ast.newSimpleType(ast.newSimpleName("Object"));
			rewrite.replace(superClass, newSuperclass, null);

			List superInterfaces= roleType.superInterfaceTypes();
			assertTrue("Has super interfaces", !superInterfaces.isEmpty());
			SimpleType newSuperinterface= ast.newSimpleType(ast.newSimpleName("Cloneable"));
			rewrite.replace((ASTNode) superInterfaces.get(0), newSuperinterface, null);
			
			Type baseClass= roleType.getBaseClassType();
			assertTrue("Has base type", baseClass != null);
			SimpleType newBaseclass= ast.newSimpleType(ast.newSimpleName("System"));
			rewrite.replace(baseClass, newBaseclass, null);

		}

		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class T {\n");
		buf.append("    protected team class R1 extends Object implements Cloneable, Serializable playedBy System {\n");
		buf.append("    }\n");		
		buf.append("}\n");			
		assertEqualString(preview, buf.toString());

	}
	public void testRoleTypeDeclChanges2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test2", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test2;\n");
		buf.append("public team class T {\n");
		buf.append("    public team class R1 implements Runnable, Serializable playedBy String {}\n");
		buf.append("    public class R2 extends Object implements Runnable {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T.java", buf.toString(), false, null);			

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		
		TypeDeclaration teamType= findTypeDeclaration(astRoot, "T");
		{  
			// change R1 to non-team, add supertype, remmove first interface, remove base-class
			RoleTypeDeclaration role1Type= findRoleTypeDeclaration(teamType, "R1");

			// change to non-team
			rewrite.set(role1Type, RoleTypeDeclaration.TEAM_PROPERTY, Boolean.FALSE, null);


			Type superClass= role1Type.getSuperclassType();
			assertTrue("Has no super type", superClass == null);
			SimpleType newSuperclass= ast.newSimpleType(ast.newSimpleName("Object"));
			rewrite.set(role1Type, RoleTypeDeclaration.SUPERCLASS_TYPE_PROPERTY, newSuperclass, null);

			List superInterfaces= role1Type.superInterfaceTypes();
			assertTrue("Has super interfaces", !superInterfaces.isEmpty());
			rewrite.getListRewrite(role1Type, RoleTypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY).remove((Type)superInterfaces.get(0), null);
			
			Type baseClass= role1Type.getBaseClassType();
			assertTrue("Has base type", baseClass != null);
			rewrite.remove(baseClass, null);

		}
		{  
			// change R2: remove supertype, add super-interface, add base-class
			RoleTypeDeclaration role2Type= findRoleTypeDeclaration(teamType, "R2");

			Type superClass= role2Type.getSuperclassType();
			assertTrue("Has super type", superClass != null);
			rewrite.remove(superClass, null);

			List superInterfaces= role2Type.superInterfaceTypes();
			assertTrue("Has super interfaces", !superInterfaces.isEmpty());
			Type newSuperInterface = ast.newSimpleType(ast.newSimpleName("Cloneable"));
			rewrite.getListRewrite(role2Type, RoleTypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY).insertLast(
							newSuperInterface, 
							null);
			
			Type baseClass= role2Type.getBaseClassType();
			assertTrue("Has no base type", baseClass == null);
			SimpleType newBaseclass= ast.newSimpleType(ast.newSimpleName("System"));
			rewrite.set(role2Type, RoleTypeDeclaration.BASECLASS_TYPE_PROPERTY, newBaseclass, null);

		}

		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test2;\n");
		buf.append("public team class T {\n");
		buf.append("    public class R1 extends Object implements Serializable {}\n");
		buf.append("    public class R2 implements Runnable, Cloneable playedBy System {}\n");
		buf.append("}\n");			
		assertEqualString(preview, buf.toString());

	}
	public void testRoleTypeMakePackageTeam() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test2", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("@NonNullByDefault ");
		buf.append("package test2;\n");
		buf.append("public class R1 implements Runnable, Serializable playedBy String {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("R1.java", buf.toString(), false, null);			

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		
		PackageDeclaration packageDecl = astRoot.getPackage();
		{  
			// change to team
			ListRewrite modifiersRewrite = rewrite.getListRewrite(packageDecl, PackageDeclaration.MODIFIERS_PROPERTY);
			Modifier teamModifier = ast.newModifier(ModifierKeyword.TEAM_KEYWORD);
			modifiersRewrite.insertLast(teamModifier, null);

			// correct the package name
			rewrite.set(packageDecl, PackageDeclaration.NAME_PROPERTY, ast.newName("test2.MyTeam"), null);
		}

		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("@NonNullByDefault ");
		buf.append("team package test2.MyTeam;\n");
		buf.append("public class R1 implements Runnable, Serializable playedBy String {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		// re-get to also challenge ASTConverter and NaiveASTFlattener:
		IPackageFragment pack2 = this.sourceFolder.createPackageFragment("test2.MyTeam", false, null);
		cu= pack2.createCompilationUnit("R1.java", buf.toString(), false, null);		
		astRoot= createAST(cu);
		assertEqualString(astRoot.toString(), buf.toString());
	}
}
