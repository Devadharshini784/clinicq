import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';

export default function useLiveQueue(doctorId) {
  const [snapshot, setSnapshot] = useState(null);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!doctorId) {
      setSnapshot(null);
      setConnected(false);
      return;
    }

    let active = true;

    const client = new Client({
      brokerURL: `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws`,
reconnectDelay: 5000,

    onConnect: () => {
    if (!active) return;

    setConnected(true);
    setError('');

    client.subscribe(`/topic/queue/${doctorId}`, (message) => {
        if (!active) return;

        try {
            setSnapshot(JSON.parse(message.body));
        } catch {
            setError('Received an invalid queue update.');
        }
    });
},

    onDisconnect: () => {
    if (active) setConnected(false);
},

    onWebSocketClose: () => {
    if (active) setConnected(false);
},

    onStompError: () => {
    if (active) {
        setConnected(false);
        setError('Unable to receive live queue updates.');
    }
},
});

client.activate();

return () => {
    active = false;
    setConnected(false);
    client.deactivate();
};
}, [doctorId]);

return { snapshot, connected, error };
}
