/*
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
define(["testJasmine/Player", "testJasmine/Song", "specs/helper/SpecHelper" ], function(Player, Song, helper) {
	describe("Player", function() {
		var player;
		var song;

		beforeEach(function() {
			player = new Player();
			song = new Song();
		});

		it("should be able to play a Song", function() {
			player.play(song);
			expect(player.currentlyPlayingSong).toEqual(song);

			// demonstrates use of custom matcher
			expect(player).toBePlaying(song);
		});

		describe("when song has been paused", function() {
			beforeEach(function() {
				player.play(song);
				player.pause();
			});

			it("should indicate that the song is currently paused", function() {
				expect(player.isPlaying).toBeFalsy();

				// demonstrates use of 'not' with a custom matcher
				expect(player).not.toBePlaying(song);
			});

			it("should be possible to resume", function() {
				player.resume();
				expect(player.isPlaying).toBeTruthy();
				expect(player.currentlyPlayingSong).toEqual(song);
			});
		});

		// demonstrates use of spies to intercept and test method calls
		it("tells the current song if the user has made it a favorite", function() {
			spyOn(song, 'persistFavoriteStatus');

			player.play(song);
			player.makeFavorite();

			expect(song.persistFavoriteStatus).toHaveBeenCalledWith(true);
		});

		// demonstrates use of expected exceptions
		describe("#resume", function() {
			it("should throw an exception if song is already playing", function() {
				player.play(song);

				expect(function() {
					player.resume();
				}).toThrowError("song is already playing");
			});
		});
	});
});
