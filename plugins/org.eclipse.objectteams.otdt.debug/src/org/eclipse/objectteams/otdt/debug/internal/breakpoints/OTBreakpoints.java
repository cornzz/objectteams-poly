/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2017 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany, and others.
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
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.internal.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;

import static org.eclipse.objectteams.otdt.debug.internal.breakpoints.IOOTBreakPoints.*;

/**
 * @author ike
 *
 * This class provides methods to create OOT-specific (org.objectteams.Team) breakpoints
 *
 */
@SuppressWarnings("nls")
public class OTBreakpoints {

	private static final String DEBUG_HOOKS = "org.eclipse.objectteams.runtime.DebugHooks";
	private static final int LINE_afterRedefineClasses = 30;
	
	public enum Descriptor {
		/** associated with "public Team() {}" */
		TeamConstructor(		".TeamBreakpoint.Constructor", 				LINE_TeamConstructor),
	    /** associated with "doRegistration();" */
		TeamActivate(			".TeamBreakpoint.ActivateMethod", 			LINE_ActivateMethod),
	    /** associated with "_OT$lazyGlobalActiveFlag = false;" */
		TeamDeactivate(			".TeamBreakpoint.DeactivateMethod", 		LINE_DeactivateMethod),
	    /** associated with "implicitActivationsPerThread.set(Integer.valueOf(implActCount + 1 ));" */
		TeamImplicitActivate(	".TeamBreakpoint.ImplicitActivateMethod", 	LINE_ImplicitActivateMethod),
	    /** associated with "implicitActivationsPerThread.set(Integer.valueOf(implActCount - 1));" */
		TeamImplicitDeactivate(	".TeamBreakpoint.ImplicitDeactivateMethod", LINE_ImplicitDeactivateMethod),
		/** associated with implicit "return;" */
		TeamFinalize(			".TeamBreakpoint.FinalizeMethod",			LINE_FinalizeMethod),

		/** after InstrumentationImpl.redefineClasses() via DebugHook.afterRedefineClasses(). */
		RedefineClasses(		".InstrumentationBreakpoint.redefineClasses", LINE_afterRedefineClasses) {
			@Override public boolean isOOTBreakPoint() { return false; }
			@Override public String getTypeName() { return DEBUG_HOOKS; }
		};

		String BP_ID;
		int lineNumber;
		private Descriptor(String id, int lineNumber) {
			this.BP_ID = OTDebugPlugin.PLUGIN_ID + id;
			this.lineNumber = lineNumber;
		}

		/** Is this a descriptor for a breakpoint on org.objectteams.Team? */
		public boolean isOOTBreakPoint() { return true; }

		/** Does the given breakpoint match this descriptor? */
		public boolean matches(IBreakpoint breakpoint) throws CoreException {
			return breakpoint.getMarker().getAttribute(BP_ID) != null;
		}

		/** Answer the qualified name of the type on which this descriptor installs its breakpoint. */
		public String getTypeName() {
			return String.valueOf(IOTConstants.STR_ORG_OBJECTTEAMS_TEAM);
		}

		/** Check if 'collected' already contains a matching BP, otherwise create and insert a new BP. */
		public void insertInto(IType type, Map<String,IBreakpoint> collected) throws CoreException {
			if (!collected.containsKey(this.BP_ID))
				collected.put(this.BP_ID, createBreakpoint(type));
		}

		Map<String, Boolean> getBreakpointAttributes() {
	        Map<String, Boolean> attrs = new HashMap<String, Boolean>();
	        attrs.put(OTDebugPlugin.PLUGIN_ID + ".SyntheticBreakpoint", Boolean.TRUE);
	        return attrs;
	    }
		/* default impl, individual descriptors may override. */
		IBreakpoint createBreakpoint(IType oot) throws CoreException {
			Map<String, Boolean> attributes = getBreakpointAttributes();
			attributes.put(BP_ID, Boolean.TRUE);
			return createLineBreakpoint(oot, this.lineNumber, attributes);
	    }
	
		private IBreakpoint createLineBreakpoint(IType oot, int linenumber, Map attributes) 
				throws CoreException
		{
			IResource teamResource = oot.getJavaProject().getResource();
			IJavaBreakpoint breakpoint = JDIDebugModel.createLineBreakpoint(
					teamResource,
					oot.getFullyQualifiedName(),
					linenumber,
					-1, -1, 0,
					false /*register*/,
					attributes);
			breakpoint.setPersisted(false);
			
			return breakpoint;
		}
	}
}