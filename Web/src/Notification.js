import React, {useState, useEffect} from 'react';
import toast, { Toaster } from 'react-hot-toast';
import { requestForToken, onMessageListener } from './util/firebase';

const Notification = () => {
  const [notification, setNotification] = useState({title: '', body: ''});
  
  const notify = () =>  toast(<ToastDisplay/>);
  function ToastDisplay() {
    return (
      <div>
        <p><b>{notification?.title}</b></p>
        <p>{notification?.body}</p>
      </div>
    );
  };

// if there is a notification call the output function notify
  useEffect(() => {
    if (notification?.title ){
     notify()
    }
  }, [notification])
  
// get valid token
  requestForToken();

// listening to message and set the notification
  onMessageListener()
    .then((payload) => {
      setNotification({title: payload?.notification?.title, body: payload?.notification?.body});     
    })
    .catch((err) => console.log('failed: ', err));

  return (
     <Toaster/>
  )
}

export default Notification;