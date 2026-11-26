import { useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import { getStoredAuthToken } from "../services/AuthService";

/**
 * Custom hook for WebSocket farm notifications using STOMP protocol.
 * The backend validates the same Bearer token used by the REST API during STOMP CONNECT.
 */
export function useFarmNotifications(onFarmChanged, options = {}) {
  const {
    wsUrl = "ws://localhost:8080/ws",
    topic = "/topic/farms",
    reconnectDelay = 5000,
    token = getStoredAuthToken(),
  } = options;

  const callbackRef = useRef(onFarmChanged);
  const clientRef = useRef(null);
  const subscriptionRef = useRef(null);
  const isConnectingRef = useRef(false);

  useEffect(() => {
    callbackRef.current = onFarmChanged;
  }, [onFarmChanged]);

  useEffect(() => {
    if (!callbackRef.current) {
      console.warn("useFarmNotifications: onFarmChanged callback not provided");
      return undefined;
    }

    if (!token) {
      console.warn("useFarmNotifications: auth token missing");
      return undefined;
    }

    const connectWebSocket = () => {
      if (isConnectingRef.current || clientRef.current?.active) {
        return;
      }

      isConnectingRef.current = true;

      const client = new Client({
        brokerURL: wsUrl,
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        reconnectDelay,
        onConnect: () => {
          console.log("[WebSocket] Connected to farm notifications");
          isConnectingRef.current = false;

          subscriptionRef.current = client.subscribe(topic, (message) => {
            try {
              const notification = JSON.parse(message.body);
              console.log("[WebSocket] Received notification:", notification);
              callbackRef.current?.(notification);
            } catch (error) {
              console.error("[WebSocket] Failed to parse message:", error, message.body);
            }
          });
        },
        onDisconnect: () => {
          console.log("[WebSocket] Disconnected from farm notifications");
          subscriptionRef.current = null;
        },
        onStompError: (error) => {
          console.error("[WebSocket] STOMP error:", error);
          isConnectingRef.current = false;
        },
        onWebSocketError: (error) => {
          console.error("[WebSocket] WebSocket error:", error);
          isConnectingRef.current = false;
        },
      });

      clientRef.current = client;
      client.activate();
    };

    connectWebSocket();

    return () => {
      if (subscriptionRef.current) {
        subscriptionRef.current.unsubscribe();
        subscriptionRef.current = null;
      }

      if (clientRef.current?.active) {
        clientRef.current.deactivate();
        clientRef.current = null;
      }
    };
  }, [wsUrl, topic, reconnectDelay, token]);
}
