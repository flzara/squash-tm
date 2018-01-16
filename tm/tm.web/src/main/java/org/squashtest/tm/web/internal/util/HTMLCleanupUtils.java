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
package org.squashtest.tm.web.internal.util;

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.springframework.web.util.HtmlUtils;

public final class HTMLCleanupUtils {

	private HTMLCleanupUtils(){

	}

	public static String htmlToText(String html){

		String fixedHtml = html!=null ? html : "";

		String replacedHtml = fixedHtml.replaceFirst("\n", "");

		Source htmlSource = new Source(replacedHtml);
		Segment htmlSegment = new Segment(htmlSource, 0, replacedHtml.length());
		Renderer htmlRend = new Renderer(htmlSegment);
		String encoded = htmlRend.toString();
		return encoded.trim();

	}

	/* note : Unescape is idempotent when applied on unescaped data. We use that trick to prevent double html encoding*/
	public static String forceHtmlEscape(String html){
		String fixedHtml = html!=null ? html : "";
		String unescaped = HtmlUtils.htmlUnescape(fixedHtml);
		return HtmlUtils.htmlEscape(unescaped);
	}

	public static String stripJavascript(String json){
		if(StringUtils.isNotBlank(json)){
			Document.OutputSettings outputSettings = new Document.OutputSettings();
			outputSettings.prettyPrint(false);
			outputSettings.outline(false);
			String cleaned = Jsoup.clean(json,"", Whitelist.relaxed(), outputSettings);
			// We need to unescape here as JSoup escape json characters and make subsequent use of JSON crash
			// For html content we should escape before persistence
			// There is a little performance hit but it's safer to use JSoup than a custom solution.
			return HtmlUtils.htmlUnescape(cleaned);
		}
		return StringUtils.EMPTY;
	}

	public static String getBriefText(String text, int maxLength){
		text = htmlToText(text);
		if (text.length() > maxLength){
			text = text.substring(0, maxLength-3) + "...";
		}
		return text;
	}

}
