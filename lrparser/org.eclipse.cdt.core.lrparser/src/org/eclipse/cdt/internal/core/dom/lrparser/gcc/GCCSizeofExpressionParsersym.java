/*******************************************************************************
* Copyright (c) 2006, 2008 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*********************************************************************************/

// This file was generated by LPG

package org.eclipse.cdt.internal.core.dom.lrparser.gcc;

public interface GCCSizeofExpressionParsersym {
    public final static int
      TK_auto = 30,
      TK_break = 86,
      TK_case = 87,
      TK_char = 39,
      TK_const = 11,
      TK_continue = 88,
      TK_default = 89,
      TK_do = 90,
      TK_double = 40,
      TK_else = 91,
      TK_enum = 52,
      TK_extern = 31,
      TK_float = 41,
      TK_for = 92,
      TK_goto = 93,
      TK_if = 94,
      TK_inline = 32,
      TK_int = 42,
      TK_long = 43,
      TK_register = 33,
      TK_restrict = 13,
      TK_return = 95,
      TK_short = 44,
      TK_signed = 45,
      TK_sizeof = 19,
      TK_static = 28,
      TK_struct = 53,
      TK_switch = 96,
      TK_typedef = 34,
      TK_union = 54,
      TK_unsigned = 46,
      TK_void = 47,
      TK_volatile = 14,
      TK_while = 97,
      TK__Bool = 48,
      TK__Complex = 49,
      TK__Imaginary = 50,
      TK_integer = 20,
      TK_floating = 21,
      TK_charconst = 22,
      TK_stringlit = 18,
      TK_identifier = 1,
      TK_Completion = 6,
      TK_EndOfCompletion = 7,
      TK_Invalid = 98,
      TK_LeftBracket = 17,
      TK_LeftParen = 2,
      TK_LeftBrace = 29,
      TK_Dot = 55,
      TK_Arrow = 71,
      TK_PlusPlus = 15,
      TK_MinusMinus = 16,
      TK_And = 12,
      TK_Star = 8,
      TK_Plus = 9,
      TK_Minus = 10,
      TK_Tilde = 23,
      TK_Bang = 24,
      TK_Slash = 56,
      TK_Percent = 57,
      TK_RightShift = 37,
      TK_LeftShift = 38,
      TK_LT = 58,
      TK_GT = 59,
      TK_LE = 60,
      TK_GE = 61,
      TK_EQ = 65,
      TK_NE = 66,
      TK_Caret = 67,
      TK_Or = 68,
      TK_AndAnd = 69,
      TK_OrOr = 72,
      TK_Question = 73,
      TK_Colon = 62,
      TK_DotDotDot = 51,
      TK_Assign = 63,
      TK_StarAssign = 74,
      TK_SlashAssign = 75,
      TK_PercentAssign = 76,
      TK_PlusAssign = 77,
      TK_MinusAssign = 78,
      TK_RightShiftAssign = 79,
      TK_LeftShiftAssign = 80,
      TK_AndAssign = 81,
      TK_CaretAssign = 82,
      TK_OrAssign = 83,
      TK_Comma = 35,
      TK_RightBracket = 64,
      TK_RightParen = 25,
      TK_RightBrace = 36,
      TK_SemiColon = 84,
      TK_typeof = 26,
      TK___alignof__ = 27,
      TK_MAX = 99,
      TK_MIN = 100,
      TK___attribute__ = 3,
      TK___declspec = 4,
      TK_asm = 5,
      TK_ERROR_TOKEN = 70,
      TK_EOF_TOKEN = 85;

      public final static String orderedTerminalSymbols[] = {
                 "",
                 "identifier",
                 "LeftParen",
                 "__attribute__",
                 "__declspec",
                 "asm",
                 "Completion",
                 "EndOfCompletion",
                 "Star",
                 "Plus",
                 "Minus",
                 "const",
                 "And",
                 "restrict",
                 "volatile",
                 "PlusPlus",
                 "MinusMinus",
                 "LeftBracket",
                 "stringlit",
                 "sizeof",
                 "integer",
                 "floating",
                 "charconst",
                 "Tilde",
                 "Bang",
                 "RightParen",
                 "typeof",
                 "__alignof__",
                 "static",
                 "LeftBrace",
                 "auto",
                 "extern",
                 "inline",
                 "register",
                 "typedef",
                 "Comma",
                 "RightBrace",
                 "RightShift",
                 "LeftShift",
                 "char",
                 "double",
                 "float",
                 "int",
                 "long",
                 "short",
                 "signed",
                 "unsigned",
                 "void",
                 "_Bool",
                 "_Complex",
                 "_Imaginary",
                 "DotDotDot",
                 "enum",
                 "struct",
                 "union",
                 "Dot",
                 "Slash",
                 "Percent",
                 "LT",
                 "GT",
                 "LE",
                 "GE",
                 "Colon",
                 "Assign",
                 "RightBracket",
                 "EQ",
                 "NE",
                 "Caret",
                 "Or",
                 "AndAnd",
                 "ERROR_TOKEN",
                 "Arrow",
                 "OrOr",
                 "Question",
                 "StarAssign",
                 "SlashAssign",
                 "PercentAssign",
                 "PlusAssign",
                 "MinusAssign",
                 "RightShiftAssign",
                 "LeftShiftAssign",
                 "AndAssign",
                 "CaretAssign",
                 "OrAssign",
                 "SemiColon",
                 "EOF_TOKEN",
                 "break",
                 "case",
                 "continue",
                 "default",
                 "do",
                 "else",
                 "for",
                 "goto",
                 "if",
                 "return",
                 "switch",
                 "while",
                 "Invalid",
                 "MAX",
                 "MIN"
             };

    public final static boolean isValidForParser = true;
}
