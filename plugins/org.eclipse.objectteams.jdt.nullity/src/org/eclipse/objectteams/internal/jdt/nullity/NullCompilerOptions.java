/*******************************************************************************
 * Copyright (c) 2011 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation 
 *******************************************************************************/
package org.eclipse.objectteams.internal.jdt.nullity;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.IrritantSet;

/** Additional constants for {@link CompilerOptions}. */
@SuppressWarnings("restriction")
public interface NullCompilerOptions {
	public static final String OPTION_ReportNullContractViolation = "org.eclipse.jdt.core.compiler.problem.nullContractViolation";  //$NON-NLS-1$
	public static final String OPTION_ReportPotentialNullContractViolation = "org.eclipse.jdt.core.compiler.problem.potentialNullContractViolation";  //$NON-NLS-1$
	public static final String OPTION_ReportNullContractInsufficientInfo = "org.eclipse.jdt.core.compiler.problem.nullContractInsufficientInfo";  //$NON-NLS-1$

	public static final String OPTION_NullableAnnotationName = "org.eclipse.jdt.core.compiler.annotation.nullable"; //$NON-NLS-1$
	public static final String OPTION_NonNullAnnotationName = "org.eclipse.jdt.core.compiler.annotation.nonnull"; //$NON-NLS-1$
	public static final String OPTION_EmulateNullAnnotationTypes = "org.eclipse.jdt.core.compiler.annotation.emulate"; //$NON-NLS-1$
	
	public static final int NullContractViolation = IrritantSet.GROUP2 | ASTNode.Bit7;
	public static final int PotentialNullContractViolation = IrritantSet.GROUP2 | ASTNode.Bit8;
	public static final int NullContractInsufficientInfo = IrritantSet.GROUP2 | ASTNode.Bit9;
}