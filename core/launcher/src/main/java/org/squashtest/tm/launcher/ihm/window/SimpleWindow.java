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
package org.squashtest.tm.launcher.ihm.window;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class SimpleWindow extends JFrame {

	private static final String WINDOW_TITLE = "Squash";
	private static final String MESSAGE_STRING = "Squash is starting, please wait...";
	private static final String TITLE = "Squash Launcher";

	public SimpleWindow ()
 {
		super();
		build();
	}

	private JPanel buildContent ()
 {
		// Panels...
		JPanel panel = new JPanel();
		JPanel panelHeader = new JPanel();
		JPanel progressArea = new JPanel();
		JPanel panelArea = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panelArea.setLayout(new FlowLayout());

		// Separator
		JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);

		JLabel label = new JLabel(TITLE);
		// title font
		Font titleFont = new Font(label.getFont().getName(), label.getFont().getStyle(), 22);
		label.setFont(titleFont);

		label.setAlignmentX(CENTER_ALIGNMENT);
		label.setAlignmentY(CENTER_ALIGNMENT);
		panelHeader.add(label);
		panelHeader.add(separator);

		// Progress bars
		JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setIndeterminate(true);
		progressArea.add(progressBar);

		// Main text

		JLabel message = new JLabel(MESSAGE_STRING);
		Color specificRed = new Color(204, 0, 0);
		Font font = new Font(message.getFont().getName(), message.getFont().getStyle(), 15);
		message.setFont(font);
		message.setForeground(specificRed);

		panelArea.add(message);

		panel.add(panelHeader);
		panel.add(progressArea);
		panel.add(panelArea);

		return panel;
	}

	public void build ()
 {
		setTitle(WINDOW_TITLE);
		setSize(400, 300);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setContentPane(buildContent());
	}
}
