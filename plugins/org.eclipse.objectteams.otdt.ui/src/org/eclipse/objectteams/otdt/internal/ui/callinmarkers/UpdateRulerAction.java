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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.ui.OTDTUIPluginConstants;
import org.eclipse.objectteams.otdt.ui.IUpdateRulerActionExtender;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action that is executed when the user right clicks on a binding marker at
 * the vertical ruler and chooses a mapping (bound role class or callin mapping)
 * from the ObjectTeams submenu.
 *
 * @author brcan
 * @version $Id: UpdateRulerAction.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class UpdateRulerAction extends AbstractRulerActionDelegate
{
	public final static String OT_PLAYEDBY_MENU_LABEL = OTDTUIPlugin.getResourceString("CallinMarker.menu_playedby_title"); //$NON-NLS-1$
	public final static String OT_CALLIN_MENU_LABEL = OTDTUIPlugin.getResourceString("CallinMarker.menu_callin_title"); //$NON-NLS-1$
	public final static String OT_CALLOUT_MENU_LABEL = OTDTUIPlugin.getResourceString("CallinMarker.menu_callout_title"); //$NON-NLS-1$
	
	private IEditorPart        _editor = null;
	private IVerticalRulerInfo _rulerInfo = null;
	
	private List<IUpdateRulerActionExtender> extenders = null;

	public UpdateRulerAction()
	{
	}

	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor)
	{
		_editor = targetEditor;

		super.setActiveEditor(callerAction, targetEditor);
	}
	
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo)
	{
		_rulerInfo = rulerInfo;
    	
		return null;
	}
	
	public void menuAboutToShow(IMenuManager contextMenu)
	{
		IDocument    document    = null;
		int          clickedLine = _rulerInfo.getLineOfLastMouseButtonActivity();
		if (this._editor instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) this._editor;
			document= textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
		}
		try
		{
			IMarker[] markers = findCallinMarkers();

			if (markers != null && markers.length != 0)
			{				
				for (int idx = 0; idx < markers.length; idx++)
				{
					IMarker curMarker = markers[idx];

					int line = curMarker.getAttribute(IMarker.LINE_NUMBER, -1);
					if (line != -1)
					{
						if (line-1  == clickedLine) // IMarker.LINE_NUMBER is 1-based, others are 0-based
							insertTeamMenus(contextMenu, curMarker);
					} else if (document != null) {
						// markers in ClassFileEditor have no line number, must go via position:
						int start = curMarker.getAttribute(IMarker.CHAR_START, -1);
						try {
							if (clickedLine == document.getLineOfOffset(start))
								insertTeamMenus(contextMenu, curMarker);
						}
						catch (BadLocationException e) { /* nop */ }
					} 
				}
			}
		}
		catch (CoreException ex)
		{
			OTDTUIPlugin.logException("Problems extending ruler context menu", ex); //$NON-NLS-1$
		}
		// load and notify extenders:
		if (this.extenders == null)
			loadExtenders();
		for (IUpdateRulerActionExtender extender : this.extenders)
			extender.menuAboutToShow(contextMenu, document, this._editor, clickedLine);
	}

	private IMarker[] findCallinMarkers() throws CoreException
	{
		
		final IEditorInput editorInput = _editor.getEditorInput();
		if (editorInput instanceof IFileEditorInput) 
		{
			IFileEditorInput fileEditorInput = (IFileEditorInput)editorInput;
			IFile            file            = fileEditorInput.getFile();			
	
			IMarker[] result = CallinMarker.getAllBindingMarkers(file);
			return result;
		} 
		else if (editorInput instanceof IClassFileEditorInput) 
		{			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IMarker[] allMarkers = CallinMarker.getAllBindingMarkers(root);
			
			// now we have all CallinMarkers for all class files in the workspace, need to filter now: 
			IClassFile classFile = ((IClassFileEditorInput) editorInput).getClassFile();
			List<IMarker> filteredMarkers = new ArrayList<IMarker>(13);
			for (IMarker marker : allMarkers)
				if (JavaCore.isReferencedBy(classFile, marker))
					filteredMarkers.add(marker);
			return filteredMarkers.toArray(new IMarker[filteredMarkers.size()]);
		}
		return null;
	}

	/** Get the context menu of kind 'markerKind', creating it if ondemand. */
    private IMenuManager getObjectTeamsMenu(IMenuManager contextMenu, String markerKind)
    {
    	String label;
    	if (CallinMarker.CALLIN_ID.equals(markerKind))
    		label = OT_CALLIN_MENU_LABEL;
    	else if (CallinMarker.CALLOUT_ID.equals(markerKind))
    		label = OT_CALLOUT_MENU_LABEL;
    	else
    		label = OT_PLAYEDBY_MENU_LABEL;
    	
    	IMenuManager subMenu = getSubMenu(contextMenu, label);
    	if (subMenu != null)
    		return subMenu;
    	
		MenuManager otMenu = new MenuManager(label, markerKind); // id cannot be null, re-use the markerKind
		if (contextMenu.isEmpty())
		    contextMenu.add(otMenu);
		else // insert on top
		    contextMenu.insertBefore(contextMenu.getItems()[0].getId(), otMenu);
		return otMenu;
    }

    private IAction createOpenEditorAction(String label, final IJavaElement target)
    {
		Action result = new Action(label)
		{
			public void run()
			{
				try
				{
					IEditorPart part = EditorUtility.openInEditor(target);
					if (target.exists()) // also initializes source positions if necessary
						EditorUtility.revealInEditor(part, target);
				}
				catch (PartInitException ex)
				{
					OTDTUIPlugin.logException("Problems initializing editor", ex); //$NON-NLS-1$
				}
			}
		};
    	
        return result;
    }

	List<IMember> getMappings (IMarker marker) throws CoreException 
	{
    	Object attr = marker.getAttribute(CallinMarker.ATTR_ROLE_ELEMENTS);
    	if (attr == null || !(attr instanceof String))
    		return null;
    	String str = (String)attr;
    	List<IMember> result = new ArrayList<IMember>();
    	int start = 0;
    	int pos;
    	while ((pos = str.indexOf('\n', start)) != -1) {
    		result.add((IMember)JavaCore.create(str.substring(start, pos)));
    		start = pos+1;
    	}
    	return result;
    }
    
    private void insertTeamMenus(IMenuManager contextMenu, IMarker marker) throws CoreException
    {
    	List<IMember> mappings = getMappings(marker);
    	if (mappings == null) return;
    	
    	IMenuManager otMenu = getObjectTeamsMenu(contextMenu, marker.getType());
    	
        for (IMember curMapping : mappings) 
        {
        	IType type = (IType)(curMapping.getAncestor(IJavaElement.TYPE));
        	IOTType otType = OTModelManager.getOTElement(type); // FIXME(SH): doesn't find role files??? (try StubUtility2)
        	if (otType == null || !otType.isRole())
        		continue;
			
        	IOTType teamType = ((IRoleType) otType).getTeam();
 			
			IMenuManager curTeamMenu = null;
					 			
 			if (!isSubMenuContained(otMenu, teamType.getElementName()))
 			{
	            curTeamMenu = new MenuManager(teamType.getElementName());
				otMenu.add(curTeamMenu);
 			}
 			else
 			{
 				curTeamMenu = getSubMenu(otMenu, teamType.getElementName());
 			}
			String actLabel = getMappingLabel(type, curMapping);
			curTeamMenu.add(createOpenEditorAction(actLabel, curMapping));
        }
    }

    private String getMappingLabel(IType type, IMember mapping)
    {
    	if (type.equals(mapping)) return type.getElementName();
    	
        return type.getElementName() + ": " + mapping.getElementName(); //$NON-NLS-1$
    }

    private IMenuManager getSubMenu(IMenuManager otMenu, String subMenuName)
    {
    	if (otMenu == null)
    		return null;

    	IContributionItem[] items = otMenu.getItems();
    	
    	for (int idx = 0; idx < items.length; idx++)
        {
            if (items[idx] instanceof IMenuManager)
            {
            	MenuManager cur = (MenuManager)items[idx];
            	if (cur.getMenuText().equals(subMenuName))
            	{
            		return cur; 
            	}
            }
        }
        
        return null;
    }

    private boolean isSubMenuContained(IMenuManager menu, String subMenuName)
    {
        return getSubMenu(menu, subMenuName) != null;
    }

	private void loadExtenders() {
		this.extenders = new ArrayList<IUpdateRulerActionExtender>();
		IConfigurationElement[] configs = RegistryFactory.getRegistry().getConfigurationElementsFor(
				OTDTUIPluginConstants.UIPLUGIN_ID, OTDTUIPluginConstants.UPDATE_RULER_ACTION_EXTENDER_ID);
		for (IConfigurationElement config : configs) {
			try {
				if (this._editor.getClass().getName().equals(config.getAttribute(OTDTUIPluginConstants.UPDATE_RULER_ACTION_EXTENDER_EDITORCLASS)))
					this.extenders.add((IUpdateRulerActionExtender) config.createExecutableExtension(OTDTUIPluginConstants.UPDATE_RULER_ACTION_EXTENDER_CLASS));
			} catch (CoreException e) {
				OTDTUIPlugin.log(e);
			}
		}		
	}
}
