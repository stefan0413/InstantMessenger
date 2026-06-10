import type { ChannelEvent } from "./messagesService";
import { getAuthToken, WS_URL } from "./apiConfig";

type EventHandler = (event: ChannelEvent) => void;
type StatusHandler = (status: "connecting" | "connected" | "disconnected" | "error") => void;

export interface PresenceEvent {
  userId: number;
  status: "ONLINE" | "OFFLINE";
}

export interface TypingPayload {
  channelId: string;
  typing: boolean;
}

function encodeFrame(command: string, headers: Record<string, string> = {}, body = ""): string {
  const allHeaders = body ? { ...headers, "content-length": String(new Blob([body]).size) } : headers;
  const headerLines = Object.entries(allHeaders).map(([key, value]) => `${key}:${value}`);
  return `${command}\n${headerLines.join("\n")}\n\n${body}\0`;
}

function decodeFrames(chunk: string): Array<{ command: string; headers: Record<string, string>; body: string }> {
  return chunk
    .split("\0")
    .map((frame) => frame.replace(/^\n+/, ""))
    .filter(Boolean)
    .map((frame) => {
      const [head, body = ""] = frame.split("\n\n");
      const [command, ...headerLines] = head.replace(/\r/g, "").split("\n");
      const headers = Object.fromEntries(
        headerLines
          .filter((line) => line.includes(":"))
          .map((line) => {
            const separator = line.indexOf(":");
            return [line.slice(0, separator), line.slice(separator + 1)];
          }),
      );

      return { command, headers, body };
    });
}

const MAX_RECONNECT_DELAY_MS = 30_000;

export class ChatSocketClient {
  private socket?: WebSocket;
  private handlers = new Map<string, EventHandler>();
  private presenceHandler?: (event: PresenceEvent) => void;
  private connected = false;
  private pendingFrames: string[] = [];
  private subscriptionIds = new Set<string>();
  private destinationToSubId = new Map<string, string>();
  private statusHandler?: StatusHandler;
  private reconnectAttempts = 0;
  private reconnectTimer?: ReturnType<typeof setTimeout>;
  private intentionalDisconnect = false;

  connect(onStatus?: StatusHandler): void {
    if (this.socket && this.socket.readyState <= WebSocket.OPEN) {
      return;
    }

    this.statusHandler = onStatus;
    this.statusHandler?.("connecting");
    this.createSocket();
  }

  private createSocket(): void {
    this.socket = new WebSocket(WS_URL);

    this.socket.addEventListener("open", () => {
      const token = getAuthToken();
      this.socket?.send(encodeFrame("CONNECT", {
        "accept-version": "1.2",
        host: new URL(WS_URL).host,
        "heart-beat": "0,0",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      }));
    });

    this.socket.addEventListener("message", (event) => {
      for (const frame of decodeFrames(String(event.data))) {
        if (frame.command === "CONNECTED") {
          this.connected = true;
          this.reconnectAttempts = 0;
          this.statusHandler?.("connected");
          this.resubscribeAndFlush();
          continue;
        }

        if (frame.command === "ERROR") {
          this.statusHandler?.("error");
          console.error("WebSocket STOMP error", frame.headers.message, frame.body);
          continue;
        }

        if (frame.command === "MESSAGE") {
          const destination = frame.headers.destination;
          if (destination === "/topic/presence" && this.presenceHandler) {
            this.presenceHandler(JSON.parse(frame.body));
          } else {
            const handler = destination ? this.handlers.get(destination) : undefined;
            if (handler) handler(JSON.parse(frame.body));
          }
        }
      }
    });

    this.socket.addEventListener("close", () => {
      this.connected = false;
      this.subscriptionIds.clear();
      this.statusHandler?.("disconnected");
      if (!this.intentionalDisconnect) {
        this.scheduleReconnect();
      }
      this.intentionalDisconnect = false;
    });

    this.socket.addEventListener("error", () => {
      this.connected = false;
      this.statusHandler?.("error");
    });
  }

