// Firebase конфігурація (Compat версія для сумісності з index.html)
const firebaseConfig = {
  apiKey: "AIzaSyC0KUdpK89pF3pZCsd0fXgMIo0P4oQ7H_I",
  authDomain: "sport-tracker-6011e.firebaseapp.com",
  projectId: "sport-tracker-6011e",
  storageBucket: "sport-tracker-6011e.firebasestorage.app",
  messagingSenderId: "599835124344",
  appId: "1:599835124344:web:3198b5dbede6480727c38d",
  measurementId: "G-2R26DDSCLV"
};

// Ініціалізація Firebase (Compat версія)
firebase.initializeApp(firebaseConfig);

const auth = firebase.auth();
const db = firebase.firestore();