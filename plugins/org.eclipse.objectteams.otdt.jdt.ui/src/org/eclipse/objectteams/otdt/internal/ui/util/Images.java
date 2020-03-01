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
package org.eclipse.objectteams.otdt.internal.ui.util;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.objectteams.otdt.ui.ImageManager;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("restriction")
public class Images {

	public static Image getImage(String imgKey) {
		return JavaPlugin.getImageDescriptorRegistry().get(
				ImageManager.getSharedInstance().getDescriptor(imgKey));
	}

	public static Image decorateImage(Image baseImage, String overlayKey, int position) {
		ImageDescriptor[] descs = new ImageDescriptor[5];
		descs[position] = ImageManager.getSharedInstance().getDescriptor(overlayKey);
		DecorationOverlayIcon icon = new DecorationOverlayIcon(baseImage, descs);
		return JavaPlugin.getImageDescriptorRegistry().get(icon);
	}
}
