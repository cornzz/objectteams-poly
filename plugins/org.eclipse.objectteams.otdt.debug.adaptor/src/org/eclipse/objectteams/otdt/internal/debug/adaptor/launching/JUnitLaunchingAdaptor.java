/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
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

import base org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationDelegate;

/**
 * This team hooks the adaptation of its super team into JUnit launches.
 * Note, that only an instance of this sub-team is activated a boot-time.
 * 
 * @author stephan
 * @since 1.2.1
 */
public team class JUnitLaunchingAdaptor extends JDTLaunchingAdaptor {

	protected class JUnitLaunchConfigurationDelegate
			extends AbstractJavaLaunchConfigurationDelegate
			playedBy JUnitLaunchConfigurationDelegate 
	{
		// empty: this role only helps OT/Equinox so that it will weave into an overriding method.
		//        all adaptations are defined in the super role.
	}
}
