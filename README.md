# xilfton

This is an Android Webview app for the Xilfton media streamer PWA.
It just loads the Xilfton webinterface like a browser would.

## TODO

### Implement URL chooser.

The first page should be local, and ask the user to enter the URL
for their xilfton instance. It should have a 'enable dev options' checkbox,
which would allow self-signed certs when accessing 192.168.0.0/16 or localhost.

Pressing the 'back' button 6 times in rapid succession should unload
force the app to reload the initial page, so that the settings can
be changed, or in the case that the xilfton PWA is not responsive.

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
