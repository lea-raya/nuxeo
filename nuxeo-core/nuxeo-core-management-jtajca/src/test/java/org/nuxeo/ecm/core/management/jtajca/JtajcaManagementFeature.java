/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Stephane Lacoin
 */
package org.nuxeo.ecm.core.management.jtajca;

import java.util.Set;

import javax.inject.Inject;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.transaction.TransactionManager;

import org.nuxeo.ecm.core.storage.sql.IgnoreNonPooledCondition;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.jtajca.NuxeoContainer;
import org.nuxeo.runtime.management.ServerLocator;
import org.nuxeo.runtime.test.runner.ConditionalIgnoreRule;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.RuntimeFeature;
import org.nuxeo.runtime.test.runner.SimpleFeature;

import com.google.inject.Binder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

@Features(RuntimeFeature.class)
@Deploy({ "org.nuxeo.runtime.jtajca", "org.nuxeo.runtime.metrics", "org.nuxeo.runtime.management", "org.nuxeo.runtime.datasource",
        "org.nuxeo.ecm.core.management.jtajca" })
@LocalDeploy({ "org.nuxeo.ecm.core.management.jtajca:login-config.xml" })
@ConditionalIgnoreRule.Ignore(condition = IgnoreNonPooledCondition.class)
public class JtajcaManagementFeature extends SimpleFeature {

    protected ObjectName nameOf(Class<?> itf) {
        try {
            return new ObjectName(Defaults.instance.name(itf, "*"));
        } catch (MalformedObjectNameException cause) {
            throw new AssertionError("Cannot name monitor", cause);
        }
    }

    protected <T> void bind(Binder binder, MBeanServer mbs, Class<T> type) {
        final Set<ObjectName> names = mbs.queryNames(nameOf(type), null);
        for (ObjectName name : names) {
            T instance = type.cast(JMX.newMXBeanProxy(mbs, name, type));
            binder.bind(type).annotatedWith(Names.named(name.getKeyProperty("name"))).toInstance(instance);
        }
    }

    CoreFeature core;

    @Override
    public void start(FeaturesRunner runner) throws Exception {
        core = runner.getFeature(CoreFeature.class);
    }

    @Override
    public void configure(FeaturesRunner runner, Binder binder) {
        if (core == null) {
            return;
        }
        // bind repository
        String repositoryName = core.getStorageConfiguration().getRepositoryName();
        NuxeoContainer.getConnectionManager(repositoryName);

        MBeanServer mbs = Framework.getLocalService(ServerLocator.class).lookupServer();
        bind(binder, mbs, ConnectionPoolMonitor.class);
        bind(binder, mbs, CoreSessionMonitor.class);
        TransactionManager tm = NuxeoContainer.getTransactionManager();
        if (tm != null) {
            bind(binder, mbs, TransactionMonitor.class);
            binder.bind(TransactionManager.class).toInstance(tm);
        }
    }

    class TxChecker {
        @Inject
        @Named("default")
        TransactionMonitor monitor;

        void assertNoTransactions() {
            final long count = monitor.getActiveCount();
            if (count == 0) {
                return;
            }
            throw new AssertionError(String.format("still have tx active (%d) %s", count, monitor.getActiveStatistics()));
        }

        public TxChecker(FeaturesRunner runner) {
            runner.getInjector().injectMembers(this);
        }
    }

    TxChecker txChecker;

    @Override
    public void beforeSetup(FeaturesRunner runner) throws Exception {
        if (core == null) {
            return;
        }
        txChecker = new TxChecker(runner);
    }

    @Override
    public void afterTeardown(FeaturesRunner runner) throws Exception {
        if (txChecker == null) {
            return;
        }
        txChecker.assertNoTransactions();
        txChecker = null;
    }
}
