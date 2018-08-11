/*
Copyright 2018 Mario Danelli (mario.danelli[at]gmail.com)
 
This file is part of MEP4J.

MEP4J is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation version 3 of the License.

MEP4J is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the Lesser GNU General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with MEP4J.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.sourceforge.mep4j.core;

/**
* MEP4J OperationType enum.
*
* @version 1.0.1
* @author Mario Danelli (mario.danelli[at]gmail.com)
*/
enum OperationType {
	MINUS, 		// +
	PLUS, 		// -
	MULTIPLY,	// *
	DIVIDE,		// /
	MODULUS,	// %
	ABS, 		// ¬ - 170	
	ACOS, 		// ½ - 171	
	ASIN, 		// ¼ - 172	
	COSH, 		// ¡ - 173	
	SINH, 		// ░ - 176	
	COS, 		// ▒ - 177 
	SIN, 		// ▓ - 178 
	TAN,		// ╣ - 185 
	TANH,		// ║ - 186
	ATAN,		// ╗ - 187
	SQRT,		// ╝ - 188
	CBRT,		// ¢ - 189
	LOG,		// ¥ - 190
	LOG10,		// ┐ - 191
	LOG1P,		// └ - 192
	EXP,		// ┴ - 193
	EXPM1,		// ┬ - 194
	ROOT,		// ├ - 195
	ATAN2,		// ┼ - 197
	POW;		// ╚ - 200
}