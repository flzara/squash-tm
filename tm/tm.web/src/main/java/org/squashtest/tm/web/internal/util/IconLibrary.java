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

 import java.util.Arrays;
 import java.util.List;

public final class IconLibrary {

	private static final List<String> ICON_NAMES = Arrays.asList("sq-icon-accept", "sq-icon-accept_blue", "sq-icon-add",
			"sq-icon-alarm", "sq-icon-anchor", "sq-icon-application", "sq-icon-application2",
			"sq-icon-application_add", "sq-icon-application_cascade", "sq-icon-application_delete",
			"sq-icon-application_double", "sq-icon-application_edit", "sq-icon-application_error",
			"sq-icon-application_form", "sq-icon-application_get", "sq-icon-application_go",
			"sq-icon-application_home", "sq-icon-application_key", "sq-icon-application_lightning",
			"sq-icon-application_link", "sq-icon-application_osx", "sq-icon-application_osx_terminal",
			"sq-icon-application_put", "sq-icon-application_side_boxes", "sq-icon-application_side_contract",
			"sq-icon-application_side_expand", "sq-icon-application_side_list", "sq-icon-application_side_tree",
			"sq-icon-application_split", "sq-icon-application_tile_horizontal", "sq-icon-application_tile_vertical",
			"sq-icon-application_view_columns", "sq-icon-application_view_detail", "sq-icon-application_view_gallery",
			"sq-icon-application_view_icons", "sq-icon-application_view_list", "sq-icon-application_view_tile",
			"sq-icon-application_view_xp", "sq-icon-application_view_xp_terminal", "sq-icon-arrow_branch",
			"sq-icon-arrow_divide", "sq-icon-arrow_in", "sq-icon-arrow_inout", "sq-icon-arrow_join",
			"sq-icon-arrow_left", "sq-icon-arrow_merge", "sq-icon-arrow_out", "sq-icon-arrow_redo",
			"sq-icon-arrow_refresh", "sq-icon-arrow_right", "sq-icon-arrow_undo", "sq-icon-asterisk_orange",
			"sq-icon-attach", "sq-icon-attach_2", "sq-icon-award_star_gold", "sq-icon-bandaid", "sq-icon-basket",
			"sq-icon-bell", "sq-icon-bin_closed", "sq-icon-blog", "sq-icon-blueprint", "sq-icon-blueprint_horizontal",
			"sq-icon-bluetooth", "sq-icon-bomb", "sq-icon-book", "sq-icon-bookmark", "sq-icon-bookmark_book",
			"sq-icon-bookmark_book_open", "sq-icon-bookmark_document", "sq-icon-bookmark_folder", "sq-icon-books",
			"sq-icon-book_addresses", "sq-icon-book_next", "sq-icon-book_open", "sq-icon-book_previous", "sq-icon-box",
			"sq-icon-brick", "sq-icon-bricks", "sq-icon-briefcase", "sq-icon-bug", "sq-icon-buildings",
			"sq-icon-bullet_add_1", "sq-icon-bullet_add_2", "sq-icon-bullet_key", "sq-icon-cake", "sq-icon-calculator",
			"sq-icon-calendar_1", "sq-icon-calendar_2", "sq-icon-camera", "sq-icon-cancel", "sq-icon-car",
			"sq-icon-cart", "sq-icon-cd", "sq-icon-chart_bar", "sq-icon-chart_curve", "sq-icon-chart_line",
			"sq-icon-chart_organisation", "sq-icon-chart_pie", "sq-icon-clipboard_paste_image",
			"sq-icon-clipboard_sign", "sq-icon-clipboard_text", "sq-icon-clock", "sq-icon-cog", "sq-icon-coins",
			"sq-icon-color_swatch_1", "sq-icon-color_swatch_2", "sq-icon-comment", "sq-icon-compass",
			"sq-icon-compress", "sq-icon-computer", "sq-icon-connect", "sq-icon-contrast", "sq-icon-control_eject",
			"sq-icon-control_end", "sq-icon-control_equalizer", "sq-icon-control_fastforward", "sq-icon-control_pause",
			"sq-icon-control_play", "sq-icon-control_repeat", "sq-icon-control_rewind", "sq-icon-control_start",
			"sq-icon-control_stop", "sq-icon-control_wheel", "sq-icon-counter", "sq-icon-counter_count",
			"sq-icon-counter_count_up", "sq-icon-counter_reset", "sq-icon-counter_stop", "sq-icon-cross",
			"sq-icon-cross_octagon", "sq-icon-cross_octagon_fram", "sq-icon-cross_shield", "sq-icon-cross_shield_2",
			"sq-icon-crown", "sq-icon-crown_bronze", "sq-icon-crown_silver", "sq-icon-css", "sq-icon-cursor",
			"sq-icon-cut", "sq-icon-dashboard", "sq-icon-data", "sq-icon-database", "sq-icon-databases",
			"sq-icon-def_cat_business", "sq-icon-def_cat_ergonomic", "sq-icon-def_cat_functional",
			"sq-icon-def_cat_noicon", "sq-icon-def_cat_non-functional", "sq-icon-def_cat_performance",
			"sq-icon-def_cat_security", "sq-icon-def_cat_technic", "sq-icon-def_cat_test-requirement",
			"sq-icon-def_cat_undefined", "sq-icon-def_cat_use-case", "sq-icon-def_cat_user-story", "sq-icon-delete",
			"sq-icon-delivery", "sq-icon-desktop", "sq-icon-desktop_empty", "sq-icon-direction", "sq-icon-disconnect",
			"sq-icon-disk", "sq-icon-doc_access", "sq-icon-doc_break", "sq-icon-doc_convert", "sq-icon-doc_excel_csv",
			"sq-icon-doc_excel_table", "sq-icon-doc_film", "sq-icon-doc_illustrator", "sq-icon-doc_music",
			"sq-icon-doc_music_playlist", "sq-icon-doc_offlice", "sq-icon-doc_page", "sq-icon-doc_page_previous",
			"sq-icon-doc_pdf", "sq-icon-doc_photoshop", "sq-icon-doc_resize", "sq-icon-doc_resize_actual",
			"sq-icon-doc_shred", "sq-icon-doc_stand", "sq-icon-doc_table", "sq-icon-doc_tag", "sq-icon-doc_text_image",
			"sq-icon-door", "sq-icon-door_in", "sq-icon-drawer", "sq-icon-drink", "sq-icon-drink_empty",
			"sq-icon-drive", "sq-icon-drive_burn", "sq-icon-drive_cd", "sq-icon-drive_cd_empty",
			"sq-icon-drive_delete", "sq-icon-drive_disk", "sq-icon-drive_error", "sq-icon-drive_go",
			"sq-icon-drive_link", "sq-icon-drive_network", "sq-icon-drive_rename", "sq-icon-dvd", "sq-icon-email",
			"sq-icon-email_open", "sq-icon-email_open_image", "sq-icon-emoticon_evilgrin", "sq-icon-emoticon_grin",
			"sq-icon-emoticon_happy", "sq-icon-emoticon_smile", "sq-icon-emoticon_surprised",
			"sq-icon-emoticon_tongue", "sq-icon-emoticon_unhappy", "sq-icon-emoticon_waii", "sq-icon-emoticon_wink",
			"sq-icon-envelope", "sq-icon-envelope_2", "sq-icon-error", "sq-icon-exclamation",
			"sq-icon-exclamation_octagon_fram", "sq-icon-eye", "sq-icon-feed", "sq-icon-feed_ballon",
			"sq-icon-feed_document", "sq-icon-female", "sq-icon-film", "sq-icon-films", "sq-icon-find",
			"sq-icon-flag_blue", "sq-icon-folder", "sq-icon-font", "sq-icon-funnel", "sq-icon-grid",
			"sq-icon-grid_dot", "sq-icon-group", "sq-icon-hammer", "sq-icon-hammer_screwdriver", "sq-icon-hand",
			"sq-icon-hand_point", "sq-icon-heart", "sq-icon-heart_break", "sq-icon-heart_empty", "sq-icon-heart_half",
			"sq-icon-heart_small", "sq-icon-help", "sq-icon-highlighter", "sq-icon-house", "sq-icon-html",
			"sq-icon-images", "sq-icon-image_1", "sq-icon-image_2", "sq-icon-inbox", "sq-icon-ipod",
			"sq-icon-ipod_cast", "sq-icon-joystick", "sq-icon-key", "sq-icon-keyboard", "sq-icon-layers",
			"sq-icon-layer_treansparent", "sq-icon-layout", "sq-icon-layout_header_footer_3",
			"sq-icon-layout_header_footer_3_mix", "sq-icon-layout_join", "sq-icon-layout_join_vertical",
			"sq-icon-layout_select", "sq-icon-layout_select_content", "sq-icon-layout_select_footer",
			"sq-icon-layout_select_sidebar", "sq-icon-layout_split", "sq-icon-layout_split_vertical",
			"sq-icon-lifebuoy", "sq-icon-lightbulb", "sq-icon-lightbulb_off", "sq-icon-lightning", "sq-icon-link",
			"sq-icon-link_break", "sq-icon-lock", "sq-icon-lock_unlock", "sq-icon-magnet", "sq-icon-magnifier",
			"sq-icon-magnifier_zoom_in", "sq-icon-male", "sq-icon-map", "sq-icon-marker", "sq-icon-medal_bronze_1",
			"sq-icon-medal_gold_1", "sq-icon-media_player_small_blue", "sq-icon-microphone", "sq-icon-mobile_phone",
			"sq-icon-money", "sq-icon-money_dollar", "sq-icon-money_euro", "sq-icon-money_pound", "sq-icon-money_yen",
			"sq-icon-monitor", "sq-icon-mouse", "sq-icon-music", "sq-icon-music_beam", "sq-icon-neutral",
			"sq-icon-new", "sq-icon-newspaper", "sq-icon-note", "sq-icon-nuclear", "sq-icon-package", "sq-icon-page",
			"sq-icon-page_2", "sq-icon-page_2_copy", "sq-icon-page_code", "sq-icon-page_copy", "sq-icon-page_excel",
			"sq-icon-page_lightning", "sq-icon-page_paste", "sq-icon-page_red", "sq-icon-page_refresh",
			"sq-icon-page_save", "sq-icon-page_white_cplusplus", "sq-icon-page_white_csharp", "sq-icon-page_white_cup",
			"sq-icon-page_white_database", "sq-icon-page_white_delete", "sq-icon-page_white_dvd",
			"sq-icon-page_white_edit", "sq-icon-page_white_error", "sq-icon-page_white_excel",
			"sq-icon-page_white_find", "sq-icon-page_white_flash", "sq-icon-page_white_freehand",
			"sq-icon-page_white_gear", "sq-icon-page_white_get", "sq-icon-page_white_paintbrush",
			"sq-icon-page_white_paste", "sq-icon-page_white_php", "sq-icon-page_white_picture",
			"sq-icon-page_white_powerpoint", "sq-icon-page_white_put", "sq-icon-page_white_ruby",
			"sq-icon-page_white_stack", "sq-icon-page_white_star", "sq-icon-page_white_swoosh",
			"sq-icon-page_white_text", "sq-icon-page_white_text_width", "sq-icon-page_white_tux",
			"sq-icon-page_white_vector", "sq-icon-page_white_visualstudio", "sq-icon-page_white_width",
			"sq-icon-page_white_word", "sq-icon-page_white_world", "sq-icon-page_white_wrench",
			"sq-icon-page_white_zip", "sq-icon-paintbrush", "sq-icon-paintcan", "sq-icon-palette", "sq-icon-paper_bag",
			"sq-icon-paste_plain", "sq-icon-paste_word", "sq-icon-pencil", "sq-icon-photo", "sq-icon-photos",
			"sq-icon-photo_album", "sq-icon-piano", "sq-icon-picture", "sq-icon-pilcrow", "sq-icon-pill",
			"sq-icon-pin", "sq-icon-pipette", "sq-icon-plaing_card", "sq-icon-plug", "sq-icon-plugin",
			"sq-icon-printer", "sq-icon-projection_screen", "sq-icon-projection_screen_present", "sq-icon-rainbow",
			"sq-icon-report", "sq-icon-rocket", "sq-icon-rosette", "sq-icon-rss", "sq-icon-ruby", "sq-icon-ruler_1",
			"sq-icon-ruler_2", "sq-icon-ruler_crop", "sq-icon-ruler_triangle", "sq-icon-safe", "sq-icon-script",
			"sq-icon-selection", "sq-icon-selection_select", "sq-icon-server", "sq-icon-shading",
			"sq-icon-shape_aling_bottom", "sq-icon-shape_aling_center", "sq-icon-shape_aling_left",
			"sq-icon-shape_aling_middle", "sq-icon-shape_aling_right", "sq-icon-shape_aling_top",
			"sq-icon-shape_flip_horizontal", "sq-icon-shape_flip_vertical", "sq-icon-shape_group",
			"sq-icon-shape_handles", "sq-icon-shape_move_back", "sq-icon-shape_move_backwards",
			"sq-icon-shape_move_forwards", "sq-icon-shape_move_front", "sq-icon-shape_square", "sq-icon-shield",
			"sq-icon-sitemap", "sq-icon-slide", "sq-icon-slides", "sq-icon-slides_stack", "sq-icon-smiley_confuse",
			"sq-icon-smiley_cool", "sq-icon-smiley_cry", "sq-icon-smiley_fat", "sq-icon-smiley_mad",
			"sq-icon-smiley_red", "sq-icon-smiley_roll", "sq-icon-smiley_slim", "sq-icon-smiley_yell",
			"sq-icon-socket", "sq-icon-sockets", "sq-icon-sort", "sq-icon-sort_alphabet", "sq-icon-sort_date",
			"sq-icon-sort_disable", "sq-icon-sort_number", "sq-icon-sort_price", "sq-icon-sort_quantity",
			"sq-icon-sort_rating", "sq-icon-sound", "sq-icon-sound_note", "sq-icon-spellcheck", "sq-icon-sport_8ball",
			"sq-icon-sport_basketball", "sq-icon-sport_football", "sq-icon-sport_golf", "sq-icon-sport_raquet",
			"sq-icon-sport_shuttlecock", "sq-icon-sport_soccer", "sq-icon-sport_tennis", "sq-icon-stamp",
			"sq-icon-star_1", "sq-icon-star_2", "sq-icon-status_online", "sq-icon-stop", "sq-icon-style",
			"sq-icon-sum", "sq-icon-sum_2", "sq-icon-switch", "sq-icon-tab", "sq-icon-table", "sq-icon-tag",
			"sq-icon-tag_blue", "sq-icon-target", "sq-icon-telephone", "sq-icon-television", "sq-icon-textfield",
			"sq-icon-textfield_rename", "sq-icon-text_align_center", "sq-icon-text_align_justify",
			"sq-icon-text_align_left", "sq-icon-text_align_right", "sq-icon-text_allcaps", "sq-icon-text_bold",
			"sq-icon-text_columns", "sq-icon-text_dropcaps", "sq-icon-text_heading_1", "sq-icon-text_horizontalrule",
			"sq-icon-text_indent", "sq-icon-text_indent_remove", "sq-icon-text_italic", "sq-icon-text_kerning",
			"sq-icon-text_letterspacing", "sq-icon-text_letter_omega", "sq-icon-text_linespacing",
			"sq-icon-text_list_bullets", "sq-icon-text_list_numbers", "sq-icon-text_lowercase",
			"sq-icon-text_padding_bottom", "sq-icon-text_padding_left", "sq-icon-text_padding_right",
			"sq-icon-text_padding_top", "sq-icon-text_signature", "sq-icon-text_smallcaps",
			"sq-icon-text_strikethrough", "sq-icon-text_subscript", "sq-icon-ticket", "sq-icon-timeline_marker",
			"sq-icon-traffic", "sq-icon-transmit", "sq-icon-trophy", "sq-icon-trophy_bronze", "sq-icon-trophy_silver",
			"sq-icon-ui_combo_box", "sq-icon-ui_saccordion", "sq-icon-ui_slider_1", "sq-icon-ui_slider_2",
			"sq-icon-ui_tab_bottom", "sq-icon-ui_tab_content", "sq-icon-ui_tab_disable", "sq-icon-ui_tab_side",
			"sq-icon-ui_text_field_hidden", "sq-icon-ui_text_field_password", "sq-icon-umbrella", "sq-icon-user",
			"sq-icon-user_black_female", "sq-icon-user_business", "sq-icon-user_business_boss", "sq-icon-user_female",
			"sq-icon-user_silhouette", "sq-icon-user_thief", "sq-icon-user_thief_baldie", "sq-icon-vcard",
			"sq-icon-vector", "sq-icon-wait", "sq-icon-wall", "sq-icon-wall_break", "sq-icon-wall_brick",
			"sq-icon-wall_disable", "sq-icon-wand", "sq-icon-weather_clouds", "sq-icon-weather_cloudy",
			"sq-icon-weather_lightning", "sq-icon-weather_rain", "sq-icon-weather_snow", "sq-icon-weather_sun",
			"sq-icon-webcam", "sq-icon-world", "sq-icon-zone", "sq-icon-zones", "sq-icon-zone_money");

	private IconLibrary() {}

	public static List<String> getIconNames() {
		return ICON_NAMES;
	}
}
