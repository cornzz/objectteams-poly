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

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;

/**
 * @author gis
 */
public class CallinMarkerRemover
{
	public static void removeCallinMarker(IMember member, IResource resource)
    {
        // we need to pass the resource, as the method might already be removed and hence would
        // not be able to give us a resource.
    	if (resource.exists())
    	{
	        try
            {
                IMarker marker;
                if (member.getElementType() == IJavaElement.METHOD) {
	                marker = getCallinMarker(member, CallinMarker.CALLIN_ID, resource);
	                if (marker != null)
	                    marker.delete();
                }
                // method or field:
                marker = getCallinMarker(member, CallinMarker.CALLOUT_ID, resource);
                if (marker != null)
                    marker.delete();
            }
	        catch (ResourceException ex) {
	        	// tree might be locked for modifications
	        	// FIXME(SH): handle this case, currently we just ignore this situation
	        }
            catch (CoreException ex)
            {
    			OTDTUIPlugin.logException("Problems removing callin marker", ex); //$NON-NLS-1$
            }
    	}
    }
    
    /**
     * Finds the marker attached to the given method.
     * Note: may return null if nothing found.
     */
    private static IMarker getCallinMarker(IMember baseElement, String markerKind, IResource resource) throws JavaModelException, CoreException
    {
        IMarker[] markers = resource.findMarkers(markerKind, true, IResource.DEPTH_INFINITE);

        String methodId = baseElement.getHandleIdentifier();
        
        for (int i = 0; i < markers.length; i++)
        {
            if (methodId.equals(markers[i].getAttribute(CallinMarker.ATTR_BASE_ELEMENT, null)))
                return markers[i];
        }
        return null;
    }
}
