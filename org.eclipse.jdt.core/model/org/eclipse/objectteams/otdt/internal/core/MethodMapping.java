/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;
import org.eclipse.jdt.internal.core.TypeParameter;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IMethodSpec;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.internal.core.util.MethodData;


/**
 * OT_COPY_PASTE from Member.getCategories()
 *               from Member.getJavadocRange()
 *               from SourceRefElement.getOpenableParent()
 *               
 * Generic Method Mapping, needs to be subclassed to add missing behaviour
 * for IMethodMapping.getMappingKind()
 *  
 * @author jwloka
 */
public abstract class MethodMapping extends OTJavaElement implements IMethodMapping
{
    protected static final String[] EMPTY_STRING_ARRAY = new String[0];
    protected static final ITypeParameter[] NO_TYPE_PARAMETERS = new ITypeParameter[0];

    private int              declarationSourceStart;
    private int              sourceStart;
    private int              sourceEnd;
    private int              declarationSourceEnd;
    private IMethod          roleMethod;
	protected MethodData     roleMethodHandle;
	private boolean			 hasSearchedRoleMethod;
	private boolean          hasSignature;

    public MethodMapping(int        declarationSourceStart,
                         int        sourceStart,
                         int        sourceEnd,
                         int        declarationSourceEnd,
                         int        type,
                         IMethod	correspondingJavaElem,
                         IType  	parent,
                         MethodData roleMethodHandle, 
						 boolean    hasSignature)
    {
        super(type, correspondingJavaElem, parent);
        this.roleMethodHandle       = roleMethodHandle;
        this.declarationSourceStart = declarationSourceStart;
        this.sourceStart            = sourceStart;
        this.sourceEnd			    = sourceEnd;
        this.declarationSourceEnd   = declarationSourceEnd;
        this.hasSignature           = hasSignature;
    }
    
    public MethodMapping(int            declarationSourceStart,
			             int        	sourceStart,
			             int			sourceEnd,
			             int        	declarationSourceEnd,
			             int        	type,
			             IMethod		correspondingJavaElem,
			             IType  		parent,
			             MethodData 	roleMethodHandle, 
			             boolean    	hasSignature,
			             boolean    	addAsChild)
	{
		super(type, correspondingJavaElem, parent, addAsChild);
		this.roleMethodHandle       = roleMethodHandle;
		this.declarationSourceStart = declarationSourceStart;
		this.sourceStart        	= sourceStart;
		this.sourceEnd				= sourceEnd;
		this.declarationSourceEnd   = declarationSourceEnd;
		this.hasSignature    		= hasSignature;
	}
        
    // ==== memento generation: ====
    @Override
    public String getHandleIdentifier() {
    	StringBuffer buff = new StringBuffer();
    	IJavaElement myParent = getParent();
    	if (myParent instanceof IOTJavaElement)
    		myParent = ((IOTJavaElement)myParent).getCorrespondingJavaElement();
    	// prefix
		buff.append(((JavaElement)myParent).getHandleMemento());
    	char delimiter = OTJavaElement.OTEM_METHODMAPPING;
    	// start:
    	buff.append(delimiter);
    	// mapping kind:
    	buff.append(getMappingKindChar());
    	// long or short?
    	buff.append(this.hasSignature ? 'l' : 's');
    	buff.append(delimiter);
    	// mapping name (if any);
    	getNameForHandle(buff);
    	// role method:
    	getMethodForHandle(this.roleMethodHandle, buff);
		// base methods:
    	getBaseMethodsForHandle(buff);
    	buff.append(delimiter);
		return buff.toString();
    }
    protected void getNameForHandle(StringBuffer buff) { /* default: mapping has no name. */ }
    /** 
     * Answer a char encoding the mapping kind with this information:
     * callin: a=after, b=before, r=replace; 
     * callout: o=regular, g=getter, s=setter [capital=isOverride].
     */
    abstract protected char getMappingKindChar();
    abstract protected void getBaseMethodsForHandle(StringBuffer buff);
	
