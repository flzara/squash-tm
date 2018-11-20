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
package org.squashtest.tm.domain.scm;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.net.URL;

@Entity
@Table(name = "SCM_SERVER")
public class ScmServer {

	@Id
	@Column(name = "SCM_SERVER_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator ="scm_server_server_id_seq")
	@SequenceGenerator(name = "scm_server_server_id_seq", sequenceName = "scm_server_server_id_seq")
	private Long id;

	@Column(name = "NAME")
	@Size(max = 255)
	@NotBlank
	private String name;

	@Column(name = "URL")
	@org.hibernate.validator.constraints.URL
	private String url;

	@Column(name = "KIND")
	@Size(max = 30)
	private String kind;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
}
