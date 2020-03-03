/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2013 Fraunhofer Gesellschaft, Munich, Germany,
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
package org.eclipse.objectteams.otdt.ui.tests.refactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceManipulation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.ITypeNameRequestor;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.internal.core.AbstractCalloutMapping;

@SuppressWarnings("restriction")
public abstract class RefactoringTest extends TestCase
{
    private IPackageFragmentRoot _root;
    private IPackageFragment _packageP;

    public boolean _isVerbose = false;

    public static final String TEST_PATH_PREFIX = "";

    protected static final String TEST_INPUT_INFIX = "/in/";
    protected static final String TEST_OUTPUT_INFIX = "/out/";
    protected static final String CONTAINER = "src";

    public RefactoringTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        _root = MySetup.getDefaultSourceFolder();
        _packageP = MySetup.getPackageP();

        if (_isVerbose)
        {
            System.out
                    .println("\n---------------------------------------------");
            System.out.println("\nTest:" + getClass() + "." + getName());
        }
        RefactoringCore.getUndoManager().flush();
    }

    protected void tearDown() throws Exception
    {
        refreshFromLocal();
        performDummySearch();

        if (getPackageP().exists())
        {
            tryDeletingAllJavaChildren(getPackageP());
            tryDeletingAllNonJavaChildResources(getPackageP());
        }

        if (getRoot().exists())
        {
            IJavaElement[] packages = getRoot().getChildren();
            for (int i = 0; i < packages.length; i++)
            {
                try
                {
                    IPackageFragment pack = (IPackageFragment)packages[i];
                    if (!pack.equals(getPackageP()) && pack.exists()
                            && !pack.isReadOnly())
                        pack.delete(true, null);
                }
                catch (JavaModelException ex)
                {
                    //try to delete'em all
                    ex.printStackTrace();
                }
            }
        }
    }

    private void refreshFromLocal() throws CoreException
    {
        if (getRoot().exists())
        {
            getRoot().getResource().
            		  refreshLocal(IResource.DEPTH_INFINITE, null);
        }
        else if (getPackageP().exists())//don't refresh package if root already refreshed
        {
            getPackageP().getResource().
            			  refreshLocal(IResource.DEPTH_INFINITE, null);
        }
    }

    private static void tryDeletingAllNonJavaChildResources(
            IPackageFragment pack) throws JavaModelException
    {
        Object[] nonJavaKids = pack.getNonJavaResources();
        for (int i = 0; i < nonJavaKids.length; i++)
        {
            if (nonJavaKids[i] instanceof IResource)
            {
                IResource resource = (IResource)nonJavaKids[i];
                try
                {
                    resource.delete(true, null);
                }
                catch (CoreException ex)
                {
                    //try to delete'em all
                    ex.printStackTrace();
                }
            }
        }
    }

    private static void tryDeletingAllJavaChildren(IPackageFragment pack)
            throws JavaModelException
    {
        IJavaElement[] kids = pack.getChildren();
        for (int i = 0; i < kids.length; i++)
        {
            if (kids[i] instanceof ISourceManipulation)
            {
                try
                {
                    if (kids[i].exists() && !kids[i].isReadOnly())
                        ((ISourceManipulation)kids[i]).delete(true, null);
                }
                catch (JavaModelException ex)
                {
                    //try to delete'em all
                    ex.printStackTrace();
                }
            }
        }
    }

    protected IPackageFragmentRoot getRoot()
    {
        return _root;
    }

    protected IPackageFragment getPackageP()
    {
        return _packageP;
    }

    protected final Refactoring createRefactoring(RefactoringDescriptor descriptor) throws CoreException {
	    RefactoringStatus status= new RefactoringStatus();
		Refactoring refactoring= descriptor.createRefactoring(status);
		assertNotNull("refactoring should not be null", refactoring);
		assertTrue("status should be ok", status.isOK());
	    return refactoring;
    }

	protected final RefactoringStatus performRefactoring(RefactoringDescriptor descriptor) throws Exception {
		return performRefactoring(descriptor, true);
	}

	protected final RefactoringStatus performRefactoring(RefactoringDescriptor descriptor, boolean providesUndo) throws Exception {
		Refactoring refactoring= createRefactoring(descriptor);
		return performRefactoring(refactoring, providesUndo);
	}

    protected final RefactoringStatus performRefactoring(Refactoring ref)
            throws Exception
    {
        return performRefactoring(ref, true);
    }

    protected final RefactoringStatus performRefactoring(Refactoring ref,
            boolean providesUndo) throws Exception
    {
        performDummySearch();
        IUndoManager undoManager = getUndoManager();
        CreateChangeOperation create = new CreateChangeOperation(
                new CheckConditionsOperation(ref,
                        CheckConditionsOperation.ALL_CONDITIONS),
                RefactoringStatus.FATAL);
        PerformChangeOperation perform = new PerformChangeOperation(create);
        perform.setUndoManager(undoManager, ref.getName());
        ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor());
        RefactoringStatus status = create.getConditionCheckingStatus();
        if (!status.isOK())
        {
            return status;
        }
        assertTrue("Change wasn't executed", perform.changeExecuted());
        Change undo = perform.getUndoChange();
        if (providesUndo)
        {
            assertNotNull("Undo doesn't exist", undo);
            assertTrue("Undo manager is empty", undoManager.anythingToUndo());
        }
        else
        {
            assertNull("Undo manager contains undo but shouldn't", undo);
        }
        return null;
    }

    protected final RefactoringStatus performRefactoringWithStatus(
            Refactoring ref) throws Exception
    {
        performDummySearch();
        CreateChangeOperation create = new CreateChangeOperation(
                new CheckConditionsOperation(ref,
                        CheckConditionsOperation.ALL_CONDITIONS),
                RefactoringStatus.FATAL);
        PerformChangeOperation perform = new PerformChangeOperation(create);
        perform.setUndoManager(RefactoringCore.getUndoManager(), ref.getName());
        ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor());
        RefactoringStatus status = create.getConditionCheckingStatus();
        if (status.hasFatalError())
        {
            return status;
        }
        assertTrue("Change wasn't executed", perform.changeExecuted());
        return status;
    }

    protected void performDummySearch() throws Exception
    {
        performDummySearch(getPackageP());
    }

    protected final Change performChange(final Refactoring refactoring,
            boolean storeUndo) throws Exception
    {
        CreateChangeOperation create = new CreateChangeOperation(refactoring);
        PerformChangeOperation perform = new PerformChangeOperation(create);
        if (storeUndo)
        {
            perform.setUndoManager(getUndoManager(), refactoring.getName());
        }
        ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor());
        assertTrue("Change wasn't executed", perform.changeExecuted());
        return perform.getUndoChange();
    }

    protected final Change performChange(final Change change) throws Exception
    {
        PerformChangeOperation perform = new PerformChangeOperation(change);
        ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor());
        assertTrue("Change wasn't executed", perform.changeExecuted());
        return perform.getUndoChange();
    }

    protected IUndoManager getUndoManager()
    {
        IUndoManager undoManager = RefactoringCore.getUndoManager();
        undoManager.flush();
        return undoManager;
    }

    /** ************** helpers ***************** */
    /** ** mostly not general, just shortcuts **** */

    protected IType getType(ICompilationUnit cu, String name)
            throws JavaModelException
    {
        IType[] types = cu.getAllTypes();
        for (int i = 0; i < types.length; i++)
        {
            if (types[i].getFullyQualifiedName().equals(name)
                    || types[i].getElementName().equals(name))
            {
                return types[i];
            }
        }
        return null;
    }

    /**
     * subclasses override to inform about the location of their test cases
     */
    protected String getRefactoringPath()
    {
        return "";
    }

    /**
     * example "RenameType/"
     */
    protected String getTestPath()
    {
        return TEST_PATH_PREFIX + getRefactoringPath();
    }

    /**
     * @param cuName
     * @param infix
     *            example "RenameTest/test0 + infix + cuName.java"
     */
    protected String createTestFileName(String cuName, String infix)
    {
        return getTestPath() + getName() + infix + cuName + ".java";
    }

    protected String getInputTestFileName(String cuName)
    {
        return createTestFileName(cuName, TEST_INPUT_INFIX);
    }

    /**
     * @param subDirName
     *            example "p/" or "org/eclipse/jdt/"
     */
    protected String getInputTestFileName(String cuName, String subDirName)
    {
        return createTestFileName(cuName, TEST_INPUT_INFIX + subDirName);
    }

    protected String getOutputTestFileName(String cuName)
    {
        return createTestFileName(cuName, TEST_OUTPUT_INFIX);
    }

    /**
     * @param subDirName
     *            example "p/" or "org/eclipse/jdt/"
     */
    protected String getOutputTestFileName(String cuName, String subDirName)
    {
        return createTestFileName(cuName, TEST_OUTPUT_INFIX + subDirName);
    }

    protected ICompilationUnit createCUfromTestFile(IPackageFragment pack,
            String cuName) throws Exception
    {
        return createCUfromTestFile(pack, cuName, true);
    }

    protected ICompilationUnit createCUfromTestFile(IPackageFragment pack,
            String cuName, String subDirName) throws Exception
    {
        return createCUfromTestFile(pack, cuName, subDirName, true);
    }

    protected ICompilationUnit createCUfromTestFile(IPackageFragment pack,
            String cuName, boolean input) throws Exception
    {
        String contents = input ? getFileContents(getInputTestFileName(cuName))
                : getFileContents(getOutputTestFileName(cuName));
        return createCU(pack, cuName + ".java", contents);
    }

    protected ICompilationUnit createCUfromTestFile(IPackageFragment pack,
            String cuName, String subDirName, boolean input) throws Exception
    {
        String contents = input ? getFileContents(getInputTestFileName(cuName,
                subDirName)) : getFileContents(getOutputTestFileName(cuName,
                subDirName));

        return createCU(pack, cuName + ".java", contents);
    }

    protected void printTestDisabledMessage(String explanation)
    {
        System.out.println("\n" + getClass().getName() + "::" + getName()
                + " disabled (" + explanation + ")");
    }

    protected ICompilationUnit[] createCUs(String[] cuNames) throws Exception {
		ICompilationUnit[] cus = new ICompilationUnit[cuNames.length];
	
		for (int idx = 0; idx < cuNames.length; idx++) {
			Assert.isNotNull(cuNames[idx]);
			cus[idx] = createCUfromTestFile(getPackageP(), cuNames[idx]);
		}
		return cus;
	}

	protected String createInputTestFileName(ICompilationUnit[] cus, int idx) {
		return getInputTestFileName(getSimpleNameOfCu(cus[idx].getElementName()));
	}

	protected String createOutputTestFileName(ICompilationUnit[] cus, int idx) {
		return getOutputTestFileName(getSimpleNameOfCu(cus[idx].getElementName()));
	}

	private String getSimpleNameOfCu(String compUnit) {
		int dot = compUnit.lastIndexOf('.');
		return compUnit.substring(0, dot);
	}

	//-----------------------
    public static InputStream getStream(String content)
    {
        return new StringBufferInputStream(content);
    }

    public static IPackageFragmentRoot getSourceFolder(
            IJavaProject javaProject, String name) throws JavaModelException
    {
        IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++)
        {
            if (!roots[i].isArchive() && roots[i].getElementName().equals(name))
                return roots[i];
        }
        return null;
    }

    public static String getFileContents(String fileName) throws IOException
    {
        return getContents(getFileInputStream(fileName));
    }

    public static String getContents(IFile file) throws IOException,
            CoreException
    {
        return getContents(file.getContents());
    }

    public static ICompilationUnit createCU(IPackageFragment pack, String name,
            String contents) throws Exception
    {
        if (pack.getCompilationUnit(name).exists())
        {
            return pack.getCompilationUnit(name);
        }
        ICompilationUnit cu = pack.createCompilationUnit(name, contents, true,
                null);
        cu.save(null, true);
        return cu;
    }

    public static String getContents(InputStream in) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuffer sb = new StringBuffer(300);
        try
        {
            int read = 0;
            while ((read = br.read()) != -1)
            {
                sb.append((char)read);
            }
        }
        finally
        {
            br.close();
        }
        return sb.toString();
    }

    public static InputStream getFileInputStream(String fileName)
            throws IOException
    {
        return OTRefactoringTestPlugin.getDefault().getTestResourceStream(
                fileName);
    }

    public static String removeExtension(String fileName)
    {
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public static void performDummySearch(IJavaElement element)
            throws Exception
    {
        new SearchEngine().searchAllTypeNames(ResourcesPlugin.getWorkspace(),
                null, null, SearchPattern.R_EXACT_MATCH, true,
                IJavaSearchConstants.CLASS, SearchEngine
                        .createJavaSearchScope(new IJavaElement[] { element }),
                new Requestor(),
                IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
    }

    public static IMember[] merge(IMember[] a1, IMember[] a2, IMember[] a3)
    {
        return JavaElementUtil.merge(JavaElementUtil.merge(a1, a2), a3);
    }

    public static IMember[] merge(IMember[] a1, IMember[] a2)
    {
        return JavaElementUtil.merge(a1, a2);
    }

    public static IField[] getFields(IType type, String[] names)
            throws JavaModelException
    {
        if (names == null)
        {
            return new IField[0];
        }
        Set<IField> fields = new HashSet<IField>();
        for (int i = 0; i < names.length; i++)
        {
            IField field = type.getField(names[i]);
            assertTrue("field " + field.getElementName() + " does not exist",
                    field.exists());
            fields.add(field);
        }
        return fields.toArray(new IField[fields.size()]);
    }

    public static IType[] getMemberTypes(IType type, String[] names)
            throws JavaModelException
    {
        if (names == null)
        {
            return new IType[0];
        }
        Set<IType> memberTypes = new HashSet<IType>();
        for (int i = 0; i < names.length; i++)
        {
            IType memberType = type.getType(names[i]);
            assertTrue("member type " + memberType.getElementName()
                    + " does not exist", memberType.exists());
            memberTypes.add(memberType);
        }
        return memberTypes.toArray(new IType[memberTypes.size()]);
    }

    public static IMethod[] getMethods(IType type, String[] names,
            String[][] signatures) throws JavaModelException
    {
    	return getMethods(type, names, signatures, null, false);
    }
    public static IMethod[] getMethods(IType type, String[] names, String[][] signatures,
    									boolean[] flags, boolean flag) 
            		throws JavaModelException
    {
        if (names == null || signatures == null)
        {
            return new IMethod[0];
        }
        List<IMethod> methods = new ArrayList<IMethod>(names.length);
        for (int i = 0; i < names.length; i++)
        {
        	// use flags to filter sub-set of names/signatures:
        	if (flags != null && flags[i] != flag) continue;

            IMethod method = type.getMethod(names[i], signatures[i]);
            if (!method.exists()) {
            	// search a callout mapping that might be "equal" to this method:
            	for (IJavaElement child : type.getChildren()) {
            		int elementType = child.getElementType();
					if (elementType == IOTJavaElement.CALLOUT_MAPPING || elementType == IOTJavaElement.CALLOUT_TO_FIELD_MAPPING) {
						AbstractCalloutMapping map = (AbstractCalloutMapping) child;
						if (method.equals(map.getCorrespondingJavaElement())) {
							method = map;
							break;
						}
					}
            	}
            }
            assertTrue("method " + method.getElementName() + " does not exist",
                    method.exists());
            if (!methods.contains(method))
            {
                methods.add(method);
            }
        }
        return methods.toArray(new IMethod[methods.size()]);
    }

    public static IType[] findTypes(IType[] types, String[] namesOfTypesToPullUp)
    {
        List<IType> found = new ArrayList<IType>(types.length);
        for (int i = 0; i < types.length; i++)
        {
            IType type = types[i];
            for (int j = 0; j < namesOfTypesToPullUp.length; j++)
            {
                String name = namesOfTypesToPullUp[j];
                if (type.getElementName().equals(name))
                {
                    found.add(type);
                }
            }
        }
        return found.toArray(new IType[found.size()]);
    }

    public static IField[] findFields(IField[] fields,
            String[] namesOfFieldsToPullUp)
    {
        List<IField> found = new ArrayList<IField>(fields.length);
        for (int i = 0; i < fields.length; i++)
        {
            IField field = fields[i];
            for (int j = 0; j < namesOfFieldsToPullUp.length; j++)
            {
                String name = namesOfFieldsToPullUp[j];
                if (field.getElementName().equals(name))
                {
                    found.add(field);
                }
            }
        }
        return found.toArray(new IField[found.size()]);
    }

    public static IMethod[] findMethods(IMethod[] selectedMethods,
            String[] namesOfMethods, String[][] signaturesOfMethods)
    {
        List<IMethod> found = new ArrayList<IMethod>(selectedMethods.length);
        for (int i = 0; i < selectedMethods.length; i++)
        {
            IMethod method = selectedMethods[i];
            String[] paramTypes = method.getParameterTypes();
            for (int j = 0; j < namesOfMethods.length; j++)
            {
                String methodName = namesOfMethods[j];
                if (!methodName.equals(method.getElementName()))
                {
                    continue;
                }
                String[] methodSig = signaturesOfMethods[j];
                if (!areSameSignatures(paramTypes, methodSig))
                {
                    continue;
                }
                found.add(method);
            }
        }
        return found.toArray(new IMethod[found.size()]);
    }

    private static boolean areSameSignatures(String[] s1, String[] s2)
    {
        if (s1.length != s2.length)
        {
            return false;
        }
        for (int i = 0; i < s1.length; i++)
        {
            if (!s1[i].equals(s2[i]))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Line-based version of junit.framework.Assert.assertEquals(String, String)
     * without considering line delimiters.
     */
    public static void assertEqualLines(String expected, String actual)
    {
        assertEqualLines("", expected, actual);
    }

    /**
     * Line-based version of junit.framework.Assert.assertEquals(String, String, String)
     * without considering line delimiters.
     */
    public static void assertEqualLines(String message, String expected,
            String actual)
    {
        String[] expectedLines = Strings.convertIntoLines(expected);
        String[] actualLines = Strings.convertIntoLines(actual);

        String expected2 = (expectedLines == null ? null : Strings.concatenate(
                expectedLines, "\n"));
        String actual2 = (actualLines == null ? null : Strings.concatenate(
                actualLines, "\n"));
        assertEquals(message, expected2, actual2);
    }

    private static class Requestor implements ITypeNameRequestor
    {

        public void acceptClass(char[] packageName, char[] simpleTypeName,
                char[][] enclosingTypeNames, String path)
        {
        }

        public void acceptInterface(char[] packageName, char[] simpleTypeName,
                char[][] enclosingTypeNames, String path)
        {
        }
    }
}
