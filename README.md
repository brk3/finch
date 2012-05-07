Overview
--------
Finch is a functional open source Twitter app for Android.  It is "4.0 themed"
but should run on any device 2.2 and up.

Working:
* OAuth login
* Home timeline
* Connections timeline
* Timeline caching with scroll position saving
* Profile viewing
* Favoriting tweets

Needs work:
* Messaging
* Notifications
* Inline photo viewing support
* Tweet entities (photo, location, etc)
* Lots more

Will happily accept pull requests as long as they match the existing code
style.

Setup
-----
I use ant and the Android cli tools exlcusively.  If you would like to add
equivalent instructions on building with Eclipse please send a pull request.
```bash
    git clone git@github.com:brk3/finch.git
    cd finch
    android update lib-project -p \
    libs/JakeWharton-Android-ViewPagerIndicator-f09acb0/library/ \
    -t android-4
    android update lib-project -p \
    libs/JakeWharton-ActionBarSherlock-2eabf25/library/ -t android-14
    android update project -p . -t android-15
```

You will also need Twitter consumer keys (see https://dev.twitter.com/), and
add them to [src/com/bourke/finch/common/Constants.java](https://github.com/brk3/finch/blob/master/src/com/bourke/finch/common/Constants.java).

Building
--------
I recommend checking out "debug.sh", which is simple wrapper for building and
deploying Android apps using Ant:
```bash
    git clone git@github.com:brk3/debug.git
    cp debug/debug.sh finch/
    ./debug.sh -bira MainActivity
```

This will build, install, and run Finch on your connected device or emulator.

You can also just use 'ant debug'.

Screenshots:
------------
https://github.com/brk3/finch/wiki/Screenshots

Dependencies
------------
For simplicity I have decided to track dependencies manually rather than via
git submodules.

* DiskLruCache 1.1.0
  https://github.com/JakeWharton/DiskLruCache

* HttpResponseCache 1.0.0
  https://github.com/candrews/HttpResponseCache

* ActionBarSherlock 4.0.2
  https://github.com/JakeWharton/ActionBarSherlock

* Android-ViewPagerIndicator f09acb0
  https://github.com/JakeWharton/Android-ViewPagerIndicator

* Twitter4J 2.2.5
  http://twitter4j.org/en/index.html
