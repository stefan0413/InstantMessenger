import type { ChannelEvent } from "./messagesService";
import { WS_URL } from "./apiConfig";

type EventHandler = (event: ChannelEvent) => void;
type StatusHandler = (status: "connecting" | "connected" | "disconnected" | "error") => void;

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

export class ChatSocketClient {
  private socket?: WebSocket;
  private handlers = new Map<string, EventHandler>();
  private connected = false;
  private pendingFrames: string[] = [];
  private subscriptionIds = new Set<string>();
  private statusHandler?: StatusHandler;

  connect(onStatus?: StatusHandler): void {
    if (this.socket && this.socket.readyState <= WebSocket.OPEN) {
      return;
    }

    this.statusHandler = onStatus;
    this.statusHandler?.("connecting");

    this.socket = new WebSocket(WS_URL);

    this.socket.addEventListener("open", () => {
      this.socket?.send(encodeFrame("CONNECT", {
        "accept-version": "1.2",
        host: new URL(WS_URL).host,
        "heart-beat": "0,0",
      }));
    });

    this.socket.addEventListener("message", (event) => {
      for (const frame of decodeFrames(String(event.data))) {
        if (frame.command === "CONNECTED") {
          this.connected = true;
          this.statusHandler?.("connected");
          this.flush();
          continue;
        }

        if (frame.command === "ERROR") {
          this.statusHandler?.("error");
          console.error("WebSocket STOMP error", frame.headers.message, frame.body);
          continue;
        }

        if (frame.command === "MESSAGE") {
          const destination = frame.headers.destination;
          const handler = destination ? this.handlers.get(destination) : undefined;
          if (handler) {
            handler(JSON.parse(frame.body));
          }
        }
      }
    });

    this.socket.addEventListener("close", () => {
      this.connected = false;
      this.subscriptionIds.clear();
      this.statusHandler?.("disconnected");
    });

    this.socket.addEventListener("error", () => {
      this.connected = false;
      this.statusHandler?.("error");
    });
  }

  subscribeToChannel(channelId: string, handler: EventHandler): void {
    const destination = `/topic/channel/${channelId}`;
    this.handlers.set(destination, handler);
    const id = `channel-${channelId}`;

    if (!this.subscriptionIds.has(id)) {
      this.sendFrame(encodeFrame("SUBSCRIBE", { id, destination }));
      this.subscriptionIds.add(id);
    }
  }

  sendMessage(payload: { content: string; userId: string; channelId: string }): boolean {
    return this.sendFrame(
      encodeFrame(
        "SEND",
        {
          destination: "/app/chat.send",
          "content-type": "application/json",
        },
        JSON.stringify({
          content: payload.content,
          userId: Number(payload.userId),
          channelId: Number(payload.channelId),
        }),
      ),
    );
  }

  disconnect(): void {
    if (this.socket?.readyState === WebSocket.OPEN) {
      this.socket.send(encodeFrame("DISCONNECT"));
      this.socket.close();
    }
    this.connected = false;
    this.handlers.clear();
    this.subscriptionIds.clear();
  }

  private sendFrame(frame: string): boolean {
    if (this.connected && this.socket?.readyState === WebSocket.OPEN) {
      this.socket.send(frame);
      return true;
    }

    this.pendingFrames.push(frame);
    return false;
  }

  private flush(): void {
    while (this.pendingFrames.length > 0 && this.socket?.readyState === WebSocket.OPEN) {
      this.socket.send(this.pendingFrames.shift()!);
    }
  }
}
