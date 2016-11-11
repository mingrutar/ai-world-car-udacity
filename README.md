## ![logo](https://github.com/wfs/ai-world-car-team-c4/blob/master/app/src/main/res/mipmap-mdpi/ic_launcher.png?raw=true) World Car ##

### Work of ai-world-car-team-c4 ###
Global team approach to solving Self-Driving Car (SDC) challenges. Prize money donated to UNICEF.

### Android studio ###
* the app is created with Android Studio 2.1.3 and gradle version 2.2.2
* the minimum Android SDK version is 21 (because my 7" tablet is at that version). If you like to change the minimum version, change the gradle file app/build.gradle 'minSdkVersion' to the version you like. Keep in mind that you may need to fix version related issues.  

### Android studio ###
* obtain an API key from Mapbox
* create a file 'gradle.properties' at root directory.
* add line 'theMapboxApiKeyProp=you-mapbox-api-key'. The Mapbox API key is very long, mine start with 'pk.'.
* add 'gradle.properties' to your .gitignore, so your API key will not be checked in into the repository.

### Run the app ###
* when app starts, the car is at your current location. If you compare the screenshots of this check-in to the ones * from last check-in, you will find the car is at a different location because I had moved - still unpacking.
to set destination, long click the location on the map. A route will shown.
* click the fab button (the big red button), the car will move. I modified Mapbox simulator code. In the real world, we should get location from the car GPS (not coded yet). The drawing should be similar.
* when the car reaches the destination, you can set another destination. (the code is pieced together with Mapbox sample code, it is not robust.)     

### TODOs: ###
1. add notification when the car reach the destination
2. My idea is that the map and video views can interchange places. So the rider can watch a video in the big screen and the small window display the car moving during a ride. The views of these windows are in fragments, so it is easy to implement.
3. implement the video ( optional music and radio) function.
4. let user to type in destination address.   

#### Autoware the Big Challenges ####
1. connect to Autoware.  
2. The car icon moves according to car GPS. Questions:
   a) Is it possible to set destination to car? if yes (guess yes), how?
   b) How can we test the code? The Autoware simulator bag I have is at Japan, I am not familiar with that area to set up a route. My computer cannot digest 3 hours bag.
3. change the speed value (the big 20 at upper-right corner) according to car feeder.
4. show the driving mode: Auto (on the screenshot), Manual, or Pursuit( don't really know what that is)
5. show the gear position. Currently it is a static image. We need change it.
6. steering wheel angel (not quite know where to obtain that data), so rotate the steering wheel accordingly.

``` Any one has idea, please let the team know. ```

### Screenshots ###

The portrait layout

![portrait](https://github.com/wfs/ai-world-car-team-c4/blob/master/screenshots/portrait.png?raw=true)

The landscape layout

![landscape](https://github.com/wfs/ai-world-car-team-c4/blob/master/screenshots/landscape.png?raw=true)

Click the Switching button to make video in main window

![videoMain] (https://github.com/wfs/ai-world-car-team-c4/blob/master/screenshots/play_video_inmain.png?raw=true)

Or Map is in the main window

![carmoving](https://github.com/wfs/ai-world-car-team-c4/blob/master/screenshots/play_video_inside.png?raw=true)