    protected void getMethodForHandle(IMethodSpec method, StringBuffer buff) {
    	escapeMementoName(buff, method.getSelector());
    	if (this.hasSignature) {
    		for (String argType : method.getArgumentTypes()) {
    			buff.append(JavaElement.JEM_METHOD);
    			escapeMementoName(buff, argType);
    		}
    		buff.append(JavaElement.JEM_METHOD);
    		escapeMementoName(buff, method.getReturnType());
    	}
    	buff.append(OTJavaElement.OTEM_METHODMAPPING);
    }
    // ==== retreive method spec from memento: === 
    public static MethodData createMethodData(MementoTokenizer memento, String selector) {
    	String cur = memento.nextToken();
    	if (cur.charAt(0) == JavaElement.JEM_METHOD)
    		cur = memento.nextToken(); // skip initial separator
    	List<String> argTypes = new ArrayList<String>(); 
    	while (cur.charAt(0) != OTJavaElement.OTEM_METHODMAPPING) {
			StringBuffer buffer = new StringBuffer();
			while (cur.length() == 1 && Signature.C_ARRAY == cur.charAt(0)) { // backward compatible with 3.0 mementos
				buffer.append(Signature.C_ARRAY);
				if (!memento.hasMoreTokens())
					break;
				cur = memento.nextToken();
			}
			buffer.append(cur);
			argTypes.add(buffer.toString());
    		if (memento.nextToken().charAt(0) != JavaElement.JEM_METHOD)
    			break;
    		cur = memento.nextToken();
    	}
    	String returnType = null;
    	if (argTypes.size() > 0)
    		returnType = argTypes.remove(argTypes.size()-1);
    	return new MethodData(selector, argTypes.toArray(new String[argTypes.size()]), null, returnType, false);
    }
    // ====
    
	@Override
	public IMethod getRoleMethod()
    {
    	if (!this.hasSearchedRoleMethod) {
    		try {
                this.roleMethod = findRoleMethod();
            } catch (JavaModelException ex) {
            	Util.log(ex, "Failed to lookup original role method element!"); //$NON-NLS-1$
            } finally {
    			this.hasSearchedRoleMethod = true;
    		}
    	}
    	
        return this.roleMethod;
    }

	// added for the SourceTypeConverter
    @Override
	public MethodData getRoleMethodHandle()
    {
    	return this.roleMethodHandle;
    }
    
    public IMethod getRoleMethodThrowingException() throws JavaModelException
    {
    	if (!this.hasSearchedRoleMethod) {
    		try {
    			this.roleMethod = findRoleMethod();
    		} finally {
    			this.hasSearchedRoleMethod = true;
    		}
    	}
    	
        return this.roleMethod;
    }

    public void setRoleMethod(IMethod meth)
	{
		this.roleMethod = meth;
	}
    
    /**
     * Only returns the role-methods part -- subclasses must override and 
     * construct the whole element name!
     */
	@Override
	public String getElementName()
	{
	    if (this.hasSignature)
	    {
	        return this.roleMethodHandle.toString();
	    }

	    return this.roleMethodHandle.getSelector();
	}

	@Override
	public int getDeclarationSourceStart()
    {
        return this.declarationSourceStart;
    }
	
	@Override
	public int getSourceStart()
	{
		return this.sourceStart;
	}
	
	@Override
	public int getSourceEnd()
	{
		return this.sourceEnd;
	}
	
	@Override
	public int getDeclarationSourceEnd()
	{
		return this.declarationSourceEnd;
	}

	@Override
	public boolean equals(Object obj)
	{
		MethodMapping other = (MethodMapping)obj;
		
		return super.equals(other)
//				&& declarationSourceStart == other.getDeclarationSourceStart()
//				&& _declarationSourceEnd == other.getDeclarationSourceEnd()
				&& getElementName().equals(other.getElementName());
	}
	
    @Override
	@SuppressWarnings("nls")
	public String toString()
	{
		return "methodmapping: " + getElementName();
	}

    /**
	 * Tries to find JavaElement method on demand for a given method from
	 * current binding. Lookup is using role hierarchy (implicit and explicit).
	 */
	protected IMethod findRoleMethod() throws JavaModelException
	{
		IType[]    implicitParents = TypeHelper.getImplicitSuperTypes(getDeclaringRole());
		HashSet<IType> allParents      = new HashSet<IType>();

		// collect all parents in role type hierarchy
		for (int idx = 0; idx < implicitParents.length; idx++)
		{
			IType elem = implicitParents[idx];        	

			// build super class hierarchy for element
			ITypeHierarchy hierarchy =
						elem.newSupertypeHierarchy( new NullProgressMonitor() );        				
			IType[] superTypes       = hierarchy.getAllSuperclasses(elem);

			// add implicit parent...
			allParents.add(elem);
			// ...and all "extends" parents
			if (superTypes.length > 0)
			{
				allParents.addAll(Arrays.asList(superTypes));
			} 
		}
		return findMethod(allParents.toArray(new IType[allParents.size()]),
						  this.roleMethodHandle);
	}
    
