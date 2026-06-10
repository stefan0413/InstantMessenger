# InstantMessenger — FrontEnd

React + TypeScript application providing the chat UI for InstantMessenger.

## Stack

- React 19, TypeScript, Vite
- Native WebSocket with STOMP protocol (no client library)
- CSS modules per component

## Running

```bash
npm install
npm run dev
```

Requires the backend running on `http://localhost:8080`. Vite proxies all API and WebSocket requests automatically.

## Build

```bash
npm run build
```
