/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2015 Fraunhofer Gesellschaft, Munich, Germany,
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
package org.eclipse.objectteams.otdt.internal.ui.preferences;

import org.eclipse.osgi.util.NLS;

/**
 * This class integrates OT-specific messages for the property/preference pages
 * with those provided by the JDT (OT-specific messages have precedence).
 *
 * Created on Sep 11, 2005
 * 
 * @author stephan
 */
public class OTPreferencesMessages {

	private static final String BUNDLE_NAME= "org.eclipse.objectteams.otdt.internal.ui.preferences.OTPreferencesMessages";//$NON-NLS-1$


	private OTPreferencesMessages() {
	}

	static {
		NLS.initializeMessages(BUNDLE_NAME, OTPreferencesMessages.class);
	}
	
	public static String OTCompilerPreferencePageName;
	public static String OTCompilerPropertyPageName;

	public static String OTCompilerPreferencePage_title;

	public static String OTCompilerConfigurationBlock_weaving_label;
	public static String OTCompilerConfigurationBlock_weaving_otre_label;
	public static String OTCompilerConfigurationBlock_weaving_otdre_label;
	
	public static String OTCompilerConfigurationBlock_common_description;
	public static String OTCompilerProblemConfiguration_otjld_ref_description;

	public static String OTCompilerConfigurationBlock_section_decapsulation;
	
	public static String OTCompilerConfigurationBlock_decapsulation_label;
	public static String OTCompilerConfigurationBlock_decapsulation_write_label;
	public static String OTCompilerConfigurationBlock_override_final_role;
	public static String OTCompilerConfigurationBlock_adapting_deprecated_label;
	public static String OTCompilerConfigurationBlock_binding_to_system_class;
	

	public static String OTCompilerConfigurationBlock_section_unsafe;

	public static String OTCompilerConfigurationBlock_fragile_callin_label;
	public static String OTCompilerConfigurationBlock_unsafe_role_instantiation_label;
	public static String OTCompilerConfigurationBlock_abstract_potential_relevant_role_label;
	public static String OTCompilerConfigurationBlock_baseclass_cycle_label;
	public static String OTCompilerConfigurationBlock_potential_ambiguous_playedby_label;
	

	public static String OTCompilerConfigurationBlock_section_programming_problems;
	
	public static String OTCompilerConfigurationBlock_effectless_fieldaccess_label;
	public static String OTCompilerConfigurationBlock_ignoring_role_result;
	public static String OTCompilerConfigurationBlock_unused_parammap_label;
	public static String OTCompilerConfigurationBlock_ambiguous_lowering_label;

	
	public static String OTCompilerConfigurationBlock_section_control_flow;
	
	public static String OTCompilerConfigurationBlock_not_exactly_one_basecall_label;
	public static String OTCompilerConfigurationBlock_exception_in_guard;


	public static String OTCompilerConfigurationBlock_section_code_style;
	
	public static String OTCompilerConfigurationBlock_bindingconventions_label;
	public static String OTCompilerConfigurationBlock_inferred_callout_label;
	public static String OTCompilerConfigurationBlock_deprecated_path_syntax_label;
	
	
	
	public static String preferences_general_title;
	public static String preferences_general_callinmarker_label;
	public static String preferences_general_debugfilters_label;
}
