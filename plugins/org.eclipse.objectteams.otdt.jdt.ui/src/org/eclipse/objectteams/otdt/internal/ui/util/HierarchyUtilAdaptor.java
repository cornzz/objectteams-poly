/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: HierarchyUtilAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.util;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;

import base org.eclipse.jdt.internal.ui.util.OpenTypeHierarchyUtil;
import base org.eclipse.jdt.ui.actions.OpenTypeHierarchyAction;

/**
 * This team adapts different utility classes relating to hierarchies.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public team class HierarchyUtilAdaptor 
{
	/** Also consider method mappings as candidates for a hierarchy. */ 
	protected class OpenTypeHierarchyUtil playedBy OpenTypeHierarchyUtil 
	{		
		@SuppressWarnings("basecall")
		static callin IJavaElement[] getCandidates(Object input) {
			if (input instanceof IJavaElement) {
				IJavaElement elem= (IJavaElement) input;
				switch (elem.getElementType()) {
					case IOTJavaElement.CALLIN_MAPPING:
					case IOTJavaElement.CALLOUT_MAPPING:
					case IOTJavaElement.CALLOUT_TO_FIELD_MAPPING:
						return new IJavaElement[] { elem };
				} 
			}
			return base.getCandidates(input);
		}
		getCandidates <- replace getCandidates;
	}
	
	/** Enable the open type hiearchy action for method mappings. */
	protected class OpenTypeHierarchyAction playedBy OpenTypeHierarchyAction {

		@SuppressWarnings("decapsulation")
		boolean isEnabled(IStructuredSelection selection) <- replace boolean isEnabled(IStructuredSelection selection);

		callin boolean isEnabled(IStructuredSelection selection) {
			if (base.isEnabled(selection))
				return true;
			
			if (selection.size() != 1)
				return false;
			Object input= selection.getFirstElement();
			if (!(input instanceof IOTJavaElement))
				return false;
			switch (((IOTJavaElement)input).getElementType()) {
				case IOTJavaElement.CALLIN_MAPPING:
				case IOTJavaElement.CALLOUT_MAPPING:
				case IOTJavaElement.CALLOUT_TO_FIELD_MAPPING:
					return true;
				default:
					return false;
			}
		}

		@SuppressWarnings({ "rawtypes", "decapsulation" })
		void compileCandidates(List resultlist, List elements) <- before IStatus compileCandidates(List resultlist, List elements);

		@SuppressWarnings({ "rawtypes", "unchecked" }) // parameter are raw lists
		static void compileCandidates(List resultlist, List elements) {
			for (Iterator iter= elements.iterator(); iter.hasNext();) {
				IJavaElement elem= (IJavaElement)iter.next();
				switch (elem.getElementType()) {
					case IOTJavaElement.CALLIN_MAPPING:
					case IOTJavaElement.CALLOUT_MAPPING:
					case IOTJavaElement.CALLOUT_TO_FIELD_MAPPING:
						resultlist.add(elem);
				}
			}
		}		
	}

}
