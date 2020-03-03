/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2005-2009 Berlin Institute of Technology, Germany.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;



/**
 * This interface is used to abstract from the BCEL external transformations.
 *  
 * @author Christine Hundt
 * @author Juergen Widiker
 * @author Stephan Herrmann
 */
public interface ClassEnhancer {

	/**
	 * Adds the interface 'interfaceName' to the implements clause of class 'cg'.
	 */
	public void addImplements(String interfaceName, ClassGen cg);
	
	/**
	 * Adds the method 'm' to the class represented by 'cg'.
	 * @param m		the method to be added
	 * @param cg	the ClassGen of the appropriate class
	 */
	void addMethod(Method m, ClassGen cg);

	/**
	 * Adds method 'm' to the class 'cg' or, if a method with the
	 * same name and signature already exists, replace that method.
	 * @param method
	 * @param cg
	 */
	void addOrReplaceMethod(Method method, ClassGen cg);
	
	/**
	 * Adds the field 'f' to the class represented by 'cg'.
	 * @param f
	 * @param cg
	 */
	void addField(Field f, ClassGen cg);

	/**
	 * Loads the class named 'className'.
	 * @param className	the name of the class to be loaded
	 * @param client the transformer on behalf of which we are called, can be used to call checkReadClassAttributes.
	 */
	void loadClass(String className, ObjectTeamsTransformation client);
	
	/**
	 * Decapsulation of the method 'm'. This means that the access modifier of this method is set to 'public'.
	 * @param m							the name of the method to be decapsulated				
	 * @param className			the name of the belonging class
	 * @param packageName  the name of the belonging package
	 * @param cpg						the ConstantPoolGen of the class	
	 */
	void decapsulateMethod(Method m, ClassGen cg, String packageName, ConstantPoolGen cpg);


}
