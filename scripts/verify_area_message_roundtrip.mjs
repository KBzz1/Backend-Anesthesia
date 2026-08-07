#!/usr/bin/env node

const wsUrl = process.argv[2] ?? "ws://127.0.0.1:18080/ws";
const fromArea = process.argv[3] ?? "d";
const toArea = process.argv[4] ?? "f";
const timeoutMs = 12000;

const messageId = `area-msg-${Date.now()}`;
const payload = {
  type: "COMMAND",
  fromArea,
  toArea,
  messageId,
  timestamp: Date.now(),
  content: {
    type: "BP",
    action: "START",
  },
};

function frame(command, headers = {}, body = "") {
  const headerLines = Object.entries(headers).map(([k, v]) => `${k}:${v}`);
  return `${command}\n${headerLines.join("\n")}\n\n${body}\u0000`;
}

function parseFrames(raw) {
  return raw
    .split("\u0000")
    .map((part) => part.trim())
    .filter(Boolean)
    .map((part) => {
      const [headerPart, ...bodyParts] = part.split("\n\n");
      const lines = headerPart.split("\n");
      const command = lines[0];
      const headers = {};
      for (const line of lines.slice(1)) {
        const idx = line.indexOf(":");
        if (idx > -1) {
          headers[line.slice(0, idx)] = line.slice(idx + 1);
        }
      }
      return { command, headers, body: bodyParts.join("\n\n") };
    });
}

function createClient(areaId, subscribeDestination) {
  const ws = new WebSocket(wsUrl);
  const state = {
    areaId,
    subscribeDestination,
    ws,
    connected: false,
    subscribed: false,
    closed: false,
  };

  ws.addEventListener("open", () => {
    ws.send(frame("CONNECT", {
      "accept-version": "1.2",
      "heart-beat": "10000,10000",
    }));
  });

  ws.addEventListener("close", (event) => {
    if (completed) return;
    console.error(`WebSocket closed for area ${areaId}: code=${event.code} reason=${event.reason || "<empty>"}`);
  });

  return state;
}

const sender = createClient(fromArea, `/data/area/${fromArea}`);
const receiver = createClient(toArea, `/data/area/${toArea}`);

let timeoutHandle = null;
let completed = false;
let sendTriggered = false;
let senderAck = null;
let receiverMessage = null;

function closeClient(client) {
  if (client.closed) return;
  client.closed = true;
  try {
    client.ws.close();
  } catch {
  }
}

function cleanup(exitCode = 0) {
  if (completed) return;
  completed = true;
  if (timeoutHandle) clearTimeout(timeoutHandle);
  closeClient(sender);
  closeClient(receiver);
  process.exit(exitCode);
}

function maybeSend() {
  if (sendTriggered) return;
  if (!sender.subscribed || !receiver.subscribed) return;
  sendTriggered = true;
  sender.ws.send(frame("SEND", {
    destination: "/data/area/call",
    "content-type": "application/json",
  }, JSON.stringify(payload)));
}

function maybeFinish() {
  if (!senderAck || !receiverMessage) return;
  console.log(JSON.stringify({
    ok: true,
    wsUrl,
    fromArea,
    toArea,
    messageId,
    ack: senderAck,
    delivered: receiverMessage,
  }, null, 2));
  cleanup(0);
}

function parseJsonBody(frameBody) {
  try {
    return JSON.parse(frameBody);
  } catch {
    return null;
  }
}

function handleMessage(client, event) {
  const payloadText = typeof event.data === "string" ? event.data : String(event.data);
  for (const stompFrame of parseFrames(payloadText)) {
    if (stompFrame.command === "ERROR") {
      console.error(`STOMP ERROR for area ${client.areaId}: ${stompFrame.body || "<empty>"}`);
      cleanup(1);
      return;
    }

    if (stompFrame.command === "CONNECTED") {
      client.connected = true;
      client.ws.send(frame("SUBSCRIBE", {
        id: `sub-${client.areaId}`,
        destination: client.subscribeDestination,
      }));
      client.subscribed = true;
      maybeSend();
      continue;
    }

    if (stompFrame.command !== "MESSAGE") {
      continue;
    }

    const body = parseJsonBody(stompFrame.body);
    if (!body) {
      continue;
    }

    if (
      client === sender &&
      stompFrame.headers.destination === sender.subscribeDestination &&
      body.type === "ACK" &&
      body.content?.originalMessageId === messageId
    ) {
      senderAck = body;
      maybeFinish();
      continue;
    }

    if (
      client === receiver &&
      stompFrame.headers.destination === receiver.subscribeDestination &&
      body.messageId === messageId
    ) {
      receiverMessage = body;
      maybeFinish();
    }
  }
}

function handleError(client, event) {
  const details = event?.error?.message
    ?? event?.message
    ?? event?.type
    ?? "unknown error";
  console.error(`WebSocket/STOMP failed for area ${client.areaId}: ${details}`);
  cleanup(1);
}

sender.ws.addEventListener("message", (event) => handleMessage(sender, event));
receiver.ws.addEventListener("message", (event) => handleMessage(receiver, event));
sender.ws.addEventListener("error", (event) => handleError(sender, event));
receiver.ws.addEventListener("error", (event) => handleError(receiver, event));

timeoutHandle = setTimeout(() => {
  console.error(`Timed out waiting for ACK on ${sender.subscribeDestination} and delivery on ${receiver.subscribeDestination}`);
  cleanup(1);
}, timeoutMs);
