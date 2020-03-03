/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2009 Fraunhofer Gesellschaft, Munich, Germany,
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
package org.eclipse.objectteams.otdt.internal.ui;

import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;

/**
 * A collection of all plugin related ids
 * NOTE: Keep them up to date with ids in plugin.xml
 * 
 * @author kaiser
 * @version $Id: OTDTUIPluginConstants.java 23434 2010-02-03 23:52:31Z stephan $
 */
@SuppressWarnings("nls")
public interface OTDTUIPluginConstants
{
	public static final String UIPLUGIN_ID 		   = OTDTPlugin.PLUGIN_ID + ".ui";

	public static final String RESOURCES_ID        = UIPLUGIN_ID + ".OTPluginResources";

	// perspectives
	public static final String PERSPECTIVE_ID      = UIPLUGIN_ID + ".OTJavaPerspective";

	// wizards
	public static final String NEW_TEAM_WIZARD_ID  = UIPLUGIN_ID + ".wizards.NewTeamCreationWizard";
	public static final String NEW_ROLE_WIZARD_ID  = UIPLUGIN_ID + ".wizards.NewRoleCreationWizard";
	
	// extension point:
	public static final String UPDATE_RULER_ACTION_EXTENDER_ID    		= "updateRulerActionExtenders";
	public static final String UPDATE_RULER_ACTION_EXTENDER_CLASS 		= "class";
	public static final String UPDATE_RULER_ACTION_EXTENDER_EDITORCLASS = "editorClass";

}