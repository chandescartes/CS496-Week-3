// models/book.js
var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var userSchema = new Schema({
	id: String,
    nickname: String,
    lat: String,
    lng: String
}, {versionKey: false});

var roomSchema = new Schema({
	id: String,
	founder: String,
	members: Array,
	title: String,
	food: String,
	max_num: Number,
	lat: Number,
	lng: Number,
	created_at: String
})

module.exports = {
	User: mongoose.model('user', userSchema),
	Room: mongoose.model('room', roomSchema)
};
