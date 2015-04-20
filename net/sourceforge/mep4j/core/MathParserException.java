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

public class MathParserException extends Exception {
	private static final long serialVersionUID = 1L;

	public MathParserException() {
		super();
	};
	
	public MathParserException(String exceptionMsg) {
		super(exceptionMsg);
	}
	
	public MathParserException(String exceptionMsg, Throwable th) {
		super(exceptionMsg, th);
	}
}
