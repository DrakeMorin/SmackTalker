# SmackTalker
We are creating an Android messaging application that uses Bluetooth to connect to local devices.

Over the course of our Grade 12 semester, we will create a working application suitable for use on multiple Android devices.  We will be coding in Android Studio.

We have chosen to use Bluetooth to make our application unique and independent of Wi-Fi and cellular reception.  Our application will enable users to communicate within a localized area.  By using Bluetooth we hope to create conversation among peers.

Bluetooth is one of the least used modes of communication between devices.  We wish to expand in this small market as this is our group's first application.

The coding for Bluetooth is based on a conversation.  There is an initial greeting, followed by a set of questions between the devices.  If the questions are 'correct' the devices will connect.

Having learned Git and GitHub, we plan to use GitHub to help our collaboration, and track our project over its development. Keeping with that, we will endeavour to make commits based on each logical change.

Note that many commits from April 11 - 13 were attempted fixes at resolving missing code. This issue was eventually resolved.

FEATURES:
-Send and receive messages each containing message, senderID, and time stamp

-Sender image is extrapolated based on the first char of the senderID for a given message

-Bluetooth connections, both secure and insecure types

-All messages stored in mySQL database, each conversation in its own table

-Image support for all possible device screen resolutions

-Dialog for setting userID, optional randomization

-Storing IDs in Device SharedPrefernces which are constant across runtime sessions

-Ability to reset userID on demand

-Tracking messages received that have not been read

-Creation of notifications that when clicked, return you to the app

-Panic mode which will hide all messages from screen

-Our very own unique logo and colour scheme
