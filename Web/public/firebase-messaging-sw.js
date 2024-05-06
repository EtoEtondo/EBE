// Scripts for firebase and firebase messaging
importScripts('https://www.gstatic.com/firebasejs/8.2.0/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/8.2.0/firebase-messaging.js');
importScripts('https://www.gstatic.com/firebasejs/8.2.0/firebase-database.js');

// Initialize the Firebase app in the service worker by passing the generated config
const firebaseConfig = {
  apiKey: "...",
  authDomain: "schoolio.firebaseapp.com",
  projectId: "schoolio",
  storageBucket: "schoolio.appspot.com",
  messagingSenderId: "...",
  appId: "...",
  databaseURL: "https://schoolio.firebasedatabase.app/",
  measurementId: "..."
};

firebase.initializeApp(firebaseConfig);

// Retrieve firebase messaging
const messaging = firebase.messaging();
const database = firebase.database();

messaging.onBackgroundMessage(function(payload) {
  console.log('Received background message ', payload);
 // Customize notification here
  const notificationTitle = payload.notification.title;
  const notificationOptions = {
    body: payload.notification.body,
  };

  self.registration.showNotification(notificationTitle,
    notificationOptions);
});