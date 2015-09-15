'use strict';

var gulp = require('gulp');
var del = require('del');


var path = require('path');


// Load plugins
var $ = require('gulp-load-plugins')();
var browserify = require('browserify');
var watchify = require('watchify');
var source = require('vinyl-source-stream');

var srcFolder = './src/main/javascript/app',
    distFolder = './target/classes/web/dist',
    destFileName = 'app.js';

var browserSync = require('browser-sync');
var reload = browserSync.reload;

// Styles
gulp.task('styles', ['sass', 'moveCss'  ]);

gulp.task('moveCss',['clean'], function(){
  // the base option sets the relative root for the set of files,
  // preserving the folder structure
  gulp.src(['./src/main/javascript/app/styles/**/*.css'], { base: './src/main/javascript/app/styles/' })
  .pipe(gulp.dest('./target/classes/web/dist/styles'));
});

gulp.task('sass', function() {
    return $.rubySass('./src/main/javascript/app/styles', {
            style: 'expanded',
            precision: 10,
            loadPath: ['./src/main/javascript/app/bower_components']
        })
        .pipe($.autoprefixer('last 1 version'))
        .pipe(gulp.dest('./target/classes/web/dist/styles'))
        .pipe($.size());
});



var bundler = watchify(browserify({
    entries: ['./src/main/javascript/app/scripts/app.js'],
    debug: true,
    insertGlobals: true,
    cache: {},
    packageCache: {},
    fullPaths: true
}));

bundler.on('update', rebundle);
bundler.on('log', $.util.log);

function rebundle() {
    return bundler.bundle()
        // log errors if they happen
        .on('error', $.util.log.bind($.util, 'Browserify Error'))
        .pipe(source(destFileName))
        .pipe(gulp.dest('./target/classes/web/dist/scripts'))
        .on('end', function() {
            reload();
        });
}

// Scripts
gulp.task('scripts', rebundle);

gulp.task('buildScripts', function() {
    return browserify('./src/main/javascript/app/scripts/app.js')
        .bundle()
        .pipe(source(destFileName))
        .pipe(gulp.dest('./target/classes/web/dist/scripts'));
});




// HTML
gulp.task('html', function() {
    return gulp.src('./src/main/javascript/app/*.html')
        .pipe($.useref())
        .pipe(gulp.dest(distFolder))
        .pipe($.size());
});

// Images
gulp.task('images', function() {
    return gulp.src('./src/main/javascript/app/images/**/*')
        .pipe($.cache($.imagemin({
            optimizationLevel: 3,
            progressive: true,
            interlaced: true
        })))
        .pipe(gulp.dest('./target/classes/web/dist/images'))
        .pipe($.size());
});

// Fonts
gulp.task('fonts', function() {
    return gulp.src(require('main-bower-files')({
            filter: '**/*.{eot,svg,ttf,woff,woff2}'
        }).concat('./src/main/javascript/app/fonts/**/*'))
        .pipe(gulp.dest('./target/classes/web/dist/fonts'));
});

// Clean
gulp.task('clean', function(cb) {
    $.cache.clearAll();
    cb(del.sync(['./target/classes/web/dist/styles', './target/classes/web/dist/scripts', './target/classes/web/dist/images']));
});

// Bundle
gulp.task('bundle', ['styles', 'scripts', 'bower'], function() {
    return gulp.src('./src/main/javascript/app/*.html')
        .pipe($.useref.assets())
        .pipe($.useref.restore())
        .pipe($.useref())
        .pipe(gulp.dest(distFolder));
});

gulp.task('buildBundle', ['styles', 'buildScripts', 'moveLibraries', 'bower'], function() {
    return gulp.src('./src/main/javascript/app/*.html')
        .pipe($.useref.assets())
        .pipe($.useref.restore())
        .pipe($.useref())
        .pipe(gulp.dest(distFolder));
});

// Move JS Files and Libraries
gulp.task('moveLibraries',['clean'], function(){
  // the base option sets the relative root for the set of files,
  // preserving the folder structure
  gulp.src(['./src/main/javascript/app/scripts/**/*.js'], { base: './src/main/javascript/app/scripts/' })
  .pipe(gulp.dest('./target/classes/web/dist/scripts'));
});


// Bower helper
gulp.task('bower', function() {
    gulp.src('./src/main/javascript/app/bower_components/**/*.js', {
            base: './src/main/javascript/app/bower_components'
        })
        .pipe(gulp.dest('./target/classes/web/dist/bower_components/'));

});

gulp.task('json', function() {
    gulp.src('./src/main/javascript/app/json/**/*.json', {
            base: './src/main/javascript/app/scripts'
        })
        .pipe(gulp.dest('./target/classes/web/dist/scripts/'));
});

// Robots.txt and favicon.ico
gulp.task('extras', function() {
    return gulp.src(['./src/main/javascript/app/*.txt', './src/main/javascript/app/*.ico'])
        .pipe(gulp.dest(distFolder))
        .pipe($.size());
});

// Watch
gulp.task('watch', ['html', 'fonts', 'bundle'], function() {

    browserSync({
        notify: false,
        logPrefix: 'BS',
        // Run as an https by uncommenting 'https: true'
        // Note: this uses an unsigned certificate which on first access
        //       will present a certificate warning in the browser.
        // https: true,
        server: ['./target/classes/web/dist', './src/main/javascript/app']
    });

    // Watch .json files
    gulp.watch('./src/main/javascript/app/scripts/**/*.json', ['json']);

    // Watch .html files
    gulp.watch('./src/main/javascript/app/*.html', ['html']);

    gulp.watch(['./src/main/javascript/app/styles/**/*.scss', './src/main/javascript/app/styles/**/*.css'], ['styles', 'scripts', reload]);



    // Watch image files
    gulp.watch('./src/main/javascript/app/images/**/*', reload);
});

// Build
gulp.task('build', ['html', 'buildBundle', 'images', 'fonts', 'extras'], function() {
    gulp.src('./target/classes/web/dist/scripts/app.js')
        .pipe($.uglify())
        .pipe($.stripDebug())
        .pipe(gulp.dest('./target/classes/web/dist/scripts'));
});

// Default task
gulp.task('default', ['clean', 'build'  , 'jest'  ]);
