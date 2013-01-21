/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.0.
 * 
 * This is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.nask.hsn2.framework.configuration.validation;

public class MinValidator implements Validator {

    private final long min;

    public MinValidator(String validatorParam) {
        min = Long.parseLong(validatorParam);
    }

    @Override
    public void validate(String key, String value) throws ValidationException {
        long l = Long.parseLong(value);
        if (l < min)
            throw new ValidationException("Value must not be smaller than " + min);
    }

}
