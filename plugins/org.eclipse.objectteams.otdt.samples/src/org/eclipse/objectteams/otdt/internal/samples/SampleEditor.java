/*******************************************************************************
 * Copyright (c) 2016 GK Software AG, and others.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.internal.samples;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.part.EditorPart;

public class SampleEditor extends EditorPart {

	private FormToolkit toolkit;
	private ScrolledForm form;
	private FormText descText;
	private FormText instText;
	private InputFileListener inputFileListener;
	private PDEImages pdeImages;

	class InputFileListener implements IResourceChangeListener, IResourceDeltaVisitor {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				IResourceDelta delta = event.getDelta();
				try {
					delta.accept(this);
				} catch (CoreException e) {
					OTSamplesPlugin.logException(e, null, null);
				}
			}
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				if (file.equals(((IFileEditorInput) getEditorInput()).getFile())) {
					if (delta.getKind() == IResourceDelta.REMOVED || delta.getKind() == IResourceDelta.REPLACED)
						close();
					return false;
				}
			}
			return true;
		}
	}

	public SampleEditor() {
		pdeImages = PDEImages.connect(this);
	}

	/**
	 * @see EditorPart#createPartControl
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		Properties properties = loadContent();
		form.setText(properties.getProperty("name")); //$NON-NLS-1$
		TableWrapLayout layout = new TableWrapLayout();
		layout.verticalSpacing = 10;
		layout.topMargin = 10;
		layout.bottomMargin = 10;
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		form.getBody().setLayout(layout);

		final String launcher = properties.getProperty("launcher"); //$NON-NLS-1$
		final String launchTarget = properties.getProperty("launchTarget"); //$NON-NLS-1$

		descText = toolkit.createFormText(form.getBody(), true);
		descText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		String desc = properties.getProperty("description"); //$NON-NLS-1$
		String content = NLS.bind(Messages.SampleEditor_desc, (desc != null ? desc : "")); //$NON-NLS-1$
		descText.setText(content, true, false);
		final String helpURL = properties.getProperty("helpHref"); //$NON-NLS-1$
		if (helpURL != null) {
			Hyperlink moreLink = toolkit.createHyperlink(form.getBody(), "Read More", SWT.NULL); //$NON-NLS-1$
			moreLink.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpURL);
				}
			});
		}
		instText = toolkit.createFormText(form.getBody(), true);
		instText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		StringBuffer buf = new StringBuffer();
		buf.append(Messages.SampleEditor_content);
		instText.setText(buf.toString(), true, false);
		final SampleRunner runner = new SampleRunner(properties.getProperty("id")); //$NON-NLS-1$
		instText.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				Object href = e.getHref();
				if (href.equals("help")) { //$NON-NLS-1$
					PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpURL);
				} else if (href.equals("run")) { //$NON-NLS-1$
					runner.doRun(launcher, launchTarget, false);
				} else if (href.equals("debug")) { //$NON-NLS-1$
					runner.doRun(launcher, launchTarget, true);
				}
			}
		});
		instText.setImage("run", pdeImages.get(PDEImages.DESC_RUN_EXC)); //$NON-NLS-1$
		instText.setImage("debug", pdeImages.get(PDEImages.DESC_DEBUG_EXC)); //$NON-NLS-1$
		instText.setImage("help", PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK)); //$NON-NLS-1$
	}

	private Properties loadContent() {
		IStorageEditorInput input = (IStorageEditorInput) getEditorInput();
		Properties properties = new Properties();
		try {
			IStorage storage = input.getStorage();
			InputStream is = storage.getContents();
			properties.load(is);
			is.close();
		} catch (IOException e) {
			OTSamplesPlugin.logException(e, null, null);
		} catch (CoreException e) {
			OTSamplesPlugin.logException(e, null, null);
		}
		return properties;
	}

	@Override
	public void dispose() {
		if (inputFileListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(inputFileListener);
			inputFileListener = null;
		}
		toolkit.dispose();
		pdeImages.disconnect(this);
		super.dispose();
	}

	/**
	 * @see EditorPart#setFocus
	 */
	@Override
	public void setFocus() {
		form.setFocus();
	}

	/**
	 * @see EditorPart#doSave
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	/**
	 * @see EditorPart#doSaveAs
	 */
	@Override
	public void doSaveAs() {
	}

	/**
	 * @see EditorPart#isDirty
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/**
	 * @see EditorPart#isSaveAsAllowed
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * @see EditorPart#init
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		inputFileListener = new InputFileListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(inputFileListener);
	}

	public void close() {
		Display display = getSite().getShell().getDisplay();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (toolkit != null) {
					getSite().getPage().closeEditor(SampleEditor.this, false);
				}
			}
		});
	}
}
