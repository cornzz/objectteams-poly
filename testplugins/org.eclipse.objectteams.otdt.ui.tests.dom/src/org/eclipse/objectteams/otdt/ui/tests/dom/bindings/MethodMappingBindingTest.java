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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IMethodMappingBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodMappingElement;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrecedenceDeclaration;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;

/**
 * @author mkr
 * @version $Id: MethodMappingBindingTest.java 23496 2010-02-05 23:20:15Z stephan $
 */
public class MethodMappingBindingTest extends FileBasedDOMTest
{
    public static final String TEST_PROJECT = "DOM_AST";
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS4;

	private ASTParser _parser;
	private ICompilationUnit _team;

	private TypeDeclaration _teamDecl;

	private RoleTypeDeclaration _roleDecl;
	private CallinMappingDeclaration[] _callins;
    private CalloutMappingDeclaration[] _callouts;
    private MethodDeclaration[] _methods;
	private IMethodMappingBinding _testObj;

	public MethodMappingBindingTest(String name)
	{
		super(name);
	}

	public static Test suite()
	{
		return new Suite(MethodMappingBindingTest.class);
	}

	public void setUpSuite() throws Exception
	{
		setTestProjectDir(TEST_PROJECT);
		super.setUpSuite();
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		_team = getCompilationUnit(
	            getTestProjectDir(),
	            "src",
	            "bindings.teampkg",
	            "T1.java");

		_parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject( getJavaProject(TEST_PROJECT) );
		_parser.setSource(_team);
        _parser.setResolveBindings(true);

		ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit)root;
		_teamDecl = (TypeDeclaration)compUnit.types().get(0);
		_roleDecl = (RoleTypeDeclaration)_teamDecl.getRoles()[0];
        _callins = _roleDecl.getCallIns();
        _callouts = _roleDecl.getCallOuts();
        _methods = _roleDecl.getMethods();
    }

    public void testNotNull()
    {
        _testObj = _callouts[0].resolveBinding();

        assertNotNull(_testObj);
    }

    public void testGetName_NoSignature()
    {
        _testObj = _callouts[0].resolveBinding();

        String actual = _testObj.getName();
        String expected = "void m1() -> void m1() ;";

        assertEquals(expected, actual);
    }

    public void testGetName_WithSignature()
    {
        _testObj = _callouts[1].resolveBinding();

        String actual = _testObj.getName();
        String expected = "java.lang.String m2(java.lang.Integer) -> java.lang.String m2(java.lang.Integer) ;";

        assertEquals(expected, actual);
    }

    public void testGetDeclaringClass()
    {
        _testObj = _callouts[0].resolveBinding();

        ITypeBinding actual = _testObj.getDeclaringRoleClass();
        ITypeBinding expected = _roleDecl.resolveBinding();

        assertEquals(expected, actual);
    }

    public void testGetRoleMethod()
    {
        _testObj = _callins[0].resolveBinding();

        MethodDeclaration roleMethod =
            (MethodDeclaration)_methods[1];

        IMethodBinding actual = _testObj.getRoleMethod();
        IMethodBinding expected = roleMethod.resolveBinding();

        assertEquals(expected, actual);
    }

    public void testMethodSpecType()
    {
        MethodMappingElement roleMappingElement = _callins[0].getRoleMappingElement();

        // check we have the same method:
        MethodDeclaration roleMethod = (MethodDeclaration)_methods[1];
        assertEquals(roleMethod.resolveBinding(), ((MethodSpec)roleMappingElement).resolveBinding());

        // check the return types:
        ITypeBinding actual = roleMappingElement.getName().resolveTypeBinding();
        ITypeBinding expected = roleMethod.resolveBinding().getReturnType();

        assertEquals(expected, actual);
    }

    public void testGetReferencedBaseClass()
    {
        _testObj = _callins[0].resolveBinding();

        ITypeBinding actual = _testObj.getReferencedBaseClass();
        ITypeBinding expected = _roleDecl.resolveBinding().getBaseClass();

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    public void testGetModifiers_After()
    {
        _testObj = _callins[0].resolveBinding();
        int modifier = _testObj.getModifiers();

        boolean isCallinReplace = Modifier.isAfter(modifier)
            && _testObj.isCallin();

        assertTrue(isCallinReplace);
    }

    /** Resolve a qualified callin name in a precedence declaration. */
    public void testPrecedence1() {
    	PrecedenceDeclaration prec = (PrecedenceDeclaration) _teamDecl.precedences().get(0);
    	Name name = (Name) prec.elements().get(0);
    	Object binding = name.resolveBinding();
    	assertNotNull(binding);
    	assertTrue("Is mapping binding", binding instanceof IMethodMappingBinding);
    	assertEquals("Has expected representation", "ci1: java.lang.Integer m5(java.lang.Integer) <- after java.lang.Integer m5(java.lang.Integer) ;", binding.toString());
    }

    /** Try to resolve an unqualified callin name in a precedence declaration -> fails to resolve but shouldn't CCE. */
    public void testPrecedence2() {
    	PrecedenceDeclaration prec = (PrecedenceDeclaration) _teamDecl.precedences().get(0);
    	Name name = (Name) prec.elements().get(1);
    	Object binding = name.resolveBinding();
    	assertNotNull(binding);
    	assertTrue("Is mapping binding", binding instanceof IMethodMappingBinding);
    	assertEquals("Has expected representation", "missingCallin:ci2: NULL ROLE METHODS<- <unknown> ;", binding.toString());
    }

    /** Resolve an unqualified callin name in a precedence declaration */
    public void testPrecedence3() {
    	RoleTypeDeclaration role2Decl = (RoleTypeDeclaration)_teamDecl.getRoles()[1];
		PrecedenceDeclaration prec = (PrecedenceDeclaration) role2Decl.precedences().get(0);
    	Name name = (Name) prec.elements().get(0);
    	Object binding = name.resolveBinding();
    	assertNotNull(binding);
    	assertTrue("Is mapping binding", binding instanceof IMethodMappingBinding);
    	assertEquals("Has expected representation", "ci3: java.lang.Integer m3(java.lang.Integer) <- after java.lang.Integer m5(java.lang.Integer) ;", binding.toString());
    }
}
