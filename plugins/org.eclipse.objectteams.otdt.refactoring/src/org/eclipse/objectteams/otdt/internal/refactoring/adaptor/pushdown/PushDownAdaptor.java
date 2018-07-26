/**
 * 
 */
package org.eclipse.objectteams.otdt.internal.refactoring.adaptor.pushdown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor.MemberActionInfo;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;
import org.eclipse.objectteams.otdt.core.IPhantomType;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.hierarchy.OTTypeHierarchies;
import org.eclipse.objectteams.otdt.internal.refactoring.RefactoringMessages;
import org.eclipse.objectteams.otdt.internal.refactoring.util.IAmbuguityMessageCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.util.IOverloadingMessageCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.util.RefactoringUtil;
import org.eclipse.osgi.util.NLS;

import base org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor;

/**
 * @author Johannes Gebauer
 * 
 */
@SuppressWarnings("restriction")
public team class PushDownAdaptor {
	
	@SuppressWarnings("decapsulation") // base class is final
	protected class PushDownRefactoringProcessor playedBy PushDownRefactoringProcessor {
		
		// callouts
		MemberActionInfo[] getAbstractDeclarationInfos() 				 -> MemberActionInfo[] getAbstractDeclarationInfos();
		IType[] getAbstractDestinations(IProgressMonitor arg0) 			 -> IType[] getAbstractDestinations(IProgressMonitor arg0);
		IMember[] getMembersToMove()									 -> IMember[] getMembersToMove();
		IType getDeclaringType()										 -> IType getDeclaringType();
		ITypeHierarchy getHierarchyOfDeclaringClass(IProgressMonitor pm) -> ITypeHierarchy getHierarchyOfDeclaringClass(IProgressMonitor pm);
		
		private void checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context, RefactoringStatus status) throws CoreException {
			
			status.merge(checkForDirectPhantomSubRoles(pm));
			status.merge(checkForAspectBindings(pm));
			status.merge(checkShadowingFieldInImplicitHierarchy(pm));
			IType[] subclasses = getAbstractDestinations(pm);
			for (int i = 0; i < subclasses.length; i++) {
				status.merge(checkOverriding(subclasses[i],pm));
			}
			status.merge(checkOverloadingAndAmbiguity(pm));
		}
		

		void checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context, RefactoringStatus status) <- after RefactoringStatus checkFinalConditions(IProgressMonitor pm,
				CheckConditionsContext context) with {
			pm <- pm,
			context <- context,
			status <- result
		}
		
		private RefactoringStatus checkShadowingFieldInImplicitHierarchy(IProgressMonitor pm) throws JavaModelException {
			RefactoringStatus status = new RefactoringStatus();
			
			ITypeHierarchy hier = getHierarchyOfDeclaringClass(pm);
			ArrayList<IType> subTypes = new ArrayList<IType>();
			subTypes.addAll(Arrays.asList(hier.getSubtypes(getDeclaringType())));
			
			pm.beginTask(RefactoringMessages.PushDownAdaptor_checkShadowing_progress, subTypes.size());
			pm.subTask(""); //$NON-NLS-1$
			
			for (int i = 0; i < getMembersToMove().length; i++) {
				IMember element = getMembersToMove()[i];
				if(element instanceof IField){
					IField field = (IField) element;
					for (IType type : subTypes) {
						
						// shadowing fields is just forbidden in implicit hierarchies
						if(TypeHelper.isRole(type.getFlags())){
							ITypeHierarchy implicitHierarchy = type.newSupertypeHierarchy(pm);
							IType[] implicitSuperTypes = OTTypeHierarchies.getInstance().getAllTSuperTypes(implicitHierarchy, type);
							
							for (int j = 0; j < implicitSuperTypes.length; j++) {
								IType implicitSuperType = implicitSuperTypes[i];
								IField shadowingField = RefactoringUtil.fieldIsShadowedInType(field.getElementName(), field.getTypeSignature(), implicitSuperType);
								if(shadowingField != null){
									
									String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_shadowing_error, 
														  new String[] { field.getElementName(), implicitSuperType.getFullyQualifiedName('.') });
									status.addError(msg, JavaStatusContext.create(shadowingField));
									
								}
							}
						}
						pm.worked(1);
						// do not repeat errors in hierarchy
						if(status.hasError()){
							pm.done();
							return status;
						}
					}
				}
			}
			pm.done();
			return status;
			
		}
		
		/**
		 * Searches for aspect bindings that reference members to be moved.
		 * @return The refactoring status contains errors if the pushed down members are not visible in existing bidnings after refactoring.
		 */
		@SuppressWarnings("restriction")
		private RefactoringStatus checkForAspectBindings(IProgressMonitor monitor) throws CoreException {
			// search all references for the members to be moved
			IMember[] membersToMove = getMembersToMove();
			final HashMap<IMember,Set<SearchMatch>> references= new HashMap<IMember,Set<SearchMatch>>();
			IJavaSearchScope scope= SearchEngine.createWorkspaceScope();
			for (int i = 0; i < membersToMove.length; i++) {
				final IMember member = membersToMove[i];
				SearchPattern pattern= SearchPattern.createPattern(member, IJavaSearchConstants.REFERENCES, SearchPattern.R_EXACT_MATCH);
				SearchEngine engine= new SearchEngine();
				engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant()}, scope, new SearchRequestor() {
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						if (match.getAccuracy() == SearchMatch.A_ACCURATE && !match.isInsideDocComment()){
							if(references.get(member) == null){
								Set<SearchMatch> refSet = new HashSet<SearchMatch>();
								refSet.add(match);
								references.put(member, refSet);
							}else{
								references.get(member).add(match);
							}
						}
					}
				}, monitor);
			}
			
			// search the matches for aspect bindings
			RefactoringStatus status = new RefactoringStatus();
			for (int i = 0; i < membersToMove.length; i++) {
				IMember member = membersToMove[i];
				
				// do not search for aspect bindings if an abstract declaration remains
				if(leavesAbstractMethod(member)){
					continue;
				}
				
				Set<SearchMatch> refSet = references.get(member);
				if(refSet == null){
					continue;
				}
				for (SearchMatch match : refSet) {
					Object element= match.getElement();
					if (element instanceof ICalloutMapping) {
						ICalloutMapping mapping = (ICalloutMapping) element;
						if(mapping.getBoundBaseMethod().equals(member)){
							String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_referencedByCallout_error,
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						} else if(mapping.getRoleMethod() != null && mapping.getRoleMethod().equals(member)){
							String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_boundAsCallout_error,
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						}else{
							// TODO find a better way to analyze references in parameter mappings
							String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_referencedByCalloutParamMap_error,
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						}
					}
					if (element instanceof ICalloutToFieldMapping) {
						ICalloutToFieldMapping mapping = (ICalloutToFieldMapping) element;
						if(mapping.getBoundBaseField().equals(member)){
							String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_referencedByCTF_error,
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						} else if(mapping.getRoleMethod() != null && mapping.getRoleMethod().equals(member)){
							String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_boundAsCTF_error,
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						}else{
							// TODO find a better way to analyze references in parameter mappings
							String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_referencedByCTFParamMap_error,
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						}
					}
					if (element instanceof ICallinMapping) {
						ICallinMapping mapping = (ICallinMapping) element;
						boolean baseMethodFound = false;
						for (int j = 0; j < mapping.getBoundBaseMethods().length; j++) {
							if(mapping.getBoundBaseMethods()[i].equals(member)){
								String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_referencedByCallin_error,
										new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
								status.addError(msg, JavaStatusContext.create(mapping));
								baseMethodFound = true;
								break;
							}
						}
						
						if(baseMethodFound){
							continue;
						}
						
						if(mapping.getRoleMethod().equals(member)){
							String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_boundInCallin_error,
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						} else {
							// TODO find a better way to analyze references in parameter mappings
							String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_referencedInCallinParamMap_error,
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						}
					}
					
					if (element instanceof ResolvedSourceMethod) {
						ResolvedSourceMethod method = (ResolvedSourceMethod) element;
						// References in the declaring type are checked by the base
						if(!method.getDeclaringType().equals(getDeclaringType())){
							String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_referencedByMethod_error,
									new String[] { member.getElementName(), method.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(method));
						}
					}
				}
			}
			return status;
		}

		@SuppressWarnings("restriction")
		private boolean leavesAbstractMethod(IMember member) throws JavaModelException {
			MemberActionInfo[] methodsToBeDeclaredAbstract = getAbstractDeclarationInfos();
			for (int j = 0; j < methodsToBeDeclaredAbstract.length; j++) {
				if(methodsToBeDeclaredAbstract[j].getMember() == member){
					return true;
				}
			}
			return false;
		}
		
		private RefactoringStatus checkForDirectPhantomSubRoles(IProgressMonitor pm) throws JavaModelException {
			RefactoringStatus status = new RefactoringStatus();
			ITypeHierarchy hier = getHierarchyOfDeclaringClass(pm);
			
			OTTypeHierarchies.getInstance().setPhantomMode(hier, true);
			IType[] subTypes = hier.getSubtypes(getDeclaringType());
			for (int i = 0; i < subTypes.length; i++) {
				IType subType = subTypes[i];
				if(subType instanceof IPhantomType){
					String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_phantomRoleConflict_error, new String[] { getDeclaringType().getFullyQualifiedName('.'), subType.getFullyQualifiedName('.') });
					status.addError(msg, JavaStatusContext.create(subType));
				}
			}
			OTTypeHierarchies.getInstance().setPhantomMode(hier, false);
			return status;
		}
		
		/**
		 * Checks if the pushed down method overrides an implicitly inherited method.
		 * 
		 * @param type the type to check overriding in
		 * @param pm the progress monitor
		 * @return the <code>RefactoringStatus</code> indicating overriding
		 * @throws JavaModelException 
		 */
		private RefactoringStatus checkOverriding(IType type, IProgressMonitor pm) throws JavaModelException{
			RefactoringStatus status = new RefactoringStatus();
			
			// only roles inherit implicitly
			if(TypeHelper.isRole(type.getFlags())){
				
				IMember[] membersToPushDown = getMembersToMove();
				
				// create a hierarchy to check implicit super types
				ITypeHierarchy hierarchy = type.newSupertypeHierarchy(pm);
				IType[] superRoles = OTTypeHierarchies.getInstance().getTSuperTypes(hierarchy, type);
				
				pm.beginTask(RefactoringMessages.PushDownAdaptor_overriding_progress, superRoles.length);
				pm.subTask(""); //$NON-NLS-1$
				
				for (int i = 0; i < superRoles.length; i++) {
					IType superRole = superRoles[i];
					// do not search in the declaring type to avoid finding the pushed down method itself
					if(!superRole.equals(getDeclaringType())){
						for (int j = 0; j < membersToPushDown.length; j++) {
							// check only the pushed down methods
							if(membersToPushDown[j] instanceof IMethod){
								IMethod pushedDownMethod = (IMethod) membersToPushDown[j];
								IMethod overriddenMethod = superRole.getMethod(pushedDownMethod.getElementName(), pushedDownMethod.getParameterTypes());
								if(overriddenMethod.exists()){
									String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_overridingImplicitlyInherited_error, 
													pushedDownMethod.getElementName(), 
													overriddenMethod.getDeclaringType().getFullyQualifiedName('.') + '.' + overriddenMethod.getElementName());
									status.addError(msg, JavaStatusContext.create(overriddenMethod));
								}
							}
						}
					}
					pm.worked(1);
				}
			}
			pm.done();
			return status;
		}
		
		private RefactoringStatus checkOverloadingAndAmbiguityInType(IProgressMonitor pm, IType type) throws JavaModelException {
			RefactoringStatus status = new RefactoringStatus();
			ITypeHierarchy hier = getHierarchyOfDeclaringClass(pm);
			for (int i = 0; i < getMembersToMove().length; i++) {
				IMember element = getMembersToMove()[i];
				
				// overloading can only be caused by private methods
				if (Flags.isPrivate(element.getFlags()) && element instanceof IMethod){
					final IMethod method = (IMethod)element;
					String[] paramTypes = method.getParameterTypes();
					status.merge(RefactoringUtil.checkOverloadingAndAmbiguity(type, hier, method.getElementName(), paramTypes,
							new IAmbuguityMessageCreator() {

						public String createAmbiguousMethodSpecifierMsg() {
							return RefactoringMessages.PushDownAdaptor_ambiguousMethodSpec_error;
						}

					}, new IOverloadingMessageCreator() {

						public String createOverloadingMessage() {
							String msg = NLS.bind(RefactoringMessages.PushDownAdaptor_overloading_error, new String[] { method.getElementName()});
							return msg;
						}

					}, pm));
				}
			}
			return status;
		}
		
		private RefactoringStatus checkOverloadingAndAmbiguity(IProgressMonitor pm) throws JavaModelException {
			
			IType[] subtypes = getAbstractDestinations(pm);
			
			pm.beginTask(RefactoringMessages.PushDownAdaptor_overloading_progress, subtypes.length);
			pm.subTask(""); //$NON-NLS-1$
			
			RefactoringStatus status = new RefactoringStatus();
			
			// check overloading in subtypes of the destination type
			for (int i = 0; i < subtypes.length; i++) {
				status.merge(checkOverloadingAndAmbiguityInType(pm, subtypes[i]));
				pm.worked(1);
				
			}
			pm.done();
			return status;
		}
	} 
}
