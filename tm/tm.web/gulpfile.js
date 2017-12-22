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
var gulp = require('gulp');
var path = require('path');
var less = require('gulp-less');
var concatCss = require('gulp-concat-css');
var csso = require('gulp-csso');
var plumber = require('gulp-plumber');
var rename = require("gulp-rename");
var runSequence = require('run-sequence');
var gulpCopy = require('gulp-copy');
var sprite = require('gulp-sprite-generator');
var merge = require('merge-stream');


var source = './src/main/webapp'; // source directory of web assets in tm.web project
// Maven target directory
// Please note that spring boot is configured to reload from src/webapp/style but it cannot process less sources...
var destination = './target';

function concatFilesNameAndPaths (files,path){
	return files.map(function(fileName){return path + '/' + fileName});
};

function concatFileNameAndPaths (file,destination){
	return (destination + '/' + file);
};

//###################################### PROCESSING STYLES #########################################

var styleSource = source + '/styles'
var wro4jDestination =  destination + '/wro4j-spring-boot'
var styleDestination = wro4jDestination + '/styles'
var styleProdDestination = destination + '/styles'

//LESS PROCESSING
gulp.task('css',['coreCss','themesCss','squashTree','squashCoreOveride','squashSubPageOveride','squashPrint']);
gulp.task('themesCss',['squashBlue','squashGreen','squashGrey','squashPurple','squashWine','squashBlueGreen','squashGreenBlue']);


var squashCoreSources = ['structure.css','ckeditor.override.css','bootstrap.override.css']
var squashCorePaths = concatFilesNameAndPaths(squashCoreSources,styleSource);
gulp.task('coreCss', function () {
  return gulp.src(squashCorePaths)
  	.pipe(plumber())
    .pipe(less())
    .pipe(concatCss("squash.core.css"))
    .pipe(gulp.dest(styleDestination))
});

function makeLessFile(src, targetName){
	var source = concatFileNameAndPaths(src,styleSource);
    console.log('Make Less File');
    console.log(source);
    console.log(styleDestination);
	return gulp.src(source)
    	.pipe(plumber())
      .pipe(less())
      .pipe(rename(targetName))
      .pipe(gulp.dest(styleDestination))
};

var squashThemeSources = ['master.blue.less','master.green.css','master.grey.css','master.purple.css','master.wine.css','master.blue-green.css','master.green-blue.css']

gulp.task('squashBlue', function () {
  return makeLessFile('master.blue.less','squash.blue.css');
});

gulp.task('squashGreen', function () {
  return makeLessFile('master.green.less','squash.green.css');
});

gulp.task('squashGrey', function () {
  return makeLessFile('master.grey.less','squash.grey.css');
});

gulp.task('squashPurple', function () {
  return makeLessFile('master.purple.less','squash.purple.css');
});

gulp.task('squashWine', function () {
  return makeLessFile('master.wine.less','squash.wine.css');
});

gulp.task('squashBlueGreen', function () {
  return makeLessFile('master.blue-green.less','squash.blue-green.css');
});

gulp.task('squashGreenBlue', function () {
  return makeLessFile('master.green-blue.less','squash.green-blue.css');
});

gulp.task('squashTree', function () {
  return makeLessFile('squashtree.css','squash.tree.css');
});

gulp.task('squashCoreOveride', function () {
  return makeLessFile('structure.override.css','squash.core.override.css');
});

gulp.task('squashSubPageOveride', function () {
  return makeLessFile('structure.subpageoverride.css','squash.subpage.override.css');
});

var squashPrintSources = ['master.css','print.css'];
var squashPrintPaths = concatFilesNameAndPaths(squashPrintSources,styleSource);

gulp.task('squashPrint', function () {
	return makeLessFile('print.less','squash.print.css');
});

gulp.task('copyCssToProdDirectory', function () {
	return gulp.src(styleSource + '/**/*')
    	.pipe(plumber())
        .pipe(gulpCopy(styleProdDestination,{prefix: 4}))
});

gulp.task('minifyCss', function () {
	return gulp.src(styleDestination + '/*.css')
    	.pipe(plumber())
        .pipe(csso())
        .pipe(gulp.dest(styleProdDestination))
});



//###################################### /PROCESSING STYLES #########################################






//###################################### PROCESSING ICON IMAGES #####################################

var sourceImages = source + '/images/**/*.png';
var destinationImage = wro4jDestination;
//only spriting the images in /images. We don't want to proccess de Jquery image or worst.. the ugly ckeditor
var sourceImageToBeSprited = [wro4jDestination + '/images/**/*.png',wro4jDestination + '/images/*.png'];
//We exclude the tree css from sprite, because it's already sprited with jsTree.
var treeCss = '!'+ styleDestination + '/squash.tree.css';
var sourceCssToBeSprited = [styleDestination + '/*.css',treeCss];

gulp.task('copyImagesToWro4j', function () {
	return gulp.src(sourceImages)
    	.pipe(plumber())
        .pipe(gulpCopy(wro4jDestination,{prefix: 3}))
});

gulp.task('copyImages', function () {
	return gulp.src(sourceImageToBeSprited)
    	.pipe(plumber())
        .pipe(gulpCopy(destination,{prefix: 2}))
});

gulp.task('sprites',function(){
     var spriteOutput;
	spriteOutput = gulp.src(sourceCssToBeSprited)
		.pipe(sprite({
            spriteSheetName: 'sprite.png',
            spriteSheetPath:'../images',
            accumulate :true,
            filter: [
                function(image) {
                    return !image.meta.skip;
                }
            ],
		}));

    var imgStream = spriteOutput.img
    .pipe(plumber())
    .pipe(gulp.dest(wro4jDestination + '/images'));

    var cssStream = spriteOutput.css
    .pipe(plumber())
    .pipe(gulp.dest(styleDestination));

    return merge(imgStream, cssStream);

});
//###################################### /PROCESSING ICON IMAGES ####################################



//###################################### MAIN BUILD TASK ############################################
//By default perform a full prod build, use dev task to perform a dev build.

// 1. Copy Css to prod directory, as we ned to copy all files for ckeditor, jqueryui... so we take all style directory.
// 2. Perform less build. Input: src/styles -> Output : target/wro4j-spring-boot/styles
// 3. Copy the image to /wro4j-spring-boot/images, ie working directory
// 4. Generate the sprites, inside sprite directory
// 5. Copy images from target/wro4j-spring-boot/images to target/images
// 6. MinifyCss and put result in target/styles, with other asset we copied in step 1
gulp.task('default', function () {
    runSequence('copyCssToProdDirectory','css','copyImagesToWro4j','sprites','copyImages','minifyCss');
});

//###################################### /MAIN BUILD TASK ###########################################

//###################################### WATCHER FOR CSS ############################################
// Watch all the files in style, if change, copy all file to working directory and process them
gulp.task('watch', function() {
	var toWatch =  styleSource + '/*';
    gulp.watch(toWatch, function () {
        runSequence('copyCssToProdDirectory','css');
    });
});
//###################################### /WATCHER FOR CSS ###########################################
