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
package org.eclipse.objectteams.otredyn.bytecode;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;

import org.eclipse.objectteams.otredyn.transformer.jplis.otreAgent;
import org.eclipse.objectteams.runtime.DebugHooks;

/**
 * This implementation of {@link IRedefineStrategy} uses the
 * JPLIS agent {@link otreAgent} to redefine classes.
 * @author Oliver Frank
 */
public class OtreRedefineStrategy implements IRedefineStrategy {

	public void redefine(Class<?> clazz, byte[] bytecode) throws ClassNotFoundException, UnmodifiableClassException {
		ClassDefinition arr_cd[] = { new ClassDefinition(clazz, bytecode) };
		try {
			otreAgent.getInstrumentation().redefineClasses(arr_cd);
			DebugHooks.afterRedefineClasses(clazz.getName());
		} catch (ClassFormatError cfe) {
			// error output during redefinition tends to swallow the stack, print it now:
			System.err.println("OTDRE: Error redifining "+clazz.getName());
			cfe.printStackTrace();
			throw cfe;
		}
	}

}
