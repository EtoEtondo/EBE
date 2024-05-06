// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getMessaging, getToken, onMessage } from "firebase/messaging";
import { getDatabase, ref, onValue } from "firebase/database";

// Firebase configuration
const firebaseConfig = {
  apiKey: "...",
  authDomain: "schoolio.firebaseapp.com",
  projectId: "schoolio",
  storageBucket: "schoolio.appspot.com",
  messagingSenderId: "..",
  appId: "...",
  databaseURL: "https://schoolio.firebasedatabase.app/",
  measurementId: "G-R0MQSZEMFP"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// reference to services
const messaging = getMessaging(app);
const database = getDatabase(app);

// get valid Token
export const requestForToken = () => {
  return getToken(messaging, { vapidKey: "..." }) //Firebase Cloud Messaging kann mithilfe von Anwendungsidentitäts-Schlüsselpaaren eine Verbindung zu externen Push-Diensten herstellen
    .then((currentToken) => {
      if (currentToken) {
        console.log('current token for client: ', currentToken);
        // Perform any other neccessary action with the token
      } else {
        // Show permission request UI
        console.log('No registration token available. Request permission to generate one.');
      }
    })
    .catch((err) => {
      console.log('An error occurred while retrieving token. ', err);
    });
};

//FCM Message Listener
export const onMessageListener = () =>
  new Promise((resolve) => {
    onMessage(messaging, (payload) => {
      console.log("payload", payload)
      resolve(payload);
    });
});

//FRD Database Reference
export const myData = ref(database, 'Lobby/Students/');