/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.library;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

public class StandardAnalyzerWithoutStopWords extends AnalyzerWrapper {
    /**
     * As recommended per AnalyzerWrapper(...) javadoc, we shall use the wrapped analyser's Reuse Strategy.
     * Yet the recommended idiom is not possible : super(delegate.getReuseStrategy()) because super() must be the first
     * instruction of a constructor.
     */
    private static final ReuseStrategy REUSE_STRATEGY = new StandardAnalyzer(new CharArraySet(1, true)).getReuseStrategy();

    private StandardAnalyzer standardAnalyzer = new StandardAnalyzer(new CharArraySet(1, true));

    public StandardAnalyzerWithoutStopWords() {
        super(REUSE_STRATEGY);
    }

    @Override
    protected Analyzer getWrappedAnalyzer(String fieldName) {
        return standardAnalyzer;
    }

    public int getMaxTokenLength() {

        return standardAnalyzer.getMaxTokenLength();
    }

    public void setMaxTokenLength(int length) {

        standardAnalyzer.setMaxTokenLength(length);
    }

    public CharArraySet getStopwordSet() {

        return standardAnalyzer.getStopwordSet();
    }
}