	protected abstract IRoleType getDeclaringRole();

	/**
	 * Tries to find an IMethod matching the given methodHandle in a set
	 * of types.
	 * @return the first matching IMethod in the set of types or null if
	 * 		   nothing found
	 */
	protected IMethod findMethod(IType[] types, IMethodSpec methodHandle)
		throws JavaModelException
	{
		// cycle through types...
		for (int parIdx = 0; parIdx < types.length; parIdx++)
		{
			IMethod[] methods = types[parIdx].getMethods();
            // ... and compare with each method defined in current type
			for (int methIdx = 0; methIdx < methods.length; methIdx++)
			{
				IMethod tmpMethod = methods[methIdx];
				// check for equal method name and signature            	
				String selector = tmpMethod.getElementName();
				if (isEqualMethod(methodHandle, tmpMethod, selector))
					// return immediately on first match
					return tmpMethod;
			}
			IOTType otType = OTModelManager.getOTElement(types[parIdx]);
			if (otType != null && otType.isRole()) {
				for (IMethodMapping mapping : ((IRoleType)otType).getMethodMappings(IRoleType.CALLOUTS)) {
					AbstractCalloutMapping tmpMethod = (AbstractCalloutMapping)mapping;
					if (tmpMethod == this)
						continue; // callout fakes its own role method, but don't take it for real here!
					// check for equal method name and signature            	
					String selector = tmpMethod.getCorrespondingJavaElement().getElementName();
					if (isEqualMethod(methodHandle, tmpMethod, selector))
						// return immediately on first match
						return tmpMethod;
				}
			}
		}
		IMethod methodReference= SourceMethod.createHandle((JavaElement)types[0], methodHandle);
		// failure might be due to mismatching qualified/simple types
		// this variant only uses the simple types:
		for (int parIdx = 0; parIdx < types.length; parIdx++) {
			IMethod[] methods= types[parIdx].findMethods(methodReference);
			if (methods != null && methods.length == 1)
				return methods[0];
		}
		return null;		
	}

	// helper for above to generalize over real methods and callouts:
	private boolean isEqualMethod(IMethodSpec baseMethodHandle, IMethod foundMethodOrCallout, String foundSelector) {
		if (!foundSelector.equals(baseMethodHandle.getSelector()))
			return false;
		if (!baseMethodHandle.hasSignature())
			return true;
		return Util.equalArraysOrNull(foundMethodOrCallout.getParameterTypes(), baseMethodHandle.getArgumentTypes());
	}
    
//{OT_COPY_PASTE: SourceRefElement, STATE: 3.4 M7
	/**
	 * Return the first instance of IOpenable in the hierarchy of this
	 * type (going up the hierarchy from this type);
	 */
	@Override
	public IOpenable getOpenableParent() 
	{
		IJavaElement current = getParent();
		while (current != null){
			if (current instanceof IOpenable)
			{
				return (IOpenable) current;
			}
//{ObjectTeams : Teams have no parents in the ot-hierarchy.
			if(current.getElementType() == IOTJavaElement.TEAM)
			{
				IOTType otElement = (IOTType) current;
				current = otElement.getCorrespondingJavaElement();
			}
//haebor}			
			current = current.getParent();
		}
		return null;
	}
	
//{OTModelUpdate : many of this methods shouldn't be delegated
//                 to the corresponding method. Started with these three.
//	public String getSource() throws JavaModelException
//	{
//	    return getIMethod().getSource();
//	}
//	
//	public ISourceRange getSourceRange() throws JavaModelException
//	{
//	    return getIMethod().getSourceRange();
//	}
//	public ISourceRange getNameRange() throws JavaModelException
//	{
//	    return getIMethod().getNameRange();
//	}
	
//haebor}
	/**
	 * @see ISourceReference
	 */
	@Override
	public String getSource() throws JavaModelException 
	{
		IOpenable openable = getOpenableParent();
		IBuffer buffer = openable.getBuffer();
		if (buffer == null) 
		{
			return null;
		}
		ISourceRange range = getSourceRange();
		int offset = range.getOffset();
		int length = range.getLength();
		if (offset == -1 || length == 0 ) 
		{
			return null;
		}
		try 
		{
			return buffer.getText(offset, length);
		} 
		catch(RuntimeException ex) 
		{
			return null;
		}
	}
	/**
	 * @see ISourceReference
	 */
	@Override
	public ISourceRange getSourceRange() throws JavaModelException 
	{
//{ObjectTeams: we don't have an ElementInfo but we know sourcestart, sourceend
		return new SourceRange(this.declarationSourceStart, this.declarationSourceEnd - this.declarationSourceStart + 1);
//haebor}		
//orig:		
//		SourceRefElementInfo info = (SourceRefElementInfo) getElementInfo();
//		return info.getSourceRange();
	}
	
//haebor}
	@Override
	public ISourceRange getNameRange() throws JavaModelException
	{
		ISourceRange range = new SourceRange(this.sourceStart, this.sourceEnd-this.sourceStart+1);
	    return range;
	}
	/** Answer the name that represents this mapping. */
	protected String getSourceName() {
		return super.getElementName();
	}
	
//delegates	
	IMethod getIMethod()
	{
	    return (IMethod) getCorrespondingJavaElement();
	}
	
