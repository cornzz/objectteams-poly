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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.refactoring.rename;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor;

/**
 * @author jwl
 *
 */
@SuppressWarnings("restriction")
public class RenamePrivateFieldTests extends RefactoringTest
{
	private static final Class<RenamePrivateFieldTests>	clazz            = RenamePrivateFieldTests.class;
	private static final String	REFACTORING_PATH = "RenamePrivateField/";

	private Object _prefixPref;

	public RenamePrivateFieldTests(String name)
	{
		super(name);
	}

	public static Test suite()
	{
		return new MySetup(new TestSuite(clazz));
	}

	public static Test setUpTest(Test someTest)
	{
		return new MySetup(someTest);
	}

	protected String getRefactoringPath()
	{
		return REFACTORING_PATH;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void setUp() throws Exception
	{
		super.setUp();
		Hashtable options = JavaCore.getOptions();
		_prefixPref = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
		options.put(JavaCore.CODEASSIST_FIELD_PREFIXES, getPrefixes());
		options.put(OTDTPlugin.OT_COMPILER_INFERRED_CALLOUT, JavaCore.WARNING);
		JavaCore.setOptions(options);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void tearDown() throws Exception
	{
		super.tearDown();
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.CODEASSIST_FIELD_PREFIXES, _prefixPref);
		JavaCore.setOptions(options);
	}

	private String getPrefixes()
	{
		return "_";
	}

    private void performRenameRefactoring_failing(
            String[] cuNames,
            String selectionCuName,
            String declaringTypeName,
            String fieldName,
            String newFieldName,
            boolean renameGetter, 
            boolean renameSetter) throws Exception
	{
        int selectionCuIndex = firstIndexOf(selectionCuName, cuNames);
        Assert.isTrue(selectionCuIndex != -1,
                "parameter selectionCuQName must match some String in cuQNames.");
        Assert.isTrue(0 <= selectionCuIndex
                && selectionCuIndex < cuNames.length);

        ICompilationUnit[] cus = createCUs(cuNames);
        ICompilationUnit declaringCu = cus[selectionCuIndex];
        IType declaringType = getType(declaringCu, declaringTypeName);
        IField field = declaringType.getField(fieldName);
		RenameFieldProcessor processor = new RenameFieldProcessor(field);
		RenameRefactoring refactoring = new RenameRefactoring(processor);
		processor.setNewElementName(newFieldName);
		processor.setRenameGetter(renameGetter);
		processor.setRenameSetter(renameSetter);
		RefactoringStatus result = performRefactoring(refactoring);
		assertNotNull("precondition was supposed to fail", result);
	}

    private void performRenamingFtoG_passing(
            String[] cuNames,
            String declaringTypeName, 
            boolean updateReferences) throws Exception
    {
        performRenameRefactoring_passing(
                cuNames, declaringTypeName, "f", "g",
                updateReferences, false, false, false, false, false);
    }

	private void performRenameRefactoring_passing(
            String[] cuNames,
            String declaringTypeName,
            String fieldName,
            String newFieldName,
			boolean updateReferences,
            boolean updateTextualMatches,
			boolean renameGetter,
            boolean renameSetter,
			boolean expectedGetterRenameEnabled,
			boolean expectedSetterRenameEnabled) throws Exception
	{
//		ParticipantTesting.reset();
		ICompilationUnit[] cus = createCUs(cuNames);
		IType declaringType = getType(cus[0], declaringTypeName);
		IField field = declaringType.getField(fieldName);
		RenameFieldProcessor processor = new RenameFieldProcessor(field);
		RenameRefactoring refactoring = new RenameRefactoring(processor);
		processor.setUpdateReferences(updateReferences);
		processor.setUpdateTextualMatches(updateTextualMatches);
//		assertEquals("getter rename enabled", expectedGetterRenameEnabled,
//				processor.canEnableGetterRenaming() == null);
//		assertEquals("setter rename enabled", expectedSetterRenameEnabled,
//				processor.canEnableSetterRenaming() == null);
		processor.setRenameGetter(renameGetter);
		processor.setRenameSetter(renameSetter);
		processor.setNewElementName(newFieldName);
		String newGetterName = processor.getNewGetterName();
		String newSetterName = processor.getNewSetterName();

		List<IAnnotatable> elements = new ArrayList<IAnnotatable>();
		elements.add(field);
		List<RenameArguments> args = new ArrayList<RenameArguments>();
		args.add(new RenameArguments(newFieldName, updateReferences));
		if (renameGetter && expectedGetterRenameEnabled)
		{
			elements.add(processor.getGetter());
			args.add(new RenameArguments(newGetterName, updateReferences));
		}
		if (renameSetter && expectedSetterRenameEnabled)
		{
			elements.add(processor.getSetter());
			args.add(new RenameArguments(newSetterName, updateReferences));
		}
//		String[] renameHandles = ParticipantTesting.createHandles(elements
//				.toArray());

		RefactoringStatus result = performRefactoring(refactoring);
		assertEquals("was supposed to pass", null, result);
        for (int idx = 0; idx < cus.length; idx++)
		{
    		assertEqualLines("invalid renaming!",
    				getFileContents(createOutputTestFileName(cus, idx)), cus[idx].getSource());
		}

//		ParticipantTesting.testRename(renameHandles, (RenameArguments[])args
//				.toArray(new RenameArguments[args.size()]));

		assertTrue("anythingToUndo", RefactoringCore.getUndoManager()
				.anythingToUndo());
		assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager()
				.anythingToRedo());

		RefactoringCore.getUndoManager().performUndo(null,
				new NullProgressMonitor());
        for (int idx = 0; idx < cus.length; idx++)
		{
    		assertEqualLines("invalid undo",
    				getFileContents(createInputTestFileName(cus, idx)), cus[idx].getSource());
        }
        
		assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager()
				.anythingToUndo());
		assertTrue("anythingToRedo", RefactoringCore.getUndoManager()
				.anythingToRedo());

