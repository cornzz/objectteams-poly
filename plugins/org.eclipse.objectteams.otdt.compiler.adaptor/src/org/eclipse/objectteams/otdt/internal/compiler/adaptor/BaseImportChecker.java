/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007, 2012 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.compiler.adaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression.DecapsulationState;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.AccessRule;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ImportBinding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;

import base org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import base org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * This team checks whether base-imports are backed up by proper aspectBinding declarations.
 * (Only activated if the project has the PluginNature).
 * This team is only temporarily instantiated/activated by AdaptorActivator.JavaProject(JavaProject).
 * 
 * This team also handles the forcedExport declarations from aspectBindings extensions
 * and correspondingly converts some diagnostics from forbiddenAccess to decapsulationByForcedExport.
 * 
 * Other parts involved:
 * 
 *  + PDEAdaptor is responsible for adding aspectBindingData (of type AdaptedBaseBundle) 
 *    to ClasspathAccessRules and adjusting the problemID
 *  
 *  + org.eclipse.jdt.internal.core.builder.NameEnvironment
 *    - computeClasspathLocations(IWorkspaceRoot, JavaProject, SimpleLookupTable)
 *    Feed AccessRuleSet from ClasspathEntry into ClasspathLocations like ClasspathDirectory.
 *     
 *  + org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment
 *    - setAccessRestriction(ReferenceBinding, AccessRestriction)
 *    - AccessRestriction getAccessRestriction(TypeBinding type)
 *    Pass AccessRestriction from sources like ClasspathDirectory.accessRuleSet into the compiler. 
 * 
 * @author stephan
 * @since 1.1.2
 */
