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

package pl.nask.hsn2.framework.configuration;

public interface ConfigurationMapper {


    /**
     * Checks, if the mapper is able to map the key given as an argument.
     *
     * @param key key to be mapped
     * @return true, if the mapping is possible, false if not
     */
    boolean mapsKey(String key);

    /**
     * Maps the value identified with the key into configuration object
     *
     * @param configuration configuration object
     * @param key the key of the configuration value
     * @param value string representation of the value to be set
     * @throws MappingException if the mapping cannot be performed
     */
    void setValue(Configuration configuration, String key, String value) throws MappingException;

}
