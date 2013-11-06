
exports.users = function(req, res){

	var db = req.db;

	db.serialize(function() {

		db.all("SELECT * FROM clients", function(err, rows) {
	       res.send(rows);
	  	});
		 
	});
};


exports.bus = function(req, res){

	var db = req.db;

	db.serialize(function() {

		db.all("SELECT * FROM bus", function(err, rows) {
	       res.send(rows);
	  	});
		 
	});
};

exports.tickets = function(req, res){

	var db = req.db;

	db.serialize(function() {

		db.all("SELECT * FROM tickets", function(err, rows) {
	       res.send(rows);
	  	});
		 
	});
};

exports.clear = function(req, res){

	var db = req.db;

	db.serialize(function() {

		db.all("DELETE FROM tickets", function(err, rows) {
	       res.send("ok");
	  	});
		 
	});
};