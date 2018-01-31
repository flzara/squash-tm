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
package org.squashtest.tm.domain.users;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(name = "CONNECTION_ATTEMPT_LOG")
public class ConnectionLog {
	@Id
	@Column(name = "ATTEMPT_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "connection_attempt_log_attempt_id_seq")
	@SequenceGenerator(name = "connection_attempt_log_attempt_id_seq", sequenceName = "connection_attempt_log_attempt_id_seq", allocationSize = 1)
	private Long id;

	@Column(name = "LOGIN")
	@Size(min = 0, max = 50)
	private String login;

	@Column(name = "CONNECTION_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	private Date connectionDate;

	@Column(name = "SUCCESS")
	@NotNull
	private Boolean success;

	public Long getId() {
		return id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public Date getConnectionDate() {
		return connectionDate;
	}

	public void setConnectionDate(Date connectionDate) {
		this.connectionDate = connectionDate;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
}