	public String[] getExceptionTypes() throws JavaModelException
	{
	    return getIMethod().getExceptionTypes();
	}
	
	/**
	 * @deprecated (cf. IMethod.getTypeParameterSignatures())
	 */
	public String[] getTypeParameterSignatures() throws JavaModelException
	{
	    return getIMethod().getTypeParameterSignatures();
	}
	
	public int getNumberOfParameters()
	{
	    return getIMethod().getNumberOfParameters();
	}
	
	public String[] getParameterNames() throws JavaModelException
	{
		if (   this.roleMethodHandle != null
			&& this.roleMethodHandle.hasSignature())
				return this.roleMethodHandle.getArgumentNames();
	    return getIMethod().getParameterNames();
	}
	
	public String[] getParameterTypes()
	{
	    return getIMethod().getParameterTypes();
	}
	
	public String getReturnType() throws JavaModelException
	{
		if (   this.roleMethodHandle != null
			&& this.roleMethodHandle.hasSignature())
			return this.roleMethodHandle.getReturnType();
	    return getIMethod().getReturnType();
	}
	
	public String getSignature() throws JavaModelException
	{
	    return getIMethod().getSignature();
	}
	
	public boolean isConstructor() throws JavaModelException
	{
	    return getIMethod().isConstructor();
	}
	
	public boolean isMainMethod() throws JavaModelException
	{
	    return getIMethod().isMainMethod();
	}
	
	public boolean isLambdaMethod() {
		return false;
	}
	
	public boolean isSimilar(IMethod method)
	{
	    return getIMethod().isSimilar(method);
	}
	
	@Override
	public IClassFile getClassFile()
	{
	    return getIMethod().getClassFile();
	}
	
	@Override
	public ICompilationUnit getCompilationUnit()
	{
	    return getIMethod().getCompilationUnit();
	}
	
	@Override
	public IType getDeclaringType()
	{
	    return getIMethod().getDeclaringType();
	}
	
	@Override
	public int getFlags() throws JavaModelException
	{
	    return 0; // SH: method mappings have no regular flags. orig: getIMethod().getFlags();
	}
	
	@Override
	public IType getType(String name, int count)
	{
	    return getIMethod().getType(name, count);
	}
	
	@Override
	public boolean isBinary()
	{
	    return getIMethod().isBinary();
	}
	@Override
	public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws JavaModelException
	{
	    getIMethod().copy(container, sibling, rename, replace, monitor);
	}
	
	@Override
	public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException
	{
	    getIMethod().delete(force, monitor);
	}
	
