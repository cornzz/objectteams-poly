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
 * $Id: PlainAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;

/**
 * Attribute without a value.
 * Uses:
 *    CallsBaseConstructor
 *
 * @author stephan
 * @version $Id: PlainAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class PlainAttribute extends AbstractAttribute {

    /**
     * @param name
     */
    public PlainAttribute(char[] name) {
        super(name);
    }

    @Override
	int size() {
    	return 6;
    }

    @Override
	public void write (ClassFile classFile) {
        super.write(classFile);
        if (this._contentsOffset + 6 > this._contents.length) {
        	this._contents = classFile.getResizedContents(8);
        }
        // write the name
        int attributeNameIndex = this._constantPool.literalIndex(this._name);
        this._contents[this._contentsOffset++] = (byte) (attributeNameIndex >> 8);
        this._contents[this._contentsOffset++] = (byte) attributeNameIndex;
        // The length of a plain attribute is 0 (fixed-length).
        this._contents[this._contentsOffset++] = 0;
        this._contents[this._contentsOffset++] = 0;
        this._contents[this._contentsOffset++] = 0;
        this._contents[this._contentsOffset++] = 0;
        writeBack(classFile);
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
     */
    @Override
	public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) { /* noop */ }

}
