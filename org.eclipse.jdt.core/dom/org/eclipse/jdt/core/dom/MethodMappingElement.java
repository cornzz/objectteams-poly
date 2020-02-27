/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
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
 * $Id: MethodMappingElement.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * NEW for OTDT
 *
 * Abstract base class of all AST nodes that represent elements for
 * method mapping like
 * "void setValue(Integer i) -> set int val" .
 *
 * MethodSpec, e.g. "void setValue(Integer i)"
 * FieldAccessSpec, e.g. "set int val"
 *
 * @author jsv
 * @version $Id: MethodMappingElement.java 23416 2010-02-03 19:59:31Z stephan $
 */
public abstract class MethodMappingElement extends ASTNode {

	/**
	 * Creates a new AST node for an expression owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	MethodMappingElement(AST ast) {
		super(ast);
	}

	/**
	 * The signature flag.
	 * True if MethodMappingElement have a signature
	 */
	private boolean hasSignature = false;

	/**
	 * The element name; lazily initialized; defaults to an unspecified,
	 * legal Java identifier.
	 */
	private SimpleName _name = null;

	/**
	 * Returns structural property descriptor for the "signature" property
	 * of this node.
	 *
	 * @return the property descriptor
	 */
	public abstract SimplePropertyDescriptor signatureProperty();

	/**
	 * Returns structural property descriptor for the "name" property
	 * of this node.
	 *
	 * @return the property descriptor
	 */
	abstract ChildPropertyDescriptor internalNameProperty();

	/**
	 * Creates and returns a structural property descriptor for the
	 * "signature" property declared on the given concrete node type.
	 *
	 * @return the property descriptor
	 */
	static final SimplePropertyDescriptor internalSignaturePropertyFactory(Class<?> nodeClass) {
		return new SimplePropertyDescriptor(nodeClass, "signature", boolean.class, MANDATORY); //$NON-NLS-1$
	}

	/**
	 * Creates and returns a structural property descriptor for the
	 * "name" property declared on the given concrete node type.
	 *
	 * @return the property descriptor
	 */
	static final ChildPropertyDescriptor internalNamePropertyFactory(Class<?> nodeClass) {
		return new ChildPropertyDescriptor(nodeClass, "name", SimpleName.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
	}

	/**
	 * Returns the signature flag
	 */
	public boolean hasSignature()
	{
		return this.hasSignature;
	}

	/**
	 * Sets the signature flag.
	 */
	public void setSignatureFlag(boolean hasSignature)
	{
		SimplePropertyDescriptor p = signatureProperty();
		preValueChange(p);
		this.hasSignature = hasSignature;
		postValueChange(p);
	}

	/**
	 * Returns the name of the MethodMappingElement
	 *
	 * @return the method name node
	 */
	public SimpleName getName()
	{
		if (this._name == null)
		{
			// lazy init must be thread-safe for readers
			synchronized (this)
			{
				if (this._name == null)
				{
					preLazyInit();
					this._name = new SimpleName(this.ast);
					postLazyInit(this._name, internalNameProperty());
				}
			}
		}
		return this._name;
	}

	/**
	 * Sets the name of the MethodMappingElement
	 *
	 * @param newName the new element name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public void setName(SimpleName newName)
	{
		if (newName == null)
		{
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this._name;
		preReplaceChild(oldChild, newName, internalNameProperty());
		this._name = newName;
		postReplaceChild(oldChild, newName, internalNameProperty());
	}
}
