/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thierry Delprat <tdelprat@nuxeo.com>
 */
package org.nuxeo.automation.scripting.internals.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.nuxeo.automation.scripting.api.AutomationScriptingService;
import org.nuxeo.automation.scripting.internals.DocumentScriptingWrapper;
import org.nuxeo.automation.scripting.internals.MarshalingHelper;
import org.nuxeo.automation.scripting.internals.ScriptOperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.runtime.api.Framework;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.objects.NativeArray;

/**
 * @since 7.2
 */
public class ScriptingOperationImpl {

    protected final ScriptOperationContext ctx;

    protected final Map<String, Object> args;

    protected final String source;

    public ScriptingOperationImpl(String source, ScriptOperationContext ctx, Map<String, Object> args) throws ScriptException {
        this.ctx = ctx;
        this.args = args;
        this.source = source;
    }

    public Object run(Object input) throws Exception {
        try {
            AutomationScriptingService scriptingService = Framework.getService(AutomationScriptingService.class);
            scriptingService.setOperationContext(ctx);
            ScriptingOperationInterface itf = scriptingService.getInterface(ScriptingOperationInterface.class, source,
                    ctx.getCoreSession());
            input = wrapArgsAndInput(input, args);
            return unwrapResult(itf.run(input, args));
        } catch (ScriptException e) {
            throw new OperationException(e);
        } finally {
            if (ctx.get(Constants.VAR_IS_CHAIN) != null && !(Boolean) ctx.get(Constants.VAR_IS_CHAIN)) {
                ctx.deferredDispose();
            }
        }
    }

    protected Object wrapArgsAndInput(Object input, Map<String, Object> args) {
        for (String entryId : args.keySet()) {
            Object entry = args.get(entryId);
            if (entry instanceof DocumentModel) {
                args.put(entryId, new DocumentScriptingWrapper(ctx.getCoreSession(), (DocumentModel) entry));
            }
            if (entry instanceof DocumentModelList) {
                List<DocumentScriptingWrapper> docs = new ArrayList<>();
                for (DocumentModel doc : (DocumentModelList) entry) {
                    docs.add(new DocumentScriptingWrapper(ctx.getCoreSession(), doc));
                }
                args.put(entryId, docs);
            }
        }
        if (input instanceof DocumentModel) {
            return new DocumentScriptingWrapper(ctx.getCoreSession(), (DocumentModel) input);
        } else if (input instanceof DocumentModelList) {
            List<DocumentScriptingWrapper> docs = new ArrayList<>();
            for (DocumentModel doc : (DocumentModelList) input) {
                docs.add(new DocumentScriptingWrapper(ctx.getCoreSession(), doc));
            }
            return docs;
        }
        return input;
    }

    protected Object unwrapResult(Object res) {
        // Unwrap Context
        for (String entryId : ctx.keySet()) {
            Object entry = ctx.get(entryId);
            if (entry instanceof DocumentScriptingWrapper) {
                ctx.put(entryId, ((DocumentScriptingWrapper) entry).getDoc());
            } else if (entry instanceof List<?>) {
                DocumentModelList docs = new DocumentModelListImpl();
                List<?> l = (List<?>) entry;
                for (Object item : l) {
                    if (item instanceof DocumentScriptingWrapper) {
                        docs.add(((DocumentScriptingWrapper) item).getDoc());
                    }
                }
                if (docs.size() == l.size() && docs.size() > 0) {
                    ctx.put(entryId, docs);
                }
            }
        }
        // Unwrap Result
        if (res == null) {
            return null;
        }
        if (res instanceof ScriptObjectMirror) {
            Object unwrapped = MarshalingHelper.unwrap(
                    (ScriptObjectMirror) res);
            if (unwrapped instanceof List<?>) {
                DocumentModelList docs = new DocumentModelListImpl();
                List<?> l = (List<?>) unwrapped;
                for (Object item : l) {
                    if (item instanceof DocumentScriptingWrapper) {
                        docs.add(((DocumentScriptingWrapper) item).getDoc());
                    }
                }
                if (docs.size() == l.size() && docs.size() > 0) {
                    return docs;
                }
            } else if (unwrapped instanceof DocumentScriptingWrapper) {
                return ((DocumentScriptingWrapper) unwrapped).getDoc();
            }
            return unwrapped;
        } else if (res instanceof NativeArray) {
            Object[] resList = ((NativeArray) res).asObjectArray();
            DocumentModelList documentModelList = new DocumentModelListImpl();
            BlobList blobList = new BlobList();
            for (Object entry : resList) {
                if (entry instanceof DocumentModel) {
                    documentModelList.add((DocumentModel) entry);
                } else if (entry instanceof Blob) {
                    blobList.add((Blob) entry);
                } else if (entry instanceof DocumentScriptingWrapper) {
                    documentModelList.add(((DocumentScriptingWrapper) entry).getDoc());
                }
            }
            return documentModelList.isEmpty() ? blobList : documentModelList;
        } else if (res instanceof DocumentScriptingWrapper) {
            return ((DocumentScriptingWrapper) res).getDoc();
        } else if (res instanceof List<?>) {
            DocumentModelList docs = new DocumentModelListImpl();
            List<?> l = (List<?>) res;
            for (Object item : l) {
                if (item instanceof DocumentScriptingWrapper) {
                    docs.add(((DocumentScriptingWrapper) item).getDoc());
                }
            }
            if (docs.size() == l.size() && docs.size() > 0) {
                return docs;
            }
        }
        return res;
    }

}
