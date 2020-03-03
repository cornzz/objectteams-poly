/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2015 Fraunhofer Gesellschaft, Munich, Germany,
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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;


/**
 * This factory provides a WorkbenchAdapter for IAdaptable OT elements and
 * needs to be registered by the platforms AdapterManager.
 * 
 * @author kaiser
 */
public class OTElementAdapterFactory implements IAdapterFactory
{
	WorkbenchAdapter _otAdapter   = new WorkbenchAdapter();
	Class<?>[]       _allAdapters = { IWorkbenchAdapter.class };

    @SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType)
    {
    	if (IWorkbenchAdapter.class.equals(adapterType))
    		return (T)_otAdapter;
    	
    	return null;
    }

    public Class<?>[] getAdapterList() {
        return _allAdapters;
    }
}
