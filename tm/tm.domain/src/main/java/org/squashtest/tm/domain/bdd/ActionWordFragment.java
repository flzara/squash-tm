package org.squashtest.tm.domain.bdd;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

/**
 * @author qtran - created on 23/04/2020
 */
@Entity
public class ActionWordFragment {
	@Id
	@Column(name = "ACTION_WORD_FRAGMENT_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "action_word_fragment_action_word_fragment_id_seq")
	@SequenceGenerator(name = "action_word_fragment_action_word_fragment_id_seq", sequenceName = "action_word_fragment_action_word_fragment_id_seq", allocationSize = 1)
	private Long id;

	@NotNull
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "ACTION_WORD_ID")
	private ActionWord actionWord;

	public ActionWordFragment() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ActionWord getActionWord() {
		return actionWord;
	}

	public void setActionWord(ActionWord actionWord) {
		this.actionWord = actionWord;
	}
}
