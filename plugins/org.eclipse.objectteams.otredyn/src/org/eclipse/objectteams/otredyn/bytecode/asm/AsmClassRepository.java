/**********************************************************************
 * This file is part of "Object Teams Dynamic Runtime Environment"
 * 
 * Copyright 2009, 2012 Oliver Frank and others.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 *		Oliver Frank - Initial API and implementation
 *		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otredyn.bytecode.asm;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.objectteams.otredyn.bytecode.AbstractTeam;
import org.eclipse.objectteams.otredyn.bytecode.ClassRepository;
import org.eclipse.objectteams.otredyn.bytecode.IBytecodeProvider;


/**
 * Creates Instances of {@link AsmWritableBoundClass} as {@link AbstractTeam}
 * @author Oliver Frank
 */
public class AsmClassRepository extends ClassRepository {

	
	@Override
	protected AbstractTeam createClass(@NonNull String name, String id, IBytecodeProvider bytecodeProvider, ClassLoader loader) {
		return new AsmWritableBoundClass(name, id, bytecodeProvider, loader);
	}
}