		RefactoringCore.getUndoManager().performRedo(null,
				new NullProgressMonitor());
        for (int idx = 0; idx < cus.length; idx++)
        {
    		assertEqualLines("invalid redo",
    				getFileContents(createOutputTestFileName(cus, idx)), cus[idx].getSource());
        }
	}

    private int firstIndexOf(String one, String[] others)
    {
        for (int idx = 0; idx < others.length; idx++)
        {
            if (one == null && others[idx] == null || one.equals(others[idx]))
            {
                return idx;
            }
        }
        return -1;
    }

    //--------- tests ----------
    public void testRenameFieldInTeamclass() throws Exception
	{
        performRenamingFtoG_passing(new String[]{"T"}, "T", true);
	}
    
    public void testUpdateFieldReferenceInTeamMethod() throws Exception
    {
        performRenamingFtoG_passing(new String[]{"T"}, "T", true);
    }
    
    public void testUpdateFieldReferenceInLiftingMethod() throws Exception
    {
        performRenamingFtoG_passing(new String[]{"T"}, "T", true);
    }

    public void testUpdateTeamFieldReferenceInRoleclass1() throws Exception
    {
        performRenameRefactoring_passing(
                new String[]{"T"}, "T", "number", "amount",
                true, false, false, false, false, false);
    }

    public void testUpdateTeamFieldReferenceInRoleclass2() throws Exception
    {
        performRenameRefactoring_passing(
                new String[]{"T"}, "T", "number", "amount",
                true, false, false, false, false, false);
    }
    
    public void testUpdateTeamFieldReferenceInNestedTeam1() throws Exception
    {
        performRenameRefactoring_passing(
                new String[]{"T"}, "T", "number", "amount",
                true, false, false, false, false, false);
    }

    public void testUpdateTeamFieldReferenceInNestedTeam2() throws Exception
    {
        performRenameRefactoring_passing(
                new String[]{"T"}, "T", "number", "amount",
                true, false, false, false, false, false);
    }

    public void testRenameFieldInRoleclass() throws Exception
	{
        performRenamingFtoG_passing(new String[]{"T"}, "R", true);
	}
    
    public void testUpdateFieldReferenceInRoleclass1() throws Exception
    {
        performRenameRefactoring_passing(
                new String[]{"T"}, "R", "name", "newName",
                true, false, true, false, true, false);
    }

    public void testUpdateFieldReferenceInRoleclass2() throws Exception
    {
        performRenameRefactoring_passing(
                new String[]{"T"}, "R", "name", "newName",
                true, true, true, true, true, true);
    }

    public void testUpdateFieldReferenceInRoleclass3() throws Exception
    {
        performRenameRefactoring_passing(
                new String[]{"T"}, "R", "name", "newName",
                true, false, false, true, false, true);
    }

	public void testUpdateReferenceInCalloutToField1() throws Exception
	{
        performRenamingFtoG_passing(new String[]{"B", "T"}, "B", true);
	}
	//private field from super base class visible in bound base class
    public void testUpdateReferenceInCalloutToField2() throws Exception
    {
        performRenamingFtoG_passing(new String[]{"B1", "B2", "T"}, "B1", true);
    }
    //shorthand definition: callout to field access method without
    //prior abstract declaration + method names only
    public void testUpdateReferenceInCalloutToField3() throws Exception
    {
        performRenameRefactoring_passing(
                new String[]{"B", "T"}, "B", "name", "surname",
                true, false, false, false, false, false);
    }
    //shorthand definition: callout to field access method without
    //prior abstract declaration + complete signatures
    public void testUpdateReferenceInCalloutToField4() throws Exception
    {
        performRenameRefactoring_passing(
                new String[]{"B", "T"}, "B", "name", "surname",
                true, false, false, false, false, false);
    }
    // referenced by inferred callout (fully inferred)
	public void testUpdateReferenceInCalloutToField5() throws Exception
	{
        performRenamingFtoG_passing(new String[]{"B", "T"}, "B", true);
	}
    // referenced by inferred callout (explicit use of inferred accessor)
	public void testUpdateReferenceInCalloutToField6() throws Exception
	{
        performRenamingFtoG_passing(new String[]{"B", "T"}, "B", true);
	}
    
    //passing
    public void testRenameGetterAndSetter1() throws Exception
    {
        performRenameRefactoring_passing(
                new String[]{"T"}, "T", "number", "index",
                true, false, true, true, true, true);
    }
    public void testRenameGetterAndSetter2() throws Exception
    {
        performRenameRefactoring_passing(
                new String[]{"T"}, "R", "number", "index",
                true, false, true, true, true, true);
    }
    //failing
    public void testRenameGetterAndSetter3() throws Exception
    {
        performRenameRefactoring_failing(
                new String[]{"T1", "T2"}, "T2", "T2", "number", "index", true, true);
    }
    public void testRenameGetterAndSetter4() throws Exception
    {
        performRenameRefactoring_failing(
                new String[]{"T1", "T2"}, "T2", "R", "number", "index", true, true);
    }
}