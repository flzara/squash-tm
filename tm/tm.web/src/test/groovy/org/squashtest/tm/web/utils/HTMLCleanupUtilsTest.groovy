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
package org.squashtest.tm.web.utils;



import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import spock.lang.Specification;


public class HTMLCleanupUtilsTest extends Specification {
	
	def "should remove all html tags"(){
		
		given :
			def dirtyString = "<p>first paragraph</p><p><ul><li>secondParagraph</li></ul></p>";
			def stripped = "first paragraph\r\n\r\n    * secondParagraph"
		when :
			def result = HTMLCleanupUtils.htmlToText(dirtyString);
		
		
		then :
			result == stripped
		
	}
	
	def "should gracfully handle null arguments when removing html tags"(){
		given :
			String dirtyString= null;
		
		when :
			def result = HTMLCleanupUtils.htmlToText(dirtyString);
		
		then :
			result == "";
	}
		
	def "should escape html"(){	
	
		given :
			def dirtyString = "<p style=\"color:green\"><div><ul><li>line1</li><li>line2</li>toto</div></p>";
			def escaped = "&lt;p style=&quot;color:green&quot;&gt;&lt;div&gt;&lt;ul&gt;&lt;li&gt;line1&lt;/li&gt;&lt;li&gt;line2&lt;/li&gt;toto&lt;/div&gt;&lt;/p&gt;"

		
		when :
			def result = HTMLCleanupUtils.forceHtmlEscape(dirtyString);
		
		then :
			result==escaped
		
	}
	
	def "should not escape html twice" (){
		given :
		def dirtyString = "&lt;p style=&quot;color:green&quot;&gt;&lt;div&gt;&lt;ul&gt;&lt;li&gt;line1&lt;/li&gt;&lt;li&gt;line2&lt;/li&gt;toto&lt;/div&gt;&lt;/p&gt;"

		def escaped = "&lt;p style=&quot;color:green&quot;&gt;&lt;div&gt;&lt;ul&gt;&lt;li&gt;line1&lt;/li&gt;&lt;li&gt;line2&lt;/li&gt;toto&lt;/div&gt;&lt;/p&gt;"

		
		when :
			def result = HTMLCleanupUtils.forceHtmlEscape(dirtyString);
		
		then :
			result==escaped
		
	}
	
	def "should gracfully handle null arguments when escaping html"(){
		given :
			String dirtyString= null;
		
		when :
			def result = HTMLCleanupUtils.forceHtmlEscape(dirtyString);
		
		then :
			result == "";
	}
	
	def "should strip javascript tags"(){
		
		given :
			def dirtyString = "<p style=\"color:green\"><div><ul><li>line1</li><li>line2</li>toto</div><script "+
							  "type=\"text/javascript\">alert('naive xss');</script></p>";
			def stripped = "<p style=\"color:green\"><div><ul><li>line1</li><li>line2</li>toto</div></p>"

		
		when :
			def result = HTMLCleanupUtils.stripJavascript(dirtyString);
		
		then :
			result==stripped
			
	}

	
	def "should gracfully handle null arguments when stripping javascript"(){
		given :
			String dirtyString= null;
		
		when :
			def result = HTMLCleanupUtils.stripJavascript(dirtyString);
		
		then :
			result == "";
	}
	
}