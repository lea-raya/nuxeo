/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     bstefanescu
 */
package org.nuxeo.ecm.automation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.scripting.Scripting;

/**
 * An object holding the runtime parameters that should be used by an operation when run.
 * <p>
 * Parameters are injected at runtime into the operation using {@link Param} annotation.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class OperationParameters implements Serializable {

    private static final long serialVersionUID = -3215180388563955264L;

    protected final String oid;

    protected final Map<String, Object> params;

    public OperationParameters(String oid) {
        this(oid, new HashMap<String, Object>());
    }

    public OperationParameters(String oid, Map<String, Object> params) {
        this.oid = oid;
        this.params = params;
    }

    /**
     * The operation ID.
     */
    public String id() {
        return oid;
    }

    /**
     * The map of runtime parameters.
     */
    public final Map<String, Object> map() {
        return params;
    }

    public OperationParameters set(String key, Object valueRef) {
        if (valueRef instanceof String) {
            if (((String) valueRef).startsWith("expr:")) {
                valueRef = ((String) valueRef).substring(5).trim();
                // Unescape xml checking
                valueRef = StringEscapeUtils.unescapeXml(((String) valueRef));
                if (((String) valueRef).contains("@{")) {
                    params.put(key, Scripting.newTemplate(((String) valueRef)));
                } else {
                    params.put(key, Scripting.newExpression(((String) valueRef)));
                }
                return this;
            }
        }
        params.put(key, valueRef);
        return this;
    }

    public OperationParameters from(Map<String, Object> params) {
        this.params.putAll(params);
        return this;
    }

}
