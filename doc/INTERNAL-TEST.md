
# Limited testing edition.

This app is in limited testing on the Google Play Store. That means it's
not publically available yet, only for members of the test group.

If I know you and you send me the email address of the account you're
using to log in to your Android TV device, I can add you to the group.

After you've been added, click on [this Play Store link](https://play.google.com/apps/internaltest/4701183155624139068) and you'll be able to install the app onto the device.
If you're logged in on Chrome with another account, you might need to
switch to the account you use on the Android TV device first.

The android app is really nothing more than a "bootloader for the web".
It just loads the Notflix PWA ("PWA" is Portable Web App) from the website,
and hints it that it needs to support TV style navigation. So at one point
you might see what looks like an updated app, while the android app has not been
updated - the PWA has.

The app supports configurable website names. You won't need this, it's
mostly there for development at this point. However, it's good to know
it's there because you can always get back into the "boot menu" from
the PWA by pressing the "back" button six times in rapid succession.
If the PWA misbehaves, this is a straightforward way to reset it,
instead of hard stopping and restarting it from android.

The database for "favorites" and "resume / play next" are currently
stored on the android device only. I will try to make a migration of the data
to a server backend possible later on ... but no guarantees. This is
alpha software.

Good luck!
