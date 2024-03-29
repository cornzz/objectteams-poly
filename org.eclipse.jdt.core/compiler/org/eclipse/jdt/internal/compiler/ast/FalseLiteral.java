/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class FalseLiteral extends MagicLiteral {

	static final char[] source = {'f', 'a', 'l', 's', 'e'};

public FalseLiteral(int s , int e) {
	super(s,e);
}
@Override
public void computeConstant() {
	this.constant = BooleanConstant.fromValue(false);
}
/**
 * Code generation for false literal
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */
@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (valueRequired) {
		codeStream.generateConstant(this.constant, this.implicitConversion);
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
@Override
public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {

	// falseLabel being not nil means that we will not fall through into the FALSE case

	int pc = codeStream.position;
	if (valueRequired) {
		if (falseLabel != null) {
			// implicit falling through the TRUE case
			if (trueLabel == null) {
				codeStream.goto_(falseLabel);
			}
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
@Override
public TypeBinding literalType(BlockScope scope) {
	return TypeBinding.BOOLEAN;
}
/**
 *
 */
@Override
public char[] source() {
	return source;
}
@Override
public void traverse(ASTVisitor visitor, BlockScope scope) {
//{ObjectTeams: hide synthetic nodes from visitors (e.g., inserted into a base call)
	if (this.isGenerated) return;
// SH}
	visitor.visit(this, scope);
	visitor.endVisit(this, scope);
}
}
