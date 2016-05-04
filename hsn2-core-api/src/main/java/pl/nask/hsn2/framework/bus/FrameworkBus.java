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

package pl.nask.hsn2.framework.bus;

import pl.nask.hsn2.bus.api.Bus;
import pl.nask.hsn2.bus.connector.framework.JobEventsNotifier;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnector;
import pl.nask.hsn2.bus.connector.process.ProcessConnector;

/**
 * This is view on bus useful for the framework.
 * 
 *
 */
public interface FrameworkBus extends Bus, JobEventsNotifier {

	/**
	 * Gets connector to Object Store with business interface.
	 * 
	 * @return Returns	instance of <code>ObjectStoreConnector</code>.
	 * 					There is only one instance of this interface
	 * 					per <code>Bus</code>.
	 * 					Pooling is implemented on endpoints level.
	 */
	ObjectStoreConnector getObjectStoreConnector();

	/**
	 * Gets connector to Process with business interface.
	 * 
	 * @return Returns	instance of <code>ServicesConnector</code>.
	 * 					There is only one instance of this interface
	 * 					per <code>Bus</code>.
	 * 					Pooling is implemented on endpoints level.
	 */
	ProcessConnector getProcessConnector();

}
