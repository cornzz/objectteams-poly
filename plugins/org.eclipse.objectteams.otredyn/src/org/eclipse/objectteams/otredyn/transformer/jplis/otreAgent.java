/**********************************************************************
 * This file is part of "Object Teams Dynamic Runtime Environment"
 * 
 * Copyright 2002, 2010 Berlin Institute of Technology, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 *		Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otredyn.transformer.jplis;

import java.lang.instrument.Instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.tree.InsnNode;


/**
 * @author  Christine Hundt
 */
public class otreAgent {
	private static Instrumentation instCopy;

	
	private static ObjectTeamsTransformer otTransformer;

	public static void premain(String options, Instrumentation inst) {
		instCopy = inst;
		checkASM();
		otTransformer = new ObjectTeamsTransformer();
		instCopy.addTransformer(otTransformer);
	}
	
	private static void checkASM() {
		ClassVisitor.class.getName(); // asm
		AdviceAdapter.class.getName(); // asm.commons
		InsnNode.class.getName(); // asm.tree
	}

	public static Instrumentation getInstrumentation() {
		return instCopy;
	}
}
