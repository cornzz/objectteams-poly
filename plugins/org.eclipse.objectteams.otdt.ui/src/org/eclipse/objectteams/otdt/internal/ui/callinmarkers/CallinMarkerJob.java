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
package org.eclipse.objectteams.otdt.internal.ui.callinmarkers;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.objectteams.otdt.core.ext.IMarkableJavaElement;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;

/**
 * @author gis
 */
public abstract class CallinMarkerJob extends org.eclipse.core.runtime.jobs.Job
{
    private final IResource _resource;
    private final IJavaElement _javaElement; // an IClassFile
    
    public CallinMarkerJob(final IMarkableJavaElement target)
    {
        super(OTDTUIPlugin.getResourceString("CallinMarkerJob.job_title")); //$NON-NLS-1$
        if (target.isBinary()) {
        	this._resource = null;
        	this._javaElement= target.getJavaElement();
        } else {
        	this._resource = target.getResource();
	        this._javaElement= null;
        }
        // markerRule is normally null, but let's play by the rules:
        IResource resource = target.getResource();
        if (resource != null)
        	setRule(resource.getWorkspace().getRuleFactory().markerRule(resource));
    }

    protected IStatus run(IProgressMonitor monitor)
    {
        try {
            updateMarkers(monitor);
        }
        catch (OperationCanceledException ex) {
            return Status.CANCEL_STATUS;
        }
        catch (Exception ex) {
            return OTDTUIPlugin.createErrorStatus("Exception during marker creation.", ex); //$NON-NLS-1$
        }
        catch (Error error) {} // ignore other errors, like Assertions and InternalCompilerErrors

        return Status.OK_STATUS;
    }

    protected abstract void updateMarkers(IProgressMonitor monitor) throws Exception;
    
    public final IResource getResource() 
    {
        return this._resource;
    }
    
    public final IJavaElement getJavaElement() 
    {
        return this._javaElement;
    }
}
