/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

/*
 * Completion node build by the parser in any case it was intending to
 * reduce an package statement containing the cursor location.
 * e.g.
 *
 *  package java.io[cursor];
 *	class X {
 *    void foo() {
 *    }
 *  }
 *
 *	---> <CompleteOnPackage:java.io>
 *		 class X {
 *         void foo() {
 *         }
 *       }
 *
 * The source range is always of length 0.
 * The arguments of the allocation expression are all the arguments defined
 * before the cursor.
 */

import org.eclipse.jdt.internal.compiler.ast.*;

public class CompletionOnPackageReference extends ImportReference {
//{ObjectTeams: modifiers added:
/*orig:
public CompletionOnPackageReference(char[][] tokens , long[] positions) {
	super(tokens, positions, false, ClassFileConstants.AccDefault);
 :giro*/
public CompletionOnPackageReference(char[][] tokens , long[] positions, int modifiers) {
	super(tokens, positions, false, modifiers);
// SH}
}
@Override
public StringBuffer print(int indent, StringBuffer output, boolean withOnDemand) {

	printIndent(indent, output).append("<CompleteOnPackage:"); //$NON-NLS-1$
	for (int i = 0; i < this.tokens.length; i++) {
		if (i > 0) output.append('.');
		output.append(this.tokens[i]);
	}
	return output.append('>');
}
}
