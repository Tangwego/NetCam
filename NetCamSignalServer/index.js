const Server = require('ws').Server;

const io = new Server({
    port: 8080
});

let clients = [];

function sendToClient(client, data) {
    client.send(JSON.stringify(data));
}

function hasClient(number) {
    for (let i = 0; i < clients.length; i++) {
        if (clients[i].number == number) return true;
    }
    return false;
}

function removeClient(number) {
    for (let i = 0; i < clients.length; i++) {
        if (clients[i].number == number) {
            clients.splice(i);
            return true;
        }
    }
    return false;
}

function findClient(number) {
    for (let i = 0; i < clients.length; i++) {
        if (clients[i].number == number) return clients[i];
    }
    return null;
}

io.on("connection", (client, req)=> {
    console.log("connected...");
    

    client.on("message", (data)=> {
        let request = {}
        try {
            request = JSON.parse(data.toLocaleString());
            console.log("request: ", request.action, request.caller || request.from, request.callee || request.to);
            switch (request.action) {
                case "register":
                    if (hasClient(request.number)) {
                        sendToClient(client, {action: "response", result: "success", message: "register repeat!"});
                        return;
                    }
                    clients.push({
                        number: request.number,
                        address: req.socket.remoteAddress,
                        port: req.socket.remotePort,
                        family: req.socket.remoteFamily,
                        socket: client
                    });
                    sendToClient(client, {action: "response", result: "success"});
                    break;
                case "invite":
                    {
                        let callerClient = findClient(request.caller);
                        let calleeClient = findClient(request.callee);
                        if (!callerClient || !calleeClient) {
                            sendToClient(client, {action: "response", result: "failure", message: "not found!"});
                            return;
                        }
                        sendToClient(calleeClient.socket, {action: "invite", from: request.caller, sdp:request.sdp, type: request.type});
                        sendToClient(client, {action: "response", result: "success"});
                    }
                    break;
                case "answer":
                    {
                        let callerClient = findClient(request.caller);
                        let calleeClient = findClient(request.callee);
                        if (!callerClient || !calleeClient) {
                            sendToClient(client, {action: "response", result: "failure", message: "not found!"});
                            return;
                        }
                        sendToClient(callerClient.socket, {action: "answer", from: request.callee, sdp:request.sdp, type: request.type});
                        sendToClient(client, {action: "response", result: "success"});
                    }
                    break;
                case "candidate":
                    {
                        // console.log(request.candidate);
                        let fromClient = findClient(request.from);
                        let toClient = findClient(request.to);
                        if (!fromClient || !toClient) {
                            sendToClient(client, {action: "response", result: "failure", message: "not found!"});
                            return;
                        }
                        sendToClient(toClient.socket, {action: "candidate", from: request.from, to: request.to, candidate:request.candidate});
                        sendToClient(client, {action: "response", result: "success"});
                    }
                    break;
                case "hangup":
                    {
                        let fromClient = findClient(request.from);
                        let toClient = findClient(request.to);
                        if (!fromClient || !toClient) {
                            sendToClient(client, {action: "response", result: "failure", message: "not found!"});
                            return;
                        }
                        sendToClient(toClient.socket, {action: "hangup", from: request.from, to: request.to});
                        sendToClient(client, {action: "response", result: "success"});
                    }
                    break;
                case "unregister":
                    if (!hasClient(request.number)) {
                        sendToClient(client, {action: "response", result: "success", message: "not exists!"});
                        return;
                    }
                    removeClient(request.number);
                    sendToClient(client, {action: "response", result: "success"});
                    break;
                default: break;
            }
        } catch(err) {
            console.error(err);
        }
    });

});

console.log("websocket server started at port 8080");