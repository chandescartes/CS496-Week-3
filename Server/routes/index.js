// routes/index.js

module.exports = function(app, Models)
{
    // Check Whether Server is Alive
    app.get('/', function(req, res){
        console.log("request to /");
        res.render("index.html");
    })

   	app.get('/api', function(req, res){
   		console.log("request to /api");
   		res.render("list.html");
   	})

    // GET ALL USERS
    app.get('/api/user', function(req,res){
        console.log("request to /api/user")
        Models.User.find(function(err, user){
            if(err) return res.status(500).send({error: 'database failure'});
            res.json(user);
        })
    });

    // GET ALL ROOMS
    app.get('/api/room', function(req,res){
        console.log("request to /api/room")
        Models.Room.find(function(err, room){
            if(err) return res.status(500).send({error: 'database failure'});
            res.json(room);
        })
    });

    // GET SINGLE USER
    app.get('/api/user/:id', function(req, res){
		console.log("request to /api/user/" + req.params.id)
		Models.User.findOne({id: req.params.id}, function(err, user){
			if (err) return res.status(500).json({error: err});
			if (!user) return res.status(404).json({error: 'no such user exists'});
			res.json(user);
		})
	})

	// GET SINGLE ROOM
    app.get('/api/room/:id', function(req, res){
		console.log("request to /api/room/" + req.params.id)
		Models.Room.findOne({id: req.params.id}, function(err, room){
			if (err) return res.status(500).json({error: err});
			if (!room) return res.status(404).json({error: 'no such room exists'});
			res.json(room);
		})
	})

    // CREATE USER
    app.post('/api/adduser', function(req, res){
        console.log("request to /api/adduser")

        if (req.body.id == null){
            return res.status(400).json({error: 'email not found in request'});
        }

        Models.User.findOne({id: req.body.id}, function(err, user){
            if (err) return res.status(500).json({error: err});
            if (user) return res.status(406).json({error: 'user already exists'});
            var user = new Models.User();
            user.id = req.body.id;
            user.nickname = req.body.nickname;
            user.lat = req.body.lat;
            user.lng = req.body.lng;

            user.save(function(err){
                if(err){
                    console.error(err);
                    res.json({result: 0});
                    return;
                }

                res.json({result: 1});
            });
        });
    });

    // CREATE ROOM
    app.post('/api/addroom', function(req, res){
        console.log("request to /api/addroom")

        Models.Room.findOne({id: req.body.id}, function(err, room){
            if (err) return res.status(500).json({error: err});
            if (room) return res.status(406).json({error: 'room already exists'});
            var room = new Models.Room();
            room.id = req.body.id;
            room.founder = req.body.founder;
            room.lat = req.body.lat;
            room.lng = req.body.lng;
            room.created_at = req.body.created_at;
            room.members = req.body.members;
            room.title = req.body.title;
            room.food = req.body.food;
            room.max_num = req.body.max_num;

            room.save(function(err){
                if(err){
                    console.error(err);
                    res.json({result: 0});
                    return;
                }

                res.json({result: 1});
            });
        });
    });

    // UPDATE THE USER
    app.put('/api/user/:id/nickname', function(req, res){
        console.log("[PUT]request to /api/user/"+req.params.id);

        id = req.params.id;

        Models.User.findOne({id: id}, function(err, user){
        	if (err) return res.status(500).send({error: err});	
			if (!user) {
				return res.send({error: 'no such user exists'});
			}

			user.nickname = req.body.nickname;

            user.save(function(err){
            	if (err){
            		console.error(err);
            		res.json({result: 0});
            		return;
            	}

            	res.json({result: 1});
            });
        });
    });

    // UPDATE THE USER
    app.put('/api/user/:id/location', function(req, res){
        console.log("[PUT]request to /api/user/"+req.params.id);

        id = req.params.id;

        Models.User.findOne({id: id}, function(err, user){
            if (err) return res.status(500).send({error: err}); 
            if (!user) {
                return res.send({error: 'no such user exists'});
            }

            user.lat = req.body.lat;
            user.lng = req.body.lng;

            user.save(function(err){
                if (err){
                    console.error(err);
                    res.json({result: 0});
                    return;
                }

                res.json({result: 1});
            });
        });
    });

    // UPDATE THE ROOM
    app.put('/api/room/:email', function(req, res){
        console.log("[PUT]request to /api/room/"+req.params.founder);

        founder = req.params.founder;

        Models.Room.findOne({founder: founder}, function(err, room){
        	if (err) return res.status(500).send({error: err});	
			if (!room) {
				return res.send({error: 'no such room exists'});
			}
            
            room.member = req.body.member;
            room.title = req.body.title;
            room.food = req.body.food;
            room.max_num = req.body.max_num;

            room.save(function(err){
            	if (err){
            		console.error(err);
            		res.json({result: 0});
            		return;
            	}

            	res.json({result: 1});
            });
        });
    });

    // DELETE USER
    app.delete('/api/user/:id', function(req, res){
        Models.User.remove({id: req.params.id}, function(err, output){
            if(err) return res.status(500).json({ error: "database failure" });
            res.status(204).end();
        })
    });

    // DELETE ROOM
    app.delete('/api/room/:id', function(req, res){
        Models.Room.remove({id: req.params.id}, function(err, output){
	        if(err) return res.status(500).json({ error: "database failure" });
	        res.status(204).end();
    	})
    });
}