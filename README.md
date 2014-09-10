#Play Store Augmented Reality

An augmented reality demo app for Android that uses my play store listing
as a 3D model: [YouTube video](https://www.youtube.com/watch?v=eC1X9mAyZ0s)

##What's going on here

The video shows you a Nexus 7 tablet being filmed by a Moto G phone running
this app.  The screen on the Nexus 7 is being overdrawn in real time with
a 3D rendered model of a Google Play listing page that has the paper
components split into layers along the z axis.

So when the target device is rotated, you see the scene from a different
viewpoint.  The augmented reality routines make sure this generated scene
is always attached to the right place on the target.

The effect is that the display on the Nexus 7 appears to have genuine
depth, something not possible in real life of course.

In the real Play Store app, the effect of depth is simulated by a
parallax scrolling effect, where paper nearer the camera scrolls more
quickly that the background.

##How does it work?

On starting the app, you will see a preview movie from the device camera.
You'll need to take a photo of whatever you wish to use as the target,
any flat surface with reasonable detail will do.  Could be a book cover or
the screen of an Android device,for example.  The important thing is that
whatever you choose must be flat, the tracker doesn't work very well
otherwise.

There's a camera button in the bottom-right corner of the screen that takes
a snapshot.  Then you switch into tracking mode, where the OpenGL-rendered
scene is overlaid on your target image.

My app uses Qualcomm's [Vuforia](https://www.vuforia.com/) library to track
the target image.  This provides a projection matrix and modelview matrix
that are used to set up the graphics routines for rendering an overlay.

I'm not allowed to distribute the Vuforia library in the source code, but if
you want to try the app you can check the releases directory in this repo
for an APK that can be side-loaded onto your device.

If you want to build the source, you'll need to go the Vuforia website and
sign up for a developer account.  Then you can add the libraries to this
project---set up QCAR_SDK_ROOT as it tells you to, and drop the libVuforia.so
file into libs/armeabi-v7a/.  You don't need the Android NDK to build this.

On the rendering side, I have provided a GLViewSurface that first draws the
camera preview, delivered by Vuforia, in the background and then overlays
several textured polygons to represent the Play Store paper elements.  This
should be familiar to anybody who understands the way OpenGL ES2 works on
Android.

As you rotate the target in front of the camera, you should see a rendering
of the Play screen that seems to have proper depth and three-dimensionality.
Shame we can't do this for real!
