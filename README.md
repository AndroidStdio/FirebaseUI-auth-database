# FirebaseUI-auth-database

FirebaseUI-auth-database is my own work and it is based on FirebaseUI-Android which you can check on https://github.com/firebase/FirebaseUI-Android. They generally work the same way, but FirebaseUI-auth-database has some differences from the original one. It can be useful for beginners who wants to learn Firebase.

FirebaseUI-auth-database includes;
  * Email/Password Login, 
  * Google Login,
  * Email Verification(by using reset password token.When I finished this work, there isn't email verification for Android.),
  * Navigation Drawer,
  * Firebase Realtime Database.
   
![firebaseUI-new](http://i63.tinypic.com/23r1x8l.jpg)![firebaseUI-eV](http://i66.tinypic.com/k269t.png)![firebaseUI-eV2](http://i64.tinypic.com/24bw3k2.png)
   
   ![firebaseUI-ND](http://i66.tinypic.com/j909ap.png) ![firebaseUI-db](http://i65.tinypic.com/2evql9k.png)
   
   
I tried to add Java servlets running in the Google App Engine flexible environment for FirebaseUI-auth-database as a backend service by using the example on https://cloud.google.com/solutions/mobile/mobile-firebase-app-engine-flexible. However, Despite all of my efforts, backend service didn't work properly. I sent an email to Firebase support, you can see their reply in the following picture.

   ![firebaseUI-backend](http://i68.tinypic.com/2a5btxg.jpg)

I don't know whether Google App Engine still have problem or not, but you can try your chance.

# Setup
- 1.Create a Firebase account or log into an existing account.
- 2.Click "Create new project".
- 3.In Project name, enter: FirebaseUI-auth-database
- 4-Select "Add Firebase to your Android App".
- 5-In Package name, enter: com.example.firebaseUI
- 6-Enter the SHA-1 value you generated.
		- 6.1-Open Android Studio
		- 6.2-Open your Project
	   - 6.3-Click on Gradle (From Right Side Panel, you will see Gradle Bar)
      - 6.4-Click on Refresh (Click on Refresh from Gradle Bar, you will see List Gradle scripts of your Project)
      - 6.5-Click on Your Project (Your Project Name form List (root))
      - 6.6-Click on Tasks
      - 6.7-Click on Android
      - 6.8-Double Click on signingReport (You will get SHA1 and MD5 in Run Bar)
      - 6.9-Then click this button: ![button](https://i.stack.imgur.com/Wk5Tm.png) (top left of the error log)
- 7-Select Add app and download the generated file, google-services.json.
- 8-From the left menu of the Firebase console, select Authentication.
- 9-Select Sign-in method.
- 10-Select Email/Password, turn the Enable toggle on, and click Save
- 11-Select Google, turn the Enable toggle on, and click Save.
