/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {
    public final static int

      ERROR_SYMBOL      = 150,
      MAX_NAME_LENGTH   = 41,
      NUM_STATES        = 1469,

      NT_OFFSET         = 150,
      SCOPE_UBOUND      = 402,
      SCOPE_SIZE        = 403,
      LA_STATE_OFFSET   = 20347,
      MAX_LA            = 1,
      NUM_RULES         = 1088,
      NUM_TERMINALS     = 150,
      NUM_NON_TERMINALS = 496,
      NUM_SYMBOLS       = 646,
      START_STATE       = 1190,
      EOFT_SYMBOL       = 67,
      EOLT_SYMBOL       = 67,
      ACCEPT_ACTION     = 20346,
      ERROR_ACTION      = 20347;
}
