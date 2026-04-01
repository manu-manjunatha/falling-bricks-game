const canvas = document.getElementById("gameCanvas");
const ctx = canvas.getContext("2d");

const CELL = 30;

let stompClient = null;
let moveSound = new Audio("movement.wav");

let lastX = null, lastY = null;
let particles = [];
let gameStarted = false;
let wsConnected = false;
let isPaused = false;
// ------------------- CONNECT -------------------
function connect() {
    const socket = new SockJS("/ws-game");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        wsConnected = true;
        console.log("WebSocket connected");

        document.getElementById("status").innerText = "Connected!";
        document.getElementById("startBtn").disabled = false;

        stompClient.subscribe("/topic/game", msg => {
            const state = JSON.parse(msg.body);
            render(state);

            // Auto-disable start when server confirms game is running
            if (state.running && !gameStarted) {
                gameStarted = true;
                document.getElementById("startBtn").disabled = true;
            }
        });
    }, () => {
        document.getElementById("status").innerText = "Connection failed!";
        console.error("WebSocket connection failed");
    });
}

// ------------------- BUTTON ACTIONS -------------------
function startGame() {
    const btn = document.getElementById("startBtn");

    // Disable instantly (prevents double clicks)
    btn.disabled = true;

    if (!stompClient || !wsConnected || gameStarted) return;

    stompClient.send("/app/start", {}, {});
    gameStarted = true;
}

function restartGame() {
    if (!stompClient || !wsConnected) return;
    stompClient.send("/app/restart", {}, {});
    gameStarted = true;
    document.getElementById("startBtn").disabled = true;
}

function togglePause() {
    if (!stompClient || !gameStarted) return;

    isPaused = !isPaused;

    stompClient.send("/app/pause", {}, {});

    const btn = document.getElementById("pauseBtn");
    btn.innerText = isPaused ? "Resume" : "Pause";
}

// ------------------- KEY CONTROLS -------------------
document.addEventListener("keydown", e => {
    if (!stompClient || !gameStarted) return;

    if (["ArrowLeft","ArrowRight","ArrowDown"].includes(e.key)) {
        playMoveSound();
    }

    if (e.key === "ArrowLeft") stompClient.send("/app/move", {}, "LEFT");
    if (e.key === "ArrowRight") stompClient.send("/app/move", {}, "RIGHT");
    if (e.key === "ArrowDown") stompClient.send("/app/move", {}, "FAST_DROP");
    if (e.key === "ArrowUp") stompClient.send("/app/move", {}, "ROTATE");
    if (e.key === "p") togglePause();
});

// ------------------- SOUND -------------------
function playMoveSound() {
    moveSound.currentTime = 0;
    moveSound.play();
}

// ------------------- GHOST PIECE -------------------
function getGhostY(brick, board) {
    let ghostY = brick.y;
    while (true) {
        ghostY++;
        if (checkCollision(brick, board, brick.x, ghostY)) return ghostY - 1;
    }
}

function checkCollision(brick, board, x, y) {
    for (let i = 0; i < brick.shape.length; i++) {
        for (let j = 0; j < brick.shape[i].length; j++) {
            if (!brick.shape[i][j]) continue;
            let nx = x + j;
            let ny = y + i;
            if (nx < 0 || nx >= 10 || ny >= 20) return true;
            if (ny >= 0 && board[ny][nx]) return true;
        }
    }
    return false;
}

// ------------------- DRAW -------------------
function drawBlock(x, y, color, ghost=false) {
    ctx.globalAlpha = ghost ? 0.3 : 1;
    ctx.fillStyle = color;
    ctx.fillRect(x * CELL, y * CELL, CELL, CELL);
    ctx.strokeStyle = "black";
    ctx.strokeRect(x * CELL, y * CELL, CELL, CELL);
    ctx.globalAlpha = 1;
}

// ------------------- PARTICLES -------------------
function createParticles(row) {
    for (let i = 0; i < 40; i++) {
        particles.push({
            x: Math.random() * canvas.width,
            y: row * CELL,
            vx: (Math.random() - 0.5) * 10,
            vy: Math.random() * -10,
            life: 15
        });
    }
}

function updateParticles() {
    particles.forEach(p => {
        p.x += p.vx;
        p.y += p.vy;
        p.vy += 0.5;
        p.life--;
    });
    particles = particles.filter(p => p.life > 0);
}

function drawParticles() {
    particles.forEach(p => {
        ctx.fillStyle = "orange";
        ctx.fillRect(p.x, p.y, 4, 4);
    });
}

// ------------------- RENDER -------------------
function render(state) {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    document.getElementById("score").innerText = state.score;

    const b = state.currentBrick;
    if (!b) return;

    if (lastY !== null && (b.y !== lastY || b.x !== lastX)) playMoveSound();
    lastX = b.x; lastY = b.y;

    state.clearedRows.forEach(row => createParticles(row));

    state.board.forEach((row, y) => {
        row.forEach((cell, x) => {
            if (cell) drawBlock(x, y, "#38bdf8");
        });
    });

    // Ghost piece
    let ghostY = getGhostY(b, state.board);
    for (let i = 0; i < b.shape.length; i++)
        for (let j = 0; j < b.shape[i].length; j++)
            if (b.shape[i][j]) drawBlock(b.x + j, ghostY + i, "#ffffff", true);

    // Actual brick
    for (let i = 0; i < b.shape.length; i++)
        for (let j = 0; j < b.shape[i].length; j++)
            if (b.shape[i][j]) drawBlock(b.x + j, b.y + i, "#facc15");

    updateParticles();
    drawParticles();

    if (state.gameOver) {
        ctx.fillStyle = "red";
        ctx.font = "40px Arial";
        ctx.fillText("GAME OVER", 20, 300);
        gameStarted = false;
        document.getElementById("startBtn").disabled = false; // re-enable start
    }
}

// ------------------- INIT -------------------
connect();

document.addEventListener("click", () => {
    moveSound.play().then(() => {
        moveSound.pause();
        moveSound.currentTime = 0;
    }).catch(() => {});
}, { once: true });

// Hook buttons
document.getElementById("startBtn").onclick = startGame;
document.getElementById("restartBtn").onclick = restartGame;
document.getElementById("pauseBtn").onclick = togglePause;