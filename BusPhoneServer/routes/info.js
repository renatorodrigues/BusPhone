var util = require("./util");
var auth = require("./auth");

exports.info = function(req, res){

	var db = req.db;
	var token = req.query.token;

	if(token==null){
		return util.out(res,2);
	}

	db.serialize(function() {

		db.get("SELECT * FROM clients WHERE token=$token",{$token:token},
		 function(err, row) {

			if(err){ return util.out(res,3); }

			if (typeof(row) != 'undefined')
	       		util.out(res,0,{info:{name:row.name, 
	       							  username:row.username, 
	       							  creditcard:row.creditCard}
				});
	       	else
	       		util.out(res,5);
	  	});
		 
	});
};

//db.all("SELECT ticketID AS id,type,used,time as timestamp FROM tickets WHERE clientID_FK=$id AND used=0 "

exports.tickets = function(req, res){

	var db = req.db;
	var token = req.query.token;

	if(token==null){
		return util.out(res,2);
	}

	auth.auth_user(res,db,token,get_tickets);

	function get_tickets(id){
		db.serialize(function() {
			db.all("SELECT ticketID AS id,type,used,time as timestamp FROM tickets WHERE clientID_FK=$id ORDER BY timestamp desc",	
					//+"SELECT ticketID AS id,type,used,time as timestamp FROM tickets where clientID_FK=$id AND used=1 ORDER BY time LIMIT 1",
				{$id:id}, function(err, rows) {

					if(err!=null){util.out(res,3,{err:err.toString()});return;}

					var current;
					var unused = rows.filter(function(e){
						return e.used==0;
					});
					var used = rows.filter(function(e){
						return e.used==1;
					});
					if(used.length>0){
						current = used[0];
						  delete current['used'];
					}

					var t1=0,t2=0,t3=0;
					for(var i=0; i<unused.length; ++i){
						switch(unused[i].type){
							case 't1':
								++t1;
								break;
							case 't2':
								++t2;
								break;
							case 't3':
								++t3;
								break;
						}
					}
			       	util.out(res,0,{tickets:unused, t1:t1, t2:t2, t3:t3, total:t1+t2+t3, current:current});
  			});
  		});
	}
		
};