  private resubscribeAndFlush(): void {
    for (const [destination, subId] of this.destinationToSubId) {
      if (!this.subscriptionIds.has(subId)) {
        this.socket?.send(encodeFrame("SUBSCRIBE", { id: subId, destination }));
        this.subscriptionIds.add(subId);
      }
    }

    while (this.pendingFrames.length > 0 && this.socket?.readyState === WebSocket.OPEN) {
      this.socket.send(this.pendingFrames.shift()!);
    }
  }

  private scheduleReconnect(): void {
    if (this.reconnectTimer !== undefined) {
      return;
    }
    const delay = Math.min(1_000 * Math.pow(2, this.reconnectAttempts), MAX_RECONNECT_DELAY_MS);
    this.reconnectAttempts++;
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = undefined;
      if (!this.connected) {
        this.statusHandler?.("connecting");
        this.createSocket();
      }
    }, delay);
  }

  subscribeToChannel(channelId: string, handler: EventHandler): void {
    const destination = `/topic/channel/${channelId}`;
    this.handlers.set(destination, handler);
    const id = `channel-${channelId}`;
    this.destinationToSubId.set(destination, id);

    if (!this.subscriptionIds.has(id)) {
      this.sendFrame(encodeFrame("SUBSCRIBE", { id, destination }));
      this.subscriptionIds.add(id);
    }
  }

  sendMessage(payload: { content: string; channelId: string; fileUrl?: string; fileName?: string }): boolean {
    return this.sendFrame(
      encodeFrame(
        "SEND",
        {
          destination: "/app/chat.send",
          "content-type": "application/json",
        },
        JSON.stringify({
          content: payload.content || null,
          channelId: Number(payload.channelId),
          fileUrl: payload.fileUrl ?? null,
          fileName: payload.fileName ?? null,
        }),
      ),
    );
  }

  subscribeToUserNotifications(userId: string, handler: EventHandler): void {
    const destination = `/topic/user/${userId}`;
    this.handlers.set(destination, handler);
    const id = `user-${userId}`;
    this.destinationToSubId.set(destination, id);
    if (!this.subscriptionIds.has(id)) {
      this.sendFrame(encodeFrame("SUBSCRIBE", { id, destination }));
      this.subscriptionIds.add(id);
    }
  }

  subscribeToPresence(handler: (event: PresenceEvent) => void): void {
    this.presenceHandler = handler;
    const destination = "/topic/presence";
    const id = "sub-presence";
    this.destinationToSubId.set(destination, id);
    if (!this.subscriptionIds.has(id)) {
      this.sendFrame(encodeFrame("SUBSCRIBE", { id, destination }));
      this.subscriptionIds.add(id);
    }
  }

  sendTyping(payload: TypingPayload): void {
    this.sendFrame(
      encodeFrame(
        "SEND",
        { destination: "/app/chat.typing", "content-type": "application/json" },
        JSON.stringify({
          channelId: Number(payload.channelId),
          typing: payload.typing,
        }),
      ),
    );
  }

  sendUserConnect(): void {
    this.sendFrame(
      encodeFrame(
        "SEND",
        { destination: "/app/user.connect", "content-type": "application/json" },
        "{}",
      ),
    );
  }

  disconnect(): void {
    this.intentionalDisconnect = true;
    if (this.reconnectTimer !== undefined) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = undefined;
    }
    if (this.socket?.readyState === WebSocket.OPEN) {
      this.socket.send(encodeFrame("DISCONNECT"));
      this.socket.close();
    }
    this.connected = false;
    this.handlers.clear();
    this.destinationToSubId.clear();
    this.subscriptionIds.clear();
    this.pendingFrames = [];
    this.reconnectAttempts = 0;
  }

  private sendFrame(frame: string): boolean {
    if (this.connected && this.socket?.readyState === WebSocket.OPEN) {
      this.socket.send(frame);
      return true;
    }

    this.pendingFrames.push(frame);
    return false;
  }
}
