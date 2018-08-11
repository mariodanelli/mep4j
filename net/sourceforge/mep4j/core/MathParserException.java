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
* MEP4J MathParserException exception.
*
* @version 1.0.1
* @author Mario Danelli (mario.danelli[at]gmail.com)
*/
public class MathParserException extends Exception {
	private static final long serialVersionUID = 1L;
	
	/**
	 * MathParserException 'void' constructor.
	 *
	 */
	public MathParserException() {
		super();
	};

	/**
	 * MathParserException 'String' constructor.
	 *
	 * @param	exceptionMsg	the exception message 
	 */
	public MathParserException(String exceptionMsg) {
		super(exceptionMsg);
	}

	/**
	 * MathParserException 'String and Throwable' constructor.
	 *
	 * @param	exceptionMsg	the exception message 
	 * @param	cause			the exception cause 
	 */
	public MathParserException(String exceptionMsg, Throwable cause) {
		super(exceptionMsg, cause);
	}
}
