E-Leap – Final Year Project @ TU Dublin
Demo Mobile Application for paying Bus fares with the help of NFC technology
The aim of this project is to make an application that helps users to pay for their Bus fares from their Android devices. 
Users can make 2 types of accounts, a “Student” account and “Adult” account. Depending on the type of account, a certain fixed amount of payment will be deducted from their account. The users can also top up their account, which will be implemented through Stripe payment system. It will also include features where the users are able to see the route information upon giving a starting location and an ending location. The E-Leap application aims to utilize the current Android technology in combination with Near field communication (NFC) chips to make it easier for users to pay for their fares.

Technologies:
Android Studio/JAVA
Firebase
Stripe API
Google MAps API
Public HERE API
NFC Tag/ NFC card reader in Android devices

NFC Tag on Functionality
![image](https://user-images.githubusercontent.com/56804514/234758656-baf571fb-1577-4c42-a215-ee55caa53abd.png)

Figure depicts the sequence diagram for the Tag on Functionality and shows how user, mobile application, Firebase, and the NFC Card/Tag interact when the user taps on an NFC tag with their Android device.
This feature allows the User to tap a preconfigured NFC tag and pay for their fares through their Android application. 
When the Android application is launched and the user taps the NFC Tag, it will check for its content. The application will check if the NDEF message/content of the NFC tag matches the BUS agency which can be Bus Éireann, Dublin bus based on that the Front-end will connect to the Firebase and update Firestore Database accordingly, which will be then sent back from Firebase to the Front-End for the user to see.

Top-Up Functionality
![image](https://user-images.githubusercontent.com/56804514/234758969-662de89c-a571-4f8e-8d43-90b9b24ed00b.png)

Figure shows the sequence diagram for the top up process and depicts the interaction between the user, the mobile application, the Firebase, and the API during a top up process.
When a user tops up their account the mobile application will connect to the back-end server to get the required API access keys that is needed to connect to the stripe payment. When the application gets back all the required information the application will then create a payment intent and connect to the Stripe Payment Gateway which will send a response to the application if the connection is successful, when the connection is successful the Front-End will then show the payment gateway UI of Stripe where the user will fill up their card details. When the payment is successful the Stripe API will send in a success token to the Front-End, the application will then again connect with Firebase API and request to Firestore Database to update the balance of the user. The Firestore Database will update the user info and the Firebase API will send a successful response back with the user account balance which will be then displayed in the UI.  

Route Functionality
![image](https://user-images.githubusercontent.com/56804514/234759096-32d3a476-1a40-4600-b0f2-636598673ab2.png)

Figure 26 depicts the Sequence diagram for the routing process, this depicts the workflow of how the mobile application, the server and the API will communicate with each other.
User can see the numbers of the Russian Armed Forces and the so-called DPR/LPR Militia losses within the application in case they are cut off from news outlets and media in Ukraine and it can also act as a morale boost. The app updates statistics through an API. The data from the API is cached, so that information remains accessible for some time after the last successful API request even when there is no internet connection.
This functionality is used to search for routes and the live update of the time on each stop. The user will search will be able to search for a starting and ending destination name and based on that the user will get the route details. 
The user will provide the starting and ending destination in the application. Firstly, the Android application will connect to the backend server to get the necessary API keys for HERE API and Google Maps, then the application will send in the original and destination name to the Google maps API to get the Longitude and Latitude of the location, which will be then passed again to the backend server. Now the backend server will get these longitude and latitude positions and call the public routing HERE API which will respond with the route details in the form of JSON which will be then sent to the android application, the android application will parse this JSON information and show the required details in the Google Map.

Login/Registration
![image](https://user-images.githubusercontent.com/56804514/234759233-004963e8-cb11-46a7-9bb6-fbf97e49ca76.png)

Figure depicts the sequence diagram for the LogIn/Registration Functionality and shows how user, mobile application, and  Firebase interact when the user either Sign In or Register.
User Authentication.
For login, new user, and authentication all those tasks will be carried out by the built-in authentication process and database that the Firebase integrated on Android Studio.
The User will provide their email address, and password to the Front-end which will then establish a connection to the Firebase authentication system which will then check if the email is verified and if so secure, and login based on the email and password. 
The same data flow will happen when a user tries to create a new account but when the registration is completed the Firebase authentication backend will connect to the Firestore database and add the user details and send in a verification email to the email address provided by the user to verify their account. 

Firebase is used for User Authentication. When user opens the application for the first time, they are required to register. They need to fill in their details,then user profile is created in Firebase.
Also Email verfication, Profile Update and Password change has been implemented in a similar manner.

Future Plans
Secure NFC payload. 
Secure Backend Connection.
Better Overall UI 
Fix the Route page to show the Route info  in a better way.
More Testing
