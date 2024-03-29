/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

/**
 * IMPORTANT NOTE: These constants are dedicated to the internal Scanner implementation.
 * It is mirrored in org.eclipse.jdt.core.compiler public package where it is API.
 * The mirror implementation is using the backward compatible ITerminalSymbols constant
 * definitions (stable with 2.0), whereas the internal implementation uses TerminalTokens
 * which constant values reflect the latest parser generation state.
 */
/**
 * Maps each terminal symbol in the java-grammar into a unique integer.
 * This integer is used to represent the terminal when computing a parsing action.
 *
 * Disclaimer : These constant values are generated automatically using a Java
 * grammar, therefore their actual values are subject to change if new keywords
 * were added to the language (for instance, 'assert' is a keyword in 1.4).
 */
public interface TerminalTokens {

	// special tokens not part of grammar - not autogenerated
	int
		TokenNameNotAToken = 0,
	    TokenNameWHITESPACE = 1000,
		TokenNameCOMMENT_LINE = 1001,
		TokenNameCOMMENT_BLOCK = 1002,
		TokenNameCOMMENT_JAVADOC = 1003;

	// BEGIN_AUTOGENERATED_REGION
	int TokenNameIdentifier = 14,
							TokenNameabstract = 55,
							TokenNameassert = 87,
							TokenNameboolean = 113,
							TokenNamebreak = 88,
							TokenNamebyte = 114,
							TokenNamecase = 115,
							TokenNamecatch = 116,
							TokenNamechar = 117,
							TokenNameclass = 72,
							TokenNamecontinue = 89,
							TokenNameconst = 148,
							TokenNamedefault = 81,
							TokenNamedo = 90,
							TokenNamedouble = 118,
							TokenNameelse = 131,
							TokenNameenum = 77,
							TokenNameextends = 98,
							TokenNamefalse = 41,
							TokenNamefinal = 56,
							TokenNamefinally = 127,
							TokenNamefloat = 119,
							TokenNamefor = 91,
							TokenNamegoto = 149,
							TokenNameif = 92,
							TokenNameimplements = 145,
							TokenNameimport = 120,
							TokenNameinstanceof = 18,
							TokenNameint = 121,
							TokenNameinterface = 76,
							TokenNamelong = 122,
							TokenNamenative = 57,
							TokenNamenew = 37,
							TokenNamenull = 42,
							TokenNamepackage = 99,
							TokenNameprivate = 58,
							TokenNameprotected = 59,
							TokenNamepublic = 60,
							TokenNamereturn = 93,
							TokenNameshort = 123,
							TokenNamestatic = 43,
							TokenNamestrictfp = 61,
							TokenNamesuper = 35,
							TokenNameswitch = 62,
							TokenNamesynchronized = 53,
							TokenNamethis = 36,
							TokenNamethrow = 84,
							TokenNamethrows = 124,
							TokenNametransient = 63,
							TokenNametrue = 44,
							TokenNametry = 94,
							TokenNamevoid = 125,
							TokenNamevolatile = 64,
							TokenNamewhile = 85,
							TokenNamemodule = 128,
							TokenNameopen = 129,
							TokenNamerequires = 132,
							TokenNametransitive = 138,
							TokenNameexports = 133,
							TokenNameopens = 134,
							TokenNameto = 146,
							TokenNameuses = 135,
							TokenNameprovides = 136,
							TokenNamewith = 100,
							TokenNameas = 139,
							TokenNamebase = 32,
							TokenNamecallin = 65,
							TokenNameplayedBy = 147,
							TokenNameprecedence = 112,
							TokenNameteam = 54,
							TokenNametsuper = 38,
							TokenNamewhen = 97,
							TokenNamewithin = 95,
							TokenNamereplace = 140,
							TokenNameafter = 137,
							TokenNamebefore = 141,
							TokenNameget = 142,
							TokenNameset = 143,
							TokenNameIntegerLiteral = 45,
							TokenNameLongLiteral = 46,
							TokenNameFloatingPointLiteral = 47,
							TokenNameDoubleLiteral = 48,
							TokenNameCharacterLiteral = 49,
							TokenNameStringLiteral = 50,
							TokenNameTextBlock = 51,
							TokenNamePLUS_PLUS = 2,
							TokenNameMINUS_MINUS = 3,
							TokenNameEQUAL_EQUAL = 21,
							TokenNameLESS_EQUAL = 15,
							TokenNameGREATER_EQUAL = 16,
							TokenNameNOT_EQUAL = 22,
							TokenNameLEFT_SHIFT = 19,
							TokenNameRIGHT_SHIFT = 13,
							TokenNameUNSIGNED_RIGHT_SHIFT = 17,
							TokenNamePLUS_EQUAL = 101,
							TokenNameMINUS_EQUAL = 102,
							TokenNameMULTIPLY_EQUAL = 103,
							TokenNameDIVIDE_EQUAL = 104,
							TokenNameAND_EQUAL = 105,
							TokenNameOR_EQUAL = 106,
							TokenNameXOR_EQUAL = 107,
							TokenNameREMAINDER_EQUAL = 108,
							TokenNameLEFT_SHIFT_EQUAL = 109,
							TokenNameRIGHT_SHIFT_EQUAL = 110,
							TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 111,
							TokenNameOR_OR = 31,
							TokenNameAND_AND = 30,
							TokenNamePLUS = 4,
							TokenNameMINUS = 5,
							TokenNameNOT = 68,
							TokenNameREMAINDER = 10,
							TokenNameXOR = 27,
							TokenNameAND = 23,
							TokenNameMULTIPLY = 9,
							TokenNameOR = 28,
							TokenNameTWIDDLE = 69,
							TokenNameDIVIDE = 11,
							TokenNameGREATER = 12,
							TokenNameLESS = 8,
							TokenNameLPAREN = 20,
							TokenNameRPAREN = 24,
							TokenNameLBRACE = 40,
							TokenNameRBRACE = 34,
							TokenNameLBRACKET = 6,
							TokenNameRBRACKET = 71,
							TokenNameSEMICOLON = 25,
							TokenNameQUESTION = 29,
							TokenNameCOLON = 66,
							TokenNameCOMMA = 33,
							TokenNameDOT = 1,
							TokenNameEQUAL = 82,
							TokenNameAT = 39,
							TokenNameELLIPSIS = 130,
							TokenNameARROW = 79,
							TokenNameCOLON_COLON = 7,
							TokenNameBeginLambda = 52,
							TokenNameBeginIntersectionCast = 70,
							TokenNameBeginTypeArguments = 83,
							TokenNameElidedSemicolonAndRightBrace = 73,
							TokenNameAT308 = 26,
							TokenNameAT308DOTDOTDOT = 144,
							TokenNameBeginCaseExpr = 74,
							TokenNameRestrictedIdentifierYield = 86,
							TokenNameRestrictedIdentifierrecord = 78,
							TokenNameATOT = 126,
							TokenNameBINDIN = 80,
							TokenNameCALLOUT_OVERRIDE = 96,
							TokenNameSYNTHBINDOUT = 75,
							TokenNameEOF = 67,
							TokenNameERROR = 150;

	// This alias is statically inserted by GenerateParserScript.java:
	int TokenNameBINDOUT = TokenNameARROW;
}