@SuppressWarnings("restriction")
public team class BaseImportChecker extends CompilationThreadWatcher
{
	private AspectBindingReader aspectBindingReader;
	
	public BaseImportChecker() {/* emtpy ctor for OT/Equinox */ }
	
	/**
	 * @param aspectBindingReader must be non-null (and initialized). 
	 */
	public BaseImportChecker(AspectBindingReader aspectBindingReader) {
		this.aspectBindingReader= aspectBindingReader;
	}

	/**
	 * If a forced exports exist convert some diagnostics from forbiddenAccess 
	 * to decapsulationByForcedExport.
	 */
	protected class ProblemReporter playedBy ProblemReporter 
	{   
		// imports via callout:
		protected void baseImportInRegularClass(TypeDeclaration firstType, ImportReference reference) 
			-> void baseImportInRegularClass(TypeDeclaration firstType, ImportReference reference);
		protected void illegalBaseImportNoAspectBinding(ImportReference ref, String teamName) 
			-> void illegalBaseImportNoAspectBinding(ImportReference ref, String teamName);
		protected void illegalBaseImport(ImportReference ref, String expectedBasePlugin, String actualBasePlugin) 
			-> void illegalBaseImport(ImportReference ref, String expectedBasePlugin, String actualBasePlugin);
		void illegalUseOfForcedExport(ReferenceBinding type, ASTNode reference) 
			-> void illegalUseOfForcedExport(ReferenceBinding type, ASTNode reference);
		void decapsulationByForcedExport(ReferenceBinding type, ASTNode reference) 
			-> void decapsulationByForcedExport(ReferenceBinding type, ASTNode reference);
		void baseImportFromSplitPackage(ImportReference ref, String expectedPlugin)
		    -> void baseImportFromSplitPackage(ImportReference ref, String expectedPlugin);
		

		ReferenceContext getReferenceContext() -> get ReferenceContext referenceContext;
		void setReferenceContext(ReferenceContext referenceContext) -> set ReferenceContext referenceContext;
		
		/** The callin entry into this role: analyze and report various access situations. */
		@SuppressWarnings("basecall")
		callin void forbiddenReference(TypeBinding type, ASTNode location, AccessRestriction restriction) 
		{
			DecapsulationState baseclassDecapsulation = getBaseclassDecapsulation(location);
			switch (restriction.getProblemId()) {
			case IProblem.BaseclassDecapsulationForcedExport:
				switch (baseclassDecapsulation) {
				case ALLOWED:
					decapsulationByForcedExport((ReferenceBinding)type, location);
					break;
				case REPORTED:
					break;
				default:
					// no forced export for regular use!
					illegalUseOfForcedExport((ReferenceBinding)type, location);
				}
				break;
			case IProblem.AdaptedPluginAccess:
				checkAdaptedPluginAccess(location, restriction);
				break;
			default:
				// normal access restriction, but ...
				if (baseclassDecapsulation == DecapsulationState.ALLOWED) {
					AccessRule rule= restriction.getAccessRule();
					if (rule.aspectBindingData != null)
						// ... as we have aspectBindingData, those should be checked first:
						if (!checkAdaptedPluginAccess(location, restriction))
							return;
				}
				base.forbiddenReference(type, location, restriction);				
			}
		}
		
		boolean checkAdaptedPluginAccess(ASTNode location, AccessRestriction restriction) {
			// not a real error but requires consistency check against aspectBinding:
			if (location instanceof ImportReference) {
				ImportReference imp= (ImportReference)location;
				if (imp.isBase()) {
					List<String> teamNames= getReferenceTeams();
					if (teamNames.isEmpty()) {
						baseImportInRegularClass(getPublicType(), imp);
						return false;
					}
					
					Set<String> basePlugins= new HashSet<String>();
					for (String teamName : teamNames) {
						Set<String> currentBasePlugins = aspectBindingReader.getBasePlugins(teamName);
						if (currentBasePlugins != null && !currentBasePlugins.isEmpty())
							basePlugins.addAll(currentBasePlugins);
					}
					
					if (basePlugins.isEmpty()) {
						illegalBaseImportNoAspectBinding(imp, teamNames.isEmpty() ? null : teamNames.get(0));
						return false;
					}
					String baseString = flattenSet(basePlugins);
					Set<String> actualBases = new HashSet<String>();
					AccessRule rule= restriction.getAccessRule();
					if (rule.aspectBindingData != null) {
						for (Object data : rule.aspectBindingData) {
							AdaptedBaseBundle info= (AdaptedBaseBundle) data;
							for (String teamName : teamNames) {
								if (info.isAdaptedBy(teamName)) {
									// OK, no error
									if (info.hasPackageSplit) {
										ReferenceContext contextSave = getReferenceContext();
										baseImportFromSplitPackage(imp, baseString); // just a warning
										setReferenceContext(contextSave);
									}
									return true;
								}
							}
							actualBases.add(info.getSymbolicName());
						}
					}
					illegalBaseImport(imp, baseString, flattenSet(actualBases));
					return false;
				}
			}
			return true;
		}
		void forbiddenReference(TypeBinding type, ASTNode location, AccessRestriction restriction)
		<- replace void forbiddenReference(TypeBinding type, ASTNode location, byte entryType, AccessRestriction restriction)
		   with { type <- type, location <- location, restriction <- restriction }

		void forbiddenReference(TypeBinding type, ASTNode location, AccessRestriction restriction) 
		<- replace void forbiddenReference(MethodBinding method, InvocationSite location, byte entryType, AccessRestriction restriction)
		   with { type <- method.declaringClass, location <- (ASTNode)location, restriction <- restriction }

		void forbiddenReference(TypeBinding type, ASTNode location, AccessRestriction restriction) 
		<- replace void forbiddenReference(FieldBinding field, ASTNode location, byte entryType, AccessRestriction restriction)
		   with { type <- field.declaringClass, location <- location, restriction <- restriction }

		
		private DecapsulationState getBaseclassDecapsulation(ASTNode location) {
			if (location instanceof Expression) {
				if (location instanceof AllocationExpression)
					return DecapsulationState.REPORTED; // base-ctor expression.
				if (location instanceof MessageSend)
					return DecapsulationState.REPORTED; // callout message send.
				Expression expr= (Expression) location;
				return expr.getBaseclassDecapsulation();
			} 
			if (location instanceof ImportReference) {
				ImportReference impRef= (ImportReference)location;
				if (impRef.isBase())
					return DecapsulationState.ALLOWED; // always need to report 
			}
			return DecapsulationState.NONE;
		}
		private List<String> getReferenceTeams() {
			TypeDeclaration type= getPublicType();
			if (type != null && type.isTeam()) {
				List<String> names = new ArrayList<String>();
				addTeamNames(type.binding, names);
				return names;
			}
			return Collections.emptyList();
		}
		private void addTeamNames(ReferenceBinding type, List<String> names) {
			char[] binaryName = CharOperation.concatWith(type.compoundName, '.');
			names.add(String.valueOf(CharOperation.replace(binaryName, IOTConstants.OT_DELIM_NAME, new char[0])));
			for (ReferenceBinding member : type.memberTypes())
				if (member != null && member.isTeam())
					addTeamNames(member, names);
		}
		private TypeDeclaration getPublicType() {
			ReferenceContext context= getReferenceContext();
			if (context instanceof CompilationUnitDeclaration) {
				CompilationUnitDeclaration unit= (CompilationUnitDeclaration)context;
				if (unit.types == null) return null;
				for (TypeDeclaration type : unit.types)
					if (Flags.isPublic(type.modifiers))
						return type;
			}
			return null;
		}
	}
	
	
	protected class ImportTracker playedBy CompilationUnitScope
	{
		SourceTypeBinding[] getTopLevelTypes() -> get SourceTypeBinding[] topLevelTypes;
	  private // don't publically expose protected role ProblemReporter
		ProblemReporter     problemReporter()  ->     ProblemReporter    problemReporter();

		/** When setting the base imports to a CUScope, check for imports from undeclared plug-ins. */
		void setBaseImports(ImportBinding[] resolvedBaseImports, int baseCount, ImportReference[] refs) 
		<- before void setBaseImports(ImportBinding[] resolvedBaseImports, int baseCount, ImportReference[] refs);
		void setBaseImports(ImportBinding[] resolvedBaseImports, int baseCount, ImportReference[] refs) 
		{
			if (baseCount == 0) return;
			ReferenceBinding teamType = findMainType();
			String teamName= (teamType != null)
								? new String(teamType.readableName()) : null;
			for (int i=0; i<baseCount; i++) {
				if (teamType == null) {
					problemReporter().baseImportInRegularClass(null, refs[i]);
					continue;
				}
				if (resolvedBaseImports[i].onDemand) // syntactically impossible
					throw new InternalCompilerError("Ondemand base import not supported");  //$NON-NLS-1$
				String basePlugins= null;
				if (resolvedBaseImports[i].resolvedImport instanceof ReferenceBinding) {
					ReferenceBinding importedType= (ReferenceBinding)resolvedBaseImports[i].resolvedImport;
					if (!importedType.isValidBinding())
						continue; // already reported
					if (importedType.hasRestrictedAccess())
						continue; // checked by forbiddenReference()
					if (aspectBindingReader.isAdaptingSelf(teamName)) {
						char[][] current= CharOperation.splitOn('/', teamType.getFileName());
						char[][] imported= CharOperation.splitOn('/', importedType.getFileName());
						if (CharOperation.equals(current[1], imported[1]))
							return;
						basePlugins= "<self>"; //$NON-NLS-1$
					}
				}
				if (basePlugins == null)
					basePlugins= flattenSet(aspectBindingReader.getBasePlugins(teamName));
				if (basePlugins != null)
					problemReporter().illegalBaseImport(refs[i], basePlugins, null);
				else
					problemReporter().illegalBaseImportNoAspectBinding(refs[i], teamName);
			}
		}
		private ReferenceBinding findMainType() {
			SourceTypeBinding[] toplevelTypes = getTopLevelTypes();
			if (toplevelTypes != null)
				for (SourceTypeBinding referenceBinding : toplevelTypes) 
					if (referenceBinding.isPublic())
						return referenceBinding;

			return null;
		}
	}
	@SuppressWarnings("nls")
	String flattenSet(Set<String> stringSet) {
		if (stringSet == null || stringSet.size() == 0) return null;
		Iterator<String> iterator = stringSet.iterator();
		if (stringSet.size()==1) {
			return iterator.next();
		} else {
			String result = "[";
			while(true) {
				result += iterator.next();
				if (!iterator.hasNext()) break;
				result += ", ";
			}
			return result + "]";
		}
	}
}
