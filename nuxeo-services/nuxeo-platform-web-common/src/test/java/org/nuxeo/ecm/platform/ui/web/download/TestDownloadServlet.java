/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Florent Guillaume
 */
package org.nuxeo.ecm.platform.ui.web.download;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class TestDownloadServlet {

    @Test
    public void testParse() {
        assertParsed("blobholder:0", null, "");
        assertParsed("blobholder:0", null, "/");
        assertParsed("foo", null, "/foo");
        assertParsed("foo", null, "/foo/");
        assertParsed("foo", "bar", "/foo/bar");
        assertParsed("foo/bar", "baz", "/foo/bar/baz");
        assertParsed("foo/bar/baz", "moo", "/foo/bar/baz/moo");
    }

    protected static void assertParsed(String path, String filename, String string) {
        Pair<String, String> pair = DownloadServlet.parsePath("somerepo/someid" + string);
        assertEquals(path + " " + filename, pair.getLeft() + " " + pair.getRight());
    }

}
