
// [LOAD PACKAGES]
var app 		= require('express')();
var http 		= require('http').Server(app);
var io 			= require('socket.io')(http);
var bodyParser  = require('body-parser');
var mongoose 	= require('mongoose');

// GLOBALS AND MACROS
var PORT 				= 4000;

var S_CONNECTION 		= "connection";
var S_DISCONNECT 		= "disconnect";
var S_NEW_MESSAGE 		= "new-message";
var S_USER_JOINED 		= "user-joined";
var S_USER_LEFT 		= "user-left";
var S_USER_DISCONNECTED = "user-disconnected";
var S_GET_ROOMS			= "get-rooms";
var S_CREATE_ROOM		= "create-room";
var S_JOIN_ROOM			= "join-room";
var S_SELF_JOINED		= "self-joined";

var NONEXISTENT 		= 0;
var FULL 				= 1;

// [CONFIGURE APP TO USE bodyParser]
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

app.set('views', __dirname + '/views');
app.set('view engine', 'ejs');
app.engine('html', require('ejs').renderFile);

// [CONFIGURE mongoose]
var db = mongoose.connection;
db.on('error', console.error);
db.once('open', function () {
    console.log("Connected to mongod server");
});

// DEFINE MODEL
var Models = require('./models/schemas');

mongoose.connect('mongodb://localhost/CS496_Week3');

// [CONFIGURE ROUTER]
var router = require('./routes')(app, Models);

// SOCKETS

console.log(Object.keys(io.sockets.sockets));
console.log(io.sockets.adapter.rooms);

io.on(S_CONNECTION, function (socket) {
	console.log('[NEW CONNECTION]	' + socket.id);
	socket.leave(socket.id);

	socket.on(S_GET_ROOMS, function (data) {
		console.log("[GET ROOMS]");
		socket.emit(S_GET_ROOMS, io.sockets.adapter.rooms);
	});

	socket.on(S_CREATE_ROOM, function (data) {
		var id = data.id;
		var created_at = data.created_at;
		var title = data.title;
		var founder = data.founder;
		var food = data.food;
		var limit = data.limit;
		var lat = data.lat;
		var long = data.long;

		console.log("[CREATE ROOM]		" + id);
		console.log("	ID: " + id);
		console.log("	TITLE: " + title);
		console.log("	FOUNDER: " + founder);
		console.log("	FOOD: " + food);
		console.log("	LIMIT: " + limit);
		console.log("	LAT: " + lat + " LONG: " + long);

		socket.join(id);
		io.sockets.adapter.rooms[id]["created_at"] = created_at;
		io.sockets.adapter.rooms[id]["title"] = title;
		io.sockets.adapter.rooms[id]["founder"] = founder;
		io.sockets.adapter.rooms[id]["food"] = food;
		io.sockets.adapter.rooms[id]["limit"] = limit;
		io.sockets.adapter.rooms[id]["lat"] = lat;
		io.sockets.adapter.rooms[id]["long"] = long;
	});

	socket.on(S_JOIN_ROOM, function (data) {
		var room = data["room"];
		var nickname = data["nickname"];

		if (io.sockets.adapter.rooms[room] == undefined) {
			console.log("[JOIN ROOM]		NONEXISTENT");
			socket.emit(S_JOIN_ROOM, {"result": false, "reason": NONEXISTENT});
			return;
		}

		if (socket.room != undefined) {
			console.log("[JOIN ROOM] Socket is already in a room");
			return;
		}

		var title = io.sockets.adapter.rooms[room]["title"];
		var limit = io.sockets.adapter.rooms[room]["limit"];
		var length = io.sockets.adapter.rooms[room]["length"];
		var result = length < limit;

		console.log("[JOIN ROOM]		" + nickname + " joined " + room);
		console.log("	LENGTH: " + length);
		console.log("	LIMIT: 	" + limit);

		var data = {"result": result, "room": room, "reason": FULL, "title": title};

		socket.emit(S_JOIN_ROOM, data);
	});

	socket.on(S_USER_JOINED, function (data) {
		var room = data["room"];
		var nickname = data["nickname"];

		socket.room = room;
		socket.nickname = nickname;
		socket.join(room);

		var numUsers = io.sockets.adapter.rooms[room]["length"];
		console.log("[JOIN ROOM]		" + nickname + " joined. (" + numUsers + " members)");

		var returnData = {"nickname": nickname, "numUsers": numUsers}; // change numUsers

		socket.broadcast.to(room).emit(S_USER_JOINED, returnData);
	});

	socket.on(S_SELF_JOINED, function (data) {
		var room = data["room"];

		var numUsers = io.sockets.adapter.rooms[room]["length"];
		console.log("[SELF JOIN]");

		var returnData = {"numUsers": numUsers}; // change numUsers

		socket.emit(S_SELF_JOINED, returnData);
	});

	socket.on(S_NEW_MESSAGE, function (data) {
		var room = data["room"];
		var nickname = data["nickname"];
		var message = data["message"];

		console.log("[" + nickname + "]		" + message);

		var returnData = {"nickname": nickname, "message": message};
		io.sockets.in(room).emit(S_NEW_MESSAGE, returnData);
	});

	socket.on(S_USER_DISCONNECTED, function (data) {
		var room = data["room"];
		var nickname = data["nickname"];

		socket.leave(room);

		console.log("[USER DISCONNECTED]	" + nickname);
		console.log(io.sockets.adapter.rooms[socket.room]);

		if (io.sockets.adapter.rooms[socket.room] != undefined) {
			console.log("	length: " + io.sockets.adapter.rooms[socket.room]["length"]);
			var numUsers = io.sockets.adapter.rooms[socket.room]["length"];
			var returnData = {"username": nickname, "numUsers": numUsers};
			socket.broadcast.to(socket.room).emit(S_USER_LEFT, returnData);
		}

		socket.room = undefined;
	});

	socket.on(S_DISCONNECT, function () {
		console.log("[DISCONNECT]");
	});

});

// RUN SERVER
http.listen(PORT, function () {
	console.log('Listening to port 4000');
});