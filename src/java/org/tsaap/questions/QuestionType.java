/*
 * Copyright (C) 2013-2016 Université Toulouse 3 Paul Sabatier
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tsaap.questions;

/**
 * @author franck Silvestre
 */
public enum QuestionType {

    Undefined(0),
    ExclusiveChoice(1),
    MultipleChoice(2),
    TrueFalse(3),
    FillInTheBlank(4);

    private int code;

    QuestionType(int code) {
        this.code = code;
    }

    /**
     * Get the code of que question type
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }
}


