var util = require("./util");
var auth = require("./auth");

exports.validate = function(req, res){

	var db = req.db;
	var token = req.query.token,
		ticket_id = req.query.id;

	if(token==null || ticket_id==null || token=="" || ticket_id==""){
		return util.out(res,2);
	}

	auth.auth_bus(res,db,token,check_ticket);

	function check_ticket(bus_id){
		db.serialize(function() {
			db.all("SELECT type FROM tickets WHERE ticketID=$ticket_id AND used=0",
				{$ticket_id:ticket_id }, function(err, rows) {

					if(err!=null){ return util.out(res,3,{err:err.toString()})}

					if(rows.length>0)
						return use_ticket(bus_id, rows[0].type);
					else
						return util.out(res,8);
  			});
  		});
	}

	function use_ticket(bus_id,type){
		db.serialize(function() {
			var q = db.prepare("UPDATE tickets SET used=1, time=Datetime('now','localtime'), busID_FK=$bus_id WHERE ticketID=$ticket_id");
			q.run({$bus_id:bus_id, $ticket_id:ticket_id},function(err,rows){

				if(err!=null){ return util.out(res,3,{err:err.toString()})}

				return util.out(res,0,{type:type});
			});
  		});	
	}
};

exports.inspect = function(req, res){

	var db = req.db;
	var bus_id = req.query.bus;/*,
		ticket_id = req.query.ticket;*/

	if(bus_id==null ||/* ticket_id==null ||*/ bus_id=="" /*|| ticket_id==""*/){
		return util.out(res,2);
	}

	var query = 
	"SELECT * FROM tickets WHERE busID_FK=$bus_id AND " +
	"time >= Datetime('now','localtime', '-90 Minute')";

	/*
	"CASE "+
	"WHEN type='t1' THEN time >= Datetime('now','localtime', '-15 Minute') "+
	"WHEN type='t2' THEN time >= Datetime('now','localtime', '-30 Minute') "+
	"WHEN type='t3' THEN time >= Datetime('now','localtime', '-60 Minute') END";
	*/

	db.serialize(function() {
		var q = db.all(query, {$bus_id:1},function(err,rows){

			if(err!=null){ return util.out(res,3,{err:err.toString()})}

			return util.out(res,0,{tickets:rows});
		});
	});	
};