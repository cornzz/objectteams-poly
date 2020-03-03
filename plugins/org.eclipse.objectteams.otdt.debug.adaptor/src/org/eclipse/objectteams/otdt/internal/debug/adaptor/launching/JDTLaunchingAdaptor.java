/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008, 2014 Technical University Berlin, Germany.
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
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor.launching;


import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.objectteams.otdt.core.ext.OTJavaNature;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;
import org.eclipse.objectteams.otdt.core.ext.WeavingScheme;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;
import org.eclipse.objectteams.otdt.debug.OTVMRunnerAdaptor;
import org.eclipse.objectteams.otdt.debug.OTBreakpointInstaller;
import org.eclipse.objectteams.otdt.internal.debug.adaptor.dynamic.RedefineClassesBPListener;
import org.eclipse.pde.internal.ui.IPDEUIConstants;

import base org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import base org.eclipse.jdt.launching.StandardClasspathProvider;

/**
 * This team observes java launches and potentially modifies them for OT/J capabilities.
 * <ul>
 * <li>Add BCEL and otre_min to the classpath (role {@link ClasspathExtender})
 * <li>Maintain an OTVMRunnerAdaptor for adapting vm- and program args (role {@link JDTLaunchingAdaptor.AbstractJavaLaunchConfigurationDelegate}).
 * </ul>
 * Role {@link JDTLaunchingAdaptor.AbstractJavaLaunchConfigurationDelegate} only binds the above behavior into Java launches.
 * See {@link JUnitLaunchingAdaptor.JUnitLaunchConfigurationDelegate} for equal binding to JUnit launches.
 *  
 * @author stephan
 * @since 1.2.1
 */
@SuppressWarnings("restriction")
public team class JDTLaunchingAdaptor {

	/**
	 * This role extends the classpath with BCEL and otre_min paths if OT/J is enabled for the launch.
	 */
	protected class ClasspathExtender playedBy StandardClasspathProvider {

		IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration config) 
				<- replace IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration config);

		callin IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration config) throws CoreException 
		{
	    	IRuntimeClasspathEntry[] origEntries = base.computeUnresolvedClasspath(config);
	    	
	    	if (!isOTJApplicationLaunch(config))
	    		return origEntries;
	    	
	    	// add BCEL or ASM and otre_min or otredyn.min (classpath / bootclasspath)
	    	WeavingScheme scheme = OTJavaNature.getWeavingScheme(JavaRuntime.getJavaProject(config));
	    	int oldLength = origEntries.length;
			IRuntimeClasspathEntry[] otRuntimeEntries = computePathsToAdd(origEntries, scheme);
			
			// merge results:
	    	IRuntimeClasspathEntry[] result = new IRuntimeClasspathEntry[oldLength + otRuntimeEntries.length];
			System.arraycopy(origEntries, 0, result, 0, oldLength);
			System.arraycopy(otRuntimeEntries, 0, result, oldLength, otRuntimeEntries.length);
	        return result;
		}
		
	    static IRuntimeClasspathEntry[] computePathsToAdd(IRuntimeClasspathEntry[] origEntries, WeavingScheme scheme)
		{
			boolean hasBytecodeLib = false;
			boolean hasOTRE_min = false;
	
			IPath otreMinJarPath = OTREContainer.getOtreMinJarPath();
			IPath[] weaverPaths = OTREContainer.getWeaverPaths(scheme);
			int pathsNeeded = weaverPaths.length;
			for (int i = 0; i < origEntries.length; i++) {
	            IPath entryPath = origEntries[i].getPath();
	            if (otreMinJarPath.equals(entryPath)) {
	            	hasOTRE_min = true;
	            } else {
	            	for (IPath bcPath : weaverPaths) {
	            		if (bcPath.equals(entryPath)) {
	            			pathsNeeded--;
	            			break;
	            		}
	            	}
	            	if (pathsNeeded == 0)
	            		hasBytecodeLib = true;
				}
	        }
	
			List<IRuntimeClasspathEntry> result = new LinkedList<IRuntimeClasspathEntry>();
			IRuntimeClasspathEntry entry;
	
			if (!hasBytecodeLib) {
				int l = weaverPaths.length;
				for (int i = 0; i < l; i++) {					
					entry = JavaRuntime.newArchiveRuntimeClasspathEntry(weaverPaths[i]);
					result.add(entry);			
				}
			}

			if (!hasOTRE_min) {
				entry = JavaRuntime.newArchiveRuntimeClasspathEntry(otreMinJarPath);
				entry.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
				result.add(entry);
			}
			
			return result.toArray(new IRuntimeClasspathEntry[result.size()]);
		}

	}
	
	/**
	 * This role performs the adaptations of vmargs and program args (including main class name).
	 */
	protected class AbstractJavaLaunchConfigurationDelegate playedBy AbstractJavaLaunchConfigurationDelegate 
	{
		IJavaProject getJavaProject(ILaunchConfiguration arg0) -> IJavaProject getJavaProject(ILaunchConfiguration arg0);

		OTVMRunnerAdaptor fAdaptor;
		String fOriginalMain;

		// --- Initiate adaptations: (this callin actually applies to sub-base-classes)
		void prepareLaunch(ILaunchConfiguration config, String mode, ILaunch launch)
				<- before void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor);
		void prepareLaunch(ILaunchConfiguration config, String mode, ILaunch launch) throws CoreException 
		{
			this.fOriginalMain = null; // reset potential left over from previous launching
			if (!isOTJApplicationLaunch(config)) {
				this.fAdaptor = null;
				return;
			}
			IJavaProject javaProject = getJavaProject(config);
			this.fAdaptor = new OTVMRunnerAdaptor(javaProject);
			this.fAdaptor.setAdaptationArgs(config, mode, launch);
			// install OT-breakpoints
			if (ILaunchManager.DEBUG_MODE.equals(mode)) {
				OTBreakpointInstaller.installOTBreakpoints(javaProject,
						RedefineClassesBPListener.get(this.fAdaptor.getWeavingScheme()));
			}
		}

		// --- VM Arguments: ---
		String getVMArguments(ILaunchConfiguration configuration) 
				<- replace String getVMArguments(ILaunchConfiguration configuration)
			when (this.fAdaptor != null);


		callin String getVMArguments(ILaunchConfiguration config) throws CoreException {
			String vmArgs = base.getVMArguments(config);
			return this.fAdaptor.adaptVMArgumentString(vmArgs);
		}

		// --- Program Arguments: ---
		String getProgramArguments(ILaunchConfiguration config) 
				<- replace String getProgramArguments(ILaunchConfiguration config)
			when (this.fOriginalMain != null);

		callin String getProgramArguments(ILaunchConfiguration config) throws CoreException {
			String programArguments = base.getProgramArguments(config);
			return this.fOriginalMain + ' ' + programArguments;
		}		
	}
		
	static boolean isOTJApplicationLaunch(ILaunchConfiguration config) {
		try {
			return    config.getAttribute(OTDebugPlugin.OT_LAUNCH, false)                               // OT/J ?
				  && (config.getAttribute(IPDEUIConstants.LAUNCHER_PDE_VERSION, (String)null) == null); // not PDE ?
		} catch (CoreException e) {
			return false; // don't apply adaptations to bogus config
		}
	}
}
