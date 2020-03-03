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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LiftingType;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;
import org.eclipse.objectteams.otdt.ui.tests.dom.TypeDeclarationFinder;

/**
 * @author Michael Krueger
 * @version $Id: TypeBindingTest.java 23496 2010-02-05 23:20:15Z stephan $
 */
public class TypeBindingTest extends FileBasedDOMTest
{
	public static final String TEST_PROJECT = "DOM_AST";
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS4;
	
	private ASTParser _parser;
	private ICompilationUnit _cuTA;
    private ICompilationUnit _cuTB;
    private ICompilationUnit _cuMyT;

    private RoleTypeDeclaration _roleTAT2R1;
    private RoleTypeDeclaration _roleTBT1R1;
    private RoleTypeDeclaration _roleTBT2R1;
    
    private TypeDeclaration _teamMyTeam;

    private RoleTypeDeclaration _focus;
    
	public TypeBindingTest(String name) 
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(TypeBindingTest.class);
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir(TEST_PROJECT);
		super.setUpSuite();
	}
	
	protected void setUp() throws Exception 
	{
		super.setUp();
		_cuTA = getCompilationUnit(
				getTestProjectDir(),
				"src",
				"roleTypeDeclaration.teampkg",
		        "TA.java");
        _cuTB = getCompilationUnit(
                getTestProjectDir(),
                "src",
                "roleTypeDeclaration.teampkg",
                "TB.java");
        _cuMyT = getCompilationUnit(
                getTestProjectDir(),
                "src",
                "roleTypeDeclaration.teampkg",
                "MyTeam.java");
        
		_parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject(super.getJavaProject(TEST_PROJECT));
        _parser.setResolveBindings(true);
		_parser.setSource(_cuTA);
		
		ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit) root;
        TypeDeclarationFinder finder = new TypeDeclarationFinder();

        finder.setName("TA.T2.R1");
		compUnit.accept(finder);
		_roleTAT2R1 = (RoleTypeDeclaration)finder.getTypeDeclaration();

        _parser.setSource(_cuTB);
        _parser.setResolveBindings(true);
        
        root = _parser.createAST( new NullProgressMonitor() );
        compUnit = (CompilationUnit) root;

        finder.setName("TB.T1.R1");
        compUnit.accept(finder);
        _roleTBT1R1 = (RoleTypeDeclaration)finder.getTypeDeclaration();

        finder.setName("TB.T2.R1");
        compUnit.accept(finder);
        _roleTBT2R1 = (RoleTypeDeclaration)finder.getTypeDeclaration();        

        _parser.setSource(_cuMyT);
        _parser.setResolveBindings(true);
        
        root = _parser.createAST( new NullProgressMonitor() );
        compUnit = (CompilationUnit) root;

        finder.setName("MyTeam");
        compUnit.accept(finder);
        _teamMyTeam = finder.getTypeDeclaration();
	}
	
	public void testInstanceTypes()
	{
        assertNotNull(_roleTAT2R1);
        assertNotNull(_roleTBT1R1);
        assertNotNull(_roleTBT2R1);
		assertTrue(_roleTAT2R1 instanceof RoleTypeDeclaration);
        assertTrue(_roleTBT1R1 instanceof RoleTypeDeclaration);
        assertTrue(_roleTBT2R1 instanceof RoleTypeDeclaration);
	}
	
    public void testGetSuperRoles()
    {
        _focus = _roleTBT2R1;
        ITypeBinding binding = (ITypeBinding)_focus.resolveBinding();
        
        ITypeBinding[] expected = new ITypeBinding[]
                                   {
                                    _roleTBT1R1.resolveBinding(),
                                    _roleTAT2R1.resolveBinding()
                                   };
        ITypeBinding[] actual = binding.getSuperRoles();

        assertEquals(expected.length, actual.length);
        // compare the optimal name, since TAT2R1 has no key
        assertEquals(expected[0].getOptimalName(), actual[0].getOptimalName());
        assertEquals(expected[1].getOptimalName(), actual[1].getOptimalName());
    }
    
    public void testDeclaredLiftingType() {
    	MethodDeclaration method = (MethodDeclaration) _teamMyTeam.bodyDeclarations().get(3);
    	SingleVariableDeclaration arg1 = (SingleVariableDeclaration) method.parameters().get(0);
    	Type type = arg1.getType();
		ITypeBinding typeBinding = type.resolveBinding();
    	assertEquals("Wrong type binding for type", "MyClass", typeBinding.getName());
    	Name typeName = ((LiftingType)type).getName();
		typeBinding = typeName.resolveTypeBinding();
    	assertEquals("Wrong type binding for name", "MyClass", typeBinding.getName());
    }

	// Bug 352605 - Eclipse is reporting "Could not retrieve superclass" every few minutes
    public void testBug352605() throws CoreException {
		ASTParser parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		parser.setProject(getJavaProject(TEST_PROJECT));
        parser.setResolveBindings(true);
        parser.setUnitName("C");
		parser.setSource(("public class C {\n" +
				"public bug352605.Sub f;\n" +
				"}\n").toCharArray());
		
		ASTNode root = parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit) root;
        
		TypeDeclaration type = (TypeDeclaration) compUnit.types().get(0);
		FieldDeclaration field = (FieldDeclaration) type.bodyDeclarations().get(0);
		Type fieldType = field.getType();
		ITypeBinding typeBinding = fieldType.resolveBinding();
		assertNotNull("Field type binding should be non-null", typeBinding);
		assertFalse("typeBinding should be from class file", typeBinding.isFromSource());
		typeBinding = typeBinding.getSuperclass();
		assertNotNull("super class should be non-null", typeBinding);
		assertTrue("typeBinding should be from source", typeBinding.isFromSource());
		typeBinding = typeBinding.getSuperclass();
		assertNotNull("2nd super class should be non-null", typeBinding);
		assertTrue("2nd super class should be Object", typeBinding.getQualifiedName().equals("java.lang.Object"));
    }
}
