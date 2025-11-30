## ChatApp – Distributed Systems Project

This project is a simple distributed chat system built with **Java**, **Spring Boot**, **WebSockets (STOMP)**, and a **web frontend**.  
It supports user accounts, friends, global chat, private one‑on‑one chats, and persistent message history.

---

### 1. Architecture Overview

- **Backend**: Spring Boot application (`Application.java`)

  - **REST APIs**:
    - `AuthController` – user registration and login
      - `POST /api/auth/register`
      - `POST /api/auth/login`
    - `FriendController` – manage friendships and chatroom IDs
      - `POST /api/friends/add`
      - `GET /api/friends/list?username=alice`
      - `GET /api/friends/chatroom-id?userA=alice&userB=bob`
    - `MessageHistoryController` – message history per chatroom
      - `GET /api/messages/history?chatroomId=...&limit=50`
      - `DELETE /api/messages/history?chatroomId=...`
  - **WebSocket endpoints** (configured via `WebSocketConfig`):
    - STOMP endpoint: `/ws` (SockJS)
    - Application destinations (client → server):
      - `/app/message` – global/public chat
      - `/app/private/{chatroomId}` – private chatroom between two users
      - `/app/connect` – presence: user connected
      - `/app/disconnect` – presence: user disconnected
    - Broker destinations (server → clients):
      - `/topic/messages` – global/public messages
      - `/topic/private/{chatroomId}` – messages for a specific private chatroom
      - `/topic/users` – list of online users

- **Frontend**: Static HTML/JS pages under `src/main/resources/static`
  - `login.html` – login/register screen
  - `app.html` – main application UI (friends list, chats list, messages panel)
  - `test-chat.html` / `login-chat.html` – earlier test pages (kept for debugging)

---

### 2. Data Storage (JSON Files)

The backend uses simple JSON files for persistence (ignored by Git via `.gitignore`):

- **User accounts** – `users.json`  
  Managed by `UserService`.

  Structure: list of user objects, each with:

  - `username`: lowercased username (e.g. `"alice"`)
  - `password`: **BCrypt password hash** (not plain text)

  Operations:

  - `register(UserAccount)` – hashes password and appends a new user
  - `login(UserAccount)` – verifies using `BCryptPasswordEncoder.matches(...)`

- **Friend relationships** – `friends.json`  
  Managed by `FriendService`.

  Stored as a list of `Friend` objects, each representing an undirected friendship between two users.
  Usernames are normalized and stored in **sorted order** so `Friend("alice","bob")` equals `Friend("bob","alice")`.

  Provides:

  - `addFriendship(userA, userB)`
  - `getFriends(username)` → list of usernames
  - `areFriends(userA, userB)`
  - `getChatroomId(userA, userB)` → stable ID like `"alice_bob"`

- **Message history per chatroom** – `messages_<chatroomId>.json`  
  Managed by `MessageLogService`.

  One file **per chatroom**:

  - Global chat: `messages_public.json`
  - Private chat between Alice & Bob: `messages_alice_bob.json`

  Each file is a JSON array of `Message` objects:

  - `user` – sender username
  - `text` – message text
  - `timestamp` – ISO‑8601 string from `Instant.now().toString()`
  - `chatroomId` – `"public"` or a private ID like `"alice_bob"`

  Operations:

  - `append(Message)` – adds a message to the appropriate file
  - `loadRecent(chatroomId, limit)` – returns the last N messages
  - `deleteHistory(chatroomId)` – deletes the file for that chatroom

---

### 3. Message Flow

#### 3.1 Global Chat

1. User logs in via `login.html` (using `/api/auth/login`), username stored in `localStorage`.
2. In `app.html`:
   - Client connects to `/ws` using SockJS + STOMP.
   - Subscribes to `/topic/messages`.
   - Sends a presence notification to `/app/connect`.