	@Override
	public void move(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws JavaModelException
	{
	    getIMethod().move(container, sibling, rename, replace, monitor);
	}
	
	@Override
	public void rename(String name, boolean replace, IProgressMonitor monitor) throws JavaModelException
	{
	    getIMethod().rename(name, replace, monitor);
	}
	
    @Override
	public boolean hasSignature()
    {
        return this.hasSignature;
    }
    
	@Override
	public boolean exists()
	{
		IJavaElement myParent = getParent();
		if (!myParent.exists())
			return false;
		try {
			for (IJavaElement child : ((IType)myParent).getChildren())
				if (this.equals(child)) {
					// side-effect: fetch source range:
					if (this != child && this.declarationSourceStart == 0) {
						MethodMapping other = (MethodMapping) child;
						this.declarationSourceStart = other.declarationSourceStart;
						this.declarationSourceEnd   = other.declarationSourceEnd;
						this.sourceStart = other.sourceStart;
						this.sourceEnd   = other.sourceEnd;
					}
					return true;
				}
		} catch (JavaModelException e) { /* nop, will return false */ }
		return false;
	}

	@Override
	public boolean isStructureKnown() throws JavaModelException
	{
        // See exists()
		return getParent().isStructureKnown();
	}
	
	public String getKey() {
		// km: perhaps: calculating own key would be better
		return getIMethod().getKey();
	}
	
	public ITypeParameter[] getTypeParameters() throws JavaModelException {
		return NO_TYPE_PARAMETERS; // must not return null!
	}
	
	public String[] getRawParameterNames() throws JavaModelException {
		return EMPTY_STRING_ARRAY;
	}

	public ITypeParameter getTypeParameter(String name) {
		return new TypeParameter((JavaElement) getCorrespondingJavaElement(), name);
	}

	public boolean isResolved() {
		return false;
	}
	
	@Override
	public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	//OT_COPY_PASTE from Member.getCategories(). STATE: 3.4 M7, checked at 3.5 M7
	@Override
	@SuppressWarnings("rawtypes")
	public String[] getCategories() throws JavaModelException {
		IType type = (IType) getAncestor(IJavaElement.TYPE);
		if (type == null) return CharOperation.NO_STRINGS;
		if (type.isBinary()) {
			return CharOperation.NO_STRINGS;
		} else {
			SourceTypeElementInfo info = (SourceTypeElementInfo) ((SourceType) type).getElementInfo();
			HashMap map = info.getCategories();
			if (map == null) return CharOperation.NO_STRINGS;
			String[] categories = (String[]) map.get(this);
			if (categories == null) return CharOperation.NO_STRINGS;
			return categories;
		}
	}
	
	//OT_COPY_PASTE from Member.getJavadocRange(). STATE: 3.4 M7
	@Override
	public ISourceRange getJavadocRange() throws JavaModelException {
		ISourceRange range= this.getSourceRange();
		if (range == null) return null;
		IBuffer buf= null;
		if (this.isBinary()) {
			buf = this.getClassFile().getBuffer();
		} else {
			ICompilationUnit compilationUnit = this.getCompilationUnit();
			if (!compilationUnit.isConsistent()) {
				return null;
			}
			buf = compilationUnit.getBuffer();
		}
		final int start= range.getOffset();
		final int length= range.getLength();
		if (length > 0 && buf.getChar(start) == '/') {
			IScanner scanner= ToolFactory.createScanner(true, false, false, false);
			scanner.setSource(buf.getText(start, length).toCharArray());
			try {
				int docOffset= -1;
				int docEnd= -1;
				
				int terminal= scanner.getNextToken();
				loop: while (true) {
					switch(terminal) {
						case ITerminalSymbols.TokenNameCOMMENT_JAVADOC :
							docOffset= scanner.getCurrentTokenStartPosition();
							docEnd= scanner.getCurrentTokenEndPosition() + 1;
							terminal= scanner.getNextToken();
							break;
						case ITerminalSymbols.TokenNameCOMMENT_LINE :
						case ITerminalSymbols.TokenNameCOMMENT_BLOCK :
							terminal= scanner.getNextToken();
							continue loop;
						default :
							break loop;
					}
				}
				if (docOffset != -1) {
					return new SourceRange(docOffset + start, docEnd - docOffset + 1);
				}
			} catch (InvalidInputException ex) {
				// try if there is inherited Javadoc
			}
		}
		return null;
	}

//{CRIPPLE:
	@Override
	public int getOccurrenceCount() {
		// TODO Auto-generated method stub
		return 0;
	}
// km}

	/**
	 * Copied from Member.
	 * @see IMember#getTypeRoot()
	 */
	@Override
	public ITypeRoot getTypeRoot() {
		IJavaElement element = getParent();
		while (element instanceof IMember) {
			element= element.getParent();
		}
		return (ITypeRoot) element;
	}

	@Override
	public OTJavaElement resolved(Binding binding) {
		char[] uniqueKey = binding.computeUniqueKey();
		if (uniqueKey == null)
			throw new AbortCompilation(); // better than NPE below
		return resolved(uniqueKey);
	}

	public abstract OTJavaElement resolved(char[] uniqueKey);

	@Override
	protected char getHandleMementoDelimiter() {
		return OTEM_METHODMAPPING;
	}
}
