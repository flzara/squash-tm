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
package org.squashtest.tm.domain.bdd;

import org.hibernate.annotations.Type;
import org.squashtest.tm.domain.actionword.ActionWordFragmentVisitor;
import org.squashtest.tm.domain.actionword.ActionWordLibrary;
import org.squashtest.tm.domain.actionword.ActionWordTreeEntity;
import org.squashtest.tm.domain.actionword.ActionWordTreeEntityVisitor;
import org.squashtest.tm.domain.actionword.ConsumerForActionWordFragmentVisitor;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.KeywordTestStep;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Entity
@Auditable
public class ActionWord implements ActionWordTreeEntity {

	public static final int ACTION_WORD_MAX_LENGTH = 255;

	public static final String ACTION_WORD_TEXT_TOKEN = "T";

	public static final String ACTION_WORD_PARAM_TOKEN = "P";

	public static final String ACTION_WORD_DOUBLE_QUOTE = "\"";

	public static final String ACTION_WORD_OPEN_GUILLEMET = "<";

	public static final String ACTION_WORD_CLOSE_GUILLEMET = ">";

	public static final String ACTION_WORD_UNDERSCORE = "_";

	private static final String ACTION_WORD_TOKEN_TEXT_SEPARATOR = "-";

	@Id
	@Column(name = "ACTION_WORD_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "action_word_action_word_id_seq")
	@SequenceGenerator(name = "action_word_action_word_id_seq", sequenceName = "action_word_action_word_id_seq", allocationSize = 1)
	private Long id;

	@NotBlank
	@Column(name = "TOKEN")
	private String token;

	@NotNull
	@OneToMany(mappedBy = "actionWord", cascade = {CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
	private Set<KeywordTestStep> keywordTestSteps = new HashSet<>(0);

	@NotNull
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderColumn(name = "FRAGMENT_ORDER")
	@JoinColumn(name = "ACTION_WORD_ID")
	private List<ActionWordFragment> fragments = new ArrayList<>();

	@Lob
	@Column(name = "DESCRIPTION")
	@Type(type = "org.hibernate.type.TextType")
	private String description;

	@JoinColumn(name = "PROJECT_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Project project;

	public ActionWord() {
	}

	public ActionWord(List<ActionWordFragment> fragments) {
		setFragments(fragments);
		setToken(generateToken());
	}

	public String createWord() {
		StringBuilder builder = new StringBuilder();
		Consumer<ActionWordParameter> consumer = parameter ->
			builder.append(ACTION_WORD_DOUBLE_QUOTE).append(parameter.getName()).append(ACTION_WORD_DOUBLE_QUOTE);

		ConsumerForActionWordFragmentVisitor visitor = new ConsumerForActionWordFragmentVisitor(consumer, builder);
		for (ActionWordFragment fragment : fragments) {
			fragment.accept(visitor);
		}
		return builder.toString();
	}

	public String createWordWithDefaultValues() {
		StringBuilder builder = new StringBuilder();
		Consumer<ActionWordParameter> consumer = parameter ->
			builder.append(ACTION_WORD_DOUBLE_QUOTE).append(parameter.getDefaultValue()).append(ACTION_WORD_DOUBLE_QUOTE);

		ConsumerForActionWordFragmentVisitor visitor = new ConsumerForActionWordFragmentVisitor(consumer, builder);
		for (ActionWordFragment fragment : fragments) {
			fragment.accept(visitor);
		}
		return builder.toString();
	}

	public String generateToken() {
		StringBuilder prefixTokenBuilder = new StringBuilder();
		StringBuilder textTokenBuilder = new StringBuilder(ACTION_WORD_TOKEN_TEXT_SEPARATOR);
		ActionWordFragmentVisitor visitor = new ActionWordFragmentVisitor() {
			@Override
			public void visit(ActionWordText text) {
				prefixTokenBuilder.append(ActionWord.ACTION_WORD_TEXT_TOKEN);
				textTokenBuilder.append(text.getText()).append(ACTION_WORD_TOKEN_TEXT_SEPARATOR);
			}

			@Override
			public void visit(ActionWordParameter parameter) {
				prefixTokenBuilder.append(ActionWord.ACTION_WORD_PARAM_TOKEN);
			}
		};
		for (ActionWordFragment fragment : fragments) {
			fragment.accept(visitor);
		}
		return prefixTokenBuilder.append(textTokenBuilder).toString();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Set<KeywordTestStep> getKeywordTestSteps() {
		return keywordTestSteps;
	}

	public void setKeywordTestSteps(Set<KeywordTestStep> keywordTestSteps) {
		this.keywordTestSteps = keywordTestSteps;
	}

	public void addStep(KeywordTestStep keywordTestStep) {
		keywordTestSteps.add(keywordTestStep);
	}

	public List<ActionWordParameter> getActionWordParams() {
		List<ActionWordParameter> result = new ArrayList<>();
		ActionWordFragmentVisitor visitor = new ActionWordFragmentVisitor() {
			@Override
			public void visit(ActionWordText text) {
				//NOOP
			}

			@Override
			public void visit(ActionWordParameter parameter) {
				result.add(parameter);
			}
		};

		for (ActionWordFragment fragment : getFragments()) {
			fragment.accept(visitor);
		}
		return result;
	}

	public List<ActionWordText> getActionWordTexts() {
		List<ActionWordText> result = new ArrayList<>();
		ActionWordFragmentVisitor visitor = new ActionWordFragmentVisitor() {
			@Override
			public void visit(ActionWordText text) {
				result.add(text);
			}

			@Override
			public void visit(ActionWordParameter parameter) {
				//NOOP
			}
		};

		for (ActionWordFragment fragment : getFragments()) {
			fragment.accept(visitor);
		}
		return result;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Override
	public Project getProject() {
		return project;
	}

	@AclConstrainedObject
	public ActionWordLibrary getActionWordLibrary() {
		return project.getActionWordLibrary();
	}

	public List<ActionWordFragment> getFragments() {
		return fragments;
	}

	public void setFragments(List<ActionWordFragment> fragments) {
		this.fragments = fragments;
	}

	public void addFragment(@NotNull ActionWordFragment fragment) {
		getFragments().add(fragment);
	}

	/* ActionWordTreeEntity methods */

	@Override
	public void accept(ActionWordTreeEntityVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public ActionWordTreeEntity createCopy() {
		throw new UnsupportedOperationException();
	}

	/* TreeEntity methods */

	@Override
	public String getName() {
		return createWord();
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

}