3. When the user sends a message in the **Global Chat**:

   - Client sends a JSON payload to `/app/message`:

     ```json
     { "user": "alice", "text": "hello world", "chatroomId": "public" }
     ```

   - `MessageController.handlePublicMessage`:
     - Ensures `timestamp` and `chatroomId = "public"`.
     - Calls `MessageLogService.append(...)` to persist it.
     - Returns the message, which is broadcast to `/topic/messages`.
   - All subscribed clients display the message in the Global chat view.

#### 3.2 Private One‑on‑One Chats

1. Friendship is created between two users:

   - Client calls `POST /api/friends/add` with:

     ```json
     { "userA": "alice", "userB": "bob" }
     ```

   - `FriendService` stores the friendship in `friends.json`.

2. For a private chat:
   - `app.html` requests a chatroom ID from:
     - `GET /api/friends/chatroom-id?userA=alice&userB=bob`
     - Response: `{ "chatroomId": "alice_bob" }`.
   - The UI creates a “Chat with Bob” entry for that `chatroomId`.
3. When sending a private message:

   - Client sends to `/app/private/alice_bob`:

     ```json
     { "user": "alice", "text": "hi bob", "chatroomId": "alice_bob" }
     ```

   - `MessageController.handlePrivateMessage`:
     - Ensures timestamp.
     - Logs the message and calls `MessageLogService.append(...)`.
     - Returns the message, which is broadcast on `/topic/private/alice_bob`.
   - Both Alice and Bob, if subscribed to that topic, see the message.

#### 3.3 Message History

- When the user selects a chat in `app.html` (Global or private):

  1. UI calls:

     `GET /api/messages/history?chatroomId=<id>&limit=50`

  2. `MessageHistoryController.getHistory` calls `MessageLogService.loadRecent`.
  3. The UI renders the returned `messages` array, then continues to display new WebSocket messages in real time.

- There is also a **“Clear Chat History”** button:

  - Calls:

    `DELETE /api/messages/history?chatroomId=<id>`

  - `MessageHistoryController.deleteHistory` deletes the corresponding `messages_<id>.json` file via `MessageLogService.deleteHistory`.

---

### 4. Frontend Flow

#### 4.1 Login Page (`login.html`)

- Fields: username, password.
- Buttons:
  - **Register** – `POST /api/auth/register`
  - **Login** – `POST /api/auth/login`
- On successful login:
  - Stores `chatapp.username` in `localStorage`.
  - Redirects to `/app.html`.

#### 4.2 Main App (`app.html`)

Layout: three columns.

- **Left: Friends panel**

  - Shows existing friends from `GET /api/friends/list?username=currentUser`.
  - Add friend with `POST /api/friends/add`.
  - Clicking a friend opens/activates the private chat with that user (using `chatroomId` from `FriendController`).

- **Middle: Chats panel**

  - Always includes **Global Chat** (`chatroomId = "public"`).
  - Adds one entry per friend (private chatroom).
  - Selecting a chat:
    - Highlights it.
    - Loads recent history via `GET /api/messages/history?chatroomId=...`.

- **Right: Messages panel**
  - Shows chat title and messages for the currently selected chat.
  - **Send** input:
    - For Global Chat → sends to `/app/message`.
    - For private chat → sends to `/app/private/{chatroomId}`.
  - “Clear Chat History” button:
    - Deletes that chat’s persisted messages via `DELETE /api/messages/history?chatroomId=...`.

---

---

### 5. How to Run and Test

1. **Build / run backend**

   ```bash
   mvn spring-boot:run
   ```

   Server runs at `http://localhost:8080`.

2. **Use the web UI**

   - Open `http://localhost:8080/login.html`.
   - Register some users (e.g. `alice`, `bob`) and log in.
   - After login you are redirected to `app.html`, where you can:
     - Add friends.
     - Use **Global Chat**.
     - Open private chats with friends.
     - See persisted history and clear it if needed.

