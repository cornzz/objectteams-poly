/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Technical University Berlin, Germany.
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
package org.eclipse.objectteams.otdt.internal.ui.viewsupport;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

/**
 * This class provides a dummy implementation for a lightweight label decorator.
 * It is only provided so we can install a decorator extension that is configurable via the preferences.
 * The actual work is done by the {@link ViewAdaptor}.
 * 
 * @author stephan
 * @since 1.2.8
 */
public class DummyDecorator implements ILightweightLabelDecorator {

	/** ID of the decorator extension regarding overriding roles. */
	final static String OVERRIDING_ROLE_DECORATOR_ID = "org.eclipse.objectteams.otdt.jdt.ui.overridingRoleDecorator"; //$NON-NLS-1$
	/** ID of the decorator extension regarding bound roles. */
	final static String BOUND_ROLE_DECORATOR_ID      = "org.eclipse.objectteams.otdt.jdt.ui.boundRoleDecorator"; //$NON-NLS-1$


	public void decorate(Object element, IDecoration decoration) {
		// nop, work is done by the ViewAdaptor.
	}

	public void addListener(ILabelProviderListener listener) { /* empty */ }

	public void dispose() { /* empty */ }

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) { /* empty */ }

}
