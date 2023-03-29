# xilfton

This is an Android Webview app for the Xilfton media streamer PWA.
It just loads the Xilfton webinterface like a browser would.

## Configuration.

The app has a builtin 'bootmenu' like chooser to choose between
different PWA servers. If no default has been set yet, the menu
will be shown - otherwise the default entry will be loaded.

To get to the menu from the Xilfton app, press the "back" button
six times in rapid succession (within one second).

## BUILDING

- First run "quasar build" in the ui/ directory
- Then in Android Studio run the app, or build an APK

## TODO

### 5.1 Surround Sound.

Unfortunately on the Chromecast with Google TV, the webapp cannot
output 5.1 AC-3 audio. The same device used as a Chromecast
receiver _does_ output 5.1 audio..

We need to investigate if this can be solved. I spent a whole saturday
afternoon googling the Internet, but it appears that nobody really
has this specific problem. It's probably just a limitation of the
webview. I tested the "TV Bro" browser, it has the same problem.

A solution would be to use Exoplayer from the webview.

https://stackoverflow.com/questions/62040840/android-can-i-set-another-video-player-to-be-used-by-video-tag
https://ionicframework.com/docs/v5/native/android-exoplayer
https://github.com/cprcrack/VideoEnabledWebView

Exoplayer docs:
https://exoplayer.dev/

Media 3 exoplayer docs:
https://developer.android.com/guide/topics/media/exoplayer
