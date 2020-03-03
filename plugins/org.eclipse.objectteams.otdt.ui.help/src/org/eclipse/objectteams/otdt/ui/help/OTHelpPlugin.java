/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2008 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
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
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.help;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * The main plugin class to be used in the desktop.
 */
@SuppressWarnings("deprecation") // package admin is still recommended for this particular purpose
public class OTHelpPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static OTHelpPlugin plugin;
	
	/** this plugin: */
	public static final String PLUGIN_ID = "org.eclipse.objectteams.otdt.ui.help"; //$NON-NLS-1$
	/** companion bundle (code-less): */
	public static final String OT_DOC_BUNDLE = "org.eclipse.objectteams.otdt.doc"; //$NON-NLS-1$
	public static final String OTJLD_VIEW = "org.eclipse.objectteams.otdt.ui.help.views.OTJLDView"; //$NON-NLS-1$
		
	private static final String ICON_OTJLD = "icons/ot_paragraph.gif"; //$NON-NLS-1$


	private static BundleContext fContext;

	/**
	 * The constructor.
	 */
	public OTHelpPlugin() {
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		fContext = context;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		fContext = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static OTHelpPlugin getDefault() {
		return plugin;
	}

	/** Get the Bundle representation of org.eclipse.objectteams.otdt.doc, which has no implementation of its own. */
	public static Bundle getDocPlugin() {
		ServiceReference<PackageAdmin> ref= (ServiceReference<PackageAdmin>) fContext.getServiceReference(PackageAdmin.class);
		if (ref == null)
			throw new IllegalStateException("Cannot connect to PackageAdmin"); //$NON-NLS-1$
		PackageAdmin packageAdmin = fContext.getService(ref);
		return packageAdmin.getBundles(OT_DOC_BUNDLE, null)[0];
	}

	public static void logException(String message, Throwable exception) {
		plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, exception));
	}
	
	/** Returns the icon representing the OTJLD. */
	public static ImageDescriptor getOTJLDImage() {
		return imageDescriptorFromPlugin(PLUGIN_ID, ICON_OTJLD);
	}
}
