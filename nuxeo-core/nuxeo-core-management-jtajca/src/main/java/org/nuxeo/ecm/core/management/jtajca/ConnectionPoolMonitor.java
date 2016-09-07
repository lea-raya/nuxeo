/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     matic
 */
package org.nuxeo.ecm.core.management.jtajca;

import javax.management.MXBean;

import org.apache.geronimo.connector.outbound.PoolingAttributes;

/**
 * @author matic
 */
@MXBean
public interface ConnectionPoolMonitor extends PoolingAttributes, Monitor {

    public static String NAME = Defaults.instance.name(ConnectionPoolMonitor.class, "%s");

    /**
     * Returns the pool name
     *
     * @since 8.4
     */
    String getName();

    /**
     *
     * Returns the active timeout before the connection being killed.
     *
     * @since 8.4
     */
    int getActiveTimeoutMinutes();

    /**
     * Returns the current killed connection count
     * @since 8.4
     */
    long getKilledActiveConnectionCount();

    /**
     * Kills active timed out connections in the pool. Returns the killed count.
     *
     *
     * @since 8.4
     */
    int killActiveTimedoutConnections();

    /**
     * Destroys the current connection manager and replace it by a new one
     *
     *
     * @since 8.4
     */
    void reset();




}
