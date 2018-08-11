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
* MEP4J ExecuteResult class.
*
* @version 1.0.1
* @author Mario Danelli (mario.danelli[at]gmail.com)
*/
class ExecuteResult {
		private Double[] results = { Double.NaN, Double.NaN };
		private boolean variable= false;
		
		public ExecuteResult(Double result, Double result2, boolean variable) {
			this.results[0] = result;
			this.results[1] = result2;
			this.variable = variable;
		}
		
		public ExecuteResult(Double result) {
			this(result, null, false);
		}
		
		public ExecuteResult(Double result, boolean variable) {
			this(result, null, variable);
		}
		
		public ExecuteResult(Double result, Double result2) {
			this(result, result2, false);
		}

		public Double[] getResults() {
			return results;
		}

		public boolean isVariable() {
			return variable;
		}
	}
	