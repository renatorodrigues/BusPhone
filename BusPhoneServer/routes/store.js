var util = require("./util");
var auth = require("./auth");

var T1_COST=1.6,T2_COST=2.3,T3_COST=3.5;

exports.buy = function(req, res){

	var db = req.db;
	var token = req.body.token,
		t1 = parseInt(req.body.t1) || 0,
		t2 = parseInt(req.body.t2) || 0,
		t3 = parseInt(req.body.t3) || 0,
		buy = req.body.buy;

	if(token==null || (t1<=0&&t2<=0&&t3<=0)){
		return util.out(res,2);
	}

	auth.auth_user(res,db,token,check_tickets);

	function check_tickets(id){
		db.serialize(function() {
			db.all("SELECT type,used as timestamp FROM tickets WHERE clientID_FK=$id AND used=0",
				{$id:id}, function(err, rows) {

					if(err!=null){ return util.out(res,3,{err:err.toString()})}

					//count tickets by type
					var db_t1=0,db_t2=0,db_t3=0;
					for(var i=0; i<rows.length; ++i){
						switch(rows[i].type){
							case 't1':
								++db_t1;
								break;
							case 't2':
								++db_t2;
								break;
							case 't3':
								++db_t3;
								break;
						}
					}

					//check if buying more goes over the limit
			       	var total = {};
					if(t1>0&&t1+db_t1>10){
						total.owned_t1 = db_t1;
						total.bought_t1 = t1;
					}
					if(t2>0&&t2+db_t2>10){
						total.owned_t2 = db_t2;
						total.bought_t2 = t2;
					}
					if(t3>0&&t3+db_t3>10){
						total.owned_t3 = db_t3;
						total.bought_t3 = t3;
					}
					
					var extra;
					if(t1+t2+t3>=10){
						if(t1>0) {++t1; extra="t1"}
						else if(t2>0) {++t2; extra="t2"}
						else if(t3>0) {++t3; extra="t3"}
					}

					console.log("t1: "+t1+", db_t1: "+db_t1);

					if(Object.keys(total).length !== 0){
						return util.out(res,7,total);
					} else {

						if(buy){
							return buy_tickets(id,extra);
						}else{
							var cost = t1*T1_COST+t2*T2_COST+t3*T3_COST;
							return util.out(res,0,{t1: t1+db_t1, t2: t2+db_t2, t3: t3+db_t3,cost:cost, extra: extra});
						}

						
					}
  			});
  		});
	}

	function buy_tickets(id,extra){
		db.serialize(function() {
			var query = "INSERT INTO tickets(ticketID,clientID_FK,type,used) VALUES";
			var pos_query = "";

			var ticket_hash = {};
			ticket_hash.t1 = [];
			ticket_hash.t2 = [];
			ticket_hash.t3 = [];

			for(var i=0; i<t1; ++i)
				pos_query+=gen_ticket_tuple("t1",id,ticket_hash);
			for(var i=0; i<t2; ++i)
				pos_query+=gen_ticket_tuple("t2",id,ticket_hash);
			for(var i=0; i<t3; ++i)
				pos_query+=gen_ticket_tuple("t3",id,ticket_hash);
			
			
			var q = db.prepare(query+pos_query.substring(1));
			q.run(function(err, rows) {

				if(err!=null){ return util.out(res,3,{err:err.toString()})}

		       	return util.out(res,0,{tickets:rows, t1:ticket_hash.t1, t2:ticket_hash.t2,
									   t3:ticket_hash.t3, total:t1+t2+t3, extra:extra});
  			});
  		});
	}

};

function gen_ticket_tuple(type,id,hash){
	var uuid = util.gen_uuid();
	hash[type].push(uuid);
	return ",('"+uuid+"','"+id+"','"+type+"','0')";
}
