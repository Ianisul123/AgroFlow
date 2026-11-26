import { useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import { getStoredAuthToken } from "../services/AuthService";

/**
 * Custom hook for authenticated WebSocket farm notifications.
 * The backend validates the Bearer token and authorizes
 * subscriptions based on the user's ID and role.
 */
export function useFarmNotifications(onFarmChanged, options = {}) {
  const {
    wsUrl = "ws://localhost:8080/ws",
    topic = null,
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
      console.warn(
        "useFarmNotifications: onFarmChanged callback not provided"
      );
      return undefined;
    }

    if (!token || !topic) {
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
          if (clientRef.current !== client) {
            return;
          }

          console.log("[WebSocket] Connected to farm notifications");

          isConnectingRef.current = false;

          subscriptionRef.current = client.subscribe(topic, (message) => {
            if (clientRef.current !== client) {
              return;
            }

            try {
              const notification = JSON.parse(message.body);

              console.log("[WebSocket] Received notification:", notification);

              callbackRef.current?.(notification);
            } catch (error) {
              console.error("[WebSocket] Failed to parse message:", error);
            }
          });
        },

        onDisconnect: () => {
          if (clientRef.current !== client) {
            return;
          }

          console.log("[WebSocket] Disconnected from farm notifications");

          subscriptionRef.current = null;
        },

        onStompError: (error) => {
          console.error("[WebSocket] STOMP error:", error);

          if (clientRef.current === client) {
            isConnectingRef.current = false;
          }
        },

        onWebSocketError: (error) => {
          console.error("[WebSocket] WebSocket error:", error);

          if (clientRef.current === client) {
            isConnectingRef.current = false;
          }
        },
      });

      clientRef.current = client;
      client.activate();
    };

    connectWebSocket();

    return () => {
      const client = clientRef.current;
      const subscription = subscriptionRef.current;

      clientRef.current = null;
      subscriptionRef.current = null;
      isConnectingRef.current = false;

      if (client?.connected && subscription) {
        subscription.unsubscribe();
      }

      if (client) {
        void client.deactivate();
      }
    };
  }, [wsUrl, topic, reconnectDelay, token]);
}
