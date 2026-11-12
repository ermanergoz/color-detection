# Color Detection

> **Note:** This project was built in late 2020 as a prototype for a larger project.

A small Android app that tells you the color of anything you point the camera at.

The screen shows the live camera feed. Tap anywhere on it and two labels at the top show where
you tapped in the camera frame and the color there as a hex code, such as `#3A7F22`. Both labels
are redrawn in that color, so you can compare it with the real thing.

## What color detection is for

Reading the color at one point of a camera image is one of the most basic things computer vision
does. It is useful on its own and as a building block for bigger tasks:

* **Matching paint, fabric and makeup:** point the camera at a wall, a shirt or a sample card and
  get a code you can take to a shop or a design program.
* **Design work:** pick a color from a real object and use it in a logo, a website or an app.
* **Accessibility:** people with color vision deficiency can check whether something is red or
  green, or whether two things are the same color.
* **Inspection and sorting:** a camera checks the color of fruit, painted parts or wires to decide
  whether they are ripe, correct or in the right place.
* **Object tracking:** the color picked at one point can become the target for following an
  object of that color through the video.

This app shows the idea with a phone camera.

## How it works

The app uses OpenCV 4.5.0 for Android. OpenCV's camera view passes every frame to the app as an
RGBA image and draws it on the screen, scaled to fit and centered.

When you tap the screen, the app:

* converts the tap position on the screen to a pixel position in the camera frame by undoing
  that scaling and centering,
* takes the 8 by 8 square of pixels around that point,
* averages their red, green and blue values, which smooths out camera noise,
* shows the average as a hex code and uses it as the label color.
