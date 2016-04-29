/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.1.
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

import java.util.StringTokenizer;

public final class ValidatorFactory {

    private ValidatorFactory() {}

    private static final Validator notEmptyValidator = new NotEmptyValidator();

    private static final Validator numberValidator = new NumberValidator();

    public static Validator getInstance(String validatorDescription) {
        StringTokenizer tokenizer = new StringTokenizer(validatorDescription, " ()");
        String validatorName = tokenizer.nextToken();
        String validatorParam = null;
        if (tokenizer.hasMoreTokens()) {// a parameter!
            validatorParam = tokenizer.nextToken();
        }

        return getInstance(validatorName, validatorParam);
    }

    private static Validator getInstance(String validatorName, String validatorParam) {
        if ("notEmpty".equalsIgnoreCase(validatorName)) {
            return notEmptyValidator;
        } else if ("number".equalsIgnoreCase(validatorName)) {
            return numberValidator;
        } else if ("min".equalsIgnoreCase(validatorName)) {
            return new MinValidator(validatorParam);
        } else if ("max".equalsIgnoreCase(validatorName)) {
            return new MaxValidator(validatorParam);
        } else if ("serverSocket".equalsIgnoreCase(validatorName)) {
            return new ServerSocketValidator();
        } else {
           return null;
        }
    }
}
