# MeasureUp
## Checkout Instructions
Our Iteration 2 code can be viewed and downloaded [here](https://github.com/CS506-MeasureUp/MeasureUp/releases/tag/itr2)

## Setting Up MeasureUp
Since our project is an Android Studio project, the code will best be tested within that IDE.
Download the .zip of our project and within Android Studio choose to import a project and point to Android Studio to our code. The device or emulator running our program will need to support ARCore, if using an emulator, the apk of ARCore for emulators will need to be downloading. 
Consult the following link for assistance on running our app on an emulator: https://developers.google.com/ar/develop/java/emulator

## Using MeasureUp
Once the app is running, it will ask for permission to access the camera and filesystem. Agree to both requests in order to properly test our program.
Click the red record button to start the recording process, the circle will turn into a rounded rectangle to denote that the camera is currently capturing. When capturing, make sure you move to the left or right, keeping the camera in the rotation it was in when you began capturing. After you have moved a sufficient distance, tap the red rounded rectangle to stop the capture.
After the capture is complete, you will see a dialog asking you to name the object, click save, and then yes when the dialog asks you if you want to measure an object.

Next, after some time when the video is being processed, the point selection screen will be shown. Drag the two points to the start and end of the edge you would like to measure.
After you are done, click measure, and a dialog should display the measurment.
