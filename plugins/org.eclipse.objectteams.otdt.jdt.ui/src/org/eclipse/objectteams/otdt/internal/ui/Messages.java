/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Technical University Berlin, Germany.
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
package org.eclipse.objectteams.otdt.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = Messages.class.getName();


	public static String Completion_method_binding_label;
	public static String Completion_callout_label;
	public static String Completion_callin_label;

	public static String Completion_callout_to_field_label;

	public static String Completion_default_lifting_constructor_label;

	public static String Completion_override_role_label;

	public static String OTLayoutActionGroup_MenuOTPresentations;
	public static String OTLayoutActionGroup_MenuShowCallinLabels;
	public static String OTLayoutActionGroup_MenuDontShowCallinLabels;

	public static String PackageExplorer_DisplayRoleFilesAction;
	public static String PackageExplorer_DisplayRoleFilesDescription;
	public static String PackageExplorer_DisplayRoleFilesTooltip;

	public static String QuickOutline__and_role_files;

	public static String ViewAdaptor_guard_predicate_postfix;

	public static String NewOTProjectWizardPageOne_JREGroup_title;
	public static String NewOTProjectWizardPageOne_Weaving_label;


	public static String Validation_Target18IncompatibleWithOTRE_warning;
	public static String Validation_MultipleComplianceProblems_error;


	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() { /* do not instantiate */ }
}
