/*
Copyright 2015 Mario Danelli (mario.danelli[at]gmail.com)
 
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

import java.util.HashMap;

public class MathParser {
	private static final String EXCEPTION_PARSE_METHOD_NOT_CALLED = "The 'parse' method hasn't been called";
	private static final String EXCEPTION_CHARACTERS = "Exception while parsing characters";
	private static final String EXCEPTION_PARENTHESES = "Exception while parsing parentheses";
	private static final String EXCEPTION_EXECUTE = "Exception while executing";
	private static final String EXCEPTION_ADD_VARIABLE_NOT_MODIFABLE = "Exception while adding variable - Variable not modifable";
	private static final String EXCEPTION_FUNCTION_VARIABLE_NOT_EXIST = "Exception not existing function or variable";
	private static final String EXCEPTION_VARIABLE_NAME_TOO_LONG = "Exception due to variable name too long, max num of chars: ";
	private static final String EXCEPTION_VARIABLE_NAME_ONLY_ALPHABETIC = "Exception due to variable name contains no alphabetic chars: ";
	private static final char SPECIAL_CHAR_PLACECARD = '¿';
	private static final char SPECIAL_CHAR_VARIABLE = '?';
	private static final String VERSION_STRING = "MEP4J - ver. 1.0.0 - 03/04/2015";
	private static final MathParserException MME_NOT_PARSED = new MathParserException(EXCEPTION_PARSE_METHOD_NOT_CALLED);
	private final int NUM_TO_MULTIPLY_INIITIAL_LENGTH = 2;
	private final int NUM_MAX_FUNCTION_CHARS = 5;
	private final int NUM_MIN_FUNCTION_CHARS = 3;
	private final int NUM_MAX_VARIABLE_CHARS = NUM_MAX_FUNCTION_CHARS;
	private HashMap<String, Double> hmVariables = null;
	private HashMap<String, Double> hmVariablesNotModifable = null;
	private HashMap<String, Character> hmFunctions = null;
	private HashMap<Character, Integer> hmValidChars = null;
	private StringBuilder[] arrToExecute = null;
	private int numToExecute = 0;
	private Double[][] arrExecuted = null;
	private Double[][] arrExecutedNoVariables = null;
	private MathParserException mme = null;
	private int[] functionOpen = null;
	private int functionOpenIndex = -1;
	private boolean parsed = false;
	private boolean alreadyExecuted = false;
	
	public String getVersion() {
		return VERSION_STRING;
	}
	
	public MathParser() {
		this.initFunctions();
		this.initVariables();
		if (!alreadyExecuted) {
			this.parse("1 + abs(2)").execute();
			alreadyExecuted = true;
		}
		this.parse("");
		mme = MME_NOT_PARSED;
	}

	public MathParserException getLastException() {
		return mme;
	}

	public MathParser parse(String toExecute) {
		arrToExecute = null;
		arrExecuted = null;
		arrExecutedNoVariables = null;
		numToExecute = 0;
		functionOpen = null;
		functionOpenIndex = -1;
		try {
			functionOpen = new int[toExecute.length()];
			toExecute = this.prepare(toExecute);
			int lengthArraysMustHave = toExecute.length() * NUM_TO_MULTIPLY_INIITIAL_LENGTH;
			if (toExecute.length() > 0) {
				if (mme == MME_NOT_PARSED) {
					mme = null;
				}
				arrToExecute = new StringBuilder[lengthArraysMustHave];
				arrToExecute[0] = new StringBuilder(toExecute);
				numToExecute = 1;
				for (int i = 0; i < arrToExecute.length; ++i) {
					if (this.parseSingle(i)) {
						break;
					}
				}
			}
		} catch (MathParserException mme) {
			parsed = false;
			this.mme = mme;
		}
		if (arrToExecute != null) {
			parsed = true;
			arrExecuted = new Double[arrToExecute.length][2];
			arrExecutedNoVariables = new Double[arrToExecute.length][2];
		}
		return this;
	}

	public Double execute() {
		if (numToExecute <= 0)
			return Double.NaN;
		Double[] results = null;
		try {
			if ( (parsed) && ((arrExecutedNoVariables[0] == null) || (arrExecutedNoVariables[0][0] == null)) ) {
				for (int i = numToExecute - 1; i >= 0; i--) {
					if ((arrExecutedNoVariables[0] == null) || (arrExecutedNoVariables[i][0] == null)) {
						ExecuteResult ExecuteResult = this.executeSingle(arrToExecute[i].toString());
						arrExecuted[i] = ExecuteResult.getResults();
						if (!ExecuteResult.isVariable()) {
							arrExecutedNoVariables[i] = arrExecuted[i];
						} else {
							arrExecutedNoVariables[i] = null;
						}
					}
				}
			}
			if (parsed) {
				results = arrExecuted[0];
			}
		} catch (MathParserException mme) {
			this.mme = mme;
		}
		if (null == results)
			return Double.NaN;
		return results[0];
	}

	private String prepare(String toPrepare) throws MathParserException {
		StringBuilder prepared = new StringBuilder(toPrepare.length());
		char foundPlusMinus = 0;
		char prevC = ' ';
		char lastFunction = ' ';
		int lastFunctionPos = -1;
		for (int i = 0; i < toPrepare.length(); ++i) {
			char c = Character.toLowerCase(toPrepare.charAt(i));
			if (c == ' ') {
				continue;
			} else if ((c == '+') && ((prevC == '+') || (prevC == '-'))) {
				continue;
			} else if ((c == '-') && (prevC == '-')) {
				int preparedLength = prepared.length();
				prevC = '+';
				prepared.replace(preparedLength - 1, preparedLength, "" + prevC);
				continue;
			} else if ((c == '-') && (prevC == '+')) {
				int preparedLength = prepared.length();
				prevC = '-';
				prepared.replace(preparedLength - 1, preparedLength, "" + prevC);
				continue;
			}
			if (!((Character.isDigit(c)) || (c == '(') || (c == ')')
					|| (c == '+') || (c == '-') || (c == '*') || (c == '/')
					|| (c == '%') || (c == '.') || (c == ',') || isAValidChar(c))) {
				throw new MathParserException(EXCEPTION_CHARACTERS + " - position " + (i + 1));
			} else if (((c == prevC) && ((c == '*') || (c == '/') || (c == '%')))
					|| ((c == '*') && ((prevC == '+') || (prevC == '-')
							|| (prevC == '*') || (prevC == '/')
							|| (prevC == '%')))
					|| ((c == '/') && ((prevC == '+') || (prevC == '-')
							|| (prevC == '+') || (prevC == '*')
							|| (prevC == '%')))
					|| ((c == '%') && ((prevC == '+') || (prevC == '-')
							|| (prevC == '+') || (prevC == '*')
							|| (prevC == '/')))
					|| ((c == ')') && ((prevC == '+') || (prevC == '-')
							|| (prevC == '+') || (prevC == '*')
							|| (prevC == '%')))) {
				throw new MathParserException(EXCEPTION_CHARACTERS + " - position " + (i + 1));
			} else if ((Character.isDigit(prevC)) && (c == '(')) {
				throw new MathParserException(EXCEPTION_CHARACTERS + " - position " + (i + 1));
			} else if ((isAValidChar(prevC)) && (!isAValidChar(prevC))
					&& (c != '(')) {
				throw new MathParserException(EXCEPTION_CHARACTERS + " - position " + (i + 1));
			}
			if (foundPlusMinus != 0) {
				if ((!Character.isDigit(c)) && (c != '.')) {
					char lastChar = prepared.charAt(prepared.length() - 1);
					if ((lastChar == '-') || (lastChar == '+')) {
						prepared.append("1)*");
					} else {
						prepared.append(')');
					}
					foundPlusMinus = 0;
				}
			}
			// BEGIN FUNCTIONS-VARIABLES
			if (Character.isAlphabetic(c)) {
				String functionOrVariableStringInitial = null;
				int initialCounter = 0;
				if ((i + NUM_MAX_FUNCTION_CHARS + 1) <= toPrepare.length()) {
					functionOrVariableStringInitial = toPrepare.substring(i, i + NUM_MAX_FUNCTION_CHARS + 1);
					initialCounter = functionOrVariableStringInitial.length() - 1;
				} else {
					functionOrVariableStringInitial = toPrepare.substring(i);
					initialCounter = functionOrVariableStringInitial.length();
				}
				Character functionChar = null;
				String functionOrVariableString = functionOrVariableStringInitial;
				for (int j = initialCounter; j >= NUM_MIN_FUNCTION_CHARS; --j) {
					functionOrVariableString = functionOrVariableStringInitial.substring(0, j);
					functionChar = this
							.retrieveFunction(functionOrVariableString);
					if (functionChar != null) {
						if ((j >= functionOrVariableStringInitial.length())
								|| (functionOrVariableStringInitial.charAt(j) != '(')) {
							throw new MathParserException(
									EXCEPTION_FUNCTION_VARIABLE_NOT_EXIST);
						}
						break;
					}
				}
				if (functionChar != null) {
					prepared.append("(" + functionChar); 
					i += (functionOrVariableString.length() - 1);
					functionOpen[++functionOpenIndex] = 1;
					continue;
				} else {
					StringBuilder functionOrVariableStringBuilder = new StringBuilder(
							NUM_MAX_FUNCTION_CHARS);
					for (int j = 0; j < functionOrVariableStringInitial
							.length(); ++j) {
						char cVariable = functionOrVariableStringInitial
								.charAt(j);
						if (Character.isAlphabetic(cVariable)) {
							functionOrVariableStringBuilder.append(cVariable);
						} else {
							break;
						}
					}
					functionOrVariableString = functionOrVariableStringBuilder.toString();
					Double variableValue = this.getVariable(functionOrVariableString);
					if (variableValue != null) {
						if (((i - 2) == lastFunctionPos)
								&& (lastFunctionPos >= 0)) {
							prepared.append(lastFunction);
							lastFunction = ' ';
							lastFunctionPos = -1;
						}
						prepared.append("" + '(' + SPECIAL_CHAR_VARIABLE
								+ functionOrVariableString + ")");
						i += functionOrVariableString.length() - 1;
						prevC = ')';
						continue;
					} else {
						throw new MathParserException(
								EXCEPTION_FUNCTION_VARIABLE_NOT_EXIST);
					}
				}
			}
			// END FUNCTIONS-VARIABLES
			// BEGIN NESTED FUNCTIONS PARENTHESES
			if ((c == '(') && (functionOpenIndex >= 0)
					&& (functionOpen[functionOpenIndex] > 0)) {
				for (int j = 0; j <= functionOpenIndex; ++j) {
					++functionOpen[j];
				}
			} else if ((c == ')')) {
				for (int j = 0; j <= functionOpenIndex; ++j) {
					if ((j == functionOpenIndex)
							&& (functionOpen[functionOpenIndex] == 2)) {
						prepared.append(')');
						functionOpen[functionOpenIndex] = 0;
						--functionOpenIndex;
					} else if (functionOpen[j] > 2) {
						--functionOpen[j];
					}
				}
			}
			// END NESTED FUNCTIONS PARENTHESES
			if (((c == '-') || (c == '+')) && (!Character.isDigit(prevC))) {
				if (prevC != ')') {
					prepared.append('(');
				} else {
					prepared.append("+(");
				}
				foundPlusMinus = c;
			}
			prevC = c;
			if (((i - 2) == lastFunctionPos) && (lastFunctionPos >= 0)) {
				prepared.append(lastFunction);
				lastFunction = ' ';
				lastFunctionPos = -1;
			}
			prepared.append(c);
		}
		if (foundPlusMinus != 0) {
			prepared.append(')');
		}
		return prepared.toString();
	}

	private boolean parseSingle(int index) throws MathParserException {
		boolean toBreak = true;
		StringBuilder stringBuilderI = arrToExecute[index];
		if (stringBuilderI == null) {
			return toBreak;
		}
		String toParse = stringBuilderI.toString();
		int numPar = 0;
		int parIndexBegin = -1, parIndexEnd = -1;
		StringBuilder parsed = new StringBuilder("");
		for (int i = 0; i < toParse.length(); ++i) {
			char c = toParse.charAt(i);
			if (c == '(') {
				if (parIndexBegin < 0) {
					parIndexBegin = i;
				}
				++numPar;
			} else if (c == ')') {
				--numPar;
				parIndexEnd = i;
				if (numPar == 0) {
					StringBuilder subToParse = new StringBuilder(
							toParse.substring(parIndexBegin + 1, parIndexEnd));
					parsed.append("" + SPECIAL_CHAR_PLACECARD + numToExecute + SPECIAL_CHAR_PLACECARD);
					arrToExecute[numToExecute++] = subToParse;
					parIndexBegin = parIndexEnd = -1;
				}
				if (numPar < 0) {
					throw new MathParserException(EXCEPTION_PARENTHESES);
				}
			} else if (numPar == 0) {
				parsed.append(c);
			}
		}
		arrToExecute[index] = parsed;
		if (numPar != 0) {
			throw new MathParserException(EXCEPTION_PARENTHESES);
		} else {
			toBreak = false;
			return toBreak;
		}
	}

	private ExecuteResult executeSingle(String toExecute)
			throws MathParserException {
		boolean containsVariable = false;
		String[] toExecuteArr = toExecute.split(",");
		if (toExecuteArr.length == 2) {
			Double double0 = executeSingle(toExecuteArr[0]).getResults()[0];
			Double double1 = executeSingle(toExecuteArr[1]).getResults()[0];
			return new ExecuteResult(double0, double1, containsVariable);
		}
		char firstChar = toExecute.charAt(0);
		if (firstChar == SPECIAL_CHAR_VARIABLE) {
			return new ExecuteResult(this.getVariable(toExecute.substring(1)), true);
		}
		OperationType functionIfPresent = whichFunctionByChar(firstChar);
		if (functionIfPresent != null) {
			if (toExecute.length() > 1) {
				return executeFunction(functionIfPresent,
						toExecute.substring(1));
			} else {
				throw new MathParserException(EXCEPTION_EXECUTE);
			}
		}
		Double toRet = Double.NaN;
		int maxPlusMinus = -1, maxMultiplyDivideModule = -1, maxPower = -1, maxFunction = -1;
		OperationType operationTypePlusMinus = null, operationTypeMultiplyDivideModule = null, operationTypePower = null, operationTypeFunction = null;
		boolean toExecuteJustSub = true;
		for (int i = (toExecute.length() - 1); i >= 0; --i) {
			char c = toExecute.charAt(i);
			boolean toExit = false;
			switch (c) {
				case '+': {
					if (maxPlusMinus < 0) {
						maxPlusMinus = i;
						operationTypePlusMinus = OperationType.PLUS;
						toExecuteJustSub = false;
						toExit = true;
						if ((i == 0)
								&& ((maxMultiplyDivideModule < 0) && (maxPower < 0) && (maxFunction < 0))) {
							return new ExecuteResult(new Double(toExecute));
						}
					}
					break;
				}
				case '-': {
					if (maxPlusMinus < 0) {
						maxPlusMinus = i;
						operationTypePlusMinus = OperationType.MINUS;
						toExecuteJustSub = false;
						toExit = true;
						if ((i == 0)
								&& ((maxMultiplyDivideModule < 0) && (maxPower < 0) && (maxFunction < 0))) {
							return new ExecuteResult(new Double(toExecute));
						}
					}
					break;
				}
				case '*': {
					if (maxMultiplyDivideModule < 0) {
						maxMultiplyDivideModule = i;
						operationTypeMultiplyDivideModule = OperationType.MULTIPLY;
						toExecuteJustSub = false;
					}
					break;
				}
				case '/': {
					if (maxMultiplyDivideModule < 0) {
						maxMultiplyDivideModule = i;
						operationTypeMultiplyDivideModule = OperationType.DIVIDE;
						toExecuteJustSub = false;
					}
					break;
				}
				case '%': {
					if (maxMultiplyDivideModule < 0) {
						maxMultiplyDivideModule = i;
						operationTypeMultiplyDivideModule = OperationType.MODULE;
						toExecuteJustSub = false;
					}
					break;
				}
				case '╚': { // 200
					if (maxPower < 0) {
						maxPower = i;
						operationTypePower = OperationType.POW;
						toExecuteJustSub = false;
					}
					break;
				}
				case '¬': { // 170
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.ABS;
						toExecuteJustSub = false;
					}
					break;
				}
				case '╝': { // 188
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.SQRT;
						toExecuteJustSub = false;
					}
					break;
				}
				case '¢': { // 189
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.CBRT;
						toExecuteJustSub = false;
					}
					break;
				}
				case '▓': { // 178
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.SIN;
						toExecuteJustSub = false;
					}
					break;
				}
				case '▒': { // 177
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.COS;
						toExecuteJustSub = false;
					}
					break;
				}
				case '¥': { // 190
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.LOG;
						toExecuteJustSub = false;
					}
					break;
				}
				case '┴': { // 193
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.EXP;
						toExecuteJustSub = false;
					}
					break;
				}
				case '½': { // 171
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.ACOS;
						toExecuteJustSub = false;
					}
					break;
				}
				case '¼': { // 172
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.ASIN;
						toExecuteJustSub = false;
					}
					break;
				}
				case '¡': { // 173
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.COSH;
						toExecuteJustSub = false;
					}
					break;
				}
				case '░': { // 176
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.SINH;
						toExecuteJustSub = false;
					}
					break;
				}
				case '╣': { // 185
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.TAN;
						toExecuteJustSub = false;
					}
					break;
				}
				case '║': { // 186
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.TANH;
						toExecuteJustSub = false;
					}
					break;
				}
				case '╗': { // 187
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.ATAN;
						toExecuteJustSub = false;
					}
					break;
				}
				case '┐': { // 191
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.LOG10;
						toExecuteJustSub = false;
					}
					break;
				}
				case '└': { // 192
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.LOG1P;
						toExecuteJustSub = false;
					}
					break;
				}
				case '┬': { // 194
					if (maxFunction < 0) {
						maxFunction = i;
						operationTypeFunction = OperationType.EXPM1;
						toExecuteJustSub = false;
					}
					break;
				}
			}
			if (toExit) {
				break;
			}
		}
		if (toExecuteJustSub) {
			return executeBase(toExecute);
		} else if (maxPlusMinus >= 0) {
			String first = toExecute.substring(0, maxPlusMinus);
			String second = toExecute.substring(maxPlusMinus + 1);
			return operation(first, second, operationTypePlusMinus);
		} else if (maxMultiplyDivideModule >= 0) {
			String first = toExecute.substring(0, maxMultiplyDivideModule);
			String second = toExecute.substring(maxMultiplyDivideModule + 1);
			return operation(first, second, operationTypeMultiplyDivideModule);
		} else if (maxPower >= 0) {
			String first = toExecute.substring(0, maxPower);
			String second = toExecute.substring(maxPower + 1);
			return operation(first, second, operationTypePower);
		} else if (maxFunction >= 0) {
			String first = toExecute.substring(0, maxFunction);
			String second = toExecute.substring(maxFunction + 1);
			return operation(first, second, operationTypeFunction);
		}
		// System.out.println("TOEXECUTE toRet " + toExecute + ": " + toRet);
		return new ExecuteResult(toRet, containsVariable);
	}

	private ExecuteResult executeBase(String toExecute)
			throws MathParserException {
		Double toRet0 = Double.NaN, toRet1 = Double.NaN;
		boolean containsVariable = false;
		int foundId = findValueId(toExecute);
		if (foundId < 0) {
			try {
				toRet0 = new Double(toExecute);
			} catch (NumberFormatException nfe) {
				throw new MathParserException(MathParser.EXCEPTION_EXECUTE, nfe);
			}
		} else {
			toRet0 = arrExecuted[foundId][0];
			toRet1 = arrExecuted[foundId][1];
			if (arrExecutedNoVariables[foundId] == null) {
				containsVariable = true;
			}
		}
		return new ExecuteResult(toRet0, toRet1, containsVariable);
	}

	private ExecuteResult operation(String firstPart, String secondPart,
			OperationType operationType) throws MathParserException {
		Double toRet = Double.NaN;
		boolean containsVariable = false;
		switch (operationType) {
			case PLUS: {
				if ("".equals(firstPart)) {
					ExecuteResult executeResult = this.executeSingle(secondPart);
					containsVariable = executeResult.isVariable();
					toRet = executeResult.getResults()[0];
				} else if ("".equals(secondPart)) {
					throw new MathParserException(EXCEPTION_EXECUTE);
				} else {
					ExecuteResult executeResult1 = this.executeSingle(firstPart);
					if (executeResult1.isVariable())
						containsVariable = true;
					ExecuteResult executeResult2 = this.executeSingle(secondPart);
					if (executeResult2.isVariable())
						containsVariable = true;
					toRet = executeResult1.getResults()[0]
							+ executeResult2.getResults()[0];
				}
				break;
			}
			case MINUS: {
				if ("".equals(firstPart)) {
					ExecuteResult executeResult = this.executeSingle(secondPart);
					containsVariable = executeResult.isVariable();
					Double secondOperand = executeResult.getResults()[0];
					if (secondOperand == Double.POSITIVE_INFINITY) {
						toRet = Double.NEGATIVE_INFINITY;
					} else if (secondOperand == Double.NEGATIVE_INFINITY) {
						toRet = Double.POSITIVE_INFINITY;
					} else {
						toRet = 0 - secondOperand;
					}
				} else if ("".equals(secondPart)) {
					throw new MathParserException(EXCEPTION_EXECUTE);
				} else {
					ExecuteResult executeResult1 = this.executeSingle(firstPart);
					if (executeResult1.isVariable())
						containsVariable = true;
					ExecuteResult executeResult2 = this.executeSingle(secondPart);
					if (executeResult2.isVariable())
						containsVariable = true;
					toRet = executeResult1.getResults()[0]
							- executeResult2.getResults()[0];
				}
				break;
			}
			case DIVIDE: {
				if ("".equals(firstPart)) {
					throw new MathParserException(EXCEPTION_EXECUTE);
				} else if ("".equals(secondPart)) {
					throw new MathParserException(EXCEPTION_EXECUTE);
				} else {
					ExecuteResult executeResult1 = this.executeSingle(firstPart);
					if (executeResult1.isVariable())
						containsVariable = true;
					ExecuteResult executeResult2 = this.executeSingle(secondPart);
					if (executeResult2.isVariable())
						containsVariable = true;
					Double firstOperand = executeResult1.getResults()[0];
					Double secondOperand = executeResult2.getResults()[0];
					if (secondOperand.compareTo(new Double(0)) == 0) {
						if (firstOperand.compareTo(new Double(0)) >= 0) {
							toRet = Double.POSITIVE_INFINITY;
						} else {
							toRet = Double.NEGATIVE_INFINITY;
						}
					} else {
						toRet = firstOperand / secondOperand;
					}
				}
				break;
			}
			case MULTIPLY: {
				if ("".equals(firstPart)) {
					throw new MathParserException(EXCEPTION_EXECUTE);
				} else if ("".equals(secondPart)) {
					throw new MathParserException(EXCEPTION_EXECUTE);
				} else {
					ExecuteResult executeResult1 = this.executeSingle(firstPart);
					if (executeResult1.isVariable())
						containsVariable = true;
					ExecuteResult executeResult2 = this.executeSingle(secondPart);
					if (executeResult2.isVariable())
						containsVariable = true;
					toRet = executeResult1.getResults()[0]
							* executeResult2.getResults()[0];
				}
				break;
			}
			case MODULE: {
				if ("".equals(firstPart)) {
					throw new MathParserException(EXCEPTION_EXECUTE);
				} else if ("".equals(secondPart)) {
					throw new MathParserException(EXCEPTION_EXECUTE);
				} else {
					ExecuteResult executeResult1 = this.executeSingle(firstPart);
					if (executeResult1.isVariable())
						containsVariable = true;
					ExecuteResult executeResult2 = this.executeSingle(secondPart);
					if (executeResult2.isVariable())
						containsVariable = true;
					toRet = executeResult1.getResults()[0]
							% executeResult2.getResults()[0];
				}
				break;
			}
		}
		return new ExecuteResult(toRet, containsVariable);
	}

	private OperationType whichFunctionByChar(char charToCheck) {
		OperationType toRet = null;
		switch (charToCheck) {
			case '¬': { // 170
				toRet = OperationType.ABS;
				break;
			}
			case '╝': { // 188
				toRet = OperationType.SQRT;
				break;
			}
			case '¢': { // 189
				toRet = OperationType.CBRT;
				break;
			}
			case '▓': { // 178
				toRet = OperationType.SIN;
				break;
			}
			case '▒': { // 177
				toRet = OperationType.COS;
				break;
			}
			case '¥': { // 190
				toRet = OperationType.LOG;
				break;
			}
			case '┴': { // 193
				toRet = OperationType.EXP;
				break;
			}
			case '½': { // 171
				toRet = OperationType.ACOS;
				break;
			}
			case '¼': { // 172
				toRet = OperationType.ASIN;
				break;
			}
			case '¡': { // 173
				toRet = OperationType.COSH;
				break;
			}
			case '░': { // 176
				toRet = OperationType.SINH;
				break;
			}
			case '╣': { // 185
				toRet = OperationType.TAN;
				break;
			}
			case '║': { // 186
				toRet = OperationType.TANH;
				break;
			}
			case '╗': { // 187
				toRet = OperationType.ATAN;
				break;
			}
			case '┐': { // 191
				toRet = OperationType.LOG10;
				break;
			}
			case '└': { // 192
				toRet = OperationType.LOG1P;
				break;
			}
			case '┬': { // 194
				toRet = OperationType.EXPM1;
				break;
			}
			case '├': { // 195
				toRet = OperationType.ROOT;
				break;
			}
			case '┼': { // 197
				toRet = OperationType.ATAN2;
				break;
			}
			case '╚': { // 200
				toRet = OperationType.POW;
				break;
			}
		}
		return toRet;
	}

	private ExecuteResult executeFunction(OperationType functionType, String toExecute) throws MathParserException {
		Double toRet = null;
		boolean containsVariable = false;
		switch (functionType) {
			case ABS: { // ¬ - 170
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				toRet = Math.abs(executeResult.getResults()[0]);
				break;
			}
			case SQRT: { // ╝ - 188
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				toRet = Math.sqrt(executeResult.getResults()[0]);
				break;
			}
			case CBRT: { // ¢ - 189
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				toRet = Math.cbrt(executeResult.getResults()[0]);
				break;
			}
			case COS: { // ▒ - 177
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				//toRet = Math.cos(Math.toRadians(executeResult.getResults()[0]));
				toRet = Math.cos(executeResult.getResults()[0]);
				break;
			}
			case SIN: { // ▓ - 178
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				//toRet = Math.sin(Math.toRadians(executeResult.getResults()[0]));
				toRet = Math.sin(executeResult.getResults()[0]);
				break;
			}
			case LOG: { // ¥ - 190
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				toRet = Math.log(executeResult.getResults()[0]);
				break;
			}
			case EXP: { // ┴ - 193
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				toRet = Math.exp(executeResult.getResults()[0]);
				break;
			}
			case POW: { // ╚ - 200
				int foundId = findValueId(toExecute.substring(0));
				if (foundId < 0) {
					throw new MathParserException(MathParser.EXCEPTION_EXECUTE);
				}
				Double[] parsedDoubles = arrExecuted[foundId];
				containsVariable = arrExecutedNoVariables[foundId] != null ? false : true;
				Double firstPartExecuted = parsedDoubles[0];
				Double secondPartExecuted = parsedDoubles[1];
				toRet = Math.pow(firstPartExecuted, secondPartExecuted);
				break;
			}
			case ROOT: { // ├ - 195
				int foundId = findValueId(toExecute.substring(0));
				if (foundId < 0) {
					throw new MathParserException(MathParser.EXCEPTION_EXECUTE);
				}
				Double[] parsedDoubles = arrExecuted[foundId];
				containsVariable = arrExecutedNoVariables[foundId] != null ? false
						: true;
				Double firstPartExecuted = parsedDoubles[0];
				Double secondPartExecuted = parsedDoubles[1];
				toRet = Math.pow(Math.E, Math.log(firstPartExecuted)
						/ secondPartExecuted);
				break;
			}
			case ATAN2: { // ┼ - 197
				int foundId = findValueId(toExecute.substring(0));
				if (foundId < 0) {
					throw new MathParserException(MathParser.EXCEPTION_EXECUTE);
				}
				Double[] parsedDoubles = arrExecuted[foundId];
				containsVariable = arrExecutedNoVariables[foundId] != null ? false
						: true;
				Double firstPartExecuted = parsedDoubles[0];
				Double secondPartExecuted = parsedDoubles[1];
				//toRet = Math.atan2(firstPartExecuted, secondPartExecuted);
				toRet = Math.atan2(firstPartExecuted, secondPartExecuted);
				break;
			}
			case ACOS: { // ½ - 171
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				//toRet = Math.acos(Math.toRadians(executeResult.getResults()[0]));
				toRet = Math.acos(executeResult.getResults()[0]);
				break;
			}
			case ASIN: { // ¼ - 172
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				//toRet = Math.asin(Math.toRadians(executeResult.getResults()[0]));
				toRet = Math.asin(executeResult.getResults()[0]);
				break;
			}
			case COSH: { // ¡ - 173
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				//toRet = Math.sin(Math.toRadians(executeResult.getResults()[0]));
				toRet = Math.sin(executeResult.getResults()[0]);
				break;
			}
			case SINH: { // ░ - 176
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				//toRet = Math.sinh(Math.toRadians(executeResult.getResults()[0]));
				toRet = Math.sinh(executeResult.getResults()[0]);
				break;
			}
			case TAN: { // ╣ - 185
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				//toRet = Math.tan(Math.toRadians(executeResult.getResults()[0]));
				toRet = Math.tan(executeResult.getResults()[0]);
				break;
			}
			case TANH: { // ║ - 186
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				//toRet = Math.tanh(Math.toRadians(executeResult.getResults()[0]));
				toRet = Math.tanh(executeResult.getResults()[0]);
				break;
			}
			case ATAN: { // ╗ - 187
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				//toRet = Math.atan(Math.toRadians(executeResult.getResults()[0]));
				toRet = Math.atan(executeResult.getResults()[0]);
				break;
			}
			case LOG10: { // ┐ - 191
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				toRet = Math.log10(executeResult.getResults()[0]);
				break;
			}
			case LOG1P: { // └ - 192
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				toRet = Math.log1p(executeResult.getResults()[0]);
				break;
			}
			case EXPM1: { // ┬ - 194
				ExecuteResult executeResult = this.executeSingle(toExecute);
				containsVariable = executeResult.isVariable();
				toRet = Math.expm1(executeResult.getResults()[0]);
				break;
			}
		}
		return new ExecuteResult(toRet, containsVariable);
	}

	private boolean isAValidChar(char c) {
		return hmValidChars.containsKey(c);
	}

	private void initVariables() {
		hmVariables = new HashMap<String, Double>();
		hmVariablesNotModifable = new HashMap<String, Double>();
		hmVariables.put("e", Math.E);
		hmVariables.put("pi", Math.PI);
		hmVariablesNotModifable.put("e", Math.E);
		hmVariablesNotModifable.put("pi", Math.PI);
		addValidChar('e');
		addValidChar('p');
		addValidChar('i');
	}

	public MathParser addVariable(String varString, long varLong) throws MathParserException {
		return addVariable(varString, new Double(varLong));
	}
	
	
	public MathParser addVariable(String varString, Double varDouble)
			throws MathParserException {
		if (varString.length() > NUM_MAX_VARIABLE_CHARS) {
			throw new MathParserException(EXCEPTION_VARIABLE_NAME_TOO_LONG
					+ NUM_MAX_VARIABLE_CHARS + " - " + varString + " ("
					+ varString.length() + ")");
		}
		for (int i = 0; i < varString.length(); ++i) {
			if (!Character.isAlphabetic(varString.charAt(i))) {
				throw new MathParserException(
						EXCEPTION_VARIABLE_NAME_ONLY_ALPHABETIC + varString);
			}
		}
		varString = varString.toLowerCase();
		if (hmVariablesNotModifable.containsKey(varString)) {
			throw new MathParserException(EXCEPTION_ADD_VARIABLE_NOT_MODIFABLE);
		}
		hmVariables.put(varString, varDouble);
		for (int i = 0; i < varString.length(); ++i) {
			addValidChar(varString.charAt(i));
		}
		return this;
	}

	public Double getVariable(String variableString) {
		return hmVariables.get(variableString.toLowerCase());
	}

	private void initValidChars() {
		hmValidChars = new HashMap<Character, Integer>();
		hmValidChars.put('c', 0);
		hmValidChars.put('o', 0);
		hmValidChars.put('s', 0);
		hmValidChars.put('i', 0);
		hmValidChars.put('n', 0);
		hmValidChars.put('t', 0);
		hmValidChars.put('a', 0);
		hmValidChars.put('n', 0);
		hmValidChars.put('b', 0);
		hmValidChars.put('h', 0);
		hmValidChars.put('q', 0);
		hmValidChars.put('r', 0);
		hmValidChars.put('l', 0);
		hmValidChars.put('g', 0);
		hmValidChars.put('e', 0);
		hmValidChars.put('x', 0);
		hmValidChars.put('p', 0);
		hmValidChars.put('m', 0);
		hmValidChars.put('w', 0);
	}

	private void initFunctions() {
		hmFunctions = new HashMap<String, Character>();
		hmFunctions.put("abs", '¬');
		hmFunctions.put("acos", '½');
		hmFunctions.put("asin", '¼');
		hmFunctions.put("cosh", '¡');
		hmFunctions.put("sinh", '░');
		hmFunctions.put("cos", '▒');
		hmFunctions.put("sin", '▓');
		hmFunctions.put("tan", '╣');
		hmFunctions.put("tanh", '║');
		hmFunctions.put("atan", '╗');
		hmFunctions.put("sqrt", '╝');
		hmFunctions.put("cbrt", '¢');
		hmFunctions.put("log", '¥');
		hmFunctions.put("log10", '┐');
		hmFunctions.put("log1p", '└');
		hmFunctions.put("exp", '┴');
		hmFunctions.put("expm1", '┬');
		hmFunctions.put("root", '├');
		hmFunctions.put("atan2", '┼');
		hmFunctions.put("pow", '╚');
		initValidChars();
	}

	private void addValidChar(char cToAdd) {
		cToAdd = Character.toLowerCase(cToAdd);
		if (!hmValidChars.containsKey(cToAdd)) {
			hmValidChars.put(cToAdd, 0);
		}
	}

	private Character retrieveFunction(String functionString) {
		return hmFunctions.get(functionString.toLowerCase());
	}

	private int findValueId(String toEvaulate) {
		int foundId = -1;
		int beginToFind = -1, endToFind = -1;
		for (int i = 0; i < toEvaulate.length(); ++i) {
			if (toEvaulate.charAt(i) == SPECIAL_CHAR_PLACECARD) {
				if ((beginToFind == -1) && (endToFind == -1)) {
					beginToFind = i;
				} else if (endToFind == -1) {
					endToFind = i;
				}
				if ((beginToFind != -1) && (endToFind != -1)) {
					foundId = Integer.parseInt(toEvaulate.substring(
							beginToFind + 1, endToFind));
					beginToFind = endToFind = -1;
				}
			}
		}
		return foundId;
	}

}
