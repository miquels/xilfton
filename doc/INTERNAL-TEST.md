
# This app is in limited testing on the playstore

If I know you and you send me the email address of the account you're
using to log in on your Android TV device, I can add you to the test.

After you've been added, click in a Chrome browser in a window where you're
also logged in with the same account on
[this Play Store link](https://play.google.com/apps/internaltest/4701183155624139068)
and you'll be able to install the app onto the device.

The android app is basically nothing more than a "bootloader for the web".
It supports multiple server names (basically for development). You
can always get back into the "boot menu" from the app by pressing the
"back" button six times in rapid succession. If the app misbehaves, this
is a straightforward way to reset it, instead of hard stopping it
from android.

The database for "favorites" and "resume / play next" are currently
stored on the device only. I will try to make a migration of the data
to a server-backend possible later on ... but no guarantees. This is
alpha software.

Good luck!
