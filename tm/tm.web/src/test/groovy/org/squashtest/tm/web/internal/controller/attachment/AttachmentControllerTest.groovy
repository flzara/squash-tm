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
package org.squashtest.tm.web.internal.controller.attachment

import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.service.attachment.AttachmentManagerService
import spock.lang.Specification

import javax.servlet.ServletOutputStream
import javax.servlet.WriteListener
import javax.servlet.http.HttpServletResponse

class AttachmentControllerTest extends Specification {
    AttachmentController attachmentController = new AttachmentController()
    AttachmentManagerService service = Mock()

    def setup() {
        attachmentController.attachmentManagerService = service
    }

    def ""() {
        given:
        Attachment attachment = Mock()
        attachment.getName() >> "attachment"
        service.findAttachment(_) >> attachment

        and:
        HttpServletResponse response = Mock()
        ServletOutputStream downloadStream = new ServletOutputStream() {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream()

            public void write(int b) throws IOException {
                bytes.write(b)
            }

            @Override
            boolean isReady() {
                return false
            }

            @Override
            void setWriteListener(WriteListener writeListener) {
                // NOOP
            }
        }
        response.outputStream >> downloadStream

        when:
        attachmentController.downloadAttachment 10, response

        then:
        1 * service.writeContent(10, downloadStream)
    }
}
